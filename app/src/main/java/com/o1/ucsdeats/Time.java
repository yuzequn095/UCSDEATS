package com.o1.ucsdeats;

public class Time {
    public int hour;
    public int minute;

    public Time(){
        this.hour = 0;
        this.minute = 0;
    }

    public Time(int hour, int minute){
        this.hour = hour;
        this.minute = minute;
    }

    public boolean isValid(){
        return (this.hour <= 23 && this.hour >= 0 && this.minute <= 59 && this.minute >=0);
    }

    public Time add(Time time){
        int resultMinute = this.minute + time.minute;
        int resultHour = this.hour + time.hour;
        if (resultMinute >= 60){
            resultMinute = resultMinute % 60;
            resultHour++;
        }
        if (resultHour >= 24){
            resultHour = resultHour % 24;
        }
        return (new Time(resultHour, resultMinute));
    }
}
