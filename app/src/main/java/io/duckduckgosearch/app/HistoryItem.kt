package io.duckduckgosearch.app

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
class HistoryItem internal constructor(@field:PrimaryKey var searchTerm: String, @field:ColumnInfo(name = "search_date") var searchDate: Date)