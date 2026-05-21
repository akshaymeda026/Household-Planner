package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_members")
data class FamilyMember(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val role: String, // e.g. "Parent", "Child", "Grandparent"
    val avatarColor: Int, // Hex Color Int
    val points: Int = 0
)

@Entity(tableName = "chores")
data class Chore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val assignedMemberId: Int?, // Reference to FamilyMember.id
    val assignedMemberName: String, // Convenient cached name
    val pointsValue: Int,
    val isCompleted: Boolean = false,
    val dueDate: String, // e.g. "Today", "Tomorrow", or yyyy-MM-dd
    val frequency: String // e.g. "Once", "Daily", "Weekly"
)

@Entity(tableName = "meal_plans")
data class MealPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dayOfWeek: String, // e.g., "Monday", "Tuesday", etc.
    val mealType: String, // e.g. "Breakfast", "Lunch", "Dinner"
    val recipeTitle: String,
    val calories: Int = 0,
    val carbs: Int = 0, // grams
    val protein: Int = 0, // grams
    val fat: Int = 0, // grams
    val note: String = ""
)

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val sourceUrl: String = "",
    val ingredients: String, // newline-separated
    val instructions: String, // newline-separated
    val rating: Float = 5f,
    val collections: String = "General", // e.g. "Dessert, Favorites, General"
    val calories: Int = 0,
    val carbs: Int = 0,
    val protein: Int = 0,
    val fat: Int = 0
)

@Entity(tableName = "vacations")
data class VacationPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val budget: Double,
    val notes: String = "",
    val packingList: String = "", // newline-separated "ItemName|isPacked"
    val itinerary: String = "" // newline-separated "Day/Time - Detail"
)

@Entity(tableName = "transactions")
data class BudgetTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val amount: Double,
    val isExpense: Boolean = true,
    val category: String, // "Groceries", "Utilities", "Travel", "Rewards", "Entertainment", "Other"
    val date: String, // e.g. "2026-05-21"
    val paidByMemberName: String
)

@Entity(tableName = "notifications")
data class FamNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val category: String = "General" // "Chore", "Meal", "Budget", "Vacation"
)

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isCompleted: Boolean = false
)
