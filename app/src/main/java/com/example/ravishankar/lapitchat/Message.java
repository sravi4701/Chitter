package com.example.ravishankar.lapitchat;

/**
 * Created by ravishankar on 13/8/17.
 */

public class Message {
    private String message;
    private String type;
    private String from;
    private Long time;
    private boolean seen;

    public Message(){

    }

    public Message(String message, boolean seen, Long time, String type) {
        this.message = message;
        this.seen = seen;
        this.time = time;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTime() {
        return time;
    }

    public String getType() {
        return type;
    }
    public void setFrom(String from) {
        this.from = from;
    }

    public String getFrom() {
        return from;
    }

    public boolean isSeen() {
        return seen;
    }
}
