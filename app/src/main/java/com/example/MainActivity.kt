package com.example

import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.*
import com.example.network.RecipeJson
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.Translate
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FamilyViewModel
import com.example.ui.viewmodel.FamilyViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val app = context.applicationContext as Application
            val factory = remember { FamilyViewModelFactory(app) }
            val viewModel: FamilyViewModel = viewModel(factory = factory)

            val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
            
            // Custom Theme wrapper representing premium aesthetic overrides
            CustomFamilyTheme(darkTheme = isDark) {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

// Custom sophisticated color override for Premium Dark / Elegant Light
@Composable
fun CustomFamilyTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val darkColors = darkColorScheme(
        primary = Color(0xFF38BDF8), // Neon sky blue
        secondary = Color(0xFFC084FC), // Electric purple
        tertiary = Color(0xFF34D399), // Mint green
        background = Color(0xFF0F172A), // Dark slate
        surface = Color(0xFF1E293B), // Medium slate
        onPrimary = Color(0xFF0F172A),
        onSecondary = Color(0xFF0F172A),
        onBackground = Color(0xFFF8FAFC),
        onSurface = Color(0xFFF1F5F9)
    )

    val lightColors = lightColorScheme(
        primary = Color(0xFF0284C7), // Sky blue
        secondary = Color(0xFF7C3AED), // Royal violet
        tertiary = Color(0xFF059669), // Emerald
        background = Color(0xFFF8FAFC), // Off-white
        surface = Color(0xFFFFFFFF), // White
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = Color(0xFF0F172A),
        onSurface = Color(0xFF1E293B)
    )

    val colors = if (darkTheme) darkColors else lightColors

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScreen(viewModel: FamilyViewModel) {
    val context = LocalContext.current
    val currentTab = remember { mutableStateOf(0) } // Tabs: 0 = Dashboard, 1 = Chores, 2 = Food & Meals, 3 = Vacations, 4 = Budget
    
    val members by viewModel.members.collectAsStateWithLifecycle()
    val activeUser by viewModel.activeMember.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()

    var showActiveUserDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Logo and Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AllInclusive,
                                    contentDescription = "FamUnity Nest Logo",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Text(
                            text = "FamilyNest",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Toolbar actions: Toggle Active User / Dark Mode / Language / Notification
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Switch active user role button
                        IconButton(
                            onClick = { showActiveUserDialog = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (activeUser != null) Color(activeUser!!.avatarColor).copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = "Active Role",
                                tint = if (activeUser != null) Color(activeUser!!.avatarColor) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Theme switch
                        IconButton(onClick = { viewModel.toggleDarkMode() }) {
                            Icon(
                                imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Theme Switcher",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Language switch dropdown hint
                        IconButton(onClick = {
                            val nextLang = when (currentLang) {
                                AppLanguage.EN -> AppLanguage.ES
                                AppLanguage.ES -> AppLanguage.HI
                                AppLanguage.HI -> AppLanguage.EN
                            }
                            viewModel.changeLanguage(nextLang)
                            Toast.makeText(context, "Language: ${nextLang.name}", Toast.LENGTH_SHORT).show()
                        }) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = "Language toggle",
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = currentLang.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Notifications list button (showing raw notifications log count on top badge)
                        val unreadCount = notifications.count { !it.isRead }
                        Box {
                            IconButton(onClick = { showNotificationDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "System Log Alert Tracker",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            if (unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .align(Alignment.TopEnd)
                                        .background(Color.Red, shape = CircleShape)
                                        .padding(bottom = 1.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = unreadCount.toString(),
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Active operator profile header
                if (activeUser != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color(activeUser!!.avatarColor), shape = CircleShape)
                            )
                            Text(
                                text = "Current role: ${activeUser!!.name} (${activeUser!!.role})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "⭐ ${activeUser!!.points} pts",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                listOf(
                    Triple(0, Icons.Default.Dashboard, "family_dashboard"),
                    Triple(1, Icons.Default.Checklist, "chores"),
                    Triple(2, Icons.Default.Fastfood, "meals"),
                    Triple(3, Icons.Default.FlightTakeoff, "vacation"),
                    Triple(4, Icons.Default.AccountBalanceWallet, "budget")
                ).forEach { (tabIdx, icon, translateKey) ->
                    val isSelected = currentTab.value == tabIdx
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab.value = tabIdx },
                        label = {
                            Text(
                                text = Translate.get(translateKey, currentLang),
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = Translate.get(translateKey, currentLang)
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = currentTab.value,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "ScreenSwitch"
            ) { targetTab ->
                when (targetTab) {
                    0 -> DashboardTab(viewModel = viewModel, currentLang = currentLang, onNavigateToTab = { currentTab.value = it })
                    1 -> ChoresTab(viewModel = viewModel, currentLang = currentLang)
                    2 -> FoodTab(viewModel = viewModel, currentLang = currentLang)
                    3 -> VacationTab(viewModel = viewModel, currentLang = currentLang)
                    4 -> BudgetTab(viewModel = viewModel, currentLang = currentLang)
                }
            }
        }
    }

    // --- Active User Simulation Dialog ---
    if (showActiveUserDialog) {
        Dialog(onDismissRequest = { showActiveUserDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Switch Active Family Role",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        IconButton(onClick = { showActiveUserDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Text(
                        text = "Switch active profile to simulate point scores and task assignments from different users' perspectives.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                    ) {
                        items(members) { m ->
                            val isCurrent = m.id == activeUser?.id
                            Surface(
                                onClick = {
                                    viewModel.switchActiveMember(m)
                                    showActiveUserDialog = false
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(Color(m.avatarColor), shape = CircleShape)
                                        )
                                        Column {
                                            Text(m.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(m.role, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Text(
                                        text = "${m.points} pts",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                showAddMemberDialog = true
                                showActiveUserDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = "Add Member", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(Translate.get("add_member", currentLang), fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    // --- Dynamic Notifications Log Tray ---
    if (showNotificationDialog) {
        Dialog(onDismissRequest = { showNotificationDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Translate.get("notifications_title", currentLang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        IconButton(onClick = { showNotificationDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    // Schedule virtual notification reminder
                    var reminderTitle by remember { mutableStateOf("") }
                    var reminderMsg by remember { mutableStateOf("") }
                    var showAlarmsPlanner by remember { mutableStateOf(false) }

                    if (!showAlarmsPlanner) {
                        Button(
                            onClick = { showAlarmsPlanner = true },
                            colors = ButtonDefaults.textButtonColors(),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(Icons.Default.AddAlarm, contentDescription = "Set Custom Reminder")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Fast Alarm Planner Builder", fontSize = 12.sp)
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Text("Configure Alarms Builder", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = reminderTitle,
                                onValueChange = { reminderTitle = it },
                                label = { Text("Task Label (e.g. Wash Potatoes)", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = reminderMsg,
                                onValueChange = { reminderMsg = it },
                                label = { Text("Alarm Detail", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { showAlarmsPlanner = false }) {
                                    Text("Discard", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = {
                                        if (reminderTitle.isNotBlank()) {
                                            viewModel.triggerCustomReminder(reminderTitle, reminderMsg, 10)
                                            reminderTitle = ""
                                            reminderMsg = ""
                                            showAlarmsPlanner = false
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("Save Alarm", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(min = 120.dp, max = 320.dp)
                    ) {
                        if (notifications.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No recent alert activities log", fontSize = 13.sp, color = Color.Gray)
                            }
                        } else {
                            LazyColumn(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(notifications) { n ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                MaterialTheme.colorScheme.surface,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val icon = when (n.category) {
                                            "Chore" -> Icons.Default.Checklist
                                            "Meal" -> Icons.Default.Fastfood
                                            "Budget" -> Icons.Default.Payments
                                            "Vacation" -> Icons.Default.FlightTakeoff
                                            else -> Icons.Default.Info
                                        }
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = n.category,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(n.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(n.message, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { viewModel.clearAllNotifications() }) {
                            Text("Purge Logs")
                        }
                        TextButton(onClick = { showNotificationDialog = false }) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }
    }

    // --- Add Member Dialog ---
    if (showAddMemberDialog) {
        Dialog(onDismissRequest = { showAddMemberDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Add Family Member", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                    var memberName by remember { mutableStateOf("") }
                    var memberRole by remember { mutableStateOf("Child") } // "Parent", "Child", "Grandparent"
                    val colorsPalette = listOf(
                        0xFFE91E63.toInt(), // Pink
                        0xFF3F51B5.toInt(), // Indigo
                        0xFF4CAF50.toInt(), // Green
                        0xFFFF9800.toInt(), // Orange
                        0xFF00BCD4.toInt(), // Cyan
                        0xFF9C27B0.toInt()  // Purple
                    )
                    var selectedColorIdx by remember { mutableStateOf(0) }

                    OutlinedTextField(
                        value = memberName,
                        onValueChange = { memberName = it },
                        label = { Text("Member Name (e.g., Uncle Robert)", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("Avatar Color Theme", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        colorsPalette.forEachIndexed { idx, col ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(col), shape = CircleShape)
                                    .clickable { selectedColorIdx = idx }
                                    .border(
                                        width = if (selectedColorIdx == idx) 3.dp else 0.dp,
                                        color = if (selectedColorIdx == idx) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    Text("Select Family Role", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Parent", "Child", "Guardian").forEach { role ->
                            FilterChip(
                                selected = memberRole == role,
                                onClick = { memberRole = role },
                                label = { Text(role) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddMemberDialog = false }) {
                            Text(Translate.get("cancel", currentLang))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            if (memberName.isNotBlank()) {
                                viewModel.addFamilyMember(
                                    name = memberName,
                                    role = memberRole,
                                    color = colorsPalette[selectedColorIdx]
                                )
                                showAddMemberDialog = false
                            }
                        }) {
                            Text(Translate.get("save", currentLang))
                        }
                    }
                }
            }
        }
    }
}

// ================= TAB 0: DASHBOARD & GAMIFICATION =================
@Composable
fun DashboardTab(
    viewModel: FamilyViewModel,
    currentLang: AppLanguage,
    onNavigateToTab: (Int) -> Unit
) {
    val members by viewModel.members.collectAsStateWithLifecycle()
    val chores by viewModel.chores.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val mealPlans by viewModel.mealPlans.collectAsStateWithLifecycle()

    val completedChoresCount = chores.count { it.isCompleted }
    val totalChoresCount = chores.size

    val netBalance = transactions.sumOf { if (it.isExpense) -it.amount else it.amount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Date card with gradient highlights
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Happy Home, Happy Family 🏠",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "UTC: 2026-05-21",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                        Text(
                            text = "Family Nest Control Room",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Get real-time point rewards for checking off chores. Run nutritional health audits or plan vacations dynamically.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Leaderboard / Gamification Standings
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏆 " + Translate.get("leaderboard", currentLang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Realtime", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    if (members.isEmpty()) {
                        Text("No registered member standings found.", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        val maxPoints = members.maxOfOrNull { it.points } ?: 1
                        members.forEachIndexed { index, m ->
                            val scoreRatio = m.points.toFloat() / maxPoints.toFloat()
                            val medal = when (index) {
                                0 -> "🥇"
                                1 -> "🥈"
                                2 -> "🥉"
                                else -> "⭐️"
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(text = medal, fontSize = 12.sp)
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(Color(m.avatarColor), shape = CircleShape)
                                        )
                                        Text(
                                            text = m.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Text(
                                        text = "${m.points} pts",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                // Points progress bar
                                LinearProgressIndicator(
                                    progress = { scoreRatio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(m.avatarColor),
                                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }

        // Live stats widgets (Chores progress, Budget balance overview, Meals planned)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Chores Widget
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToTab(1) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Task Ratio", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(
                            text = "$completedChoresCount / $totalChoresCount",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text("Completed chores", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }

                // Balance Widget
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToTab(4) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Net Ledger", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(
                            text = "$${netBalance.toInt()}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = if (netBalance >= 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                        )
                        Text("Remaining funds", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Quick Meal notice card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTab(2) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), shape = RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.RestaurantMenu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.primary)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Weekly Food Calendar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        val totalMeals = mealPlans.size
                        Text(
                            text = if (totalMeals > 0) "$totalMeals meals scheduled this week" else "No plans added! Tap to build weekly meal catalog",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = Color.Gray)
                }
            }
        }
    }
}


// ================= TAB 1: COLLABORATIVE CHORES & ASSIGNMENT =================
@Composable
fun ChoresTab(viewModel: FamilyViewModel, currentLang: AppLanguage) {
    val chores by viewModel.chores.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val activeUser by viewModel.activeMember.collectAsStateWithLifecycle()

    var showAddChoreDialog by remember { mutableStateOf(false) }

    // Add Chore Form States
    var newChoreTitle by remember { mutableStateOf("") }
    var newChoreDesc by remember { mutableStateOf("") }
    var newChorePoints by remember { mutableStateOf("25") }
    var newChoreDueDate by remember { mutableStateOf("Today") }
    var newChoreFreq by remember { mutableStateOf("Weekly") }
    var selectedMemberIdx by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = Translate.get("chores", currentLang),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Gamified task assignment dashboard", fontSize = 11.sp, color = Color.Gray)
                }
                Button(onClick = { showAddChoreDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Translate.get("add_chore", currentLang), fontSize = 12.sp)
                }
            }

            if (chores.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No chores assigned! Create one using the action button.", fontSize = 13.sp, color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(chores) { chore ->
                        val assignedColor = members.find { it.id == chore.assignedMemberId }?.avatarColor ?: 0xFF9E9E9E.toInt()
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Complete Checkbox with Points gain warning
                                    Checkbox(
                                        checked = chore.isCompleted,
                                        onCheckedChange = { viewModel.toggleChoreCompletion(chore) }
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = chore.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            style = if (chore.isCompleted) MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
                                            else MaterialTheme.typography.bodyLarge
                                        )
                                        if (chore.description.isNotEmpty()) {
                                            Text(
                                                text = chore.description,
                                                fontSize = 11.sp,
                                                color = Color.Gray,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Assignee badge
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(assignedColor).copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = chore.assignedMemberName,
                                                    color = Color(assignedColor),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            // Date badge
                                            Box(
                                                modifier = Modifier
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "Due: " + chore.dueDate,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }

                                // Points Bubble and Delete action
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "+${chore.pointsValue} pts",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteChore(chore.id) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Chore Dialog
        if (showAddChoreDialog) {
            Dialog(onDismissRequest = { showAddChoreDialog = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(Translate.get("add_chore", currentLang), fontWeight = FontWeight.Bold, fontSize = 18.sp)

                        OutlinedTextField(
                            value = newChoreTitle,
                            onValueChange = { newChoreTitle = it },
                            label = { Text("Task Title (e.g. Cleans the table)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = newChoreDesc,
                            onValueChange = { newChoreDesc = it },
                            label = { Text("Short Description") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = newChorePoints,
                                onValueChange = { newChorePoints = it },
                                label = { Text(Translate.get("points", currentLang)) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = newChoreDueDate,
                                onValueChange = { newChoreDueDate = it },
                                label = { Text(Translate.get("due_date", currentLang)) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        // Member drop switcher
                        Text(Translate.get("assigned_to", currentLang), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        if (members.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                members.forEachIndexed { index, m ->
                                    val isSelected = selectedMemberIdx == index
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedMemberIdx = index },
                                        label = { Text(m.name) }
                                    )
                                }
                            }
                        } else {
                            Text("No family operators registered.", color = Color.Red, fontSize = 11.sp)
                        }

                        Text(Translate.get("frequency", currentLang), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Once", "Daily", "Weekly").forEach { freq ->
                                FilterChip(
                                    selected = newChoreFreq == freq,
                                    onClick = { newChoreFreq = freq },
                                    label = { Text(freq) }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showAddChoreDialog = false }) {
                                Text(Translate.get("cancel", currentLang))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = {
                                if (newChoreTitle.isNotBlank() && members.isNotEmpty()) {
                                    val pointsVal = newChorePoints.toIntOrNull() ?: 20
                                    val targetMember = members[selectedMemberIdx]
                                    viewModel.addChore(
                                        title = newChoreTitle,
                                        desc = newChoreDesc,
                                        points = pointsVal,
                                        dueDate = newChoreDueDate,
                                        freq = newChoreFreq,
                                        assignedId = targetMember.id,
                                        assignedName = targetMember.name
                                    )
                                    // Reset and dismiss
                                    newChoreTitle = ""
                                    newChoreDesc = ""
                                    showAddChoreDialog = false
                                }
                            }) {
                                Text(Translate.get("save", currentLang))
                            }
                        }
                    }
                }
            }
        }
    }
}


// ================= TAB 2: FOOD SCHEDULE, AI RECIPE, AND BARCODE =================
@Composable
fun FoodTab(viewModel: FamilyViewModel, currentLang: AppLanguage) {
    val mealPlans by viewModel.mealPlans.collectAsStateWithLifecycle()
    val recipes by viewModel.recipes.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var activeSubTab by remember { mutableStateOf(0) } // 0 = Week Plan, 1 = Recipe Box, 2 = Shopping & Scan

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Tab switch header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp))
                .padding(4.dp)
        ) {
            listOf("Week ahead", "Recipe Box 📕", "Shopping & Scan 🛒").forEachIndexed { idx, label ->
                val isSelected = activeSubTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { activeSubTab = idx }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Content
        when (activeSubTab) {
            0 -> WeekPlanSubTab(viewModel, mealPlans)
            1 -> RecipeBoxSubTab(viewModel, recipes, currentLang)
            2 -> ShoppingScanSubTab(viewModel, recipes)
        }
    }
}

@Composable
fun WeekPlanSubTab(viewModel: FamilyViewModel, mealPlans: List<MealPlan>) {
    var showAddMealDialog by remember { mutableStateOf(false) }

    // Forms
    var selectDay by remember { mutableStateOf("Monday") }
    var selectType by remember { mutableStateOf("Dinner") }
    var recipeNameForm by remember { mutableStateOf("") }
    var calForm by remember { mutableStateOf("450") }
    var carbForm by remember { mutableStateOf("40") }
    var proForm by remember { mutableStateOf("25") }
    var fatForm by remember { mutableStateOf("15") }
    var noteForm by remember { mutableStateOf("") }

    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Weekly Food Schedule", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Button(onClick = { showAddMealDialog = true }) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Plan")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Schedule", fontSize = 11.sp)
            }
        }

        if (mealPlans.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.HourglassEmpty, contentDescription = "Empty", tint = Color.Gray, modifier = Modifier.size(32.dp))
                    Text("No meal scheduler events planned.", fontSize = 12.sp, color = Color.Gray)
                    Text("Toggle 'Schedule' on the top right to map calories and meals for family members", fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                items(mealPlans) { plan ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = plan.dayOfWeek.take(3).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Column {
                                    Text(
                                        text = "${plan.mealType}: ${plan.recipeTitle}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "🔥 ${plan.calories} kcal",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Text(
                                            text = "C: ${plan.carbs}g | P: ${plan.protein}g | F: ${plan.fat}g",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    if (plan.note.isNotEmpty()) {
                                        Text(
                                            text = "📝 " + plan.note,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            IconButton(onClick = { viewModel.deleteMealPlan(plan.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Add Meal dialogue
        if (showAddMealDialog) {
            Dialog(onDismissRequest = { showAddMealDialog = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Schedule Food Meal", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                        Text("Day of week", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            daysOfWeek.forEach { day ->
                                FilterChip(
                                    selected = selectDay == day,
                                    onClick = { selectDay = day },
                                    label = { Text(day) }
                                )
                            }
                        }

                        Text("Meal Category", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Breakfast", "Lunch", "Dinner").forEach { type ->
                                FilterChip(
                                    selected = selectType == type,
                                    onClick = { selectType = type },
                                    label = { Text(type) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = recipeNameForm,
                            onValueChange = { recipeNameForm = it },
                            label = { Text("Menu / Recipe Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = calForm,
                                onValueChange = { calForm = it },
                                label = { Text("Calories", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = carbForm,
                                onValueChange = { carbForm = it },
                                label = { Text("Carbs", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = proForm,
                                onValueChange = { proForm = it },
                                label = { Text("Protein", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = fatForm,
                                onValueChange = { fatForm = it },
                                label = { Text("Fats", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = noteForm,
                            onValueChange = { noteForm = it },
                            label = { Text("Chef Preparation Notes", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showAddMealDialog = false }) {
                                Text("Discard")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = {
                                if (recipeNameForm.isNotBlank()) {
                                    viewModel.addMealPlan(
                                        day = selectDay,
                                        type = selectType,
                                        title = recipeNameForm,
                                        cal = calForm.toIntOrNull() ?: 450,
                                        carbs = carbForm.toIntOrNull() ?: 35,
                                        pro = proForm.toIntOrNull() ?: 20,
                                        fat = fatForm.toIntOrNull() ?: 15,
                                        note = noteForm
                                    )
                                    // reset
                                    recipeNameForm = ""
                                    noteForm = ""
                                    showAddMealDialog = false
                                }
                            }) {
                                Text("Add Schedule")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeBoxSubTab(viewModel: FamilyViewModel, recipes: List<Recipe>, currentLang: AppLanguage) {
    var instagramUrl by remember { mutableStateOf("") }
    var selectedCollection by remember { mutableStateOf("Favorites") }

    val isImporting by viewModel.isImportingRecipe.collectAsStateWithLifecycle()
    val importError by viewModel.recipeImportError.collectAsStateWithLifecycle()

    var showManualAddDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "🤖 Gemini Instagram Recipe Extractor",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Submit any Instagram reel descriptor / video copy details to extract organized ingredients, ratings, instructions, and macro nutrients instantly.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = instagramUrl,
                    onValueChange = { instagramUrl = it },
                    placeholder = { Text("https://www.instagram.com/reel/C7p9A...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        if (instagramUrl.isNotEmpty()) {
                            IconButton(onClick = { instagramUrl = "" }) { Icon(Icons.Default.Clear, contentDescription = "Clear") }
                        }
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Save to:", fontSize = 11.sp, color = Color.Gray)
                        listOf("Favorites", "General").forEach { col ->
                            FilterChip(
                                selected = selectedCollection == col,
                                onClick = { selectedCollection = col },
                                label = { Text(col, fontSize = 10.sp) }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (instagramUrl.isNotBlank()) {
                                viewModel.handleAIInstagramExtraction(instagramUrl, selectedCollection)
                            }
                        },
                        enabled = !isImporting && instagramUrl.isNotBlank()
                    ) {
                        if (isImporting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Gemini AI Extract", fontSize = 11.sp)
                        }
                    }
                }

                if (importError != null) {
                    Text(text = importError!!, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Cook Books Collection", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = { showManualAddDialog = true }) {
                Icon(Icons.Default.AddCircle, contentDescription = "Add")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Manual Save", fontSize = 11.sp)
            }
        }

        // List
        if (recipes.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("Your digital recipe collection is empty.", fontSize = 12.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                items(recipes) { r ->
                    var isExpanded by remember { mutableStateOf(false) }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Custom visual visual representational cover box
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .background(
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🍲", fontSize = 24.sp)
                                    }

                                    Column {
                                        Text(r.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("★ ${"%.1f".format(r.rating)}", color = Color(0xFFFFB300), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("• ${r.calories} kcal", fontSize = 10.sp, color = Color.Gray)
                                            Text("• Folder: ${r.collections}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { isExpanded = !isExpanded }) {
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = "Expand details"
                                        )
                                    }
                                    IconButton(onClick = { viewModel.deleteRecipe(r.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            if (isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Macro Elements Breakdown:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Carbs: ${r.carbs}g", fontSize = 11.sp)
                                        Text("Protein: ${r.protein}g", fontSize = 11.sp)
                                        Text("Fat: ${r.fat}g", fontSize = 11.sp)
                                    }

                                    Divider()

                                    Text("🛒 Ingredients:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    Text(r.ingredients, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                    Button(
                                        onClick = {
                                            val elements = r.ingredients.split("\n").filter { it.isNotBlank() }
                                            viewModel.addMultipleShoppingItems(elements)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                    ) {
                                        Icon(Icons.Default.ShoppingCart, contentDescription = "Add Shopping List", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Add Ingredients to Grocery Checklist 🛒", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Divider()

                                    Text("📖 Steps:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                    Text(r.instructions, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                    if (r.sourceUrl.isNotEmpty()) {
                                        Text(
                                            text = "Source Link: ${r.sourceUrl}",
                                            fontSize = 9.sp,
                                            color = Color.LightGray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Manual Addition Dialog
        if (showManualAddDialog) {
            var mTitle by remember { mutableStateOf("") }
            var mIngredients by remember { mutableStateOf("") }
            var mInstructions by remember { mutableStateOf("") }
            var mCalories by remember { mutableStateOf("380") }
            var mCarbs by remember { mutableStateOf("30") }
            var mProtein by remember { mutableStateOf("20") }
            var mFat by remember { mutableStateOf("12") }
            var mCategory by remember { mutableStateOf("Favorites") }

            Dialog(onDismissRequest = { showManualAddDialog = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Add Manual Recipe", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                        OutlinedTextField(value = mTitle, onValueChange = { mTitle = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = mIngredients, onValueChange = { mIngredients = it }, label = { Text("Ingredients (one per line)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = mInstructions, onValueChange = { mInstructions = it }, label = { Text("Instructions (one per line)") }, modifier = Modifier.fillMaxWidth())

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = mCalories, onValueChange = { mCalories = it }, label = { Text("Calories") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                            OutlinedTextField(value = mCarbs, onValueChange = { mCarbs = it }, label = { Text("Carbs (g)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = mProtein, onValueChange = { mProtein = it }, label = { Text("Protein (g)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                            OutlinedTextField(value = mFat, onValueChange = { mFat = it }, label = { Text("Fat (g)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        }

                        OutlinedTextField(value = mCategory, onValueChange = { mCategory = it }, label = { Text("Folder Collection Name") }, modifier = Modifier.fillMaxWidth())

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showManualAddDialog = false }) { Text("Discard") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = {
                                if (mTitle.isNotBlank()) {
                                    viewModel.manuallyAddRecipe(
                                        title = mTitle,
                                        ingredients = mIngredients,
                                        instructions = mInstructions,
                                        cal = mCalories.toIntOrNull() ?: 350,
                                        carbs = mCarbs.toIntOrNull() ?: 20,
                                        pro = mProtein.toIntOrNull() ?: 20,
                                        fat = mFat.toIntOrNull() ?: 10,
                                        category = mCategory.ifEmpty { "General" }
                                    )
                                    showManualAddDialog = false
                                }
                            }) {
                                Text("Save recipe")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppingScanSubTab(viewModel: FamilyViewModel, recipes: List<Recipe>) {
    val shoppingItems by viewModel.shoppingItems.collectAsStateWithLifecycle()
    var textInput by remember { mutableStateOf("") }
    var showBarcodeScanner by remember { mutableStateOf(false) }

    // State for Instagram Scraper inside Shopping Tab
    var igUrlForShopping by remember { mutableStateOf("") }
    var isExtractingIngredients by remember { mutableStateOf(false) }
    var igExtractError by remember { mutableStateOf<String?>(null) }
    var showIGPreviewDialog by remember { mutableStateOf(false) }
    var extractedRecipeForPreview by remember { mutableStateOf<RecipeJson?>(null) }

    val coroutineScope = rememberCoroutineScope()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Automated Shopping Ledger", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            // Scanner visual trigger button
            Button(
                onClick = { showBarcodeScanner = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scanner", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Mock Scan 📸", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // --- Premium AI Instagram Link Extraper for Planning & Shopping ---
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Scrape Instagram Video for Shopping & Planning",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Parse any Instagram food reel or post video. Automatically extract ingredients, choose what you need with checklists, and immediately populate your grocery ledger and meal plan.",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = igUrlForShopping,
                        onValueChange = { igUrlForShopping = it },
                        placeholder = { Text("Paste Instagram video or post URL...", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        trailingIcon = {
                            if (igUrlForShopping.isNotEmpty()) {
                                IconButton(onClick = { igUrlForShopping = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    )
                    Button(
                        onClick = {
                            if (igUrlForShopping.isNotBlank()) {
                                isExtractingIngredients = true
                                igExtractError = null
                                coroutineScope.launch {
                                    try {
                                        val result = viewModel.parseRecipeLinkDirectly(igUrlForShopping)
                                        if (result != null) {
                                            extractedRecipeForPreview = result
                                            showIGPreviewDialog = true
                                        } else {
                                            igExtractError = "Unable to scrape the Instagram video. Please check the URL or try again."
                                        }
                                    } catch (e: Exception) {
                                        igExtractError = "Error: ${e.localizedMessage}"
                                    } finally {
                                        isExtractingIngredients = false
                                    }
                                }
                            }
                        },
                        enabled = !isExtractingIngredients && igUrlForShopping.isNotBlank(),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        if (isExtractingIngredients) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 1.5.dp)
                        } else {
                            Text("Scrape Real", fontSize = 11.sp)
                        }
                    }
                }
                if (igExtractError != null) {
                    Text(text = igExtractError!!, color = MaterialTheme.colorScheme.error, fontSize = 10.sp)
                }
            }
        }

        // Fast recipes auto list copy button!
        if (recipes.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "Auto", tint = MaterialTheme.colorScheme.primary)
                Column(modifier = Modifier.weight(1f)) {
                    Text("Auto Ingredients Injector", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Select a cookbook recipe below to automatically add all missing raw elements to the shared checklist", fontSize = 9.sp, color = Color.Gray)
                }
            }

            // Small horizontal selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                recipes.take(3).forEach { r ->
                    OutlinedButton(
                        onClick = {
                            val elements = r.ingredients.split("\n").filter { it.isNotBlank() }
                            viewModel.addMultipleShoppingItems(elements)
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(r.title, fontSize = 10.sp)
                    }
                }
            }
        }

        // Adding input box
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Add manual grocery package item", fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        viewModel.addShoppingItem(textInput)
                        textInput = ""
                    }
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp))
                    .size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }

        // Check list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(shoppingItems) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Checkbox(
                            checked = item.isCompleted,
                            onCheckedChange = { checked ->
                                viewModel.toggleShoppingItemCompletion(item)
                            }
                        )
                        Text(
                            text = item.name,
                            fontSize = 13.sp,
                            style = if (item.isCompleted) MaterialTheme.typography.bodyLarge.copy(color = Color.Gray, textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                            else MaterialTheme.typography.bodyLarge
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.deleteShoppingItem(item.id)
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.LightGray)
                    }
                }
            }
        }

        // --- Mock Barcode Scanner console dialogue ---
        if (showBarcodeScanner) {
            var barcodeForm by remember { mutableStateOf("") }
            val fakeDb = mapOf(
                "890123" to "Barilla Authentic Penne Pasta (500g)",
                "501234" to "Prego Traditional Italian Pasta Sauce",
                "010456" to "Nishiki Premium Grade Brown Rice (1.5Kg)",
                "090876" to "Chobani Plain Organic Greek Yogurt"
            )

            var scanLogText by remember { mutableStateOf("Camera initialized. Focus red line on code.") }
            var isScanningProgress by remember { mutableStateOf(false) }

            val scope = rememberCoroutineScope()

            Dialog(onDismissRequest = { showBarcodeScanner = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), // Tech slate dark look always
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("[Mock Scanner Panel]", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 15.sp)

                        // Camera preview simulation
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(Color.Black, shape = RoundedCornerShape(10.dp))
                                .border(BorderStroke(2.dp, Color.Gray), shape = RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isScanningProgress) {
                                // Green moving scan laser line representation
                                val infiniteTransition = rememberInfiniteTransition(label = "laser")
                                val positionY by infiniteTransition.animateFloat(
                                    initialValue = -60f,
                                    targetValue = 60f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1200, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "scale"
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .height(3.dp)
                                        .offset(y = positionY.dp)
                                        .background(Color(0xFF34D399))
                                )
                                Text("Analyzing Barcode matrix...", color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(10.dp))
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = "Cam", tint = Color.Gray, modifier = Modifier.size(32.dp))
                                    Text("TAP RED ALERTS OR CODES TO LOG", color = Color.DarkGray, fontSize = 10.sp)
                                }
                            }
                        }

                        Text(text = scanLogText, color = Color.White, fontSize = 12.sp)

                        Divider(color = Color.DarkGray)

                        Text("Select dynamic scan barcode mock target:", color = Color.Gray, fontSize = 11.sp)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            fakeDb.forEach { (code, name) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0F172A), shape = RoundedCornerShape(6.dp))
                                        .clickable {
                                            scope.launch {
                                                isScanningProgress = true
                                                scanLogText = "Recognizing code format matrix [$code]..."
                                                delay(1200)
                                                isScanningProgress = false
                                                scanLogText = "Success! Matched: $name"
                                                
                                                viewModel.addShoppingItem(name)
                                            }
                                        }
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(name, color = Color.White, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    Text("Code: $code", color = Color(0xFFC084FC), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { showBarcodeScanner = false }) {
                                Text("Close Scanner", color = Color(0xFF38BDF8))
                            }
                        }
                    }
                }
            }
        }

        // --- Custom Scraped Recipe Preview and Planning Matrix ---
        val recipe = extractedRecipeForPreview
        if (showIGPreviewDialog && recipe != null) {
            val parsedIngredients = remember(recipe) {
                recipe.ingredients.split("\n")
                    .filter { it.isNotBlank() }
                    .map { it.trim() }
            }
            // Which ingredients are selected to add to the shopping list
            val selectedIngredients = remember {
                mutableStateMapOf<Int, Boolean>().apply {
                    parsedIngredients.indices.forEach { put(it, true) }
                }
            }

            var collectionName by remember { mutableStateOf("Favorites") }
            var selectedMealDay by remember { mutableStateOf("Monday") }
            var selectedMealType by remember { mutableStateOf("Dinner") }
            var noteText by remember { mutableStateOf("Extracted from Instagram video") }

            var isRecipeAddedToCookbook by remember { mutableStateOf(false) }
            var isMealAddedToPlanner by remember { mutableStateOf(false) }

            Dialog(onDismissRequest = { showIGPreviewDialog = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize()
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🤖 Scraped Recipe Options",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { showIGPreviewDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Recipe Detail Body
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Title & Macros Card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(text = recipe.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("🔥 ${recipe.calories ?: 0} kcal", fontSize = 11.sp, color = Color.Gray)
                                        Text("🥩 Pro: ${recipe.protein ?: 0}g", fontSize = 11.sp, color = Color.Gray)
                                        Text("🥑 Fat: ${recipe.fat ?: 0}g", fontSize = 11.sp, color = Color.Gray)
                                        Text("🍞 Carbs: ${recipe.carbs ?: 0}g", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                            }

                            // Checklist Card for Ingredients
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "🛒 Select Ingredients to Buy",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = "Uncheck the items you already have in stock.",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                            .padding(6.dp)
                                    ) {
                                        parsedIngredients.forEachIndexed { index, ingredient ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        selectedIngredients[index] = !(selectedIngredients[index] ?: true)
                                                    }
                                                    .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = selectedIngredients[index] ?: true,
                                                    onCheckedChange = { checked ->
                                                        selectedIngredients[index] = checked ?: true
                                                    }
                                                )
                                                Text(
                                                    text = ingredient,
                                                    fontSize = 12.sp,
                                                    style = if (!(selectedIngredients[index] ?: true)) {
                                                        MaterialTheme.typography.bodyMedium.copy(
                                                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
                                                            color = Color.Gray
                                                        )
                                                    } else {
                                                        MaterialTheme.typography.bodyMedium
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Preparation instructions block (Just visual verify)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(text = "📖 Script instructions Preview", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(text = recipe.instructions, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            // Cookbook Category selector & Save Option
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(text = "📁 Save to Cookbook Folder", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        listOf("Favorites", "General", "Pasta", "Healthy").forEach { item ->
                                            FilterChip(
                                                selected = collectionName == item,
                                                onClick = { collectionName = item },
                                                label = { Text(item, fontSize = 10.sp) }
                                            )
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.manuallyAddRecipe(
                                                title = recipe.title,
                                                ingredients = recipe.ingredients,
                                                instructions = recipe.instructions,
                                                cal = recipe.calories ?: 0,
                                                carbs = recipe.carbs ?: 0,
                                                pro = recipe.protein ?: 0,
                                                fat = recipe.fat ?: 0,
                                                category = collectionName
                                            )
                                            isRecipeAddedToCookbook = true
                                        },
                                        enabled = !isRecipeAddedToCookbook,
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = if (isRecipeAddedToCookbook) Icons.Default.Check else Icons.Default.Bookmark,
                                            contentDescription = "Save recipe",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isRecipeAddedToCookbook) "Recipe Saved Successfully!" else "Save Recipe to Cookbooks", fontSize = 12.sp)
                                    }
                                }
                            }

                            // Meal Planner Scheduler Option
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(text = "📅 Schedule directly into Meal Planner", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Day of Week:", fontSize = 10.sp, color = Color.Gray)
                                            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday").forEach { day ->
                                                    FilterChip(
                                                        selected = selectedMealDay == day,
                                                        onClick = { selectedMealDay = day },
                                                        label = { Text(day, fontSize = 9.sp) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Meal Type:", fontSize = 10.sp, color = Color.Gray)
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                listOf("Breakfast", "Lunch", "Dinner").forEach { type ->
                                                    FilterChip(
                                                        selected = selectedMealType == type,
                                                        onClick = { selectedMealType = type },
                                                        label = { Text(type, fontSize = 9.sp) }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = noteText,
                                        onValueChange = { noteText = it },
                                        label = { Text("Meal Note", fontSize = 10.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp)
                                    )

                                    Button(
                                        onClick = {
                                            viewModel.addMealPlan(
                                                day = selectedMealDay,
                                                type = selectedMealType,
                                                title = recipe.title,
                                                cal = recipe.calories ?: 0,
                                                carbs = recipe.carbs ?: 0,
                                                pro = recipe.protein ?: 0,
                                                fat = recipe.fat ?: 0,
                                                note = noteText
                                            )
                                            isMealAddedToPlanner = true
                                        },
                                        enabled = !isMealAddedToPlanner,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = if (isMealAddedToPlanner) Icons.Default.Check else Icons.Default.CalendarMonth,
                                            contentDescription = "Planner",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isMealAddedToPlanner) "Scheduled Successfully!" else "Schedule into Meal Planner", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp))

                        // Bottom Action Row
                        Button(
                            onClick = {
                                val selectedItems = parsedIngredients.filterIndexed { index, _ ->
                                    selectedIngredients[index] ?: true
                                }
                                viewModel.addMultipleShoppingItems(selectedItems)
                                showIGPreviewDialog = false
                                igUrlForShopping = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Add Shopping")
                            Spacer(modifier = Modifier.width(8.dp))
                            val checkedCount = selectedIngredients.values.count { it }
                            Text("Add $checkedCount item(s) to Shopping Ledger", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}


// ================= TAB 3: VACATIONS & TRIP BLUEPRINTS =================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VacationTab(viewModel: FamilyViewModel, currentLang: AppLanguage) {
    val vacations by viewModel.vacations.collectAsStateWithLifecycle()

    var showAddTripDialog by remember { mutableStateOf(false) }

    // Forms
    var tDestination by remember { mutableStateOf("") }
    var tStart by remember { mutableStateOf("2026-07-20") }
    var tEnd by remember { mutableStateOf("2026-07-28") }
    var tBudget by remember { mutableStateOf("2800") }
    var tNote by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(Translate.get("vacation", currentLang), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Cooperative family travel planner blueprints", fontSize = 11.sp, color = Color.Gray)
                }
                Button(onClick = { showAddTripDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Translate.get("add_vacation", currentLang), fontSize = 12.sp)
                }
            }

            if (vacations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No planned trips found. Create one now!", fontSize = 13.sp, color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxSize()) {
                    items(vacations) { v ->
                        var activeSubSection by remember { mutableStateOf(0) } // 0 = packing, 1 = itinerary
                        var showAddingPackingBox by remember { mutableStateOf(false) }
                        var packingInputVal by remember { mutableStateOf("") }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column {
                                        Text(v.destination, fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                        Text("📅 ${v.startDate} / ${v.endDate}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    IconButton(onClick = { viewModel.deleteVacation(v.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Trip Budget: $${v.budget.toInt()}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                    if (v.notes.isNotEmpty()) {
                                        Text(
                                            text = "ℹ️ ${v.notes}",
                                            fontSize = 9.sp,
                                            color = Color.LightGray,
                                            modifier = Modifier.weight(1f).padding(start = 12.dp),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Divider()

                                // Internal switch for packing checklist vs itinerary details
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = { activeSubSection = 0 },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (activeSubSection == 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                                            contentColor = if (activeSubSection == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Packing Checklist", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = { activeSubSection = 1 },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (activeSubSection == 1) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                                            contentColor = if (activeSubSection == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Itinerary Logs", fontSize = 11.sp)
                                    }
                                }

                                when (activeSubSection) {
                                    0 -> {
                                        // Packing list elements split
                                        val itemsSplitList = v.packingList.split("\n").filter { it.isNotBlank() }
                                        
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Items Checklist:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                IconButton(
                                                    onClick = { showAddingPackingBox = !showAddingPackingBox },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Add Packing item", modifier = Modifier.size(16.dp))
                                                }
                                            }

                                            if (showAddingPackingBox) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    OutlinedTextField(
                                                        value = packingInputVal,
                                                        onValueChange = { packingInputVal = it },
                                                        placeholder = { Text("Item label", fontSize = 11.sp) },
                                                        singleLine = true,
                                                        modifier = Modifier.weight(1f).height(42.dp)
                                                    )
                                                    Button(
                                                        onClick = {
                                                            viewModel.addTripPackingItem(v, packingInputVal)
                                                            packingInputVal = ""
                                                            showAddingPackingBox = false
                                                        },
                                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                                    ) {
                                                        Text("Add", fontSize = 10.sp)
                                                    }
                                                }
                                            }

                                            if (itemsSplitList.isEmpty()) {
                                                Text("No packed items elements.", fontSize = 10.sp, color = Color.Gray)
                                            } else {
                                                // Flexible wrap layout representing small blocks of checklist items
                                                FlowRow(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    itemsSplitList.forEach { line ->
                                                        val split = line.split("|")
                                                        val name = split.getOrNull(0) ?: ""
                                                        val isChecked = split.getOrNull(1)?.toBoolean() ?: false

                                                        Surface(
                                                            modifier = Modifier.clickable {
                                                                viewModel.updateTripPackingItem(v, name, !isChecked)
                                                            },
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = if (isChecked) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                                            border = BorderStroke(
                                                                width = 1.dp,
                                                                color = if (isChecked) MaterialTheme.colorScheme.tertiary else Color.Transparent
                                                            )
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.padding(6.dp),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = if (isChecked) Icons.Default.Check else Icons.Default.Close,
                                                                    contentDescription = "Tick",
                                                                    tint = if (isChecked) MaterialTheme.colorScheme.tertiary else Color.Gray,
                                                                    modifier = Modifier.size(12.dp)
                                                                )
                                                                Text(name, fontSize = 11.sp)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    1 -> {
                                        // Edit itinerary multi lines text field
                                        var itineraryEditable by remember { mutableStateOf(v.itinerary) }
                                        var isFocussedEditing by remember { mutableStateOf(false) }

                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Planned daily roadmap itinerary:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                if (isFocussedEditing) {
                                                    Button(
                                                        onClick = {
                                                            viewModel.updateTripItinerary(v, itineraryEditable)
                                                            isFocussedEditing = false
                                                        },
                                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                                        modifier = Modifier.height(28.dp)
                                                    ) {
                                                        Text("Commit", fontSize = 10.sp)
                                                    }
                                                } else {
                                                    IconButton(
                                                        onClick = { isFocussedEditing = true },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(Icons.Default.Edit, contentDescription = "Edit itinerary text", modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }

                                            if (isFocussedEditing) {
                                                OutlinedTextField(
                                                    value = itineraryEditable,
                                                    onValueChange = { itineraryEditable = it },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            } else {
                                                Text(
                                                    text = v.itinerary.ifEmpty { "No itinerary details added. Sauté the text above to outline schedule." },
                                                    lineHeight = 16.sp,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Vacation Trip alert Dialog
        if (showAddTripDialog) {
            Dialog(onDismissRequest = { showAddTripDialog = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Plan Trip Blueprint", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                        OutlinedTextField(
                            value = tDestination,
                            onValueChange = { tDestination = it },
                            label = { Text("Destination Name (e.g. Disney World, Florida)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = tStart,
                                onValueChange = { tStart = it },
                                label = { Text("Start date (yyyy-mm-dd)", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = tEnd,
                                onValueChange = { tEnd = it },
                                label = { Text("End date (yyyy-mm-dd)", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = tBudget,
                            onValueChange = { tBudget = it },
                            label = { Text("Trip Target Budget ($)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = tNote,
                            onValueChange = { tNote = it },
                            label = { Text("Accommodations or details") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showAddTripDialog = false }) { Text("Cancel") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = {
                                if (tDestination.isNotBlank()) {
                                    viewModel.addVacationTrip(
                                        destination = tDestination,
                                        start = tStart,
                                        end = tEnd,
                                        budget = tBudget.toDoubleOrNull() ?: 2000.0,
                                        note = tNote
                                    )
                                    // reset
                                    tDestination = ""
                                    tNote = ""
                                    showAddTripDialog = false
                                }
                            }) {
                                Text("Map Trip")
                            }
                        }
                    }
                }
            }
        }
    }
}


// ================= TAB 4: SHARED FAMILY BUDGET PLANNING =================
@Composable
fun BudgetTab(viewModel: FamilyViewModel, currentLang: AppLanguage) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()

    var showAddTransactionDialog by remember { mutableStateOf(false) }

    // Forms
    var ledgerDesc by remember { mutableStateOf("") }
    var ledgerAmount by remember { mutableStateOf("150") }
    var isExpenseForm by remember { mutableStateOf(true) }
    var selectCategory by remember { mutableStateOf("Groceries") }
    var selectPayerIdx by remember { mutableStateOf(0) }

    val categoryList = listOf("Groceries", "Utilities", "Travel", "Rewards", "Entertainment", "Other")

    // Calculations
    val totalExpense = transactions.filter { it.isExpense }.sumOf { it.amount }
    val totalIncome = transactions.filter { !it.isExpense }.sumOf { it.amount }
    val currentBalance = totalIncome - totalExpense

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(Translate.get("budget", currentLang), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Collaborative family budget breakdown ledger", fontSize = 11.sp, color = Color.Gray)
                }
                Button(onClick = { showAddTransactionDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Translate.get("add_item", currentLang), fontSize = 12.sp)
                }
            }

            // Stat Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Accounts summary dashboard", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Balance Pool", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = "$${currentBalance.toInt()}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = if (currentBalance >= 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Inflow", fontSize = 11.sp, color = Color.Gray)
                                Text("+$${totalIncome.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Outflow", fontSize = 11.sp, color = Color.Gray)
                                Text("-$${totalExpense.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    // Warning warning indicator if expenses exceed limits
                    if (totalExpense > totalIncome && totalIncome > 0) {
                        Surface(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ Warning: Financial cash outflows are higher than total pool deposits. Restructure chores budget.",
                                modifier = Modifier.padding(10.dp),
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Text("Historical Transactions Log", fontSize = 14.sp, fontWeight = FontWeight.Bold)

            // Transactions list
            if (transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Transaction list is dry. Sauté a ledger entry first!", fontSize = 13.sp, color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                    items(transactions) { t ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    val icon = when (t.category) {
                                        "Groceries" -> Icons.Default.ShoppingCart
                                        "Utilities" -> Icons.Default.HomeWork
                                        "Travel" -> Icons.Default.AirportShuttle
                                        "Rewards" -> Icons.Default.Stars
                                        "Entertainment" -> Icons.Default.TheaterComedy
                                        else -> Icons.Default.ReceiptLong
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                if (t.isExpense) MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                                                else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = t.category,
                                            tint = if (t.isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Column {
                                        Text(t.description, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Paid: ${t.paidByMemberName}", fontSize = 10.sp, color = Color.Gray)
                                            Text("• ${t.category}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                            Text("• ${t.date}", fontSize = 9.sp, color = Color.DarkGray)
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (t.isExpense) "-$${t.amount.toInt()}" else "+$${t.amount.toInt()}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = if (t.isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                                    )
                                    IconButton(
                                        onClick = { viewModel.deleteTransaction(t.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Transaction Dialog
        if (showAddTransactionDialog) {
            Dialog(onDismissRequest = { showAddTransactionDialog = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Enter Ledger Entry", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                        // Expense vs Inflow selector
                        Row(modifier = Modifier.fillMaxWidth()) {
                            listOf(true, false).forEach { isExpenseSelection ->
                                val label = if (isExpenseSelection) "Expense 💸" else "Income 💰"
                                val isSelected = isExpenseForm == isExpenseSelection
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isSelected) {
                                                if (isExpenseSelection) MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                                else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                            } else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { isExpenseForm = isExpenseSelection }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) {
                                            if (isExpenseSelection) MaterialTheme.colorScheme.error
                                            else MaterialTheme.colorScheme.tertiary
                                        } else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = ledgerDesc,
                            onValueChange = { ledgerDesc = it },
                            label = { Text("Transaction Details (e.g. Costco bulk shopping)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = ledgerAmount,
                            onValueChange = { ledgerAmount = it },
                            label = { Text("Amount ($)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        Text("Transaction Category", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categoryList.forEach { cat ->
                                FilterChip(
                                    selected = selectCategory == cat,
                                    onClick = { selectCategory = cat },
                                    label = { Text(cat) }
                                )
                            }
                        }

                        // Select payor
                        Text("Paid By Family Member", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        if (members.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                members.forEachIndexed { idx, m ->
                                    FilterChip(
                                        selected = selectPayerIdx == idx,
                                        onClick = { selectPayerIdx = idx },
                                        label = { Text(m.name) }
                                    )
                                }
                            }
                        } else {
                            Text("No family members found to select.", color = Color.Red, fontSize = 10.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showAddTransactionDialog = false }) { Text("Cancel") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = {
                                if (ledgerDesc.isNotBlank() && members.isNotEmpty()) {
                                    viewModel.addTransaction(
                                        desc = ledgerDesc,
                                        valAmount = ledgerAmount.toDoubleOrNull() ?: 100.0,
                                        isEx = isExpenseForm,
                                        cat = selectCategory,
                                        paidByName = members[selectPayerIdx].name
                                    )
                                    // Reset
                                    ledgerDesc = ""
                                    showAddTransactionDialog = false
                                }
                            }) {
                                Text("Record")
                            }
                        }
                    }
                }
            }
        }
    }
}
