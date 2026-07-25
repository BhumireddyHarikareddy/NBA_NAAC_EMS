package com.example.nba_naac_ems;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    TextView studentCountTv, facultyCountTv, eventCountTv, documentCountTv;
    DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Stats UI
        studentCountTv = findViewById(R.id.studentCountTv);
        facultyCountTv = findViewById(R.id.facultyCountTv);
        eventCountTv = findViewById(R.id.eventCountTv);
        documentCountTv = findViewById(R.id.documentCountTv);

        rootRef = FirebaseDatabase.getInstance().getReference();

        updateStatistics();

        LinearLayout studentModule = findViewById(R.id.studentModule);
        LinearLayout studentsModuleCard = findViewById(R.id.studentsModuleCard);
        LinearLayout facultyStatsCard = findViewById(R.id.facultyStatsCard);
        LinearLayout facultyModuleCard = findViewById(R.id.facultyModuleCard);
        LinearLayout eventStatsCard = findViewById(R.id.eventStatsCard);
        LinearLayout eventsModuleCard = findViewById(R.id.eventsModuleCard);

        studentModule.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, StudentListActivity.class));
        });

        studentsModuleCard.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, StudentListActivity.class));
        });

        facultyStatsCard.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, FacultyListActivity.class));
        });

        facultyModuleCard.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, FacultyListActivity.class));
        });

        eventStatsCard.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, EventListActivity.class));
        });

        eventsModuleCard.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, EventListActivity.class));
        });

        findViewById(R.id.navEvidence).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, EvidenceActivity.class));
        });

        findViewById(R.id.navReports).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ReportActivity.class));
        });

        // existing code
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void updateStatistics() {
        // Update Student Achievements Count
        rootRef.child("StudentAchievements").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                studentCountTv.setText(String.valueOf(count));
                updateTotalDocuments();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Update Faculty Achievements Count
        rootRef.child("FacultyAchievements").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                facultyCountTv.setText(String.valueOf(count));
                updateTotalDocuments();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Update Events Count
        rootRef.child("Events").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                eventCountTv.setText(String.valueOf(count));
                updateTotalDocuments();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateTotalDocuments() {
        // Since "Documents Uploaded" counts the entries across all modules
        try {
            long sCount = Long.parseLong(studentCountTv.getText().toString());
            long fCount = Long.parseLong(facultyCountTv.getText().toString());
            long eCount = Long.parseLong(eventCountTv.getText().toString());
            documentCountTv.setText(String.valueOf(sCount + fCount + eCount));
        } catch (Exception ignored) {}
    }
}