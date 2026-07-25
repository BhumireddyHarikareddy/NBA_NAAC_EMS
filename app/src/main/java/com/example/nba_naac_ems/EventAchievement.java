package com.example.nba_naac_ems;

import java.io.Serializable;

public class EventAchievement implements Serializable {

    public String id, name, type, department, date, venue, coordinator, speaker, participants, description;

    public EventAchievement() {
        // Required for Firebase
    }

    public EventAchievement(String id, String name, String type, String department, String date,
                           String venue, String coordinator, String speaker, String participants, String description) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.department = department;
        this.date = date;
        this.venue = venue;
        this.coordinator = coordinator;
        this.speaker = speaker;
        this.participants = participants;
        this.description = description;
    }
}
