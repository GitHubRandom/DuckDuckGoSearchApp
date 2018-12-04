package io.duckduckgosearch.app;

import java.util.Date;

import androidx.room.TypeConverter;

class DbConverters {

    @TypeConverter
    static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

}
