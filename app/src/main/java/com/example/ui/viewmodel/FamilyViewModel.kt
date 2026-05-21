package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.network.RecipeJson
import com.example.ui.localization.AppLanguage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FamilyViewModel(
    application: Application,
    private val repository: FamilyRepository
) : AndroidViewModel(application) {

    // Global Settings
    val currentLanguage = MutableStateFlow(AppLanguage.EN)
    val isDarkMode = MutableStateFlow(true) // Premium dark vibe default

    // Data Flows
    val members: StateFlow<List<FamilyMember>> = repository.members.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val chores: StateFlow<List<Chore>> = repository.chores.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val mealPlans: StateFlow<List<MealPlan>> = repository.mealPlans.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recipes: StateFlow<List<Recipe>> = repository.recipes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val vacations: StateFlow<List<VacationPlan>> = repository.vacations.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val transactions: StateFlow<List<BudgetTransaction>> = repository.transactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val notifications: StateFlow<List<FamNotification>> = repository.notifications.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val shoppingItems: StateFlow<List<ShoppingItem>> = repository.shoppingItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current Operator (Simulation of collaborative user session)
    val activeMember = MutableStateFlow<FamilyMember?>(null)

    // Scanning & Recipie progress
    val isImportingRecipe = MutableStateFlow(false)
    val recipeImportError = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            // Seed defaults if empty
            members.collectLatest { list ->
                if (list.isEmpty()) {
                    seedDatabase()
                } else if (activeMember.value == null) {
                    // Set default operator to the first member (usually Dad Steve or Mom Lisa)
                    activeMember.value = list.firstOrNull()
                }
            }
        }
    }

    private suspend fun seedDatabase() {
        // 1. Create family members
        val dadId = repository.insertMember(FamilyMember(name = "Steve (Dad)", role = "Parent", avatarColor = 0xFF3F51B5.toInt(), points = 180))
        val momId = repository.insertMember(FamilyMember(name = "Lisa (Mom)", role = "Parent", avatarColor = 0xFFE91E63.toInt(), points = 240))
        val child1Id = repository.insertMember(FamilyMember(name = "Leo (Kid)", role = "Child", avatarColor = 0xFF4CAF50.toInt(), points = 120))
        val child2Id = repository.insertMember(FamilyMember(name = "Chloe (Kid)", role = "Child", avatarColor = 0xFFFF9800.toInt(), points = 95))

        // 2. Add some chores
        repository.insertChore(Chore(
            title = "Mow the front lawn",
            description = "Cut grass to 2 inches, clear walkways",
            assignedMemberId = dadId.toInt(),
            assignedMemberName = "Steve (Dad)",
            pointsValue = 50,
            dueDate = "Tomorrow",
            frequency = "Weekly"
        ))
        repository.insertChore(Chore(
            title = "Do the dishes",
            description = "Load dishwasher after dinner, handwash pots",
            assignedMemberId = child1Id.toInt(),
            assignedMemberName = "Leo (Kid)",
            pointsValue = 20,
            dueDate = "Today",
            frequency = "Daily"
        ))
        repository.insertChore(Chore(
            title = "Clean the hamster cage",
            description = "Change wood chips, refill water and food dispenser",
            assignedMemberId = child2Id.toInt(),
            assignedMemberName = "Chloe (Kid)",
            pointsValue = 15,
            dueDate = "In 2 days",
            frequency = "Weekly"
        ))

        // 3. Add default tasty recipes
        repository.insertRecipe(Recipe(
            title = "Spaghetti Carbonara",
            sourceUrl = "https://www.instagram.com/reel/carbonara",
            ingredients = "200g Spaghetti\n100g Guanciale or Pancetta, cubed\n4 Large Egg yolks\n50g Pecorino Romano cheese\nBlack Pepper to taste\nSalt",
            instructions = "Boil spaghetti in salted water.\nFry guanciale in a skillet until crisp.\nWhisk egg yolks and cheese in a bowl with hot pasta water.\nToss spaghetti with crispy meat, remove from heat, and quickly mix in yolk mixture until creamy.",
            rating = 4.8f,
            collections = "Favorites, Pasta",
            calories = 540,
            carbs = 65,
            protein = 22,
            fat = 24
        ))
        repository.insertRecipe(Recipe(
            title = "Healthy Salmon Rice Bowl",
            sourceUrl = "https://www.instagram.com/reel/salmonbowl",
            ingredients = "1 Salmon fillet cooked\n1 cup Warm Sushi Rice\n1 tbsp Soy Sauce\n1 tbsp Kewpie Mayo\nHalf Sliced Avocado\nSesame seeds",
            instructions = "Flake the cooked salmon fillet into a bowl.\nAdd warm sushi rice, soy sauce, and kewpie mayonnaise.\nTop with sliced avocado, sesame seeds, and drizzle sriracha.\nToss together and eat with seaweed sheets.",
            rating = 5.0f,
            collections = "Healthy, Quick Lunch",
            calories = 460,
            carbs = 30,
            protein = 28,
            fat = 20
        ))

        // 4. Set weekly meal plans
        repository.insertMealPlan(MealPlan(
            dayOfWeek = "Monday",
            mealType = "Dinner",
            recipeTitle = "Spaghetti Carbonara",
            calories = 540,
            carbs = 65,
            protein = 22,
            fat = 24,
            note = "Leo can help whisk the cheese"
        ))
        repository.insertMealPlan(MealPlan(
            dayOfWeek = "Tuesday",
            mealType = "Lunch",
            recipeTitle = "Healthy Salmon Rice Bowl",
            calories = 460,
            carbs = 30,
            protein = 28,
            fat = 20,
            note = "Lisa prep"
        ))

        // 5. Setup vacation
        repository.insertVacation(VacationPlan(
            destination = "Paris Summer Getaway",
            startDate = "2026-07-15",
            endDate = "2026-07-22",
            budget = 4500.0,
            notes = "Stay at Marais District Airbnb. Bring comfortable walking shoes!",
            packingList = "Passports|true\nAdaptor Plugs|false\nRain jackets|false\nCamera gear|true\nFrench vocabulary book|false",
            itinerary = "Day 1: Arrive, check-in, stroll around Seine\nDay 2: Louvre Museum and Eiffel Tower Evening climb\nDay 3: Day trip to Versailles Palace"
        ))

        // 6. Enter initial transactions
        repository.insertTransaction(BudgetTransaction(
            description = "Monthly Airbnb Airbnb deposit",
            amount = 1200.0,
            isExpense = true,
            category = "Travel",
            date = "2026-05-18",
            paidByMemberName = "Lisa (Mom)"
        ))
        repository.insertTransaction(BudgetTransaction(
            description = "Weekly Groceries (Costco)",
            amount = 320.0,
            isExpense = true,
            category = "Groceries",
            date = "2026-05-20",
            paidByMemberName = "Steve (Dad)"
        ))
        repository.insertTransaction(BudgetTransaction(
            description = "Co-Working Salary",
            amount = 3500.0,
            isExpense = false,
            category = "Other",
            date = "2026-05-01",
            paidByMemberName = "Steve (Dad)"
        ))

        // 7. Add launch notice
        repository.insertNotification(FamNotification(
            title = "Welcome to FamilyNest!",
            message = "Collaborate with family members on chores, meals, budgeting, and vacations. Paste Instagram links to extract delicious recipes easily too!",
            category = "General"
        ))

        // 8. Seed shopping items
        repository.insertShoppingItem(ShoppingItem(name = "Whole Wheat Pasta", isCompleted = false))
        repository.insertShoppingItem(ShoppingItem(name = "Avocado slices package", isCompleted = false))
        repository.insertShoppingItem(ShoppingItem(name = "Fresh Basil leaves bottle", isCompleted = true))
        repository.insertShoppingItem(ShoppingItem(name = "Eggs", isCompleted = false))
        repository.insertShoppingItem(ShoppingItem(name = "Organic Whole Milk", isCompleted = false))
    }

    // --- Actions ---

    fun changeLanguage(lang: AppLanguage) {
        currentLanguage.value = lang
    }

    fun toggleDarkMode() {
        isDarkMode.value = !isDarkMode.value
    }

    fun switchActiveMember(member: FamilyMember) {
        activeMember.value = member
        viewModelScope.launch {
            repository.insertNotification(FamNotification(
                title = "User Changed",
                message = "${member.name} is now operating the dashboard.",
                category = "General"
            ))
        }
    }

    // Member management
    fun addFamilyMember(name: String, role: String, color: Int) {
        viewModelScope.launch {
            repository.insertMember(FamilyMember(name = name, role = role, avatarColor = color, points = 0))
            repository.insertNotification(FamNotification(
                title = "New Member Added",
                message = "$name ($role) joined the Fam!",
                category = "General"
            ))
        }
    }

    // Chore completion & Gamification
    fun toggleChoreCompletion(chore: Chore) {
        val updatedChore = chore.copy(isCompleted = !chore.isCompleted)
        viewModelScope.launch {
            repository.updateChore(updatedChore)

            // Reward points to the assigned member if completion is checked
            if (updatedChore.isCompleted) {
                chore.assignedMemberId?.let { memberId ->
                    val memberObj = repository.getMemberById(memberId)
                    if (memberObj != null) {
                        val awardedPoints = updatedChore.pointsValue
                        val updatedMember = memberObj.copy(points = memberObj.points + awardedPoints)
                        repository.updateMember(updatedMember)

                        // Trigger dynamic push reminder/notification log
                        repository.insertNotification(FamNotification(
                            title = "🏆 Points Earned!",
                            message = "${memberObj.name} completed '${updatedChore.title}' and gained $awardedPoints points!",
                            category = "Chore"
                        ))

                        // Sync with active member if it's the one modified in view
                        if (activeMember.value?.id == memberObj.id) {
                            activeMember.value = updatedMember
                        }
                    }
                }
            } else {
                // Deduct points because completion was unchecked
                chore.assignedMemberId?.let { memberId ->
                    val memberObj = repository.getMemberById(memberId)
                    if (memberObj != null) {
                        val updatedMember = memberObj.copy(points = maxOf(0, memberObj.points - chore.pointsValue))
                        repository.updateMember(updatedMember)

                        if (activeMember.value?.id == memberObj.id) {
                            activeMember.value = updatedMember
                        }
                    }
                }
            }
        }
    }

    fun addChore(title: String, desc: String, points: Int, dueDate: String, freq: String, assignedId: Int, assignedName: String) {
        viewModelScope.launch {
            repository.insertChore(Chore(
                title = title,
                description = desc,
                assignedMemberId = assignedId,
                assignedMemberName = assignedName,
                pointsValue = points,
                dueDate = dueDate,
                frequency = freq
            ))
            repository.insertNotification(FamNotification(
                title = "New Task Assigned",
                message = "Task '$title' to $assignedName ($points pts)",
                category = "Chore"
            ))
        }
    }

    fun deleteChore(id: Int) {
        viewModelScope.launch {
            repository.deleteChore(id)
        }
    }

    // Meal & Recipe management
    fun addMealPlan(day: String, type: String, title: String, cal: Int, carbs: Int, pro: Int, fat: Int, note: String) {
        viewModelScope.launch {
            repository.insertMealPlan(MealPlan(
                dayOfWeek = day,
                mealType = type,
                recipeTitle = title,
                calories = cal,
                carbs = carbs,
                protein = pro,
                fat = fat,
                note = note
            ))
            repository.insertNotification(FamNotification(
                title = "Meal Plan Updated",
                message = "$type ($day) set to $title",
                category = "Meal"
            ))
        }
    }

    fun deleteMealPlan(id: Int) {
        viewModelScope.launch {
            repository.deleteMealPlan(id)
        }
    }

    fun handleAIInstagramExtraction(link: String, collectionName: String) {
        viewModelScope.launch {
            isImportingRecipe.value = true
            recipeImportError.value = null
            try {
                val extracted = repository.addRecipeFromGemini(link, collectionName)
                if (extracted != null) {
                    repository.insertNotification(FamNotification(
                        title = "🤖 AI Recipe Extracted",
                        message = "Extracted '${extracted.title}' from Instagram link successfully, saving to $collectionName collection.",
                        category = "Meal"
                    ))
                } else {
                    recipeImportError.value = "Unable to process link. Please verify format."
                }
            } catch (e: Exception) {
                recipeImportError.value = "Error parsing link: ${e.localizedMessage}"
            } finally {
                isImportingRecipe.value = false
            }
        }
    }

    fun manuallyAddRecipe(title: String, ingredients: String, instructions: String, cal: Int, carbs: Int, pro: Int, fat: Int, category: String) {
        viewModelScope.launch {
            repository.insertRecipe(Recipe(
                title = title,
                ingredients = ingredients,
                instructions = instructions,
                rating = 5.0f,
                collections = category,
                calories = cal,
                carbs = carbs,
                protein = pro,
                fat = fat
            ))
            repository.insertNotification(FamNotification(
                title = "Recipe Saved",
                message = "Added '$title' to $category",
                category = "Meal"
            ))
        }
    }

    fun updateRecipeRating(recipe: Recipe, newRating: Float) {
        viewModelScope.launch {
            repository.updateRecipe(recipe.copy(rating = newRating))
        }
    }

    fun deleteRecipe(id: Int) {
        viewModelScope.launch {
            repository.deleteRecipe(id)
        }
    }

    // Vacation management
    fun addVacationTrip(destination: String, start: String, end: String, budget: Double, note: String) {
        viewModelScope.launch {
            repository.insertVacation(VacationPlan(
                destination = destination,
                startDate = start,
                endDate = end,
                budget = budget,
                notes = note,
                packingList = "Passports|false\nFirst aid kit|false\nChargers|false",
                itinerary = "Day 1: Arrival & check-in"
            ))
            repository.insertNotification(FamNotification(
                title = "New Vacation Blueprint",
                message = "Trip to '$destination' planned! Budget initialized at $$budget.",
                category = "Vacation"
            ))
        }
    }

    fun updateTripPackingItem(vacation: VacationPlan, itemText: String, isChecked: Boolean) {
        val lines = vacation.packingList.split("\n").filter { it.isNotBlank() }
        val updatedLines = lines.map { line ->
            val split = line.split("|")
            val name = split.getOrNull(0) ?: ""
            if (name.lowercase() == itemText.lowercase()) {
                "$name|$isChecked"
            } else {
                line
            }
        }
        val newList = updatedLines.joinToString("\n")
        viewModelScope.launch {
            repository.updateVacation(vacation.copy(packingList = newList))
        }
    }

    fun addTripPackingItem(vacation: VacationPlan, itemText: String) {
        if (itemText.isBlank()) return
        val currentList = vacation.packingList
        val entry = "$itemText|false"
        val newList = if (currentList.isBlank()) entry else "$currentList\n$entry"
        viewModelScope.launch {
            repository.updateVacation(vacation.copy(packingList = newList))
        }
    }

    fun updateTripItinerary(vacation: VacationPlan, text: String) {
        viewModelScope.launch {
            repository.updateVacation(vacation.copy(itinerary = text))
        }
    }

    fun deleteVacation(id: Int) {
        viewModelScope.launch {
            repository.deleteVacation(id)
        }
    }

    // Budget transactions
    fun addTransaction(desc: String, valAmount: Double, isEx: Boolean, cat: String, paidByName: String) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repository.insertTransaction(BudgetTransaction(
                description = desc,
                amount = valAmount,
                isExpense = isEx,
                category = cat,
                date = dateStr,
                paidByMemberName = paidByName
            ))
            val noticeType = if (isEx) "Expense logged" else "Income logged"
            repository.insertNotification(FamNotification(
                title = noticeType,
                message = "$noticeType by $paidByName - $$valAmount ($cat)",
                category = "Budget"
            ))
        }
    }

    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    // Notification centers
    fun triggerCustomReminder(title: String, message: String, delayMinutes: Int) {
        viewModelScope.launch {
            // Emulate scheduling. In typical app this triggers WorkManager. Here we execute instantly to give delightful visual feedback.
            repository.insertNotification(FamNotification(
                title = "⏰ " + title,
                message = "($delayMinutes min reminder triggered) $message",
                category = "General"
            ))
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearNotifications()
        }
    }

    // --- Shopping List Actions ---
    fun addShoppingItem(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertShoppingItem(ShoppingItem(name = name, isCompleted = false))
        }
    }

    fun toggleShoppingItemCompletion(item: ShoppingItem) {
        viewModelScope.launch {
            repository.updateShoppingItem(item.copy(isCompleted = !item.isCompleted))
        }
    }

    fun deleteShoppingItem(id: Int) {
        viewModelScope.launch {
            repository.deleteShoppingItem(id)
        }
    }

    fun addMultipleShoppingItems(names: List<String>) {
        viewModelScope.launch {
            names.forEach { name ->
                if (name.isNotBlank()) {
                    repository.insertShoppingItem(ShoppingItem(name = name, isCompleted = false))
                }
            }
            if (names.isNotEmpty()) {
                repository.insertNotification(FamNotification(
                    title = "🛒 Shopping List Updated",
                    message = "Added ${names.size} ingredient(s) from recipe to the shopping list.",
                    category = "Meal"
                ))
            }
        }
    }

    fun clearAllShopping() {
        viewModelScope.launch {
            repository.clearAllShoppingItems()
        }
    }

    suspend fun parseRecipeLinkDirectly(url: String): RecipeJson? {
        return repository.extractRecipeDirectly(url)
    }
}

class FamilyViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FamilyViewModel::class.java)) {
            val db = FamilyDatabase.getDatabase(application)
            val repo = FamilyRepository(db.familyDao())
            @Suppress("UNCHECKED_CAST")
            return FamilyViewModel(application, repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
