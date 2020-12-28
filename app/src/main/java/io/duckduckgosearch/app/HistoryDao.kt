package io.duckduckgosearch.app

import androidx.room.*

@Dao
interface HistoryDao {
    @get:Query("SELECT * FROM HistoryItem")
    val allSearchHistory: List<HistoryItem?>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg item: HistoryItem?)

    @Delete
    fun delete(item: HistoryItem?)
}