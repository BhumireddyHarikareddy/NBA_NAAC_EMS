package com.example.nba_naac_ems;

import java.io.Serializable;

public class FacultyAchievement implements Serializable {

    public String id, name, empId, year, title, department, type, description, date;

    public FacultyAchievement() {

    }

    public FacultyAchievement(String id, String name, String empId, String year,
                              String title, String department,
                              String type, String description, String date) {
        this.id = id;
        this.name = name;
        this.empId = empId;
        this.year = year;
        this.title = title;
        this.department = department;
        this.type = type;
        this.description = description;
        this.date = date;
    }
}
