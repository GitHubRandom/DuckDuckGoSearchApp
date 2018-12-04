package io.duckduckgosearch.app;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface HistoryDao {

    @Query("SELECT * FROM HistoryItem")
    List<HistoryItem> getAllSearchHistory();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(HistoryItem... item);

    @Delete
    void delete(HistoryItem item);

}
