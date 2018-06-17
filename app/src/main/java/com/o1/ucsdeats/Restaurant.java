package com.o1.ucsdeats;

import java.util.HashMap;

public class Restaurant {
    public Hours hours;
    public String name;
    public String location;
    public String menu_folder;
    public String menu_link;
    public String latitude;
    public String longitude;
    private Restaurant(){}
    public Restaurant(Hours hours, String name, String location, String menu_folder, String
            menu_link, String latitude, String longitude){
        this.hours = hours;
        this.name = name;
        this.location = location;
        this.menu_folder = menu_folder;
        this.menu_link = menu_link;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}

class Hours {
    public String mon;
    public String tues;
    public String weds;
    public String thurs;
    public String fri;
    public String sat;
    public String sun;
}