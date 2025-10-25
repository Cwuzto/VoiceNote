package com.example.voicenote.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Invoice implements Serializable {
    public long id;
    public String customer;
    public long timeMillis;
    public boolean paid;
    public List<LineItem> items = new ArrayList<>();

    public long subtotal() {
        long s = 0;
        for (LineItem i : items) s += i.unitPrice * i.qty;
        return s;
    }

    public long total() {
        return subtotal();
    }

    public static class LineItem implements Serializable {
        public String name;
        public long unitPrice;
        public int qty;
        public String note;
    }
}
