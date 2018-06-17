package com.o1.ucsdeats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class User {

    //Basic user information
    public String email;
    public String name;
    public String major;            //optional
    public String gender;           //optional
    public String intro;            //optional
    public String phoneNumber;
    public String food;
    public String profilePicFilename;
    public ArrayList<String> friendList;

    //TODO: Add profile picture (require Firebase Storage)
    //TODO: Add friend list (require list of data)
    //TODO: Add favorite restaurant (require Restaurant object)

    //Default constructor
    //Not expected to be used
    private User(){}

    //Constructor used when signing up
    //TODO: Update codes for profile picture later
    public User(String email, String name, String major, String gender, String phoneNumber,
                String intro, String food, String profilePicFilename){
        this.email = email;
        this.name = name;
        this.major = major;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.intro = intro;
        this.food = food;
        this.profilePicFilename = profilePicFilename;
        this.friendList = new ArrayList<>();
    }

    public ArrayList<String> getFriendList(){
        return friendList;
    }

    public void addFriend(String friendUid){
        friendList.add(friendUid);
    }

    public void deleteFriend(String friendUid){
        friendList.remove(friendUid);
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name",name);
        result.put("email",email);
        result.put("major",major);
        result.put("gender",gender);
        result.put("phoneNumber",phoneNumber);
        result.put("intro",intro);
        result.put("food",food);
        result.put("profilePicFilename",profilePicFilename);
        result.put("friendList",friendList);

        return result;
    }
}
