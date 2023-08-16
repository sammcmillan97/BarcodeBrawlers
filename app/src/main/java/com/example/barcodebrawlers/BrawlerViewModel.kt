package com.example.barcodebrawlers

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.barcodebrawlers.dao.BrawlerDao
import com.example.barcodebrawlers.entities.BrawlerEntity


class BrawlerViewModel(private val brawlerDao: BrawlerDao) : ViewModel() {
    val allBrawlers = mutableStateListOf<BrawlerEntity>()

    suspend fun getAllBrawlers(){
        allBrawlers.addAll(brawlerDao.getAllBrawlers())
    }
}