package com.example.borrowhub.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.borrowhub.R;
import com.example.borrowhub.data.local.AppDatabase;
import com.example.borrowhub.data.local.SessionManager;
import com.example.borrowhub.data.local.entity.CategoryEntity;
import com.example.borrowhub.data.local.entity.ItemEntity;
import com.example.borrowhub.data.remote.dto.BorrowItemRequestDTO;
import com.example.borrowhub.data.remote.dto.BorrowRecordDTO;
import com.example.borrowhub.data.remote.dto.BorrowRequestDTO;
import com.example.borrowhub.data.remote.dto.ItemDTO;
import com.example.borrowhub.data.remote.dto.StudentDTO;
import com.example.borrowhub.repository.ItemRepository;
import com.example.borrowhub.repository.StudentRepository;
import com.example.borrowhub.repository.TransactionRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class TransactionViewModel extends AndroidViewModel {

    private final TransactionRepository transactionRepository;
    private final StudentRepository studentRepository;
    private final ItemRepository itemRepository;

    // --- Models ---

    /** Lightweight in-memory model for an active borrow transaction. */
    public static class ActiveBorrow {
        public final long id;
        public final String studentNumber;
        public final String studentName;
        public final String course;
        public final String collateral;
        public final List<String> items;
        public final String formattedDateTime;

        public ActiveBorrow(long id, String studentNumber, String studentName,
                            String course, String collateral, List<String> items,
                            String formattedDateTime) {
            this.id = id;
            this.studentNumber = studentNumber;
            this.studentName = studentName;
            this.course = course;
            this.collateral = collateral;
            this.items = items;
            this.formattedDateTime = formattedDateTime;
        }
    }
    /** Model for dynamic item rows in the Borrow form. */
    public static class ItemRow {
        public String type;
        public String name;
        public int itemId;

        public ItemRow() {
            this.type = "";
            this.name = "";
            this.itemId = 0;
        }

        public ItemRow(String type, String name) {
            this.type = type == null ? "" : type;
            this.name = name == null ? "" : name;
            this.itemId = 0;
        }
        
        public ItemRow(String type, String name, int itemId) {
            this.type = type == null ? "" : type;
            this.name = name == null ? "" : name;
            this.itemId = itemId;
        }
    }

    // --- State: Info Card ---
    private final MutableLiveData<String> currentDateTime = new MutableLiveData<>();
    private final MutableLiveData<String> processedByName = new MutableLiveData<>("");
    private Handler clockHandler;
    private final SimpleDateFormat clockFormat =
            new SimpleDateFormat("MMM dd, yyyy - hh:mm:ss a", Locale.US);
    private final Runnable clockRunnable = new Runnable() {
        @Override
        public void run() {
            currentDateTime.setValue(clockFormat.format(new Date()));
            if (clockHandler != null) {
                clockHandler.postDelayed(this, 1000);
            }
        }
    };

    // --- State: Borrow Workflow ---
    private final MutableLiveData<String> studentNameInput = new MutableLiveData<>("");
    private final MutableLiveData<String> courseInput = new MutableLiveData<>("");
    private final MutableLiveData<List<String>> availableCourses = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Long> studentIdFound = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> studentFound = new MutableLiveData<>(false);
    private final MutableLiveData<List<ItemRow>> itemRows = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> submitted = new MutableLiveData<>(false);
    private final MutableLiveData<String> submitError = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // --- State: Return Workflow ---
    private final MutableLiveData<List<ActiveBorrow>> activeBorrows = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<ActiveBorrow>> filteredBorrows = new MutableLiveData<>(new ArrayList<>());
    private String normalizedSearch = "";

    // --- State: Transaction History ---
    private final MutableLiveData<List<BorrowRecordDTO>> transactionHistory = new MutableLiveData<>(new ArrayList<>());

    // --- Data: Inventory ---
    private final LiveData<List<CategoryEntity>> categories;
    private final LiveData<List<ItemEntity>> allItems;

    public TransactionViewModel(@NonNull Application application) {
        this(application,
                new TransactionRepository(new SessionManager(application)),
                new StudentRepository(AppDatabase.getInstance(application), new SessionManager(application)),
                new ItemRepository(AppDatabase.getInstance(application), new SessionManager(application)));
    }

    public TransactionViewModel(@NonNull Application application,
                                TransactionRepository transactionRepository,
                                StudentRepository studentRepository,
                                ItemRepository itemRepository) {
        super(application);
        this.transactionRepository = transactionRepository;
        this.studentRepository = studentRepository;
        this.itemRepository = itemRepository;

        this.categories = itemRepository.getAllCategories();
        this.allItems = itemRepository.getAllItems();

        // Initialize Borrow form
        List<ItemRow> initial = new ArrayList<>();
        initial.add(new ItemRow());
        itemRows.setValue(initial);

        refreshCourses();

        // Fetch initial active transactions
        fetchActiveTransactions();

        // Start live clock; guard Handler init for unit-test environments where
        // Looper stubs throw RuntimeException.
        try {
            clockHandler = new Handler(Looper.getMainLooper());
            clockRunnable.run();
        } catch (RuntimeException ignored) {
            clockHandler = null;
        }

        // Load logged-in staff name (guarded for unit tests with mocked Application context)
        String fallbackName = application.getString(R.string.transaction_staff_name);
        try {
            SessionManager sessionManager = new SessionManager(application);
            String name = sessionManager.getUserName();
            processedByName.setValue(name != null && !name.isEmpty() ? name : fallbackName);
        } catch (NullPointerException ignored) {
            processedByName.setValue(fallbackName);
        }
    }

    // --- Getters: Info Card ---
    public LiveData<String> getCurrentDateTimeLive() { return currentDateTime; }
    public LiveData<String> getProcessedByName() { return processedByName; }

    // --- Getters: Borrow Workflow ---
    public LiveData<String> getStudentName() { return studentNameInput; }
    public LiveData<String> getCourse() { return courseInput; }
    public LiveData<List<String>> getAvailableCourses() { return availableCourses; }
    public LiveData<Boolean> isStudentFound() { return studentFound; }
    public LiveData<List<ItemRow>> getItemRows() { return itemRows; }
    public LiveData<Boolean> isSubmitted() { return submitted; }
    public LiveData<String> getSubmitError() { return submitError; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<List<CategoryEntity>> getCategories() { return categories; }
    public LiveData<List<ItemEntity>> getAllItems() { return allItems; }

    // --- Getters: Return Workflow ---
    public LiveData<List<ActiveBorrow>> getFilteredBorrows() { return filteredBorrows; }

    // --- Getters: Transaction History ---
    public LiveData<List<BorrowRecordDTO>> getTransactionHistory() { return transactionHistory; }

    // --- Logic: Student Lookup ---
    public void lookupStudent(String studentNumber) {
        if (studentNumber == null || studentNumber.trim().length() < 3) {
            studentNameInput.setValue("");
            courseInput.setValue("");
            studentFound.setValue(false);
            studentIdFound.setValue(null);
            return;
        }
        
        studentRepository.getStudentByNumber(studentNumber.trim(), new StudentRepository.OperationCallbackWithData<StudentDTO>() {
            @Override
            public void onSuccess(StudentDTO data) {
                studentNameInput.postValue(data.getName());
                courseInput.postValue(data.getCourse());
                studentFound.postValue(true);
                studentIdFound.postValue(data.getId());
            }

            @Override
            public void onError(String errorMessage) {
                studentNameInput.postValue("");
                courseInput.postValue("");
                studentFound.postValue(false);
                studentIdFound.postValue(null);
            }
        });
    }

    // --- Logic: Borrow Row Management ---
    public void addItemRow() {
        List<ItemRow> current = deepCopy(itemRows.getValue());
        current.add(new ItemRow());
        itemRows.setValue(current);
    }

    public void removeItemRow(int index) {
        List<ItemRow> current = deepCopy(itemRows.getValue());
        if (current.size() > 1 && index >= 0 && index < current.size()) {
            current.remove(index);
            itemRows.setValue(current);
        }
    }

    public void updateItemRowType(int index, String type) {
        List<ItemRow> current = deepCopy(itemRows.getValue());
        if (index >= 0 && index < current.size()) {
            current.get(index).type = type == null ? "" : type;
            current.get(index).name = "";
            current.get(index).itemId = 0;
            itemRows.setValue(current);
        }
    }

    public void updateItemRowName(int index, String name, int itemId) {
        List<ItemRow> current = deepCopy(itemRows.getValue());
        if (index >= 0 && index < current.size()) {
            current.get(index).name = name == null ? "" : name;
            current.get(index).itemId = itemId;
            itemRows.setValue(current);
        }
    }

    // --- Logic: Form Submission ---
    public void submitBorrow(String studentNumber, String studentNameStr, String courseStr, String collateral) {
        if (isBlank(studentNumber) || isBlank(studentNameStr)
                || isBlank(courseStr) || isBlank(collateral)) {
            submitError.setValue("Please complete all borrower fields.");
            return;
        }
        List<ItemRow> rows = itemRows.getValue();
        if (rows == null || rows.isEmpty()) {
            submitError.setValue("Please add at least one item.");
            return;
        }

        List<BorrowItemRequestDTO> itemsToBorrow = new ArrayList<>();
        for (ItemRow row : rows) {
            if (row.itemId == 0) {
                submitError.setValue("Please complete all item selections.");
                return;
            }
            itemsToBorrow.add(new BorrowItemRequestDTO(row.itemId, 1));
        }

        isLoading.setValue(true);
        BorrowRequestDTO request = new BorrowRequestDTO(
                studentIdFound.getValue(),
                studentNumber.trim(),
                collateral.trim(),
                itemsToBorrow
        );

        transactionRepository.borrow(request, new TransactionRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                isLoading.postValue(false);
                submitted.postValue(true);
                fetchActiveTransactions(); // Refresh the return list
            }

            @Override
            public void onError(String errorMessage) {
                isLoading.postValue(false);
                submitError.postValue(errorMessage);
            }
        });
    }

    public void clearSubmitError() { submitError.setValue(null); }

    public void resetForm() {
        studentNameInput.setValue("");
        courseInput.setValue("");
        studentFound.setValue(false);
        studentIdFound.setValue(null);
        List<ItemRow> initial = new ArrayList<>();
        initial.add(new ItemRow());
        itemRows.setValue(initial);
        submitted.setValue(false);
        submitError.setValue(null);
    }

    // --- Logic: Return Workflow ---
    public void fetchActiveTransactions() {
        MutableLiveData<List<BorrowRecordDTO>> rawData = new MutableLiveData<>();
        rawData.observeForever(records -> {
            if (records != null) {
                List<ActiveBorrow> mapped = new ArrayList<>();
                for (BorrowRecordDTO dto : records) {
                    mapped.add(mapDtoToActiveBorrow(dto));
                }
                activeBorrows.setValue(mapped);
                applyFilters();
            }
        });
        transactionRepository.getActiveTransactions(rawData);
    }

    // --- Logic: Transaction History ---
    public void fetchTransactionHistory(String search) {
        transactionRepository.getTransactionHistory(search, null, null, null, 1,
                records -> transactionHistory.postValue(records));
    }

    public void setSearchQuery(String query) {
        String trimmed = query == null ? "" : query.trim();
        normalizedSearch = trimmed.toLowerCase(Locale.US);
        applyFilters();
    }

    public void processReturn(long borrowId) {
        isLoading.setValue(true);
        transactionRepository.returnItem((int) borrowId, new TransactionRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                isLoading.postValue(false);
                fetchActiveTransactions(); // Refresh the list
            }

            @Override
            public void onError(String errorMessage) {
                isLoading.postValue(false);
                // We could use a different error livedata for return, but let's reuse submitError for simplicity
                submitError.postValue(errorMessage);
            }
        });
    }

    private void applyFilters() {
        List<ActiveBorrow> source = activeBorrows.getValue();
        if (source == null) {
            filteredBorrows.setValue(new ArrayList<>());
            return;
        }

        if (normalizedSearch.isEmpty()) {
            filteredBorrows.setValue(source);
            return;
        }

        List<ActiveBorrow> filtered = new ArrayList<>();
        for (ActiveBorrow borrow : source) {
            if (matchesBorrow(borrow)) {
                filtered.add(borrow);
            }
        }
        filteredBorrows.setValue(filtered);
    }

    private boolean matchesBorrow(ActiveBorrow borrow) {
        String search = normalizedSearch;
        if (borrow.studentName != null && borrow.studentName.toLowerCase(Locale.US).contains(search)) return true;
        if (borrow.studentNumber != null && borrow.studentNumber.toLowerCase(Locale.US).contains(search)) return true;
        if (borrow.items != null) {
            for (String item : borrow.items) {
                if (item != null && item.toLowerCase(Locale.US).contains(search)) return true;
            }
        }
        return false;
    }

    // --- Helpers ---
    private List<ItemRow> deepCopy(List<ItemRow> source) {
        List<ItemRow> copy = new ArrayList<>();
        if (source != null) {
            for (ItemRow row : source) {
                copy.add(new ItemRow(row.type, row.name, row.itemId));
            }
        }
        return copy;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void refreshCourses() {
        studentRepository.refreshCoursesFromApi(courseNames ->
                availableCourses.postValue(courseNames != null ? courseNames : new ArrayList<>())
        );
    }

    public String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US);
        return sdf.format(new Date());
    }

    public String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.US);
        return sdf.format(new Date());
    }

    private ActiveBorrow mapDtoToActiveBorrow(BorrowRecordDTO dto) {
        List<String> itemNames = new ArrayList<>();
        if (dto.getItems() != null) {
            for (ItemDTO item : dto.getItems()) {
                itemNames.add(item.getName());
            }
        }

        String studentName = "Unknown";
        String studentNumber = "N/A";
        String course = "N/A";

        if (dto.getStudent() != null) {
            studentName = dto.getStudent().getName();
            studentNumber = dto.getStudent().getStudentNumber();
            course = dto.getStudent().getCourse();
        }

        // Format date from backend (e.g., 2026-03-10T12:00:00Z)
        String rawBorrowedAt = dto.getBorrowedAt();
        String formattedDateTime = rawBorrowedAt;
        try {
            // ISO 8601 parser (UTC from backend)
            SimpleDateFormat isoParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US);
            isoParser.setTimeZone(TimeZone.getTimeZone("UTC"));

            // Output format: Mar 25, 2026, 11:30PM
            SimpleDateFormat phFormatter = new SimpleDateFormat("MMM d, yyyy, hh:mma", Locale.US);
            phFormatter.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));

            Date date = isoParser.parse(rawBorrowedAt);
            if (date != null) {
                formattedDateTime = phFormatter.format(date);
            }
        } catch (Exception e) {
            // Fallback for different ISO variations
            try {
                SimpleDateFormat isoParserShort = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                isoParserShort.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = isoParserShort.parse(rawBorrowedAt);
                if (date != null) {
                    SimpleDateFormat phFormatter = new SimpleDateFormat("MMM d, yyyy, hh:mma", Locale.US);
                    phFormatter.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));
                    formattedDateTime = phFormatter.format(date);
                }
            } catch (Exception ignored) {}
        }

        return new ActiveBorrow(
                dto.getId(),
                studentNumber,
                studentName,
                course,
                dto.getCollateral(),
                itemNames,
                formattedDateTime
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (clockHandler != null) {
            clockHandler.removeCallbacks(clockRunnable);
        }
    }
}
