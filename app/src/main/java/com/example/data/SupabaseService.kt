package com.example.data

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class SupabaseSyncResult(
    val isSuccess: Boolean,
    val supabaseId: String? = null,
    val imageUrl: String? = null,
    val message: String
)

object SupabaseService {

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    fun getEffectiveUrl(customUrl: String): String {
        val trimmed = customUrl.trim()
        if (trimmed.isNotBlank() && trimmed != "https://your-project.supabase.co") {
            return trimmed.removeSuffix("/")
        }
        val buildVal = BuildConfig.SUPABASE_URL.trim()
        return if (buildVal.isNotBlank() && buildVal != "https://your-project.supabase.co") {
            buildVal.removeSuffix("/")
        } else ""
    }

    fun getEffectiveKey(customKey: String): String {
        val trimmed = customKey.trim()
        if (trimmed.isNotBlank() && trimmed != "your-supabase-anon-key") {
            return trimmed
        }
        val buildVal = BuildConfig.SUPABASE_ANON_KEY.trim()
        return if (buildVal.isNotBlank() && buildVal != "your-supabase-anon-key") {
            buildVal
        } else ""
    }

    fun isConfigured(url: String, key: String): Boolean {
        return url.isNotBlank() && key.isNotBlank() &&
                !url.contains("your-project") && !key.contains("your-supabase-anon-key")
    }

    /**
     * Uploads the snapped math photo and calculation log to Supabase.
     */
    suspend fun saveCalculationWithPhoto(
        expression: String,
        result: String,
        solution: String?,
        bitmap: Bitmap?,
        calculationType: String,
        customUrl: String,
        customKey: String
    ): SupabaseSyncResult = withContext(Dispatchers.IO) {
        val url = getEffectiveUrl(customUrl)
        val key = getEffectiveKey(customKey)

        // Compress image to Base64 thumbnail
        var base64Img: String? = null
        var imageBytes: ByteArray? = null
        if (bitmap != null) {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            imageBytes = baos.toByteArray()
            base64Img = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        }

        if (!isConfigured(url, key)) {
            return@withContext SupabaseSyncResult(
                isSuccess = false,
                message = "Saved locally to Room Database. (Configure your Supabase project in Settings to enable Cloud sync)"
            )
        }

        try {
            var uploadedPublicUrl: String? = null

            // 1. Upload photo to Supabase Storage if present
            if (imageBytes != null) {
                val filename = "math_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
                val storageEndpoint = "$url/storage/v1/object/math-snaps/$filename"

                val uploadRequest = Request.Builder()
                    .url(storageEndpoint)
                    .addHeader("apikey", key)
                    .addHeader("Authorization", "Bearer $key")
                    .addHeader("Content-Type", "image/jpeg")
                    .post(imageBytes.toRequestBody("image/jpeg".toMediaType()))
                    .build()

                val storageResponse = httpClient.newCall(uploadRequest).execute()
                if (storageResponse.isSuccessful) {
                    uploadedPublicUrl = "$url/storage/v1/object/public/math-snaps/$filename"
                }
            }

            // 2. Insert record into PostgREST table: snapped_calculations
            val isoDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date())

            val jsonRecord = JSONObject().apply {
                put("expression", expression)
                put("result", result)
                put("solution", solution ?: "")
                put("calculation_type", calculationType)
                put("image_url", uploadedPublicUrl ?: "")
                // If storage wasn't used, store data URI
                if (base64Img != null && uploadedPublicUrl == null) {
                    put("image_base64", "data:image/jpeg;base64,$base64Img")
                }
                put("created_at", isoDate)
            }

            val tableEndpoint = "$url/rest/v1/snapped_calculations"
            val insertRequest = Request.Builder()
                .url(tableEndpoint)
                .addHeader("apikey", key)
                .addHeader("Authorization", "Bearer $key")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .post(jsonRecord.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val insertResponse = httpClient.newCall(insertRequest).execute()
            val respBody = insertResponse.body?.string() ?: ""

            if (insertResponse.isSuccessful) {
                val array = JSONArray(respBody)
                val insertedObj = array.optJSONObject(0)
                val id = insertedObj?.optString("id") ?: UUID.randomUUID().toString()
                SupabaseSyncResult(
                    isSuccess = true,
                    supabaseId = id,
                    imageUrl = uploadedPublicUrl,
                    message = "Synced successfully to Supabase cloud database!"
                )
            } else {
                SupabaseSyncResult(
                    isSuccess = false,
                    message = "Supabase error (HTTP ${insertResponse.code}): $respBody"
                )
            }
        } catch (e: Exception) {
            SupabaseSyncResult(
                isSuccess = false,
                message = "Cloud sync connection failed: ${e.localizedMessage}. Saved locally."
            )
        }
    }

    /**
     * Test health check for Supabase project connectivity.
     */
    suspend fun testConnection(customUrl: String, customKey: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val url = getEffectiveUrl(customUrl)
        val key = getEffectiveKey(customKey)

        if (!isConfigured(url, key)) {
            return@withContext Pair(false, "Please provide valid Supabase Project URL and Anon Key.")
        }

        try {
            val request = Request.Builder()
                .url("$url/rest/v1/")
                .addHeader("apikey", key)
                .addHeader("Authorization", "Bearer $key")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful || response.code == 404 || response.code == 200) {
                Pair(true, "Connected successfully to Supabase API (HTTP ${response.code})")
            } else {
                Pair(false, "Connection returned HTTP ${response.code}")
            }
        } catch (e: Exception) {
            Pair(false, "Network error: ${e.localizedMessage}")
        }
    }

    /**
     * SQL script for creating the Supabase table & bucket in 1 click.
     */
    const val SQL_SETUP_SCRIPT = """
-- 1. Create snapped_calculations table
create table if not exists public.snapped_calculations (
  id uuid primary key default gen_random_uuid(),
  expression text not null,
  result text not null,
  solution text,
  calculation_type text default 'Snap & Solve',
  image_url text,
  image_base64 text,
  created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- 2. Enable Row Level Security (RLS)
alter table public.snapped_calculations enable row level security;

-- 3. Allow anonymous reads and inserts
create policy "Allow anon insert" on public.snapped_calculations for insert with check (true);
create policy "Allow anon select" on public.snapped_calculations for select using (true);

-- 4. Create public storage bucket for photos
insert into storage.buckets (id, name, public) values ('math-snaps', 'math-snaps', true)
on conflict (id) do nothing;
"""
}
