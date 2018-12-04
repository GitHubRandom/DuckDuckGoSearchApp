package io.duckduckgosearch.app;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class HistoryItem {

    @PrimaryKey
    @NonNull
    public String searchTerm;

    @ColumnInfo(name = "search_date")
    public Date searchDate;

    public String getSearchTerm() {
        return searchTerm;
    }

    public Date getSearchDate() {
        return searchDate;
    }

    public HistoryItem(@NonNull String searchTerm, Date searchDate) {
        this.searchTerm = searchTerm;
        this.searchDate = searchDate;
    }

}
