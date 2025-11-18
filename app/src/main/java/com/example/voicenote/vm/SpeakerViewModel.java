// File: com/example/voicenote/vm/SpeakerViewModel.java
package com.example.voicenote.vm;

import android.app.Application;
import android.speech.tts.TextToSpeech; // [MỚI]
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.voicenote.data.local.AppDatabase;
import com.example.voicenote.data.local.dao.SpeakerDao;
import com.example.voicenote.data.local.entity.SpeakerTemplateEntity;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpeakerViewModel extends AndroidViewModel {
    private final SpeakerDao dao;
    private final LiveData<List<SpeakerTemplateEntity>> allTemplates;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private TextToSpeech tts;

    public SpeakerViewModel(@NonNull Application application) {
        super(application);
        dao = AppDatabase.getInstance(application).speakerDao();
        allTemplates = dao.getAllTemplates();

        // Khởi tạo TTS
        tts = new TextToSpeech(application, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("vi", "VN"));
            }
        });
    }

    // Hàm phát loa
    public void speak(String text) {
        if (tts != null) {
            // Thay thế {Số tiền} bằng ví dụ (100 nghìn) để nghe thử
            String speakText = text.replace("{Số tiền}", "một trăm nghìn đồng");
            tts.speak(speakText, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    public LiveData<List<SpeakerTemplateEntity>> getAllTemplates() {
        return allTemplates;
    }

    public void insert(SpeakerTemplateEntity item) {
        executor.execute(() -> dao.insert(item)); // Gọi hàm transaction
    }

    public void update(SpeakerTemplateEntity item) {
        executor.execute(() -> dao.update(item)); // Gọi hàm transaction
    }

    public void delete(SpeakerTemplateEntity item) {
        executor.execute(() -> dao.delete(item)); // Gọi hàm transaction
    }

    public void selectTemplate(SpeakerTemplateEntity item) {
        executor.execute(() -> dao.selectTemplate(item)); // Gọi hàm transaction
    }

    @Override
    protected void onCleared() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onCleared();
    }
}