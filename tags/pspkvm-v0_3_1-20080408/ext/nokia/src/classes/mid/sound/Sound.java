
package com.nokia.mid.sound;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.VolumeControl;
import javax.microedition.media.Manager;

/**
 * 
 * @author liang.wu
 *
 */
public class Sound
{

    public static final int SOUND_PLAYING = 0;
    public static final int SOUND_STOPPED = 1;
    public static final int SOUND_UNINITIALIZED = 3;
    
    public static final int FORMAT_TONE = 1;
    public static final int FORMAT_WAV = 5;

    public Player m_player;
    private Sound m_instance;
    private SoundListener listener;

    private int status;
    private int type;
    
    public int dataLen;


    public Sound(byte data[], int _type)
    {
        type = _type;
        dataLen = data.length;
        m_instance = this;
        init(data,_type);
    }
    
    public String getType()
    {
        if(type == FORMAT_TONE)
        {
            return "FORMAT_TONE";
        }
        else if(type == FORMAT_WAV)
        {
            return "FORMAT_WAV";
        }
        else
        {
            return null;
        }
    }
    /**
     * javax.microedition.media.Manager.playTone(int, int, int).
     * @param freq
     * @param duration
     */
    public Sound(int freq, long duration)
    {
        init(freq, duration);
    }

    public static int getConcurrentSoundCount(int type)
    {
        return 1;
    }

    public int getGain()
    {
        return ((VolumeControl)m_player.getControl("VolumeControl")).getLevel();
    }

    public int getState()
    {
        return status;
    }

    public static int[] getSupportedFormats()
    {
        int formats[] = {FORMAT_TONE,FORMAT_WAV};
        return formats;
    }

    public void init(byte data[], int _type)
    {
        if(data == null)
            throw new NullPointerException();

        String type = "audio/midi";
        if (data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F') {
        	if (data[8] == 'W' && data[9] == 'A' && data[10] == 'V' && data[11] == 'E') {
	        	_type = FORMAT_WAV;
	        	type = "audio/x-wav";
        	}
        	else if (data[8] == 'R' && data[9] == 'M' && data[10] == 'I' && data[11] == 'D'){
        		type = "audio/midi";
        	}
        }
        else if (data[0] == 'F' && data[1] == 'O' && data[2] == 'R' && data[3] == 'M') {
        	_type = FORMAT_WAV;
        	type = "audio/x-wav";
        }
        else if (data[0] == 'M' && data[1] == 'T' && data[2] == 'h' && data[3] == 'd') {//MThd
        	type = "audio/midi";
        }
        
        if(_type == FORMAT_TONE)
        {
            //todo: trans ott data to midi data
            type = "audio/midi";
            System.out.println("com.nokia.mid.sound.init(byte data[], int _type) not support ott format in this version.\n");
            return;
        }
        
        try
        {
            InputStream is = new ByteArrayInputStream(data);
            m_player = Manager.createPlayer(is, type);
            m_player.addPlayerListener(new PlayerListener()
            {

                public void playerUpdate(Player player, String event, Object eventData)
                {
                   if(event.equals(PlayerListener.STOPPED))
                   {
                       status = SOUND_STOPPED;
                       if(listener != null)
                           listener.soundStateChanged(m_instance, SOUND_STOPPED);
                   }
                }
                
            });
            is.close();
            is = null;
        }
        catch(Exception e)
        {            
        }      
        
        status = SOUND_UNINITIALIZED;
    }
    
    public void init(int freq, long duration)
    {
       System.out.println("com.nokia.mid.sound.init(int freq, long duration) not implemented in this version.\n");
    }

    public void play(int loop)
    {      
    	if (m_player == null) return; 
   		m_player.setLoopCount(loop == 0 ? -1 : loop);        
   		resume();
    }

    public void release()
    {
    	if (m_player == null) return;
        if(status == SOUND_PLAYING)
            stop();
        if(status != SOUND_UNINITIALIZED)
        {
         
            m_player.deallocate();
            
            status = SOUND_UNINITIALIZED;
            if(listener != null)
                listener.soundStateChanged(this, SOUND_UNINITIALIZED);
        }
    }

    public void resume()
    {        
    	if (m_player == null) return;
        try
        {
            m_player.start();
        }
        catch (Exception e)
        {
        }
        
        status = SOUND_PLAYING;
        if(listener != null)
            listener.soundStateChanged(this, SOUND_PLAYING);
    }


    public void setGain(int level)
    {   
    	if (m_player == null) return;
    	((VolumeControl)m_player.getControl("VolumeControl")).setLevel(level);
    }

    public void setSoundListener(SoundListener soundlistener)
    {
        listener = soundlistener;
    }

    public void stop()
    {       
    	if (m_player == null) return;
        try
        {
            m_player.stop();
        }
        catch (Exception e)
        {
        }
        
        status = SOUND_STOPPED;
        
        if(listener != null)
            listener.soundStateChanged(this, SOUND_STOPPED);
    }
    
}
