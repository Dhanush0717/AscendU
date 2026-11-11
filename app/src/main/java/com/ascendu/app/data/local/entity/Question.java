package com.ascendu.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Technical question for active recall, concept explanation, and interview prep.
 */
@Entity(tableName = "questions")
public class Question {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String topic; // Java & OOP, DSA, DBMS, Operating Systems, Computer Networks
    private String difficulty; // Easy, Medium, Hard
    private String questionText;
    private String referenceAnswer;
    private int reviewStatus; // 0 = Unseen, 1 = Needs Revision, 2 = Understood, 3 = Mastered
    private long lastReviewedTimestamp;

    public Question(String topic, String difficulty, String questionText,
                    String referenceAnswer, int reviewStatus, long lastReviewedTimestamp) {
        this.topic = topic;
        this.difficulty = difficulty;
        this.questionText = questionText;
        this.referenceAnswer = referenceAnswer;
        this.reviewStatus = reviewStatus;
        this.lastReviewedTimestamp = lastReviewedTimestamp;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getReferenceAnswer() {
        return referenceAnswer;
    }

    public void setReferenceAnswer(String referenceAnswer) {
        this.referenceAnswer = referenceAnswer;
    }

    public int getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(int reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public long getLastReviewedTimestamp() {
        return lastReviewedTimestamp;
    }

    public void setLastReviewedTimestamp(long lastReviewedTimestamp) {
        this.lastReviewedTimestamp = lastReviewedTimestamp;
    }
}
