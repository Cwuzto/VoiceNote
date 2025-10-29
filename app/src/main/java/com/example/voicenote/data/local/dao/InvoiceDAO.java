// File: com/example/voicenote/data/local/dao/InvoiceDao.java
package com.example.voicenote.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.voicenote.data.local.entity.InvoiceEntity;
import com.example.voicenote.data.local.rel.InvoiceWithLines;

import java.util.List;

/**
 * EN: Invoice DAO — order by time_millis (matches InvoiceEntity).
 * VI: DAO Hoá đơn — sắp xếp theo time_millis (khớp InvoiceEntity).
 */
@Dao
public interface InvoiceDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertInvoice(InvoiceEntity invoice);

    // EN: If you really need just the invoices table (without lines)
    // VI: Nếu chỉ cần bảng invoices (không join lines)
    @Query("SELECT * FROM invoices ORDER BY time_millis DESC")
    LiveData<List<InvoiceEntity>> getAllInvoicesOnly();

    // EN: Most screens want invoice + its line items
    // VI: Thường dùng invoice kèm danh sách line items
    @Transaction
    @Query("SELECT * FROM invoices ORDER BY time_millis DESC")
    LiveData<List<InvoiceWithLines>> getInvoicesWithLines();

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :invoiceId LIMIT 1")
    LiveData<InvoiceWithLines> getInvoiceById(long invoiceId);

    @Delete
    void deleteInvoice(InvoiceEntity invoice);
}
