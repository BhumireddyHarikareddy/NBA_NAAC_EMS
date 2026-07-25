package com.example.nba_naac_ems;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class EventAchievementActivity extends AppCompatActivity {

    Spinner typeSpinner, deptSpinner;
    EditText name, date, venue, coordinator, speaker, participants, description;
    Button chooseFileBtn, submitBtn;
    TextView fileNameText, formTitle;

    DatabaseReference databaseRef;
    String eventId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_achievement);

        formTitle = findViewById(R.id.formTitle);
        typeSpinner = findViewById(R.id.typeSpinner);
        deptSpinner = findViewById(R.id.deptSpinner);
        name = findViewById(R.id.eventName);
        date = findViewById(R.id.date);
        venue = findViewById(R.id.venue);
        coordinator = findViewById(R.id.coordinator);
        speaker = findViewById(R.id.speaker);
        participants = findViewById(R.id.participants);
        description = findViewById(R.id.description);
        chooseFileBtn = findViewById(R.id.chooseFileBtn);
        fileNameText = findViewById(R.id.fileNameText);
        submitBtn = findViewById(R.id.submitBtn);
        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        databaseRef = FirebaseDatabase.getInstance().getReference("Events");

        setupSpinners();
        setupDatePicker();

        EventAchievement existing = (EventAchievement) getIntent().getSerializableExtra("event");
        if (existing != null) {
            eventId = existing.id;
            formTitle.setText("Edit Event");
            submitBtn.setText("Update Event");
            populateFields(existing);
        }

        chooseFileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, 101);
        });

        submitBtn.setOnClickListener(v -> saveEvent());

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

    private void setupSpinners() {
        String[] eventTypes = {"Select Type", "Workshop", "Seminar", "Conference", "Faculty Development Program", "Guest Lecture", "Webinar", "Hackathon", "Training Program", "Industrial Visit", "Cultural Event", "Sports Event", "Awareness Program", "Placement Drive", "Other"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, eventTypes);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        String[] depts = {"Select Department", "CSE", "ECE", "EEE", "MECH", "CIVIL", "MBA", "MCA"};
        ArrayAdapter<String> deptAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, depts);
        deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deptSpinner.setAdapter(deptAdapter);
    }

    private void setupDatePicker() {
        date.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year1, monthOfYear, dayOfMonth) -> date.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year1),
                    year, month, day);
            datePickerDialog.show();
        });
    }

    private void populateFields(EventAchievement item) {
        name.setText(item.name);
        date.setText(item.date);
        venue.setText(item.venue);
        coordinator.setText(item.coordinator);
        speaker.setText(item.speaker);
        participants.setText(item.participants);
        description.setText(item.description);
        setSpinnerSelection(typeSpinner, item.type);
        setSpinnerSelection(deptSpinner, item.department);
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void saveEvent() {
        String sName = name.getText().toString();
        String sType = typeSpinner.getSelectedItem().toString();
        String sDept = deptSpinner.getSelectedItem().toString();
        String sDate = date.getText().toString();
        String sVenue = venue.getText().toString();
        String sCoordinator = coordinator.getText().toString();
        String sSpeaker = speaker.getText().toString();
        String sParticipants = participants.getText().toString();
        String sDesc = description.getText().toString();

        if (sName.isEmpty() || sType.equals("Select Type") || sDept.equals("Select Department") || sDate.isEmpty() || sVenue.isEmpty() || sCoordinator.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (eventId == null) eventId = databaseRef.push().getKey();

        EventAchievement data = new EventAchievement(eventId, sName, sType, sDept, sDate, sVenue, sCoordinator, sSpeaker, sParticipants, sDesc);
        databaseRef.child(eventId).setValue(data).addOnSuccessListener(unused -> {
            Toast.makeText(this, "Saved Successfully", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            fileNameText.setText(data.getData().getLastPathSegment());
            fileNameText.setTextColor(Color.BLACK);
        }
    }
}
