package com.example.nba_naac_ems;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EvidenceActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private LinearLayout listContainer, filterPanel;
    private LinearLayout studentFilters, facultyFilters, eventFilters;
    private TextView currentViewTitle;
    private EditText searchBar;
    private ImageView filterBtn;
    private DatabaseReference dbRef;

    private CheckBox cbYear, cbSpec, cbDept, cbSortDate, cbEventType;
    private Spinner spinnerYear, spinnerSpec, spinnerDept, spinnerEventType;

    private String currentMode = "Students"; 
    private List<Object> allData = new ArrayList<>();
    private List<Object> filteredData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evidence);

        drawerLayout = findViewById(R.id.drawerLayout);
        listContainer = findViewById(R.id.listContainer);
        filterPanel = findViewById(R.id.filterPanel);
        currentViewTitle = findViewById(R.id.currentViewTitle);
        searchBar = findViewById(R.id.searchBar);
        filterBtn = findViewById(R.id.filterBtn);
        findViewById(R.id.menuBtn).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        bindFilters();
        setupSpinners();
        setupNavigation();
        setupSearch();
        setupFilterToggles();
        setupBottomNavigation();

        filterBtn.setOnClickListener(v -> {
            filterPanel.setVisibility(filterPanel.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        loadData("Students");
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        findViewById(R.id.navReports).setOnClickListener(v -> {
            startActivity(new Intent(this, ReportActivity.class));
            finish();
        });
    }

    private void bindFilters() {
        studentFilters = findViewById(R.id.studentFilters);
        facultyFilters = findViewById(R.id.facultyFilters);
        eventFilters = findViewById(R.id.eventFilters);

        cbYear = findViewById(R.id.cbYear);
        cbSpec = findViewById(R.id.cbSpec);
        cbDept = findViewById(R.id.cbDept);
        cbSortDate = findViewById(R.id.cbSortDate);
        cbEventType = findViewById(R.id.cbEventType);

        spinnerYear = findViewById(R.id.spinnerYear);
        spinnerSpec = findViewById(R.id.spinnerSpec);
        spinnerDept = findViewById(R.id.spinnerDept);
        spinnerEventType = findViewById(R.id.spinnerEventType);
    }

    private void setupSpinners() {
        // Year
        List<String> years = new ArrayList<>();
        for (int i = 1999; i <= 2040; i++) years.add(String.valueOf(i));
        setSpinnerAdapter(spinnerYear, years);

        // Specialization
        String[] specializations = {"MCA", "MTech", "BTech (Regular)", "BTech (AI)", "BTech (Self Supporting)"};
        setSpinnerAdapter(spinnerSpec, specializations);

        // Dept
        String[] depts = {"CSE", "ECE", "EEE", "MECH", "CIVIL", "IT", "MCA", "MBA", "S&H"};
        setSpinnerAdapter(spinnerDept, depts);

        // Event Type
        String[] eventTypes = {"Workshop", "Seminar", "Conference", "Faculty Development Program", "Guest Lecture", "Webinar", "Hackathon", "Training Program", "Industrial Visit", "Cultural Event", "Sports Event", "Awareness Program", "Placement Drive", "Other"};
        setSpinnerAdapter(spinnerEventType, eventTypes);
    }

    private void setSpinnerAdapter(Spinner spinner, Object data) {
        ArrayAdapter<String> adapter;
        if (data instanceof List) {
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, (List<String>)data);
        } else {
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, (String[])data);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { applyFilterAndSearch(); }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private void setupFilterToggles() {
        cbYear.setOnCheckedChangeListener((b, checked) -> { spinnerYear.setVisibility(checked ? View.VISIBLE : View.GONE); applyFilterAndSearch(); });
        cbSpec.setOnCheckedChangeListener((b, checked) -> { spinnerSpec.setVisibility(checked ? View.VISIBLE : View.GONE); applyFilterAndSearch(); });
        cbDept.setOnCheckedChangeListener((b, checked) -> { spinnerDept.setVisibility(checked ? View.VISIBLE : View.GONE); applyFilterAndSearch(); });
        cbSortDate.setOnCheckedChangeListener((b, checked) -> applyFilterAndSearch());
        cbEventType.setOnCheckedChangeListener((b, checked) -> { spinnerEventType.setVisibility(checked ? View.VISIBLE : View.GONE); applyFilterAndSearch(); });
    }

    private void setupNavigation() {
        findViewById(R.id.navStudents).setOnClickListener(v -> switchMode("Students"));
        findViewById(R.id.navFaculty).setOnClickListener(v -> switchMode("Faculty"));
        findViewById(R.id.navEvents).setOnClickListener(v -> switchMode("Events"));
    }

    private void switchMode(String mode) {
        currentMode = mode;
        currentViewTitle.setText("Showing: " + mode);
        drawerLayout.closeDrawers();
        
        studentFilters.setVisibility(mode.equals("Students") ? View.VISIBLE : View.GONE);
        facultyFilters.setVisibility(mode.equals("Faculty") ? View.VISIBLE : View.GONE);
        eventFilters.setVisibility(mode.equals("Events") ? View.VISIBLE : View.GONE);
        
        loadData(mode);
    }

    private void loadData(String mode) {
        String path = mode.equals("Students") ? "StudentAchievements" : (mode.equals("Faculty") ? "FacultyAchievements" : "Events");
        dbRef = FirebaseDatabase.getInstance().getReference(path);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allData.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    if (mode.equals("Students")) {
                        StudentAchievement item = data.getValue(StudentAchievement.class);
                        if (item != null) { item.id = data.getKey(); allData.add(item); }
                    } else if (mode.equals("Faculty")) {
                        FacultyAchievement item = data.getValue(FacultyAchievement.class);
                        if (item != null) { item.id = data.getKey(); allData.add(item); }
                    } else {
                        EventAchievement item = data.getValue(EventAchievement.class);
                        if (item != null) { item.id = data.getKey(); allData.add(item); }
                    }
                }
                applyFilterAndSearch();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupSearch() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilterAndSearch(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void applyFilterAndSearch() {
        String query = searchBar.getText().toString().toLowerCase().trim();
        filteredData.clear();

        for (Object obj : allData) {
            boolean matchesSearch = true;
            boolean matchesFilter = true;

            if (obj instanceof StudentAchievement) {
                StudentAchievement s = (StudentAchievement) obj;
                matchesSearch = s.name.toLowerCase().contains(query) || s.title.toLowerCase().contains(query);
                if (cbYear.isChecked()) matchesFilter &= s.year.equals(spinnerYear.getSelectedItem().toString());
                if (cbSpec.isChecked()) matchesFilter &= s.specialization.equals(spinnerSpec.getSelectedItem().toString());
            } else if (obj instanceof FacultyAchievement) {
                FacultyAchievement f = (FacultyAchievement) obj;
                matchesSearch = f.name.toLowerCase().contains(query) || f.title.toLowerCase().contains(query);
                if (cbDept.isChecked()) matchesFilter &= f.department.equals(spinnerDept.getSelectedItem().toString());
            } else if (obj instanceof EventAchievement) {
                EventAchievement e = (EventAchievement) obj;
                matchesSearch = e.name.toLowerCase().contains(query) || e.type.toLowerCase().contains(query);
                if (cbEventType.isChecked()) matchesFilter &= e.type.equals(spinnerEventType.getSelectedItem().toString());
            }

            if (matchesSearch && matchesFilter) filteredData.add(obj);
        }

        if (currentMode.equals("Events") && cbSortDate.isChecked()) {
            Collections.sort(filteredData, (a, b) -> ((EventAchievement) a).date.compareTo(((EventAchievement) b).date));
        }

        displayData();
    }

    private void displayData() {
        listContainer.removeAllViews();
        for (Object obj : filteredData) {
            if (obj instanceof StudentAchievement) addStudentCard((StudentAchievement) obj);
            else if (obj instanceof FacultyAchievement) addFacultyCard((FacultyAchievement) obj);
            else if (obj instanceof EventAchievement) addEventCard((EventAchievement) obj);
        }
    }

    private void addStudentCard(StudentAchievement item) { addGenericCard(item.name, item.title, item.date, item.type, "#E0F2FE", "#0369A1", item); }
    private void addFacultyCard(FacultyAchievement item) { addGenericCard(item.name, item.title, item.date, item.type, "#DCFCE7", "#166534", item); }
    private void addEventCard(EventAchievement item) { addGenericCard(item.name, item.type, item.date, item.department, "#F3F4F6", "#374151", item); }

    private void addGenericCard(String name, String sub, String date, String tag, String bgColor, String txtColor, Object fullData) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 30);
        card.setLayoutParams(params);
        card.setRadius(20);
        card.setCardBackgroundColor(Color.WHITE);
        card.setCardElevation(4);
        card.setContentPadding(25, 25, 25, 25);
        card.setClickable(true);
        card.setFocusable(true);

        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra("data", (java.io.Serializable) fullData);
            startActivity(intent);
        });

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_folder_24);
        icon.setLayoutParams(new LinearLayout.LayoutParams(80, 80));
        icon.setBackgroundResource(R.drawable.circle_bg);
        icon.setPadding(15, 15, 15, 15);
        topRow.addView(icon);

        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        tParams.setMargins(20, 0, 10, 0);
        textLayout.setLayoutParams(tParams);

        TextView nTv = new TextView(this); nTv.setText(name); nTv.setTextSize(16); nTv.setTypeface(null, Typeface.BOLD); nTv.setTextColor(Color.BLACK);
        TextView sTv = new TextView(this); sTv.setText(sub); sTv.setTextSize(13); sTv.setTextColor(Color.DKGRAY);
        TextView dTv = new TextView(this); dTv.setText(date); dTv.setTextSize(11); dTv.setTextColor(Color.GRAY);

        textLayout.addView(nTv); textLayout.addView(sTv); textLayout.addView(dTv);
        topRow.addView(textLayout);
        layout.addView(topRow);

        LinearLayout bottomRow = new LinearLayout(this);
        bottomRow.setGravity(Gravity.END);
        TextView tagTv = new TextView(this);
        tagTv.setText(tag); tagTv.setTextSize(10); tagTv.setPadding(15, 5, 15, 5);
        GradientDrawable gd = new GradientDrawable(); gd.setColor(Color.parseColor(bgColor)); gd.setCornerRadius(12);
        tagTv.setBackground(gd); tagTv.setTextColor(Color.parseColor(txtColor));
        bottomRow.addView(tagTv);
        layout.addView(bottomRow);

        card.addView(layout);
        listContainer.addView(card);
    }
}
