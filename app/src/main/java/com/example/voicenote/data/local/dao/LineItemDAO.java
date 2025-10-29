// File: com/example/voicenote/data/local/dao/LineItemDao.java
package com.example.voicenote.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.voicenote.data.local.entity.LineItemEntity;

import java.util.List;

@Dao
public interface LineItemDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertLineItem(LineItemEntity item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLineItems(List<LineItemEntity> items);

    @Query("DELETE FROM line_items WHERE invoice_id = :invoiceId")
    void deleteByInvoiceId(long invoiceId);

    @Query("SELECT * FROM line_items WHERE invoice_id = :invoiceId ORDER BY id ASC")
    List<LineItemEntity> getByInvoiceId(long invoiceId);
}
