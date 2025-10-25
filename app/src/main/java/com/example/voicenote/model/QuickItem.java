package com.example.voicenote.model;

public class QuickItem {
    public long id;
    public String name;
    public String initial;
    public long price;
    public int selected;       // số lần nhấn (+1, +2, ...)
    public boolean showRemove; // hiển thị dấu trừ góc trái?

    public QuickItem(long id, String name, String initial, long price){
        this.id=id; this.name=name; this.initial=initial; this.price=price;
    }
}