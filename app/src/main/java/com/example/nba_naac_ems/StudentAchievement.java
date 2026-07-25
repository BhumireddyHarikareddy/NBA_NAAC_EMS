package com.example.nba_naac_ems;

import java.io.Serializable;

public class StudentAchievement implements Serializable {

    public String id, name, admission, year, title, specialization, type, description, date;

    public StudentAchievement() {
        // Required for Firebase
    }

    public StudentAchievement(String id, String name, String admission, String year,
                              String title, String specialization,
                              String type, String description, String date) {
        this.id = id;
        this.name = name;
        this.admission = admission;
        this.year = year;
        this.title = title;
        this.specialization = specialization;
        this.type = type;
        this.description = description;
        this.date = date;
    }
}
