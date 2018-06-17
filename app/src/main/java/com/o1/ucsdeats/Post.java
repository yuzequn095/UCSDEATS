package com.o1.ucsdeats;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Post {
    public int maxNumber;
    public String user;
    public Time meetingTime;
    public ArrayList<String> members = new ArrayList<>();
    public Time countDown;
    public String restaurantName;

    public Post() {
        meetingTime = new Time();
        countDown = new Time();
    }

    public Post(int maxNumber, String userId, int meetingTimeHour, int meetingTimeMinute,
                int countDownHour, int countDownMinute, String restaurantName, Date date){
        this.maxNumber = maxNumber;
        this.user = userId;
        this.meetingTime = new Time(meetingTimeHour, meetingTimeMinute);
        this.countDown = new Time(countDownHour, countDownMinute);
        this.restaurantName = restaurantName;
    }

    public void joinMeeting(String uid){

        if(members.size() < maxNumber)
            this.members.add(uid);

    }

    public void leaveMeeting(String uid){

        this.members.remove(uid);
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("maxNumber",maxNumber);
        result.put("members",members);
        result.put("meetingTime",meetingTime);
        result.put("user",user);
        result.put("countDown",countDown);
        result.put("restaurantName",restaurantName);

        return result;
    }

}

class SortByTime implements Comparator<Post>
{
    // Used for sorting in decending order

    @Override
    public int compare(Post lhs, Post rhs) {
        if (lhs.meetingTime.hour > rhs.meetingTime.hour)
            return -1;
        else if (lhs.meetingTime.hour == rhs.meetingTime.hour) {
            if (lhs.meetingTime.minute > rhs.meetingTime.minute)
                return -1;
            else
                return 1;
        } else
            return 1;
    }
}
