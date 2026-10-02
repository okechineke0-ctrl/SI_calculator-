package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class CalculationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val expression: String,
    val result: String,
    val solution: String? = null,
    val calculationType: String = "Scientific",
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val imageBase64: String? = null,
    val imageUrl: String? = null,
    val isSyncedToSupabase: Boolean = false,
    val supabaseId: String? = null
)
