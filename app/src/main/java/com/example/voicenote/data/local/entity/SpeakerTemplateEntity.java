// File: com/example/voicenote/data/local/entity/SpeakerTemplateEntity.java
package com.example.voicenote.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "speaker_templates")
public class SpeakerTemplateEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "content")
    public String content; // Ví dụ: "Đã nhận {Số tiền}"

    @ColumnInfo(name = "is_selected")
    public boolean isSelected; // Mẫu đang được chọn

    public SpeakerTemplateEntity(String content, boolean isSelected) {
        this.content = content;
        this.isSelected = isSelected;
    }

    public SpeakerTemplateEntity() {}
}