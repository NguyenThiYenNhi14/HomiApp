package com.yn.homi.model;

public class NotificationItem {
    private String title;
    private String description;
    private String timeAgo;   // e.g. "25 min", "1 day"
    private int    imageRes;

    public NotificationItem(String title, String description,
                            String timeAgo, int imageRes) {
        this.title = title;
        this.description = description;
        this.timeAgo = timeAgo;
        this.imageRes = imageRes;
    }

    public String getTitle()       { return title; }
    public String getDescription() { return description; }
    public String getTimeAgo()     { return timeAgo; }
    public int    getImage()       { return imageRes; }
}