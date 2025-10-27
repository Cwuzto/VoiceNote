package com.example.voicenote.data.local.entity;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;


/**
 * EN: Line item with public fields (name, qty, unitPrice, note) to match UI.
 * VI: Dòng hàng với field public (name, qty, unitPrice, note) cho khớp UI.
 */
@Entity(
        tableName = "line_items",
        foreignKeys = @ForeignKey(
                entity = InvoiceEntity.class,
                parentColumns = "id",
                childColumns = "invoice_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("invoice_id")}
)
public class LineItemEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public long id;


    @ColumnInfo(name = "invoice_id")
    public long invoiceId;


    @ColumnInfo(name = "name")
    public String name;


    @ColumnInfo(name = "unit_price")
    public long unitPrice;


    @ColumnInfo(name = "qty")
    public int qty;


    @ColumnInfo(name = "note")
    public String note;


    public LineItemEntity() {}


    public LineItemEntity(long invoiceId, String name, int qty, long unitPrice, String note) {
        this.invoiceId = invoiceId;
        this.name = name;
        this.qty = qty;
        this.unitPrice = unitPrice;
        this.note = note;
    }
}