package com.example.ravishankar.lapitchat;

/**
 * Created by ravishankar on 10/7/17.
 */

public class Users {
    public String name;
    public String image;
    public String status;


    public String thumbnail;


    public Users(){

    }
    public Users(String name, String image, String status, String thumbnail) {
        this.name = name;
        this.image = image;
        this.status = status;
        this.thumbnail = thumbnail;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getStatus() {
        return status;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
