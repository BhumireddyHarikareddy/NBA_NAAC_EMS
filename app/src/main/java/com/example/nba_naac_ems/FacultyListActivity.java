package com.example.nba_naac_ems;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FacultyListActivity extends AppCompatActivity {

    Button addBtn;
    TextView backBtn;
    LinearLayout listContainer;
    DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_list);

        addBtn = findViewById(R.id.addBtn);
        backBtn = findViewById(R.id.backBtn);
        listContainer = findViewById(R.id.listContainer);

        databaseRef = FirebaseDatabase.getInstance().getReference("FacultyAchievements");

        backBtn.setOnClickListener(v -> finish());
        addBtn.setOnClickListener(v -> startActivity(new Intent(FacultyListActivity.this, FacultyAchievementActivity.class)));

        loadAchievements();
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

    private void loadAchievements() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listContainer.removeAllViews();
                if (!snapshot.exists()) {
                    showNoRecords();
                    return;
                }
                for (DataSnapshot data : snapshot.getChildren()) {
                    FacultyAchievement achievement = data.getValue(FacultyAchievement.class);
                    if (achievement != null) {
                        if (achievement.id == null || achievement.id.isEmpty()) {
                            achievement.id = data.getKey();
                        }
                        addCardToUI(achievement);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FacultyListActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNoRecords() {
        TextView tv = new TextView(this);
        tv.setText("No records yet");
        tv.setTextColor(Color.GRAY);
        tv.setTextSize(16);
        tv.setPadding(0, 50, 0, 0);
        tv.setGravity(Gravity.CENTER);
        listContainer.addView(tv);
    }

    private void addCardToUI(FacultyAchievement item) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 30);
        card.setLayoutParams(cardParams);
        card.setRadius(20);
        card.setCardBackgroundColor(Color.WHITE);
        card.setCardElevation(4);
        card.setContentPadding(25, 25, 25, 25);

        LinearLayout mainContent = new LinearLayout(this);
        mainContent.setOrientation(LinearLayout.VERTICAL);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.TOP);

        ImageView profileIcon = new ImageView(this);
        profileIcon.setImageResource(R.drawable.ic_group_24); 
        profileIcon.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
        profileIcon.setBackgroundResource(R.drawable.circle_bg);
        profileIcon.setPadding(20, 20, 20, 20);
        topRow.addView(profileIcon);

        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        textParams.setMargins(30, 0, 10, 0);
        textLayout.setLayoutParams(textParams);

        TextView nameTv = new TextView(this);
        nameTv.setText(item.name);
        nameTv.setTextSize(17);
        nameTv.setTypeface(null, Typeface.BOLD);
        nameTv.setTextColor(Color.parseColor("#111827"));

        TextView titleTv = new TextView(this);
        titleTv.setText(item.title);
        titleTv.setTextSize(14);
        titleTv.setTextColor(Color.parseColor("#4B5563"));

        TextView dateTv = new TextView(this);
        dateTv.setText(item.date);
        dateTv.setTextSize(12);
        dateTv.setTextColor(Color.parseColor("#9CA3AF"));

        textLayout.addView(nameTv);
        textLayout.addView(titleTv);
        textLayout.addView(dateTv);
        topRow.addView(textLayout);

        TextView menuBtn = new TextView(this);
        menuBtn.setText("⋮");
        menuBtn.setTextSize(24);
        menuBtn.setPadding(10, 0, 10, 0);
        menuBtn.setTextColor(Color.parseColor("#9CA3AF"));
        menuBtn.setOnClickListener(v -> showPopupMenu(v, item));
        topRow.addView(menuBtn);

        mainContent.addView(topRow);

        LinearLayout bottomRow = new LinearLayout(this);
        bottomRow.setOrientation(LinearLayout.HORIZONTAL);
        bottomRow.setGravity(Gravity.END);
        bottomRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView typeTag = new TextView(this);
        typeTag.setText(item.type);
        typeTag.setTextSize(11);
        typeTag.setPadding(20, 5, 20, 5);
        
        String tagColor = "#E0F2FE"; 
        String textColor = "#0369A1"; 
        
        if (item.type != null) {
            String t = item.type.toLowerCase();
            if (t.contains("publication") || t.contains("paper")) {
                tagColor = "#FFEDD5"; textColor = "#9A3412"; 
            } else if (t.contains("workshop") || t.contains("fdp") || t.contains("development")) {
                tagColor = "#DBEAFE"; textColor = "#1E40AF"; 
            } else if (t.contains("award") || t.contains("recognition")) {
                tagColor = "#DCFCE7"; textColor = "#166534"; 
            } else if (t.contains("project") || t.contains("consultancy")) {
                tagColor = "#F3E8FF"; textColor = "#6B21A8"; 
            } else if (t.contains("phd") || t.contains("patent")) {
                tagColor = "#FEE2E2"; textColor = "#991B1B"; 
            }
        }

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor(tagColor));
        gd.setCornerRadius(12);
        typeTag.setBackground(gd);
        typeTag.setTextColor(Color.parseColor(textColor));
        typeTag.setGravity(Gravity.CENTER);
        
        bottomRow.addView(typeTag);
        mainContent.addView(bottomRow);

        card.addView(mainContent);
        listContainer.addView(card);
    }

    private void showPopupMenu(View view, FacultyAchievement item) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add("Edit");
        popup.getMenu().add("Delete");

        popup.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getTitle().equals("Edit")) {
                Intent intent = new Intent(this, FacultyAchievementActivity.class);
                intent.putExtra("achievement", item);
                startActivity(intent);
            } else if (menuItem.getTitle().equals("Delete")) {
                deleteAchievement(item.id);
            }
            return true;
        });
        popup.show();
    }

    private void deleteAchievement(String id) {
        databaseRef.child(id).removeValue().addOnSuccessListener(unused -> 
                Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show()
        ).addOnFailureListener(e -> 
                Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
        );
    }
}
