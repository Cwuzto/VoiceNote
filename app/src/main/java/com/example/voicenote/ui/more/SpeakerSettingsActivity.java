// File: com/example/voicenote/ui/more/SpeakerSettingsActivity.java
package com.example.voicenote.ui.more;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.voicenote.R;
import com.example.voicenote.data.local.entity.SpeakerTemplateEntity;
import com.example.voicenote.ui.dialog.AddEditSpeakerTemplateDialog;
import com.example.voicenote.ui.more.adapter.SpeakerTemplateAdapter;
import com.example.voicenote.vm.SpeakerViewModel;

public class SpeakerSettingsActivity extends AppCompatActivity {

    private SpeakerViewModel viewModel;
    private SpeakerTemplateAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speaker_settings);

        viewModel = new ViewModelProvider(this).get(SpeakerViewModel.class);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnDone).setOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rvTemplates);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SpeakerTemplateAdapter(new SpeakerTemplateAdapter.Listener() {
            @Override
            public void onSelect(SpeakerTemplateEntity item) {
                viewModel.selectTemplate(item);
            }

            @Override
            public void onEdit(SpeakerTemplateEntity item) {
                openDialog(item);
            }

            @Override
            public void onDelete(SpeakerTemplateEntity item) {
                viewModel.delete(item);
            }

            @Override
            public void onTest(SpeakerTemplateEntity item) {
                viewModel.speak(item.content);
            }

            @Override
            public void onAddNew() {
                openDialog(null);
            }
        });
        rv.setAdapter(adapter);

        viewModel.getAllTemplates().observe(this, list -> {
            adapter.submitList(list);
        });
    }

    private void openDialog(SpeakerTemplateEntity item) {
        AddEditSpeakerTemplateDialog.newInstance(
                item,
                template -> {
                    if (template.id == 0) viewModel.insert(template);
                    else viewModel.update(template);
                },
                textToTest -> { // Listener nghe thử
                    viewModel.speak(textToTest);
                }
        ).show(getSupportFragmentManager(), "SpeakerDialog");
    }
}