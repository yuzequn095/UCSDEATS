package com.o1.ucsdeats;

public class Review {
    public Long Rating;
    public String Date;
    public String Text;

    public Review(Long rating, String text, String date) {
        this.Rating = rating;
        this.Text = text;
        this.Date = date;
    }
 }