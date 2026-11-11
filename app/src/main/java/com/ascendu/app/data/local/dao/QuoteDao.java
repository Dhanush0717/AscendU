package com.ascendu.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ascendu.app.data.local.entity.Quote;

import java.util.List;

@Dao
public interface QuoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Quote quote);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Quote> quotes);

    @Update
    void update(Quote quote);

    @Delete
    void delete(Quote quote);

    @Query("SELECT * FROM quotes WHERE isFavorite = 1 ORDER BY id DESC")
    LiveData<List<Quote>> getFavoriteQuotes();

    @Query("SELECT * FROM quotes ORDER BY id ASC")
    List<Quote> getAllQuotesDirect();

    @Query("SELECT * FROM quotes WHERE lastDisplayedDate = :dateString LIMIT 1")
    Quote getQuoteForDateDirect(String dateString);

    @Query("SELECT * FROM quotes ORDER BY RANDOM() LIMIT 1")
    Quote getRandomQuoteDirect();

    @Query("SELECT COUNT(*) FROM quotes")
    int getQuoteCountDirect();

    @Query("UPDATE quotes SET isFavorite = :isFavorite WHERE id = :id")
    void setFavorite(long id, boolean isFavorite);

    @Query("UPDATE quotes SET lastDisplayedDate = :dateString WHERE id = :id")
    void setLastDisplayedDate(long id, String dateString);
}
