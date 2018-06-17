package com.o1.ucsdeats;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utils {
    public static String createDateStamp() {
        return new SimpleDateFormat("MMddyyyyHHmmss").format(Calendar.getInstance().getTime());
    }

    public static Date parseDateStamp(String date) {
        try {
            return new SimpleDateFormat("MMddyyyyHHmmss").parse(date);
        } catch(Exception e) {
            return new Date();
        }
    }

    public static String prettifyDate(Date date) {
        return new SimpleDateFormat("MM-dd-YYYY").format(date).toString();
    }
}