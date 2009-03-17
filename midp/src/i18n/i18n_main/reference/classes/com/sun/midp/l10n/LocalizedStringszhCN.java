package com.sun.midp.l10n;

import com.sun.midp.i18n.ResourceBundle;
import com.sun.midp.i18n.ResourceConstants;

/**
 * The zh-CN localization of ResourceBundle 
 *
 * LocalizedStringsBasezhCN.java is generated from 
 * src/configuration/configurator/share/l10n/zh-CN.xml
 */
public class LocalizedStringszhCN extends LocalizedStringsBasezhCN
implements ResourceBundle {
    public String getString(int index) {
        return getContent(index);
    }

    public String getLocalizedDateString(String dayOfWeek,
                                         String date,
                                         String month,
                                         String year) {
        return dayOfWeek + ", " + year + " " + month + " " + date;
    }

    public String getLocalizedTimeString(String hour, String min,
                                         String sec, String ampm) {
        if((ampm != null) && (ampm.equals("PM"))) {
            int h24 = Integer.parseInt(hour);
            h24 += 12;
            return(h24 + ":" + min + ":" + sec);
        } else {
            return(hour + ":" + min + ":" + sec);
        }
    }

    public String getLocalizedDateTimeString(String dayOfWeek, String date,
                                             String month, String year,
                                             String hour, String min,
                                             String sec, String ampm) {
        return getLocalizedDateString(dayOfWeek, 
                               date, 
                               month, 
                               year) + " " +
        getLocalizedTimeString(hour, 
                               min, 
                               sec, 
                               ampm);
    }

    public int getLocalizedFirstDayOfWeek() {
        return java.util.Calendar.SUNDAY;
    }

    public boolean isLocalizedAMPMafterTime() {
        return false;
    }
}

