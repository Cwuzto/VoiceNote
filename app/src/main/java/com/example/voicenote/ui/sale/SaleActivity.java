// File: com/example/voicenote/ui/sale/SaleActivity.java
package com.example.voicenote.ui.sale;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voicenote.MainActivity;
import com.example.voicenote.R;
import com.example.voicenote.data.local.entity.OrderEntity;
import com.example.voicenote.data.local.entity.OrderItemEntity;
import com.example.voicenote.data.local.entity.ProductEntity;
import com.example.voicenote.ui.custom.WaveformView;
import com.example.voicenote.ui.dialog.AddProductSheet;
import com.example.voicenote.ui.dialog.CustomerNameDialog;
import com.example.voicenote.ui.dialog.EditOrderItemDialog;
import com.example.voicenote.ui.order.OrderDetailActivity;
import com.example.voicenote.ui.quick.GridSpacingItemDecoration;
import com.example.voicenote.ui.quick.QuickGridAdapter;
import com.example.voicenote.util.SessionManager;
import com.example.voicenote.vm.OrderEditViewModel;
import com.example.voicenote.vm.ProductViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SaleActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO = 101;

    // --- Views ---
    private RecyclerView rvQuickGrid, rvOrderLines;
    private LinearLayout quickBar, rowCustomer;
    private LinearLayout contentGuide;
    private NestedScrollView cartScrollView;
    private EditText edtLine;
    private TextView tvCustomer, btnDone, tvTotal;
    private ImageButton btnMic, btnSendListening, btnCancelListening, btnSend;
    private View includeListening;

    private WaveformView waveView ;
    // --- Adapters & ViewModels ---
    private QuickGridAdapter quickGridAdapter;
    private OrderLineAdapter orderLineAdapter;
    private ProductViewModel productViewModel;
    private OrderEditViewModel orderEditViewModel;

    // --- Data ---
    private final List<ProductEntity> quickProducts = new ArrayList<>();
    private final List<OrderItemEntity> currentOrderItems = new ArrayList<>(); // Giỏ hàng

    // Biến theo dõi chế độ Sửa
    private boolean isEditMode = false;
    private long editingOrderId = 0; // ID của đơn hàng đang sửa
    private long editingCreatedAt = 0; // Biến lưu thời gian tạo cũ

    private boolean isListening = false; // gán biến để theo dõi trạng thái mic (mặc định false)
    private boolean gridVisible = false;
    boolean listeningVisible = false;
    private boolean isCancelled = false;
    private SpeechRecognizer speechRecognizer;
    private SessionManager sessionManager;
    private long userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale);

        // --- Khởi tạo ViewModels ---
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        orderEditViewModel = new ViewModelProvider(this).get(OrderEditViewModel.class); // [MỚI]

        // --- Ánh xạ Views ---
        findViews();

        // --- Thiết lập Listeners ---
        setupListeners();

        // Kiểm tra xem có phải chế độ Sửa không
        checkEditMode();

        // --- Thiết lập RecyclerViews ---
        setupQuickGrid(); // Lưới chọn nhanh
        setupOrderLines(); // Giỏ hàng [MỚI]

        // --- Lắng nghe dữ liệu DB ---
        observeQuickProducts();

        // --- Xử lý nút Back ---
        setupBackButton();

        // --- Cập nhật UI lần đầu ---
        updateCartUI();
        // Hàm kiểm tra quyền Microphone
        checkMicPermission();
        // Khởi tạo SpeechRecognizer và thiết lập listener
        initSpeechRecognizer();
    }

    private void checkMicPermission() {
        // Nếu chưa được cấp quyền RECORD_AUDIO
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Yêu cầu cấp quyền RECORD_AUDIO từ người dùng
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO);
        }
    }

    // Xử lý kết quả người dùng chấp nhận hoặc từ chối quyền
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO) { // kiểm tra request code
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                btnMic.setEnabled(true);  // bật nút Microphone nếu được phép
            } else {
                btnMic.setEnabled(false); // tắt nút Microphone nếu bị từ chối
            }
        }
    }


    /**
     * Kiểm tra Intent xem có phải là sửa đơn hàng không
     */
    private void checkEditMode() {
        Intent intent = getIntent();
        if (intent.hasExtra("EDIT_ORDER_ID")) {
            isEditMode = true;
            editingOrderId = intent.getLongExtra("EDIT_ORDER_ID", 0);

            // Lấy dữ liệu cũ
            editingCreatedAt = intent.getLongExtra("CREATED_AT", System.currentTimeMillis()); // thời gian tạo đơn cũ
            String customerName = intent.getStringExtra("CUSTOMER_NAME");
            ArrayList<OrderItemEntity> items = intent.getParcelableArrayListExtra("ORDER_ITEMS");

            // Tải dữ liệu vào UI
            if (customerName != null) {
                tvCustomer.setText(customerName);
            }
            if (items != null) {
                currentOrderItems.clear();
                currentOrderItems.addAll(items);
                // Đã có adapter, chỉ cần thông báo
                if (orderLineAdapter != null) {
                    orderLineAdapter.notifyDataSetChanged();
                }
            }
            // Đổi text nút "Xong"
            btnDone.setText("Cập nhật");
        }
    }

    private void findViews() {
        quickBar = findViewById(R.id.quickBar);
        edtLine = findViewById(R.id.edtLine);
        rvQuickGrid = findViewById(R.id.rvQuickGrid);
        rvOrderLines = findViewById(R.id.rvOrderLines);
        rowCustomer = findViewById(R.id.rowCustomer);
        tvCustomer = findViewById(R.id.tvCustomer);
        cartScrollView = findViewById(R.id.cartScrollView);
        btnDone = findViewById(R.id.btnDone);
        tvTotal = findViewById(R.id.tvTotal);
        contentGuide = findViewById(R.id.contentGuide);
        btnMic = findViewById(R.id.btnMic);
        includeListening = findViewById(R.id.includeListening);
        waveView = includeListening.findViewById(R.id.waveView);
        btnSendListening = findViewById(R.id.btnSendListening);
        btnCancelListening = findViewById(R.id.btnCancelListening);
        btnSend = findViewById(R.id.btnSend);
    }

    private void setupListeners() {
        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        btnDone.setEnabled(false); // Mặc định tắt
        btnDone.setOnClickListener(v -> saveOrderAndFinish()); // Gọi hàm mới

        rowCustomer.setOnClickListener(v -> {
            String current = tvCustomer.getText() != null ? tvCustomer.getText().toString() : "";
            CustomerNameDialog.newInstance(current)
                    .setCallback(name -> tvCustomer.setText(name))
                    .show(getSupportFragmentManager(), "customer_name");
        });

        // Nhấn nút grid
        findViewById(R.id.btnGrid).setOnClickListener(v -> {
            gridVisible = !gridVisible;
            listeningVisible = false; // đóng includeListening
            hideKeyboard();
            updateLayout();
        });

        edtLine.setOnClickListener(v -> {
            if (gridVisible) {
                gridVisible = false;
                updateLayout();
            }
            if (listeningVisible) {
                listeningVisible = false;
                updateLayout();
            }
        });
        edtLine.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // 1. Nếu grid hoặc listening panel đang mở, ẩn chúng
                if (gridVisible || listeningVisible) hidePanels();

                // 2. Post task để focus và mở bàn phím sau khi layout ổn định
                v.post(() -> {
                    edtLine.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) imm.showSoftInput(edtLine, InputMethodManager.SHOW_IMPLICIT);
                });
            }
            // Trả về false để hệ thống vẫn xử lý focus và bàn phím
            return false;
        });

        edtLine.setOnEditorActionListener((v, actionId, event) -> {
            String line = edtLine.getText().toString().trim();
            if (!line.isEmpty()) {
                addItemFromEditText();
                edtLine.setText("");
            }
            return true;
        });
        // Nhấn nút mic
        btnMic.setOnClickListener(v -> {
            listeningVisible = !listeningVisible;
            gridVisible = false; // đóng rvQuickGrid
            hideKeyboard();
            updateLayout();
            if (listeningVisible) {
                startListening();
            } else {
                stopListening();
            }
        });
        btnSendListening.setOnClickListener(v -> {
            listeningVisible = false;   // ẩn layout lắng nghe
            updateLayout();             // cập nhật UI
            isCancelled = false;
            stopListening();            // dừng SpeechRecognizer
        });
        btnCancelListening.setOnClickListener(v -> {
            edtLine.setText("");
            listeningVisible = false;
            updateLayout();             // cập nhật UI
            isCancelled = true;         // đặt cờ hủy
            stopListening();            // dừng speech recognizer
        });

        // Thêm TextWatcher cho EditText
        edtLine.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần xử lý
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Không cần xử lý
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Kiểm tra logic
                // Dùng TextUtils.isEmpty để kiểm tra null và rỗng an toàn
                if (TextUtils.isEmpty(s)) {
                    // Nếu EditText rỗng: Hiện Mic, Ẩn Gửi
                    btnMic.setVisibility(View.VISIBLE);
                    btnSend.setVisibility(View.GONE);
                } else {
                    // Nếu EditText có chữ: Ẩn Mic, Hiện Gửi
                    btnMic.setVisibility(View.GONE);
                    btnSend.setVisibility(View.VISIBLE);
                }
            }
        });
        btnSend.setOnClickListener(v -> {
            String line = edtLine.getText().toString().trim();
            if (!line.isEmpty()) {
                addItemFromEditText();
                edtLine.setText("");
                listeningVisible = false;
                updateLayout();             // cập nhật UI
                isCancelled = true;         // đặt cờ hủy
                stopListening();            // dừng speech recognizer
            }
        });
    }
    // hàm helper mới để đóng các panel
    private void hidePanels() {
        boolean panelWasOpen = false;

        if (gridVisible) {
            gridVisible = false;
            panelWasOpen = true;
        }

        if (listeningVisible) {
            listeningVisible = false;
            panelWasOpen = true;

            // Quan trọng: Dừng SpeechRecognizer nếu nó đang chạy
            isCancelled = true; // Đặt cờ hủy
            stopListening();    // Gọi hàm stop
        }

        if (panelWasOpen) {
            updateLayout(); // Chỉ cập nhật layout nếu có gì đó thay đổi
        }
    }

    private void setupQuickGrid() {
        rvQuickGrid.setLayoutManager(new GridLayoutManager(this, 4));
        rvQuickGrid.addItemDecoration(new GridSpacingItemDecoration(4, dp(10), true));
        rvQuickGrid.setNestedScrollingEnabled(true);

        quickGridAdapter = new QuickGridAdapter(
                quickProducts,
                this::openAddDialog,
                this::onProductPicked, // Logic thêm vào giỏ hàng
                (item, position) -> productViewModel.deleteProduct(item)
        );
        rvQuickGrid.setAdapter(quickGridAdapter);
    }

    // Thiết lập RecyclerView cho giỏ hàng
    private void setupOrderLines() {
        rvOrderLines.setLayoutManager(new LinearLayoutManager(this));
        rvOrderLines.setNestedScrollingEnabled(false);

        orderLineAdapter = new OrderLineAdapter(currentOrderItems, new OrderLineAdapter.OnItemInteractionListener() {
            @Override
            public void onQuantityChanged(int position, int newQuantity) {
                // Tìm sản phẩm tương ứng trong quick grid để cập nhật badge
                for (ProductEntity p : quickProducts) {
                    if (p.name.equals(currentOrderItems.get(position).productName)) {
                        p.selected = newQuantity;
                        break;
                    }
                }
                quickGridAdapter.notifyDataSetChanged();
                
                if (newQuantity <= 0) {
                    // Nếu số lượng về 0, xoá
                    currentOrderItems.remove(position);
                    orderLineAdapter.notifyItemRemoved(position);
                } else {
                    currentOrderItems.get(position).quantity = newQuantity;
                    orderLineAdapter.notifyItemChanged(position);
                }
                // Nếu item đang mở → tự đóng lại
                if (openedViewHolder != null) {
                    View content = openedViewHolder.itemView.findViewById(R.id.contentView);
                    if (content != null && content.getTranslationX() != 0) {
                        content.animate()
                                .translationX(0)
                                .setDuration(150)
                                .setInterpolator(new DecelerateInterpolator())
                                .start();
                        openedViewHolder = null;
                    }
                }

                updateCartUI(); // Cập nhật tổng tiền và UI
            }

            @Override
            public void onNoteChanged(int position, String newNote) {
                if (position < currentOrderItems.size()) {
                    currentOrderItems.get(position).note = newNote;
                }
            }

            @Override
            public void onItemClicked(int position, OrderItemEntity item) {
                openEditDialog(item, position); // Mở dialog chỉnh sửa
            }

            @Override
            public void onDeleteClicked(int position, OrderItemEntity item) {
                // Reset badge sản phẩm
                for (ProductEntity p : quickProducts) {
                    if (p.name.equals(item.productName)) {
                        p.selected = 0;
                        break;
                    }
                }
                quickGridAdapter.notifyDataSetChanged();
                // Reset translationX cho tất cả item để đảm bảo trạng thái view được reset TRƯỚC KHI xoá.
                if (openedViewHolder != null) {
                    View content = openedViewHolder.itemView.findViewById(R.id.contentView);
                    if (content != null && content.getTranslationX() != 0) {
                        content.animate()
                                .translationX(0)
                                .setDuration(150) // Có thể dùng 0 nếu muốn đóng ngay lập tức
                                .setInterpolator(new DecelerateInterpolator())
                                .start();
                        openedViewHolder = null; //  Reset lại cờ
                    }
                }

                // Xóa an toàn khỏi currentOrderItems
                if (position != RecyclerView.NO_POSITION && position < currentOrderItems.size()) {
                    currentOrderItems.remove(position);
                    orderLineAdapter.notifyItemRemoved(position);
                }
                orderLineAdapter.notifyDataSetChanged();
                updateCartUI();
            }
        });
        rvOrderLines.setAdapter(orderLineAdapter);

        // Gắn Swipe Helper
        attachSwipeHelper();
    }

    // Lắng nghe danh sách sản phẩm từ DB
    private void observeQuickProducts() {
        productViewModel.getAllProducts().observe(this, productEntities -> {
            quickProducts.clear();
            if (productEntities != null) {
                quickProducts.addAll(productEntities);
            }
            quickGridAdapter.notifyDataSetChanged();
        });
    }

    private void setupBackButton() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (gridVisible) {
                    gridVisible = false;
                    updateLayout();
                } else {
                    setEnabled(false);
                    SaleActivity.super.getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    // phương thúc nhấn ngoài để đóng grid, listening
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // Chỉ kiểm tra khi có sự kiện nhấn xuống (ACTION_DOWN)
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {

            // Chỉ xử lý nếu 1 trong 2 panel đang mở
            if (gridVisible || listeningVisible) {

                // Lấy vùng chữ nhật (Rect) của các view "an toàn"
                // (những view mà khi nhấn vào SẼ KHÔNG đóng panel)

                // 1. Vùng an toàn: quickBar
                Rect quickBarRect = new Rect();
                quickBar.getHitRect(quickBarRect);

                // Nếu nhấn vào quickBar, thì không làm gì, để hệ thống tự xử lý
                if (quickBarRect.contains((int) ev.getX(), (int) ev.getY())) {
                    return super.dispatchTouchEvent(ev);
                }

                // 2. Vùng an toàn: rvQuickGrid (nếu đang mở)
                if (gridVisible) {
                    Rect gridRect = new Rect();
                    rvQuickGrid.getHitRect(gridRect);
                    // Nếu nhấn vào trong grid, không làm gì
                    if (gridRect.contains((int) ev.getX(), (int) ev.getY())) {
                        return super.dispatchTouchEvent(ev);
                    }
                }

                // 3. Vùng an toàn: includeListening (nếu đang mở)
                if (listeningVisible) {
                    Rect listeningRect = new Rect();
                    includeListening.getHitRect(listeningRect);
                    // Nếu nhấn vào trong panel listening, không làm gì
                    if (listeningRect.contains((int) ev.getX(), (int) ev.getY())) {
                        return super.dispatchTouchEvent(ev);
                    }
                }
                // tới đây nghĩa là 1 trong 2 panel đang MỞ, và người dùng nhấn ra NGOÀI tất cả các vùng an toàn (quickBar, grid, listening).
                //  gọi hàm hidePanels()
                hidePanels();
            }
        }

        // Luôn gọi super để các sự kiện chạm khác vẫn hoạt động bình thường
        return super.dispatchTouchEvent(ev);
    }

    // --- Logic nghiệp vụ chính ---
    // --- Speech Recognizer ---
    private void initSpeechRecognizer() {
        // Kiểm tra thiết bị có hỗ trợ Speech Recognition không
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = null;
            return;
        }

        // Tạo đối tượng SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        // Thiết lập RecognitionListener để nhận các callback từ SpeechRecognizer
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {
                // Callback khi SpeechRecognizer đã sẵn sàng nhận giọng nói
            }

            @Override public void onBeginningOfSpeech() {
                // Callback khi người dùng bắt đầu nói
            }

            @Override public void onRmsChanged(float rmsdB) {
                // rmsdB là cường độ âm thanh hiện tại (decibel)
                // Chỉ dùng để vẽ sóng âm thanh theo thời gian thực
                int amp = (int) (rmsdB * 100);  // chuyển đổi cường độ âm thanh thành giá trị int
                waveView.addAmplitude(amp);      // thêm giá trị vào view vẽ sóng
            }
            @Override
            public void onBufferReceived(byte[] buffer) {
                // Callback khi nhận được một đoạn dữ liệu âm thanh thô (raw audio) từ micro
                // Ở đây để trống vì không cần lưu trữ âm thanh thô
            }

            @Override
            public void onEndOfSpeech() {
                // Callback khi người dùng ngừng nói (SpeechRecognizer nhận thấy kết thúc giọng nói)
                // Ở đây để trống, xử lý chính sẽ ở onResults, onPartialResults
            }
            @Override
            public void onError(int error) {
                if (isCancelled) return; // Nếu người dùng đã chủ động hủy thì thôi

                // Các lỗi này nghĩa là người dùng đã dừng nói.
                // dừng UI và lưu kết quả.
                switch (error) {
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    case SpeechRecognizer.ERROR_NO_MATCH:
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: // Thêm lỗi này để tránh treo
                        // Tắt UI
                        listeningVisible = false;
                        updateLayout();

                        // Phát âm báo dừng
                        playStopTone();

                        // Xử lý text cuối cùng (lấy từ edtLine)
                        addItemFromEditText();
                        break;

                    // Các lỗi nghiêm trọng khác (mạng, quyền, v.v.)
                    default:
                        // Chỉ dừng UI
                        listeningVisible = false;
                        updateLayout();
                        playStopTone();
                        // Có thể thông báo lỗi cho người dùng nếu cần
                }
            }

            // **Nhận text tạm thời realtime**
            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    // Chuyển các chữ số dưới 10 sang số trước khi hiển thị
                    String normalizedText = normalizeNumbers(matches.get(0));

                    edtLine.setText(normalizedText); // realtime update
                }
            }

            // **Nhận kết quả cuối cùng**
            @Override
            public void onResults(Bundle results) {
                if (isCancelled) return;  // nếu đã hủy thì bỏ qua

                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    // Chuyển các chữ số dưới 10 sang số trước khi hiển thị
                    String normalizedText = normalizeNumbers(matches.get(0));

                    edtLine.setText(normalizedText); // cuối cùng
                    listeningVisible = false;          // ẩn layout
                    updateLayout();                    // cập nhật UI
                    stopListening();
                    addItemFromEditText();
                }
            }

            @Override public void onEvent(int eventType, Bundle params) {}
        });
    }
    private String normalizeNumbers(String input) {
        if (input == null) return "";

        String[] words = {"một","hai","ba","bốn","năm","sáu","bảy","tám","chín","mười"};
        for (int i = 0; i < words.length; i++) {
            input = input.replaceAll("\\b" + words[i] + "\\b", String.valueOf(i + 1));
        }
        return input;
    }

    private void startListening() {
        if (speechRecognizer == null) {
            Toast.makeText(this, "Thiết bị không hỗ trợ nhận dạng giọng nói", Toast.LENGTH_SHORT).show();
            return;
        }
        isListening = true;
        playStartTone(); // âm báo bắt đầu

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        // không gọi start nếu speechRecognizer null
        try {
            speechRecognizer.startListening(intent);
        } catch (Exception ignored) {}
    }

    private void stopListening() {
        if (isListening) {
            playStopTone();  // chỉ phát âm khi đang nghe mic
        }

        isListening = false;

        if (speechRecognizer != null) { // kiểm tra xem mic có đang chạy không, nếu có thì mới gọi hàm dừng
            try {
                speechRecognizer.stopListening();
            } catch (Exception ignored) {}
        }
    }

    @Override
    protected void onDestroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        super.onDestroy();
    }
    private void playStartTone() {
        ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 200); // 200ms
    }

    private void playStopTone() {
        ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneGen.startTone(ToneGenerator.TONE_PROP_PROMPT, 200); // 200ms
    }

    // Khi chọn 1 món từ lưới (QuickGrid)
    private void onProductPicked(ProductEntity product, int position) {
        // Nếu item đang mở → tự đóng lại
        if (openedViewHolder != null) {
            View content = openedViewHolder.itemView.findViewById(R.id.contentView);
            if (content != null && content.getTranslationX() != 0) {
                content.animate()
                        .translationX(0)
                        .setDuration(150)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
                openedViewHolder = null;
            }
        }
        // 1. Kiểm tra xem món đã có trong giỏ hàng chưa
        for (int i = 0; i < currentOrderItems.size(); i++) {
            OrderItemEntity item = currentOrderItems.get(i);
            // Giả sử tên sản phẩm là duy nhất
            if (item.productName.equals(product.name)) {
                // Đã có -> Tăng số lượng
                item.quantity++;
                orderLineAdapter.notifyItemChanged(i);
                updateCartUI();
                return;
            }
        }

        // 2. Nếu chưa có -> Tạo OrderItemEntity mới
        OrderItemEntity newItem = new OrderItemEntity();
        newItem.productName = product.name;
        newItem.unitPrice = product.price;
        newItem.quantity = 1;
        newItem.note = "";

        currentOrderItems.add(newItem);
        orderLineAdapter.notifyItemInserted(currentOrderItems.size() - 1);
        updateCartUI();
    }
    /**
     * Xử lý chuỗi nhập trong edtLine.
     * Chuỗi có thể là:
     * - "3 coca 10000"
     * - "2 mì tôm"
     * - "coca"
     * - "coca 10000"
     * 10k = 10000
     */
    private void addItemFromEditText() {
        String line = edtLine.getText().toString().trim();
        if (line.isEmpty()) return;

        String[] tokens = line.split("\\s+");
        if (tokens.length == 0) return;

        String first = tokens[0];
        String last = tokens[tokens.length - 1];

        boolean firstIsNum = isInteger(first);
        Long parsedPrice = parsePrice(last); // Dùng hàm parsePrice để check giá
        boolean lastIsPrice = (parsedPrice != null);

        String name;
        int quantity = 1;
        Long price = null; // Dùng kiểu Long (nullable)

        // CASE 1: so_luong + ten sp + gia (VD: "3 coca 10k")
        if (firstIsNum && lastIsPrice && tokens.length >= 3) {
            quantity = Integer.parseInt(first);
            price = parsedPrice; // Lấy giá đã được parse
            name = joinTokens(tokens, 1, tokens.length - 1);
        }
        // CASE 2: ten sp + gia (VD: "coca 10000")
        else if (!firstIsNum && lastIsPrice && tokens.length >= 2) {
            quantity = 1;
            price = parsedPrice; // Lấy giá đã được parse
            name = joinTokens(tokens, 0, tokens.length - 1);
        }
        // CASE 3: so_luong + ten sp (VD: "2 mì tôm" - không có giá)
        else if (firstIsNum && !lastIsPrice) {
            quantity = Integer.parseInt(first);
            name = joinTokens(tokens, 1, tokens.length);
            // price vẫn là null
        }
        // CASE 4: chỉ ten sp (VD: "mì tôm")
        else { // Bao gồm trường hợp (!firstIsNum && !lastIsPrice)
            quantity = 1;
            name = joinTokens(tokens, 0, tokens.length);
            // price vẫn là null
        }

        // Yêu cầu: Nếu giá null (Case 3 hoặc 4) -> ưu tiên lấy trong danh sách
        if (price == null) {
            boolean found = false;
            for (ProductEntity p : quickProducts) {
                if (p.name.equalsIgnoreCase(name)) {
                    price = (long) p.price; // Lấy giá từ danh sách sản phẩm
                    found = true;
                    break;
                }
            }
            if (!found) {
                price = 0L; // Giá mặc định nếu không nhập và cũng không có trong danh sách
            }
        }

        // Gọi hàm addItemToOrder
        addItemToOrder(name, quantity, price);
        edtLine.setText("");
    }
    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    /**
     * Chuyển đổi chuỗi (ví dụ "10000", "10k", "10.000", "10,000") thành giá trị Long.
     * Trả về null nếu không phải là định dạng giá hợp lệ.
     */
    private Long parsePrice(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        str = str.toLowerCase().trim();

        // Loại bỏ ký hiệu tiền tệ
        str = str.replace("đ", "")
                .replace("₫", "")
                .replace("vnđ", "")
                .replace("vnd", "")
                .trim();

        // 1. Ưu tiên xử lý định dạng "k" (vì "10.5k" dùng dấu . làm thập phân)
        if (str.endsWith("k")) {
            // Bỏ chữ 'k'
            String numPart = str.substring(0, str.length() - 1);
            try {
                // Dùng double để xử lý trường hợp "10.5k" -> 10500
                double value = Double.parseDouble(numPart);
                return (long) (value * 1000);
            } catch (NumberFormatException e2) {
                // Không hợp lệ (VD: "abck")
                return null;
            }
        }

        // 2. Nếu không phải "k", loại bỏ tất cả dấu . và , (coi là phân cách hàng nghìn)
        // "5.000" -> "5000"
        // "5,000" -> "5000"
        String sanitizedStr = str.replace(".", "").replace(",", "");

        // 3. Thử parse chuỗi đã được làm sạch (VD: "5000" hoặc "10000")
        try {
            return Long.parseLong(sanitizedStr);
        } catch (NumberFormatException e) {
            // Không phải "k" và cũng không phải số hợp lệ sau khi làm sạch (VD: "abc")
            return null;
        }
    }
    // Nối các phần tử trong mảng tokens thành một chuỗi, cách nhau bởi dấu cách
    private String joinTokens(String[] tokens, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++) {
            if (i > start) sb.append(" ");
            sb.append(tokens[i]);
        }
        return sb.toString();
    }
    /**
     * Thêm item vào đơn hàng.
     * Gộp item nếu CẢ TÊN VÀ GIÁ đều trùng khớp.
     */
    private void addItemToOrder(String name, int quantity, long price) {
        if (quantity <= 0) quantity = 1;

        int finalQuantity = quantity; // số lượng cuối cùng dùng để update badge

        // Kiểm tra xem sản phẩm đã có trong giỏ chưa (tên + giá)
        for (int i = 0; i < currentOrderItems.size(); i++) {
            OrderItemEntity item = currentOrderItems.get(i);
            if (item.productName.equalsIgnoreCase(name) && item.unitPrice == price) {
                item.quantity += quantity; // gộp số lượng
                finalQuantity = item.quantity; // cập nhật số lượng cuối cùng
                orderLineAdapter.notifyItemChanged(i);
                updateCartUI();
                break; // đã tìm thấy, thoát vòng lặp
            }
        }

        // Nếu không tìm thấy, thêm sản phẩm mới
        boolean isNewItem = true;
        for (OrderItemEntity item : currentOrderItems) {
            if (item.productName.equalsIgnoreCase(name) && item.unitPrice == price) {
                isNewItem = false;
                break;
            }
        }

        if (isNewItem) {
            OrderItemEntity newItem = new OrderItemEntity();
            newItem.productName = name;
            newItem.quantity = quantity;
            newItem.unitPrice = price;
            newItem.note = ""; // mặc định
            currentOrderItems.add(newItem);
            orderLineAdapter.notifyItemInserted(currentOrderItems.size() - 1);
        }

        // --- Cập nhật Quick Grid ---
        for (ProductEntity p : quickProducts) {
            if (p.name.equalsIgnoreCase(name) && p.price == price) {
                p.selected = finalQuantity;
                break;
            }
        }
        quickGridAdapter.notifyDataSetChanged();
        updateCartUI();
    }


    // Khi mở dialog thêm sản phẩm (nút +)
    private void openAddDialog() {
        AddProductSheet sheet = new AddProductSheet((name, price) -> {
            ProductEntity product = new ProductEntity(name, price);
            productViewModel.insertProduct(product);
            // LiveData sẽ tự động cập nhật lưới quickProducts
        });
        sheet.show(getSupportFragmentManager(), "add_product");
    }

    // Mở dialog chỉnh sửa
    private void openEditDialog(OrderItemEntity item, int position) {
        EditOrderItemDialog dialog = EditOrderItemDialog.newInstance(item);
        dialog.setOnSaveListener(updatedItem -> {
            // Cập nhật item trong list
            currentOrderItems.set(position, updatedItem);
            orderLineAdapter.notifyItemChanged(position);
            // Tìm sản phẩm tương ứng trong quick grid để cập nhật badge
            for (ProductEntity p : quickProducts) {
                if (p.name.equals(currentOrderItems.get(position).productName)) {
                    p.selected = currentOrderItems.get(position).quantity;
                    break;
                }
            }
            quickGridAdapter.notifyDataSetChanged();
            updateCartUI();
        });
        dialog.setOnDeleteListener(itemToDelete -> {
            // Xoá item
            // Xóa an toàn khỏi currentOrderItems
            if (position != RecyclerView.NO_POSITION && position < currentOrderItems.size()) {
                currentOrderItems.remove(position);
                orderLineAdapter.notifyItemRemoved(position);
            }
            orderLineAdapter.notifyDataSetChanged();
            updateCartUI();
        });
        dialog.show(getSupportFragmentManager(), "edit_order_item");
    }

    // Cập nhật UI giỏ hàng (tổng tiền, ẩn/hiện guide)
    private void updateCartUI() {
        if (currentOrderItems.isEmpty()) {
            // Giỏ hàng rỗng
            contentGuide.setVisibility(View.VISIBLE);
            cartScrollView.setVisibility(View.GONE);
            btnDone.setEnabled(false);
        } else {
            // Có hàng
            contentGuide.setVisibility(View.GONE);
            cartScrollView.setVisibility(View.VISIBLE);
            btnDone.setEnabled(true);
        }

        // Tính tổng tiền
        long total = 0;
        for (OrderItemEntity item : currentOrderItems) {
            total += (item.unitPrice * item.quantity);
        }
        tvTotal.setText(String.format(Locale.US, "%,d", total));
    }

    // Lưu đơn hàng và thoát
    private void saveOrderAndFinish() {
        // 1. Kiểm tra (dù nút đã bị disable)
        if (currentOrderItems.isEmpty()) {
            return;
        }
        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();

        // 2. Tạo OrderEntity mới
        OrderEntity order = new OrderEntity();

        // Nếu là Sửa, dùng lại ID cũ
        if (isEditMode) {
            order.id = editingOrderId; // Gán lại ID cũ để Update
            // Gán lại thời gian tạo cũ Nếu không gán, nó sẽ là 0, và Room sẽ ghi đè số 0 vào DB
            order.createdAt = editingCreatedAt;
        }

        // 3. Lấy thông tin khách hàng
        String customerName = tvCustomer.getText().toString();
        if (customerName.equals("Khách hàng, phòng bàn...")) {
            order.customerName = "Khách lẻ"; // Tên mặc định
        } else {
            order.customerName = customerName;
        }

        // 4. Tính tổng tiền (an toàn hơn là tính lại)
        long total = 0;
        for (OrderItemEntity item : currentOrderItems) {
            total += (item.unitPrice * item.quantity);
        }
        order.totalAmount = total;

        // 5. Set các trường mặc định cho đơn hàng mới
        order.status = "UNPAID"; // Mặc định là chưa thanh toán
        order.paymentMethod = "CASH"; // Mặc định là tiền mặt
        order.sellerId = userId; // Sẽ cập nhật sau khi có logic login
        // (createdAt và updatedAt sẽ được OrderRepository tự động thêm)

        // 6. Gọi ViewModel để lưu
        // (currentOrderItems chính là List<OrderItemEntity> mà ViewModel cần)
        orderEditViewModel.saveOrder(order, currentOrderItems);

        // Logic điều hướng
        if (isEditMode) {
            //Quay trở lại OrderDetailActivity
            Intent intent = new Intent(this, OrderDetailActivity.class);
            intent.putExtra("order_id", editingOrderId); // Gửi lại ID
            startActivity(intent);
            finish(); // Đóng SaleActivity
        } else {
            // Nếu là Tạo mới, quay về Main và mở tab Order
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("NAVIGATE_TO", "ORDERS_TAB");
            startActivity(intent);
            finish();
        }

        // 7. Chuyển hướng về MainActivity VÀ yêu cầu mở tab Order
        Intent intent = new Intent(this, MainActivity.class);

        // Đặt cờ để không tạo MainActivity mới nếu nó đã chạy
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Gửi "tin nhắn"
        intent.putExtra("NAVIGATE_TO", "ORDERS_TAB");

        startActivity(intent);

        // 7. Lưu thành công, đóng Activity
        finish();
    }

    // Gắn ItemTouchHelper để xử lý vuốt
    private RecyclerView.ViewHolder openedViewHolder = null; // giữ item đang mở
    private void attachSwipeHelper() {
        final int buttonWidth = dp(160);
        final long animDuration = 160;

        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Không thực hiện xóa gì ở đây
                orderLineAdapter.notifyItemChanged(viewHolder.getBindingAdapterPosition());
            }

            @Override
            public void onChildDraw(@NonNull Canvas c,
                                    @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View content = viewHolder.itemView.findViewById(R.id.contentView);

                    // Đảm bảo chỉ 1 item mở
                    for (int i = 0; i < recyclerView.getChildCount(); i++) {
                        View child = recyclerView.getChildAt(i);
                        if (child != viewHolder.itemView) {
                            View cv = child.findViewById(R.id.contentView);
                            if (cv != null && cv.getTranslationX() != 0) {
                                cv.animate().translationX(0).setDuration(100).start();
                            }
                        }
                    }

                    float current = content.getTranslationX();
                    float newTx = current + dX;

                    // Giới hạn trượt
                    newTx = Math.max(-buttonWidth, Math.min(0, newTx));
                    content.setTranslationX(newTx);

                    // Khi thả tay
                    if (!isCurrentlyActive) {
                        if (newTx < -buttonWidth / 2f) {
                            // Mở hẳn
                            content.animate()
                                    .translationX(-buttonWidth)
                                    .setDuration(animDuration)
                                    .setInterpolator(new DecelerateInterpolator())
                                    .withEndAction(() -> {
                                        // Nếu có item khác đang mở → đóng nó
                                        if (openedViewHolder != null && openedViewHolder != viewHolder) {
                                            View oldContent = openedViewHolder.itemView.findViewById(R.id.contentView);
                                            if (oldContent != null) {
                                                oldContent.animate()
                                                        .translationX(0)
                                                        .setDuration(120)
                                                        .start();
                                            }
                                        }
                                        // Ghi lại item hiện tại là đang mở
                                        openedViewHolder = viewHolder;
                                    })
                                    .start();
                        } else if (newTx > -buttonWidth / 4f) {
                            // Đóng hẳn
                            content.animate()
                                    .translationX(0)
                                    .setDuration(animDuration)
                                    .setInterpolator(new DecelerateInterpolator())
                                    .withEndAction(() -> {
                                        // Nếu đây là item đang mở → clear
                                        if (openedViewHolder == viewHolder) {
                                            openedViewHolder = null;
                                        }
                                    })
                                    .start();
                        }
                    }
                }
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder) {
                // Luôn cho phép kéo 2 hướng
                return ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            }

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                // Giữ nguyên vị trí sau khi vuốt (ngăn tự reset)
                return 2f; // >1 để ItemTouchHelper KHÔNG coi là vuốt hoàn tất
            }

            @Override
            public float getSwipeEscapeVelocity(float defaultValue) {
                // Ngăn ItemTouchHelper "tự kéo về"
                return Float.MAX_VALUE;
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(rvOrderLines);
    }
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view == null) view = new View(this);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void updateLayout() {
        // Hiển thị/ẩn rvQuickGrid
        rvQuickGrid.setVisibility(gridVisible ? RecyclerView.VISIBLE : RecyclerView.GONE);

        // Hiển thị/ẩn includeListening
        includeListening.setVisibility(listeningVisible ? View.VISIBLE : View.GONE);

        // Cập nhật quickBar
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) quickBar.getLayoutParams();
        params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.removeRule(RelativeLayout.ABOVE);

        if (gridVisible) {
            params.addRule(RelativeLayout.ABOVE, R.id.rvQuickGrid);
        } else if (listeningVisible) {
            params.addRule(RelativeLayout.ABOVE, R.id.includeListening);
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }

        quickBar.setLayoutParams(params);
    }


    private int dp(int dp) {
        return Math.round(getResources().getDisplayMetrics().density * dp);
    }
}