package io.duckduckgosearch.app

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [HistoryItem::class], version = 1, exportSchema = false)
@TypeConverters(DbConverters::class)
abstract class HistoryDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        var instance:HistoryDatabase? = null

        fun getHistoryDatabase(context: Context): HistoryDatabase? {
            if (instance == null) {
                synchronized(HistoryDatabase::class) {
                    instance = Room.databaseBuilder(context.applicationContext,HistoryDatabase::class.java,HistoryFragment.HISTORY_DB_NAME)
                            .build()
                }
            }
            return instance
        }

        fun destroyHistoryDatabase() {
            instance = null
        }
    }
}