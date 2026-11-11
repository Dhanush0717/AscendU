package com.ascendu.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Motivational quote catalog.
 * Supports daily rotation and user favorite bookmarking.
 */
@Entity(tableName = "quotes")
public class Quote {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String quoteText;
    private String author;
    private boolean isFavorite;
    private String lastDisplayedDate; // "YYYY-MM-DD"

    public Quote(String quoteText, String author, boolean isFavorite, String lastDisplayedDate) {
        this.quoteText = quoteText;
        this.author = author;
        this.isFavorite = isFavorite;
        this.lastDisplayedDate = lastDisplayedDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getQuoteText() {
        return quoteText;
    }

    public void setQuoteText(String quoteText) {
        this.quoteText = quoteText;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public String getLastDisplayedDate() {
        return lastDisplayedDate;
    }

    public void setLastDisplayedDate(String lastDisplayedDate) {
        this.lastDisplayedDate = lastDisplayedDate;
    }
}
