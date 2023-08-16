package com.example.barcodebrawlers.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.barcodebrawlers.entities.BrawlerEntity

@Dao
interface BrawlerDao {

    @Query("SELECT * FROM brawlers")
    fun getAllBrawlers(): List<BrawlerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(brawler: BrawlerEntity)

    @Query("DELETE FROM brawlers WHERE ID = :id")
    fun delete(id: Int)

    @Query("DELETE FROM brawlers")
    fun deleteAll()

    @Query("SELECT COUNT(*) FROM brawlers")
    fun getBrawlersCount(): Int
}