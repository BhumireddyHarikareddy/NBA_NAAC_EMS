package com.example.nba_naac_ems;

import android.app.DatePickerDialog;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StudentAchievementActivity extends AppCompatActivity {

    Spinner typeSpinner, yearSpinner, specializationSpinner;
    EditText otherType, name, admission, date, title, description;
    Button chooseFileBtn, submitBtn;
    TextView fileNameText, formTitle;

    DatabaseReference databaseRef;
    String achievementId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_achievement);

        // UI LINKING
        formTitle = findViewById(R.id.formTitle);
        typeSpinner = findViewById(R.id.typeSpinner);
        yearSpinner = findViewById(R.id.yearSpinner);
        specializationSpinner = findViewById(R.id.specializationSpinner);
        otherType = findViewById(R.id.otherType);
        name = findViewById(R.id.name);
        admission = findViewById(R.id.admission);
        date = findViewById(R.id.date);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        chooseFileBtn = findViewById(R.id.chooseFileBtn);
        fileNameText = findViewById(R.id.fileNameText);
        submitBtn = findViewById(R.id.submitBtn);
        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        databaseRef = FirebaseDatabase.getInstance().getReference("StudentAchievements");

        setupSpinners();
        setupDatePicker();

        // Check for Edit Mode
        StudentAchievement existing = (StudentAchievement) getIntent().getSerializableExtra("achievement");
        if (existing != null) {
            achievementId = existing.id;
            formTitle.setText("Edit Achievement");
            submitBtn.setText("Update Record");
            populateFields(existing);
        }

        chooseFileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, 101);
        });

        submitBtn.setOnClickListener(v -> saveAchievement());

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
        // Achievement Types
        String[] types = {"Select Type", "Academic", "Research", "Internship", "Placement", "Certification", "Hackathon", "Coding Contest", "Quiz Competition", "Paper Presentation", "Project Expo", "Workshop", "Sports", "Cultural", "Other"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                otherType.setVisibility(parent.getItemAtPosition(position).toString().equals("Other") ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Passed Out Year (1999 to 2040)
        List<String> years = new ArrayList<>();
        years.add("Select Year");
        for (int i = 1999; i <= 2040; i++) years.add(String.valueOf(i));
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        // Specialization
        String[] specializations = {"Select Specialization", "MCA", "MTech", "BTech (Regular)", "BTech (AI)", "BTech (Self Supporting)"};
        ArrayAdapter<String> specAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, specializations);
        specAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        specializationSpinner.setAdapter(specAdapter);
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

    private void populateFields(StudentAchievement item) {
        name.setText(item.name);
        admission.setText(item.admission);
        date.setText(item.date);
        title.setText(item.title);
        description.setText(item.description);

        setSpinnerSelection(yearSpinner, item.year);
        setSpinnerSelection(specializationSpinner, item.specialization);
        
        // Handle type spinner
        boolean found = false;
        for (int i = 0; i < typeSpinner.getCount(); i++) {
            if (typeSpinner.getItemAtPosition(i).toString().equals(item.type)) {
                typeSpinner.setSelection(i);
                found = true;
                break;
            }
        }
        if (!found && item.type != null) {
            typeSpinner.setSelection(typeSpinner.getCount() - 1); // Select "Other"
            otherType.setVisibility(View.VISIBLE);
            otherType.setText(item.type);
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void saveAchievement() {
        String sName = name.getText().toString();
        String sAdmission = admission.getText().toString();
        String sYear = yearSpinner.getSelectedItem().toString();
        String sSpec = specializationSpinner.getSelectedItem().toString();
        String sDate = date.getText().toString();
        String sTitle = title.getText().toString();
        String sDesc = description.getText().toString();
        String sType = typeSpinner.getSelectedItem().toString();

        if (sType.equals("Other")) sType = otherType.getText().toString();

        if (sName.isEmpty() || sAdmission.isEmpty() || sYear.equals("Select Year") || sSpec.equals("Select Specialization") || sDate.isEmpty() || sTitle.isEmpty() || sType.equals("Select Type")) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (achievementId == null) {
            achievementId = databaseRef.push().getKey();
        }

        StudentAchievement data = new StudentAchievement(achievementId, sName, sAdmission, sYear, sTitle, sSpec, sType, sDesc, sDate);

        databaseRef.child(achievementId).setValue(data)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Saved Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            fileNameText.setText(data.getData().getLastPathSegment());
            fileNameText.setTextColor(0xFF000000);
        }
    }
}
