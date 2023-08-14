package com.example.barcodebrawlers.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.barcodebrawlers.dao.BrawlerDao
import com.example.barcodebrawlers.entities.BrawlerEntity

@Database(entities = [BrawlerEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun brawlerDao(): BrawlerDao

    object DatabaseProvider {
        private var database: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            if (database == null) {
                database = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "brawler-database"
                )
                    .allowMainThreadQueries()
                    .build()
            }
            return database!!
        }
    }
}
