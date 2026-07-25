package com.example.nba_naac_ems;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    private LinearLayout detailsContainer;
    private TextView detailTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        detailsContainer = findViewById(R.id.detailsContainer);
        detailTitle = findViewById(R.id.detailTitle);
        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        Object data = getIntent().getSerializableExtra("data");
        if (data instanceof StudentAchievement) {
            displayStudentDetails((StudentAchievement) data);
        } else if (data instanceof FacultyAchievement) {
            displayFacultyDetails((FacultyAchievement) data);
        } else if (data instanceof EventAchievement) {
            displayEventDetails((EventAchievement) data);
        }

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        findViewById(R.id.navEvidence).setOnClickListener(v -> {
            startActivity(new Intent(this, EvidenceActivity.class));
            finish();
        });
        findViewById(R.id.navReports).setOnClickListener(v -> {
            startActivity(new Intent(this, ReportActivity.class));
            finish();
        });
    }

    private void displayStudentDetails(StudentAchievement s) {
        detailTitle.setText("Student Achievement");
        addDetailRow("Student Name", s.name);
        addDetailRow("Admission No", s.admission);
        addDetailRow("Specialization", s.specialization);
        addDetailRow("Passed Out Year", s.year);
        addDetailRow("Achievement Date", s.date);
        addDetailRow("Achievement Title", s.title);
        addDetailRow("Achievement Type", s.type);
        addDetailRow("Description", s.description);
    }

    private void displayFacultyDetails(FacultyAchievement f) {
        detailTitle.setText("Faculty Achievement");
        addDetailRow("Faculty Name", f.name);
        addDetailRow("Employee ID", f.empId);
        addDetailRow("Department", f.department);
        addDetailRow("Academic Year", f.year);
        addDetailRow("Achievement Date", f.date);
        addDetailRow("Achievement Title", f.title);
        addDetailRow("Achievement Type", f.type);
        addDetailRow("Description", f.description);
    }

    private void displayEventDetails(EventAchievement e) {
        detailTitle.setText("Event Details");
        addDetailRow("Event Name", e.name);
        addDetailRow("Event Type", e.type);
        addDetailRow("Department", e.department);
        addDetailRow("Event Date", e.date);
        addDetailRow("Venue", e.venue);
        addDetailRow("Coordinator", e.coordinator);
        addDetailRow("Speaker / Guest", e.speaker);
        addDetailRow("Participants", e.participants);
        addDetailRow("Description", e.description);
    }

    private void addDetailRow(String label, String value) {
        if (value == null || value.isEmpty()) return;

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, 0, 0, 30);

        TextView labelTv = new TextView(this);
        labelTv.setText(label);
        labelTv.setTextSize(14);
        labelTv.setTextColor(Color.GRAY);
        labelTv.setAllCaps(true);
        labelTv.setTypeface(null, Typeface.BOLD);

        TextView valueTv = new TextView(this);
        valueTv.setText(value);
        valueTv.setTextSize(17);
        valueTv.setTextColor(Color.BLACK);
        valueTv.setPadding(0, 5, 0, 0);

        row.addView(labelTv);
        row.addView(valueTv);
        detailsContainer.addView(row);

        // Add a divider
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2));
        divider.setBackgroundColor(Color.parseColor("#EEEEEE"));
        detailsContainer.addView(divider);
        
        // Extra padding for next row
        View space = new View(this);
        space.setLayoutParams(new LinearLayout.LayoutParams(1, 20));
        detailsContainer.addView(space);
    }
}
