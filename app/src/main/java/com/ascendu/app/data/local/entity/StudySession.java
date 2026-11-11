package com.ascendu.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Records a discrete study or coding session with verified elapsed minutes.
 */
@Entity(tableName = "study_sessions")
public class StudySession {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String subjectOrSkill;
    private int durationMinutes;
    private String sessionType; // Focused Study, Coding, DSA, Revision, Communication
    private String notes;
    private long timestamp;

    public StudySession(String subjectOrSkill, int durationMinutes, String sessionType, String notes, long timestamp) {
        this.subjectOrSkill = subjectOrSkill;
        this.durationMinutes = durationMinutes;
        this.sessionType = sessionType;
        this.notes = notes;
        this.timestamp = timestamp;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSubjectOrSkill() {
        return subjectOrSkill;
    }

    public void setSubjectOrSkill(String subjectOrSkill) {
        this.subjectOrSkill = subjectOrSkill;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
