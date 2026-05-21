package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyDao {

    // --- Family Members ---
    @Query("SELECT * FROM family_members ORDER BY points DESC")
    fun getAllMembers(): Flow<List<FamilyMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: FamilyMember): Long

    @Update
    suspend fun updateMember(member: FamilyMember)

    @Delete
    suspend fun deleteMember(member: FamilyMember)

    @Query("SELECT * FROM family_members WHERE id = :id LIMIT 1")
    suspend fun getMemberById(id: Int): FamilyMember?


    // --- Chores ---
    @Query("SELECT * FROM chores ORDER BY isCompleted ASC, dueDate ASC")
    fun getAllChores(): Flow<List<Chore>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChore(chore: Chore): Long

    @Update
    suspend fun updateChore(chore: Chore)

    @Query("DELETE FROM chores WHERE id = :id")
    suspend fun deleteChoreById(id: Int)


    // --- Meal Plans ---
    @Query("SELECT * FROM meal_plans")
    fun getAllMealPlans(): Flow<List<MealPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlan(mealPlan: MealPlan): Long

    @Query("DELETE FROM meal_plans WHERE id = :id")
    suspend fun deleteMealPlanById(id: Int)

    @Query("DELETE FROM meal_plans")
    suspend fun clearAllMealPlans()


    // --- Recipes ---
    @Query("SELECT * FROM recipes ORDER BY title ASC")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe): Long

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipeById(id: Int)


    // --- Vacations ---
    @Query("SELECT * FROM vacations ORDER BY startDate ASC")
    fun getAllVacations(): Flow<List<VacationPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVacation(vacation: VacationPlan): Long

    @Update
    suspend fun updateVacation(vacation: VacationPlan)

    @Query("DELETE FROM vacations WHERE id = :id")
    suspend fun deleteVacationById(id: Int)


    // --- Budget Transactions ---
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun getAllTransactions(): Flow<List<BudgetTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: BudgetTransaction): Long

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Int)


    // --- Notifications ---
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<FamNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: FamNotification): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Int)

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()

    // --- Shopping List Items ---
    @Query("SELECT * FROM shopping_items ORDER BY isCompleted ASC, id DESC")
    fun getAllShoppingItems(): Flow<List<ShoppingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItem(item: ShoppingItem): Long

    @Update
    suspend fun updateShoppingItem(item: ShoppingItem)

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun deleteShoppingItemById(id: Int)

    @Query("DELETE FROM shopping_items")
    suspend fun clearAllShoppingItems()
}
