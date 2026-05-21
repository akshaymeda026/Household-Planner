package com.example.data

import com.example.network.GeminiClient
import com.example.network.RecipeJson
import kotlinx.coroutines.flow.Flow
import java.util.Locale

class FamilyRepository(private val familyDao: FamilyDao) {

    // Member Flow
    val members: Flow<List<FamilyMember>> = familyDao.getAllMembers()

    suspend fun insertMember(member: FamilyMember): Long {
        return familyDao.insertMember(member)
    }

    suspend fun updateMember(member: FamilyMember) {
        familyDao.updateMember(member)
    }

    suspend fun deleteMember(member: FamilyMember) {
        familyDao.deleteMember(member)
    }

    suspend fun getMemberById(id: Int): FamilyMember? {
        return familyDao.getMemberById(id)
    }


    // Chore Flow
    val chores: Flow<List<Chore>> = familyDao.getAllChores()

    suspend fun insertChore(chore: Chore): Long {
        return familyDao.insertChore(chore)
    }

    suspend fun updateChore(chore: Chore) {
        familyDao.updateChore(chore)
    }

    suspend fun deleteChore(id: Int) {
        familyDao.deleteChoreById(id)
    }


    // Meal Plan Flow
    val mealPlans: Flow<List<MealPlan>> = familyDao.getAllMealPlans()

    suspend fun insertMealPlan(mealPlan: MealPlan): Long {
        return familyDao.insertMealPlan(mealPlan)
    }

    suspend fun deleteMealPlan(id: Int) {
        familyDao.deleteMealPlanById(id)
    }

    suspend fun clearMealPlans() {
        familyDao.clearAllMealPlans()
    }


    // Recipe Flow
    val recipes: Flow<List<Recipe>> = familyDao.getAllRecipes()

    suspend fun insertRecipe(recipe: Recipe): Long {
        return familyDao.insertRecipe(recipe)
    }

    suspend fun updateRecipe(recipe: Recipe) {
        familyDao.updateRecipe(recipe)
    }

    suspend fun deleteRecipe(id: Int) {
        familyDao.deleteRecipeById(id)
    }


    // Vacation Plan Flow
    val vacations: Flow<List<VacationPlan>> = familyDao.getAllVacations()

    suspend fun insertVacation(vacation: VacationPlan): Long {
        return familyDao.insertVacation(vacation)
    }

    suspend fun updateVacation(vacation: VacationPlan) {
        familyDao.updateVacation(vacation)
    }

    suspend fun deleteVacation(id: Int) {
        familyDao.deleteVacationById(id)
    }


    // Transactions Flow
    val transactions: Flow<List<BudgetTransaction>> = familyDao.getAllTransactions()

    suspend fun insertTransaction(transaction: BudgetTransaction): Long {
        return familyDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(id: Int) {
        familyDao.deleteTransactionById(id)
    }


    // Notifications Flow
    val notifications: Flow<List<FamNotification>> = familyDao.getAllNotifications()

    suspend fun insertNotification(notification: FamNotification): Long {
        return familyDao.insertNotification(notification)
    }

    suspend fun markNotificationRead(id: Int) {
        familyDao.markNotificationAsRead(id)
    }

    suspend fun clearNotifications() {
        familyDao.clearAllNotifications()
    }

    // Shopping List Flow
    val shoppingItems: Flow<List<ShoppingItem>> = familyDao.getAllShoppingItems()

    suspend fun insertShoppingItem(item: ShoppingItem): Long {
        return familyDao.insertShoppingItem(item)
    }

    suspend fun updateShoppingItem(item: ShoppingItem) {
        familyDao.updateShoppingItem(item)
    }

    suspend fun deleteShoppingItem(id: Int) {
        familyDao.deleteShoppingItemById(id)
    }

    suspend fun clearAllShoppingItems() {
        familyDao.clearAllShoppingItems()
    }

    suspend fun extractRecipeDirectly(promptOrUrl: String): RecipeJson? {
        return GeminiClient.extractRecipe(promptOrUrl)
    }

    // AI Helper for parsing recipe URLs or input texts.
    suspend fun addRecipeFromGemini(promptOrUrl: String, collectionName: String): Recipe? {
        val recipeJson = GeminiClient.extractRecipe(promptOrUrl) ?: return null
        val recipe = Recipe(
            title = recipeJson.title,
            sourceUrl = promptOrUrl,
            ingredients = recipeJson.ingredients,
            instructions = recipeJson.instructions,
            rating = 5f,
            collections = collectionName.ifEmpty { "General" },
            calories = recipeJson.calories ?: 0,
            carbs = recipeJson.carbs ?: 0,
            protein = recipeJson.protein ?: 0,
            fat = recipeJson.fat ?: 0
        )
        val id = familyDao.insertRecipe(recipe)
        return recipe.copy(id = id.toInt())
    }
}
