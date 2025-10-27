package com.example.voicenote.data.local.rel;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.voicenote.data.local.entity.InvoiceEntity;
import com.example.voicenote.data.local.entity.LineItemEntity;

import java.util.List;

/**
 * EN: Relation model joining Invoice and its Line Items using Room's @Relation.
 * VI: Lớp liên kết giữa bảng Invoice và LineItem (1-n) trong Room.
 */
public class InvoiceWithLines {

    @Embedded
    public InvoiceEntity invoice;

    @Relation(
            parentColumn = "id",
            entityColumn = "invoice_id"
    )
    public List<LineItemEntity> lineItems;
}
