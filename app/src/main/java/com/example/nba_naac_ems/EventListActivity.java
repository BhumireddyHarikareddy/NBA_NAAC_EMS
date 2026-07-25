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

public class EventListActivity extends AppCompatActivity {

    Button addBtn;
    TextView backBtn;
    LinearLayout listContainer;
    DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        addBtn = findViewById(R.id.addBtn);
        backBtn = findViewById(R.id.backBtn);
        listContainer = findViewById(R.id.listContainer);

        databaseRef = FirebaseDatabase.getInstance().getReference("Events");

        backBtn.setOnClickListener(v -> finish());
        addBtn.setOnClickListener(v -> startActivity(new Intent(EventListActivity.this, EventAchievementActivity.class)));

        loadEvents();
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

    private void loadEvents() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listContainer.removeAllViews();
                if (!snapshot.exists()) {
                    showNoRecords();
                    return;
                }
                for (DataSnapshot data : snapshot.getChildren()) {
                    EventAchievement event = data.getValue(EventAchievement.class);
                    if (event != null) {
                        if (event.id == null) event.id = data.getKey();
                        addCardToUI(event);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EventListActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNoRecords() {
        TextView tv = new TextView(this);
        tv.setText("No events yet");
        tv.setTextColor(Color.GRAY);
        tv.setTextSize(16);
        tv.setPadding(0, 50, 0, 0);
        tv.setGravity(Gravity.CENTER);
        listContainer.addView(tv);
    }

    private void addCardToUI(EventAchievement item) {
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

        ImageView eventIcon = new ImageView(this);
        eventIcon.setImageResource(R.drawable.ic_folder_24); 
        eventIcon.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
        eventIcon.setBackgroundResource(R.drawable.circle_bg);
        eventIcon.setPadding(20, 20, 20, 20);
        topRow.addView(eventIcon);

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

        TextView typeTv = new TextView(this);
        typeTv.setText(item.type);
        typeTv.setTextSize(14);
        typeTv.setTextColor(Color.parseColor("#4B5563"));

        TextView dateTv = new TextView(this);
        dateTv.setText(item.date);
        dateTv.setTextSize(12);
        dateTv.setTextColor(Color.parseColor("#9CA3AF"));

        textLayout.addView(nameTv);
        textLayout.addView(typeTv);
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

        TextView deptTag = new TextView(this);
        deptTag.setText(item.department);
        deptTag.setTextSize(11);
        deptTag.setPadding(20, 5, 20, 5);
        
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor("#F3F4F6")); 
        gd.setCornerRadius(12);
        deptTag.setBackground(gd);
        deptTag.setTextColor(Color.parseColor("#374151"));
        deptTag.setGravity(Gravity.CENTER);
        
        bottomRow.addView(deptTag);
        mainContent.addView(bottomRow);

        card.addView(mainContent);
        listContainer.addView(card);
    }

    private void showPopupMenu(View view, EventAchievement item) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add("Edit");
        popup.getMenu().add("Delete");
        popup.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getTitle().equals("Edit")) {
                Intent intent = new Intent(this, EventAchievementActivity.class);
                intent.putExtra("event", item);
                startActivity(intent);
            } else if (menuItem.getTitle().equals("Delete")) {
                databaseRef.child(item.id).removeValue();
            }
            return true;
        });
        popup.show();
    }
}
