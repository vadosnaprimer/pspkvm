/*
 *  Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License version
 *  2 only, as published by the Free Software Foundation.
 *  
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License version 2 for more details (a copy is
 *  included at /legal/license.txt).
 *  
 *  You should have received a copy of the GNU General Public License
 *  version 2 along with this work; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA
 *  
 *  Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 *  Clara, CA 95054 or visit www.sun.com if you need additional
 *  information or have any questions.
 */
package com.sun.mmedia;

import javax.microedition.media.MediaException;
import com.sun.mmedia.TonePlayer;

/**
 *  Tone Player to play a single note of specified duration
 *  and at the specified volume.
 *
 * @created    January 13, 2005
 */
 
public class NativeTonePlayer implements TonePlayer {
    /**
     * The native implementation method to play a tone.
     *
     * @param  note  Defines the tone of the note.
     * @param  dur   The duration of the tone in milli-seconds.
     * @param  vol   Audio volume range from 0 to 100.
     * @return       the tone player handle, or 0 if it failed to play the tone
     */
    private native boolean nPlayTone(int note, int dur, int vol);
    private native boolean nStopTone();
    // native finalizer 
    private native void finalize();
    
    public void playTone(int note, int duration, int volume)
         throws MediaException {
        
        if (false == nPlayTone(note, duration, volume)) {
            throw new MediaException("can't play tone");
        }
    }    

    /**
     * Stop current tone playing
     */
    public void stopTone() {
        nStopTone();
    }
}
