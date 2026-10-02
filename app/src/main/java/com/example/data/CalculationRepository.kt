package com.example.data

import android.graphics.Bitmap
import android.util.Base64
import kotlinx.coroutines.flow.Flow
import java.io.ByteArrayOutputStream

class CalculationRepository(private val dao: CalculationDao) {

    val allHistory: Flow<List<CalculationEntity>> = dao.getAllHistory()
    val favorites: Flow<List<CalculationEntity>> = dao.getFavorites()

    fun searchHistory(query: String): Flow<List<CalculationEntity>> = dao.searchHistory(query)

    suspend fun saveCalculation(
        expression: String,
        result: String,
        solution: String? = null,
        type: String = "Scientific",
        bitmap: Bitmap? = null
    ): Long {
        var base64Img: String? = null
        if (bitmap != null) {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos)
            base64Img = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
        }

        return dao.insert(
            CalculationEntity(
                expression = expression,
                result = result,
                solution = solution,
                calculationType = type,
                imageBase64 = base64Img,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun saveAndSyncToSupabase(
        expression: String,
        result: String,
        solution: String?,
        bitmap: Bitmap?,
        type: String,
        supabaseUrl: String,
        supabaseKey: String
    ): Pair<Long, SupabaseSyncResult> {
        // 1. First save locally in Room Database for instant zero-latency offline persistence
        val localId = saveCalculation(expression, result, solution, type, bitmap)

        // 2. Perform Supabase sync
        val syncResult = SupabaseService.saveCalculationWithPhoto(
            expression = expression,
            result = result,
            solution = solution,
            bitmap = bitmap,
            calculationType = type,
            customUrl = supabaseUrl,
            customKey = supabaseKey
        )

        // 3. If cloud sync succeeds, update Room record
        if (syncResult.isSuccess) {
            dao.updateSyncStatus(
                id = localId,
                synced = true,
                supabaseId = syncResult.supabaseId,
                imageUrl = syncResult.imageUrl
            )
        }

        return Pair(localId, syncResult)
    }

    suspend fun toggleFavorite(id: Long, currentFav: Boolean) {
        dao.setFavorite(id, !currentFav)
    }

    suspend fun delete(entity: CalculationEntity) {
        dao.delete(entity)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}
