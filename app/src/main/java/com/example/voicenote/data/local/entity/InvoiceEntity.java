package com.example.voicenote.data.local.entity;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


/**
 * EN: Invoice entity aligned with your UI (customer, timeMillis, total, paid).
 * VI: Entity hoá đơn khớp UI của bạn (customer, timeMillis, total, paid).
 * Public fields to avoid changing existing UI code.
 */
@Entity(tableName = "invoices")
public class InvoiceEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public long id;


    @ColumnInfo(name = "customer")
    public String customer;


    @ColumnInfo(name = "time_millis")
    public long timeMillis;


    @ColumnInfo(name = "total")
    public long total; // use long for VND


    @ColumnInfo(name = "paid")
    public boolean paid;


    public InvoiceEntity() {}


    public InvoiceEntity(String customer, long timeMillis, long total, boolean paid) {
        this.customer = customer;
        this.timeMillis = timeMillis;
        this.total = total;
        this.paid = paid;
    }
}