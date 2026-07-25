package com.example.nba_naac_ems;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {

    private CheckBox checkStudents, checkFaculty, checkEvents, checkEvidence;
    private Spinner deptSpinner, yearSpinner;
    private EditText fromDate, toDate;
    private TextView sumStudents, sumFaculty, sumEvents, sumDocs;
    private TextView insightTopCategory, insightActiveModule, insightLatestEvent;
    private LinearLayout analyticsChartContainer, detailBreakdownContainer;
    private DatabaseReference dbRef;

    private final Map<String, Map<String, Integer>> reportData = new HashMap<>();
    private final String institutionName = "JNTUACEA Anantapuramu";
    private String globalLatestEvent = "N/A";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        dbRef = FirebaseDatabase.getInstance().getReference();
        initViews();
        setupSpinners();
        setupDatePickers();
        setupNavigation();

        findViewById(R.id.generateReportBtn).setOnClickListener(v -> generateReport());
        findViewById(R.id.downloadPdfBtn).setOnClickListener(v -> createPdfReport());
        findViewById(R.id.shareReportBtn).setOnClickListener(v -> shareReport());

        generateReport();
    }

    private void initViews() {
        checkStudents = findViewById(R.id.checkStudents);
        checkFaculty = findViewById(R.id.checkFaculty);
        checkEvents = findViewById(R.id.checkEvents);
        checkEvidence = findViewById(R.id.checkEvidence);
        deptSpinner = findViewById(R.id.deptSpinner);
        yearSpinner = findViewById(R.id.yearSpinner);
        fromDate = findViewById(R.id.fromDate);
        toDate = findViewById(R.id.toDate);

        sumStudents = findViewById(R.id.sumStudents);
        sumFaculty = findViewById(R.id.sumFaculty);
        sumEvents = findViewById(R.id.sumEvents);
        sumDocs = findViewById(R.id.sumDocs);

        insightTopCategory = findViewById(R.id.insightTopCategory);
        insightActiveModule = findViewById(R.id.insightActiveModule);
        insightLatestEvent = findViewById(R.id.insightLatestEvent);

        analyticsChartContainer = findViewById(R.id.analyticsChartContainer);
        detailBreakdownContainer = findViewById(R.id.detailBreakdownContainer);
    }

    private void setupSpinners() {
        String[] depts = {"CSE", "ECE", "EEE", "MECH", "CIVIL", "IT", "MCA", "MBA"};
        deptSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, depts));
        List<String> years = new ArrayList<>();
        for (int i = 2020; i <= 2030; i++) years.add(String.format(Locale.getDefault(), "%d-%d", i, (i + 1)));
        yearSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, years));
    }

    private void setupDatePickers() {
        fromDate.setOnClickListener(v -> showDatePicker(fromDate));
        toDate.setOnClickListener(v -> showDatePicker(toDate));
    }

    private void showDatePicker(EditText et) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> 
                et.setText(String.format(Locale.getDefault(), "%d/%d/%d", day, (month + 1), year)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setupNavigation() {
        findViewById(R.id.backBtn).setOnClickListener(v -> finish());
        findViewById(R.id.navHome).setOnClickListener(v -> { startActivity(new Intent(this, MainActivity.class)); finish(); });
        findViewById(R.id.navEvidence).setOnClickListener(v -> { startActivity(new Intent(this, EvidenceActivity.class)); finish(); });
    }

    private void generateReport() {
        analyticsChartContainer.removeAllViews();
        detailBreakdownContainer.removeAllViews();
        reportData.clear();
        globalLatestEvent = "N/A";
        
        insightTopCategory.setText(R.string.na_text);
        insightActiveModule.setText(R.string.na_text);
        insightLatestEvent.setText(R.string.na_text);

        if (checkStudents.isChecked()) fetchModuleData("StudentAchievements", "STUDENT ACHIEVEMENTS", "#6D28D9", sumStudents);
        else sumStudents.setText("0");

        if (checkFaculty.isChecked()) fetchModuleData("FacultyAchievements", "FACULTY ACHIEVEMENTS", "#16A34A", sumFaculty);
        else sumFaculty.setText("0");

        if (checkEvents.isChecked()) fetchModuleData("Events", "EVENTS", "#EA580C", sumEvents);
        else sumEvents.setText("0");

        if (checkEvidence.isChecked()) fetchModuleData("Evidence", "EVIDENCE REPOSITORY", "#2563EB", sumDocs);
        else sumDocs.setText("0");
    }

    private void fetchModuleData(String path, String label, String color, TextView sumView) {
        dbRef.child(path).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Integer> counts = new HashMap<>();
                int total = 0;

                for (DataSnapshot data : snapshot.getChildren()) {
                    String type = data.child("type").getValue(String.class);
                    if (type != null) {
                        Integer currentCount = counts.get(type);
                        counts.put(type, (currentCount != null ? currentCount : 0) + 1);
                        total++;
                    }
                    if (path.equals("Events")) {
                        String eventName = data.child("name").getValue(String.class);
                        if (eventName != null) globalLatestEvent = eventName;
                    }
                }

                sumView.setText(String.valueOf(total));
                reportData.put(label, counts);
                
                addChartSection(label, counts, color);
                addBreakdownSection(label, counts);
                updateInsights();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateInsights() {
        if (reportData.containsKey("STUDENT ACHIEVEMENTS")) {
            Map<String, Integer> counts = reportData.get("STUDENT ACHIEVEMENTS");
            if (counts != null) insightTopCategory.setText(getMaxCategory(counts));
        }
        if (reportData.containsKey("FACULTY ACHIEVEMENTS")) {
            Map<String, Integer> counts = reportData.get("FACULTY ACHIEVEMENTS");
            if (counts != null) insightActiveModule.setText(getMaxCategory(counts));
        }
        if (!globalLatestEvent.equals("N/A")) {
            insightLatestEvent.setText(globalLatestEvent);
        }
    }
    
    private String getMaxCategory(Map<String, Integer> counts) {
        String maxCat = "N/A";
        int max = -1;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                maxCat = entry.getKey();
            }
        }
        return maxCat;
    }

    private void addChartSection(String title, Map<String, Integer> counts, String color) {
        TextView tView = new TextView(this);
        tView.setText(title); tView.setTextSize(14); tView.setTypeface(null, Typeface.BOLD); 
        tView.setTextColor(Color.BLACK); tView.setPadding(0, 10, 0, 10);
        analyticsChartContainer.addView(tView);

        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(0, 5, 0, 5);

            TextView label = new TextView(this);
            label.setText(entry.getKey()); label.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 0.4f)); 
            label.setTextSize(12); label.setTextColor(Color.BLACK);

            View bar = new View(this);
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(entry.getValue() * 20, 24);
            barParams.setMargins(10, 0, 10, 0);
            bar.setLayoutParams(barParams); bar.setBackgroundColor(Color.parseColor(color));

            TextView val = new TextView(this);
            val.setText(String.valueOf(entry.getValue())); val.setTextSize(12); 
            val.setTypeface(null, Typeface.BOLD); val.setTextColor(Color.BLACK);

            row.addView(label); row.addView(bar); row.addView(val);
            analyticsChartContainer.addView(row);
        }
    }

    private void addBreakdownSection(String title, Map<String, Integer> counts) {
        TextView tView = new TextView(this);
        tView.setText(title); tView.setTextSize(14); tView.setTypeface(null, Typeface.BOLD); 
        tView.setTextColor(Color.BLACK); tView.setPadding(0, 10, 0, 5);
        detailBreakdownContainer.addView(tView);

        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            TextView item = new TextView(this);
            item.setText(String.format(Locale.getDefault(), "%s : %d", entry.getKey(), entry.getValue()));
            item.setPadding(20, 2, 0, 2); item.setTextColor(Color.BLACK);
            detailBreakdownContainer.addView(item);
        }
    }

    private void createPdfReport() {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        int margin = 50;
        int pageWidth = 595;
        int y = 60;
        int blueColor = Color.parseColor("#2563EB");

        // --- HEADER ---
        paint.setTextSize(18);
        paint.setFakeBoldText(true);
        paint.setColor(Color.BLACK);
        canvas.drawText("NBA / NAAC ACCREDITATION REPORT", 100, y, paint);
        y += 25;
        
        paint.setTextSize(14);
        paint.setFakeBoldText(false);
        canvas.drawText(institutionName, 180, y, paint);
        y += 15;

        // Header Horizontal Line
        paint.setStrokeWidth(2f);
        canvas.drawLine(margin, y, pageWidth - margin, y, paint);
        y += 30;

        // Details
        paint.setTextSize(12);
        canvas.drawText("Department: CSE", margin, y, paint); y += 20;
        canvas.drawText(String.format("Academic Year: %s", yearSpinner.getSelectedItem().toString()), margin, y, paint); y += 20;
        canvas.drawText(String.format("Generated On: %s", new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(new Date())), margin, y, paint);
        y += 40;

        // --- EXECUTIVE SUMMARY ---
        y = addSection(canvas, "EXECUTIVE SUMMARY", y, blueColor, margin, pageWidth);
        paint.setColor(Color.BLACK);
        paint.setTextSize(11);
        canvas.drawText("This report provides a comprehensive summary of student achievements,", margin, y, paint); y += 18;
        canvas.drawText("faculty achievements, events conducted, and supporting evidence uploaded", margin, y, paint); y += 18;
        canvas.drawText("during the selected academic period.", margin, y, paint);
        y += 40;

        // --- REPORT SUMMARY ---
        y = addSection(canvas, "REPORT SUMMARY", y, blueColor, margin, pageWidth);
        paint.setTextSize(12);
        canvas.drawText(String.format("🎓 Student Achievements      : %s", sumStudents.getText()), margin + 20, y, paint); y += 25;
        canvas.drawText(String.format("👨‍🏫 Faculty Achievements      : %s", sumFaculty.getText()), margin + 20, y, paint); y += 25;
        canvas.drawText(String.format("📅 Events Conducted          : %s", sumEvents.getText()), margin + 20, y, paint); y += 25;
        canvas.drawText(String.format("📁 Documents Uploaded        : %s", sumDocs.getText()), margin + 20, y, paint);
        y += 40;

        // --- ACHIEVEMENT ANALYTICS ---
        y = addSection(canvas, "ACHIEVEMENT ANALYTICS", y, blueColor, margin, pageWidth);
        paint.setTextSize(11);
        canvas.drawText("[ Bar Graph Data Visualization ]", margin + 20, y, paint); y += 25;
        if (checkStudents.isChecked()) { canvas.drawText("• Student Achievements Statistics included", margin + 40, y, paint); y += 18; }
        if (checkFaculty.isChecked()) { canvas.drawText("• Faculty Achievements Statistics included", margin + 40, y, paint); y += 18; }
        if (checkEvents.isChecked()) { canvas.drawText("• Events & Workshops Statistics included", margin + 40, y, paint); y += 18; }
        y += 30;

        // --- MODULE BREAKDOWNS ---
        for (String label : reportData.keySet()) {
            if (y > 700) { // Simple page break check
                document.finishPage(page);
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 60;
            }
            y = addSection(canvas, label, y, blueColor, margin, pageWidth);
            Map<String, Integer> counts = reportData.get(label);
            if (counts != null) {
                for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                    canvas.drawText(String.format(Locale.getDefault(), "%s : %d", entry.getKey(), entry.getValue()), margin + 20, y, paint);
                    y += 20;
                }
            }
            y += 20;
        }

        // --- TOP INSIGHTS ---
        y = addSection(canvas, "TOP INSIGHTS", y, blueColor, margin, pageWidth);
        canvas.drawText(String.format("Student Achievement: %s", insightTopCategory.getText()), margin + 20, y, paint); y += 25;
        canvas.drawText(String.format("Faculty Achievement: %s", insightActiveModule.getText()), margin + 20, y, paint); y += 25;
        canvas.drawText(String.format("📅 Latest Event: %s", insightLatestEvent.getText()), margin + 20, y, paint);
        y += 45;

        // --- CONCLUSION ---
        y = addSection(canvas, "CONCLUSION", y, blueColor, margin, pageWidth);
        paint.setTextSize(11);
        canvas.drawText("The department has demonstrated active participation in academic, research, and", margin, y, paint); y += 18;
        canvas.drawText("co-curricular activities. The uploaded evidence supports accreditation requirements", margin, y, paint); y += 18;
        canvas.drawText("and helps maintain organized institutional records.", margin, y, paint);
        y += 60;

        // Footer
        paint.setColor(Color.GRAY);
        paint.setTextSize(10);
        canvas.drawLine(margin, y, pageWidth - margin, y, paint);
        y += 20;
        canvas.drawText("Generated by NBA/NAAC Evidence Management System", 160, y, paint);

        document.finishPage(page);
        File file = new File(getExternalFilesDir(null), "NBA_Report.pdf");
        try {
            document.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "PDF Generated Successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, String.format("Error saving PDF: %s", e.getMessage()), Toast.LENGTH_SHORT).show();
        }
        document.close();
    }

    private int addSection(Canvas canvas, String title, int y, int color, int margin, int pageWidth) {
        Paint p = new Paint();
        p.setStrokeWidth(1f);
        p.setColor(Color.LTGRAY);
        canvas.drawLine(margin, y, pageWidth - margin, y, p);
        y += 25;
        
        p.setColor(color);
        p.setFakeBoldText(true);
        p.setTextSize(14);
        canvas.drawText(title, margin, y, p);
        y += 10;
        
        p.setStrokeWidth(1f);
        p.setColor(Color.LTGRAY);
        canvas.drawLine(margin, y, pageWidth - margin, y, p);
        y += 25;
        
        return y;
    }

    private void shareReport() {
        File file = new File(getExternalFilesDir(null), "NBA_Report.pdf");
        if (!file.exists()) { createPdfReport(); }
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Share Report"));
    }
}
