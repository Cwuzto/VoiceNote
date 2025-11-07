// File: com/example/voicenote/vm/StoreViewModel.java
package com.example.voicenote.vm;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.voicenote.data.local.entity.StoreEntity;
import com.example.voicenote.data.local.entity.UserEntity;
import com.example.voicenote.data.repo.StoreRepository;
import com.example.voicenote.data.repo.UserRepository;

public class StoreViewModel extends AndroidViewModel {

    private final StoreRepository repository;
    private final UserRepository userRepository;
    private final MutableLiveData<Boolean> createStoreResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateStoreResult = new MutableLiveData<>();

    public StoreViewModel(@NonNull Application application) {
        super(application);
        this.repository = new StoreRepository(application);
        this.userRepository = new UserRepository(application);
    }

    public LiveData<Boolean> getCreateStoreResult() {
        return createStoreResult;
    }

    // [MỚI]
    public LiveData<Boolean> getUpdateStoreResult() {
        return updateStoreResult;
    }

    // [MỚI] Lấy LiveData Store
    public LiveData<StoreEntity> getStore(long ownerId) {
        return repository.getStoreByOwnerIdLiveData(ownerId);
    }

    // [MỚI] Lấy LiveData Owner
    public LiveData<UserEntity> getOwner(long ownerId) {
        return userRepository.getUserById(ownerId);
    }

    public void createStore(StoreEntity store) {
        repository.insertStore(store, () -> {
            createStoreResult.postValue(true);
        });
    }

    // Cập nhật store
    public void updateStore(StoreEntity store) {
        repository.updateStore(store);
        updateStoreResult.postValue(true);
    }
}