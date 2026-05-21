package com.example.ui.localization

enum class AppLanguage {
    EN, ES, HI
}

object Translate {
    private val translations = mapOf(
        "family_dashboard" to mapOf(
            AppLanguage.EN to "Family Dashboard",
            AppLanguage.ES to "Panel Familiar",
            AppLanguage.HI to "परिवार डैशबोर्ड"
        ),
        "chores" to mapOf(
            AppLanguage.EN to "Chores",
            AppLanguage.ES to "Tareas",
            AppLanguage.HI to "घर के काम"
        ),
        "meals" to mapOf(
            AppLanguage.EN to "Food & Meals",
            AppLanguage.ES to "Comidas",
            AppLanguage.HI to "भोजन और योजना"
        ),
        "vacation" to mapOf(
            AppLanguage.EN to "Vacations",
            AppLanguage.ES to "Vacaciones",
            AppLanguage.HI to "यात्रा योजना"
        ),
        "budget" to mapOf(
            AppLanguage.EN to "Finances",
            AppLanguage.ES to "Finanzas",
            AppLanguage.HI to "पारिवारिक बजट"
        ),
        "leaderboard" to mapOf(
            AppLanguage.EN to "Family Points",
            AppLanguage.ES to "Tabla de Puntos",
            AppLanguage.HI to "परिवार के अंक"
        ),
        "shopping_list" to mapOf(
            AppLanguage.EN to "Shopping List",
            AppLanguage.ES to "Lista de Compras",
            AppLanguage.HI to "खरीदारी सूची"
        ),
        "add_item" to mapOf(
            AppLanguage.EN to "Add Item",
            AppLanguage.ES to "Añadir Artículo",
            AppLanguage.HI to "आइटम जोड़ें"
        ),
        "save" to mapOf(
            AppLanguage.EN to "Save",
            AppLanguage.ES to "Guardar",
            AppLanguage.HI to "सहेजें"
        ),
        "cancel" to mapOf(
            AppLanguage.EN to "Cancel",
            AppLanguage.ES to "Cancelar",
            AppLanguage.HI to "रद्द करें"
        ),
        "add_chore" to mapOf(
            AppLanguage.EN to "Add Chore",
            AppLanguage.ES to "Añadir Tarea",
            AppLanguage.HI to "नया काम जोड़ें"
        ),
        "assigned_to" to mapOf(
            AppLanguage.EN to "Assigned To",
            AppLanguage.ES to "Asignado A",
            AppLanguage.HI to "सौंपा गया"
        ),
        "points" to mapOf(
            AppLanguage.EN to "Points",
            AppLanguage.ES to "Puntos",
            AppLanguage.HI to "अंक"
        ),
        "due_date" to mapOf(
            AppLanguage.EN to "Due Date",
            AppLanguage.ES to "Fecha Límite",
            AppLanguage.HI to "नियत तारीख"
        ),
        "frequency" to mapOf(
            AppLanguage.EN to "Frequency",
            AppLanguage.ES to "Frecuencia",
            AppLanguage.HI to "आवृत्ति"
        ),
        "completed" to mapOf(
            AppLanguage.EN to "Completed",
            AppLanguage.ES to "Completados",
            AppLanguage.HI to "पूरा हुआ"
        ),
        "pending" to mapOf(
            AppLanguage.EN to "Pending",
            AppLanguage.ES to "Pendientes",
            AppLanguage.HI to "लंबित"
        ),
        "add_member" to mapOf(
            AppLanguage.EN to "Add Member",
            AppLanguage.ES to "Añadir Miembro",
            AppLanguage.HI to "सदस्य जोड़ें"
        ),
        "role" to mapOf(
            AppLanguage.EN to "Role",
            AppLanguage.ES to "Rol",
            AppLanguage.HI to "भूमिका"
        ),
        "budget_expense" to mapOf(
            AppLanguage.EN to "Expense",
            AppLanguage.ES to "Gasto",
            AppLanguage.HI to "खर्च"
        ),
        "budget_income" to mapOf(
            AppLanguage.EN to "Income",
            AppLanguage.ES to "Ingreso",
            AppLanguage.HI to "आय"
        ),
        "category" to mapOf(
            AppLanguage.EN to "Category",
            AppLanguage.ES to "Categoría",
            AppLanguage.HI to "श्रेणी"
        ),
        "amount" to mapOf(
            AppLanguage.EN to "Amount",
            AppLanguage.ES to "Cantidad",
            AppLanguage.HI to "राशि"
        ),
        "destination" to mapOf(
            AppLanguage.EN to "Destination",
            AppLanguage.ES to "Destino",
            AppLanguage.HI to "गंतव्य"
        ),
        "add_vacation" to mapOf(
            AppLanguage.EN to "Add Trip",
            AppLanguage.ES to "Añadir Viaje",
            AppLanguage.HI to "यात्रा जोड़ें"
        ),
        "add_meal" to mapOf(
            AppLanguage.EN to "Add Meal Plan",
            AppLanguage.ES to "Planificar Comida",
            AppLanguage.HI to "भोजन शेड्यूल करें"
        ),
        "add_recipe" to mapOf(
            AppLanguage.EN to "Import Recipe",
            AppLanguage.ES to "Importar Receta",
            AppLanguage.HI to "रेसिपी मंगाएं"
        ),
        "instagram_extract" to mapOf(
            AppLanguage.EN to "Extract Recipe from Instagram Reel/Post Link",
            AppLanguage.ES to "Extraer Receta de Enlace de Instagram",
            AppLanguage.HI to "इंस्टाग्राम वीडियो से नुस्खा निकालें"
        ),
        "notifications_title" to mapOf(
            AppLanguage.EN to "Activity & Notifications",
            AppLanguage.ES to "Actividad y Avisos",
            AppLanguage.HI to "गतिविधि और सूचनाएं"
        ),
        "custom_reminders" to mapOf(
            AppLanguage.EN to "Custom Reminders",
            AppLanguage.ES to "Recordatorios Propios",
            AppLanguage.HI to "कस्टम अनुस्मारक"
        ),
        "barcode_sub" to mapOf(
            AppLanguage.EN to "Barcode Scanner (Stock Room)",
            AppLanguage.ES to "Escáner de Compras",
            AppLanguage.HI to "बारकोड स्कैनर"
        )
    )

    fun get(key: String, language: AppLanguage): String {
        return translations[key]?.get(language) ?: key
    }
}
