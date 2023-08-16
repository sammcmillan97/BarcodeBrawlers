package com.example.barcodebrawlers.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "brawlers")
data class BrawlerEntity(
    @PrimaryKey val ID: Int,
    val name: String,
    val description: String,
    val strength: Int,
    val agility: Int,
    val intelligence: Int,
    val imageResId: Int
)