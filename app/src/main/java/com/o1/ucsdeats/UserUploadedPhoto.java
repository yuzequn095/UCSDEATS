package com.o1.ucsdeats;

public class UserUploadedPhoto {
    public String Image;
    public String Text;
    public String UserId;
    public UserUploadedPhoto(String image, String text, String userId) {
        this.Image = image;
        this.Text = text;
        this.UserId = userId;
    }
}
