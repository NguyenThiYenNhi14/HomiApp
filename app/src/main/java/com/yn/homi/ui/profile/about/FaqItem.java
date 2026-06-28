package com.yn.homi.ui.profile.about;

public class FaqItem {
    private String question;
    private String answer;
    private boolean isExpanded; // true = đang mở, false = đang đóng

    public FaqItem(String question, String answer) {
        this.question = question;
        this.answer = answer;
        this.isExpanded = false; // mặc định đóng
    }

    // Getter & Setter
    public String getQuestion() { return question; }
    public String getAnswer() { return answer; }
    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { isExpanded = expanded; }
}
