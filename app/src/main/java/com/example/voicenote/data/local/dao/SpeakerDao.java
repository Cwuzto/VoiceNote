// File: com/example/voicenote/data/local/dao/SpeakerDao.java
package com.example.voicenote.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.voicenote.data.local.entity.SpeakerTemplateEntity;
import java.util.List;

@Dao
public interface SpeakerDao {
    @Query("SELECT * FROM speaker_templates ORDER BY id DESC")
    LiveData<List<SpeakerTemplateEntity>> getAllTemplates();

    // Internal methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertInternal(SpeakerTemplateEntity template);

    @Update
    void updateInternal(SpeakerTemplateEntity template);

    @Delete
    void deleteInternal(SpeakerTemplateEntity template);

    @Query("UPDATE speaker_templates SET is_selected = 0")
    void unselectAll();

    @Query("SELECT COUNT(*) FROM speaker_templates")
    int getCount();

    @Query("SELECT * FROM speaker_templates LIMIT 1")
    SpeakerTemplateEntity getFirstTemplate();

    // --- LOGIC ---

    @Transaction
    default void insert(SpeakerTemplateEntity template) {
        if (getCount() == 0) {
            template.isSelected = true; // Cái đầu tiên luôn được chọn
        }
        if (template.isSelected) {
            unselectAll();
        }
        insertInternal(template);
    }

    @Transaction
    default void update(SpeakerTemplateEntity template) {
        if (template.isSelected) {
            unselectAll();
        } else {
            // Nếu chỉ còn 1 cái thì không cho phép bỏ chọn
            if (getCount() == 1) {
                template.isSelected = true;
            }
        }
        updateInternal(template);
    }

    // Hàm này dùng khi user bấm vào item để chọn
    @Transaction
    default void selectTemplate(SpeakerTemplateEntity template) {
        unselectAll();
        template.isSelected = true;
        updateInternal(template);
    }

    @Transaction
    default void delete(SpeakerTemplateEntity template) {
        boolean wasSelected = template.isSelected;
        deleteInternal(template);

        // Nếu xóa cái đang chọn, chọn cái khác thay thế
        if (wasSelected) {
            SpeakerTemplateEntity nextSelected = getFirstTemplate();
            if (nextSelected != null) {
                nextSelected.isSelected = true;
                updateInternal(nextSelected);
            }
        }
    }

    @Query("SELECT * FROM speaker_templates WHERE is_selected = 1 LIMIT 1")
    SpeakerTemplateEntity getSelectedTemplateSync();
}