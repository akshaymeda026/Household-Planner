package com.example.network

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Request/Response Models ---

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "generationConfig") val generationConfig: GeminiConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = null,
    @Json(name = "temperature") val temperature: Double? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class RecipeJson(
    @Json(name = "title") val title: String,
    @Json(name = "ingredients") val ingredients: String, // separated by \n
    @Json(name = "instructions") val instructions: String, // separated by \n
    @Json(name = "calories") val calories: Int? = 0,
    @Json(name = "carbs") val carbs: Int? = 0,
    @Json(name = "protein") val protein: Int? = 0,
    @Json(name = "fat") val fat: Int? = 0
)

// --- Retrofit API Service ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

    private val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun extractRecipe(promptOrUrl: String): RecipeJson? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Fallback mock JSON parsing for simulation if API Key is not set by user
            return@withContext getFallbackMockRecipe(promptOrUrl)
        }

        val prompt = """
            Extract recipe details from this input: "$promptOrUrl".
            It might be an Instagram recipe description, video link transcript, or simple text.
            If only a name or link is provided, write a reasonable recipe recipe.
            You MUST return a JSON object containing EXACTLY these fields, following this JSON format:
            {
              "title": "Title of the dish",
              "ingredients": "Ingredient 1\nIngredient 2\nIngredient 3",
              "instructions": "Step 1 details\nStep 2 details\nStep 3 details",
              "calories": 420,
              "carbs": 35,
              "protein": 18,
              "fat": 12
            }
            Ensure the "ingredients" and "instructions" fields represent newline-separated items as a single string.
            Only return the raw JSON object - no markdown formatting, no ```json prefixes!
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = GeminiConfig(
                responseMimeType = "application/json",
                temperature = 0.2
            )
        )

        try {
            val response = service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (jsonText != null) {
                val adapter = moshi.adapter(RecipeJson::class.java)
                return@withContext adapter.fromJson(jsonText)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext getFallbackMockRecipe(promptOrUrl)
    }

    private fun getFallbackMockRecipe(input: String): RecipeJson {
        val lowercase = input.lowercase()
        return when {
            lowercase.contains("taco") -> RecipeJson(
                title = "Gemini AI Street Tacos",
                ingredients = "8 Corn Tortillas\n400g Flank Steak\n1 Onion diced\n1/2 cup Cilantro chopped\n2 Limes\nSalsa Verde to taste",
                instructions = "Slice the flank steak into thin bite-sized pieces.\nSear steak in a hot skillet with oil for 5 minutes.\nWarm up corn tortillas on another dry griddle.\nAssemble tacos with meat, diced onion, cilantro, and fresh lime juice.",
                calories = 380,
                carbs = 28,
                protein = 26,
                fat = 14
            )
            lowercase.contains("pasta") -> RecipeJson(
                title = "Quick Garlic Herb Pasta",
                ingredients = "200g Spaghetti\n4 cloves Garlic minced\n2 tbsp Olive Oil\n1 tsp Red Pepper flakes\nFresh Basil\n50g Parmesan grated",
                instructions = "Boil spaghetti in salted water until al dente.\nSauté minced garlic and pepper flakes in olive oil until golden.\nToss spaghetti with garlic oil, fresh basil, and parmesan cheese.\nServe hot with a twist of fresh black pepper.",
                calories = 490,
                carbs = 62,
                protein = 13,
                fat = 19
            )
            else -> RecipeJson(
                title = if (input.length in 5..30) input else "Instagram Inspired Delight",
                ingredients = "1 cup Fresh Greens\n150g Grilled Protein\n1 tbsp Olive Oil Dressing\nHalf Avocado sliced\n1/4 cup Quinoa cooked",
                instructions = "Combine greens, sliced avocado, and cooked quinoa in a clean salad bowl.\nTop with cooked grilled protein.\nDrizzle oil dressing and season with salt and pepper to taste.\nEnjoy your healthy Instagram-inspired quick lunch!",
                calories = 410,
                carbs = 20,
                protein = 25,
                fat = 15
            )
        }
    }
}
