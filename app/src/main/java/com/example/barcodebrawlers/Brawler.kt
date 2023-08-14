package com.example.barcodebrawlers

import com.example.barcodebrawlers.entities.BrawlerEntity

class Brawler (val name: String,
               val ID: Int,
               val Description: String,
               val Strength: Int,
               val Agility: Int,
               val Intelligence: Int) {
    override fun toString() = name

    fun toEntity(): BrawlerEntity {
        return BrawlerEntity(
            ID = this.ID,
            name = this.name,
            description = this.Description,
            strength = this.Strength,
            agility = this.Agility,
            intelligence = this.Intelligence
        )
    }
}
