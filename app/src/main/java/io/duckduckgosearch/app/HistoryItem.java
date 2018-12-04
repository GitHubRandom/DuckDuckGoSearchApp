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
    String searchTerm;

    @ColumnInfo(name = "search_date")
    Date searchDate;

    String getSearchTerm() {
        return searchTerm;
    }

    Date getSearchDate() {
        return searchDate;
    }

    HistoryItem(@NonNull String searchTerm, Date searchDate) {
        this.searchTerm = searchTerm;
        this.searchDate = searchDate;
    }

}
