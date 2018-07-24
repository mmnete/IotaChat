package com.companywesbite.iotachat;

public class Messages{


    private String message, type, sender_id;
    private long time;
    private boolean seen;

    public Messages (String message, String type, String sender_id, long time, boolean seen)
    {
        this.message = message;
        this.seen = seen;
        this.time = time;
        this.type = type;
        this.sender_id = sender_id;
    }


    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public Messages()
    {

    }


    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }
}
