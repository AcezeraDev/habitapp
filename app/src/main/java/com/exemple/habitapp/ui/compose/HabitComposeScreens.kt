package com.exemple.habitapp.ui.compose

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.exemple.habitapp.R
import com.exemple.habitapp.data.HabitDashboardState
import com.exemple.habitapp.data.HabitDraft
import com.exemple.habitapp.data.HabitUiState
import com.exemple.habitapp.data.ReportPdfExporter
import com.exemple.habitapp.notifications.NotificationHelper
import com.exemple.habitapp.notifications.ReminderScheduler
import com.exemple.habitapp.ui.components.ElevatedPanel
import com.exemple.habitapp.ui.components.EmptyPanel
import com.exemple.habitapp.ui.components.HeroCard
import com.exemple.habitapp.ui.components.MetricCard
import com.exemple.habitapp.ui.components.PrimaryActionButton
import com.exemple.habitapp.ui.components.RoundIcon
import com.exemple.habitapp.ui.components.ScoreRing
import com.exemple.habitapp.ui.components.ScreenSection
import com.exemple.habitapp.ui.components.StatusPill
import com.exemple.habitapp.ui.navigation.HabitRoute
import com.exemple.habitapp.ui.navigation.bottomRoutes
import com.exemple.habitapp.ui.theme.Coral
import com.exemple.habitapp.ui.theme.HabitTheme
import com.exemple.habitapp.ui.theme.Study
import com.exemple.habitapp.ui.theme.Success
import com.exemple.habitapp.ui.theme.Water
import com.exemple.habitapp.ui.theme.Warning
import com.exemple.habitapp.ui.theme.screenGradient
import com.exemple.habitapp.viewmodel.FocusTimerState
import com.exemple.habitapp.viewmodel.HabitMainViewModel
import java.io.File
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HabitAppRoot(
    viewModel: HabitMainViewModel,
    requestedRoute: String? = null,
    onRouteConsumed: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val timer by viewModel.timer.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val navController = rememberNavController()
    val actions = remember(viewModel) {
        HabitActions(
            login = viewModel::login,
            completeDailySetup = viewModel::completeDailySetup,
            addWater = viewModel::addWater,
            undoWater = viewModel::undoWater,
            resetWater = viewModel::resetWater,
            setWaterGoal = viewModel::setWaterGoal,
            setFocusGoal = viewModel::setFocusGoal,
            addManualFocus = viewModel::addManualFocus,
            setSessionMinutes = viewModel::setSessionMinutes,
            toggleTimer = viewModel::toggleTimer,
            resetTimer = viewModel::resetTimer,
            setChecklist = viewModel::setChecklist,
            setRoutinePeriod = viewModel::setRoutinePeriod,
            setRoutineBlock = viewModel::setRoutineBlock,
            completeRoutine = viewModel::completeRoutine,
            setMood = viewModel::setMood,
            setEnergy = viewModel::setEnergy,
            addHabit = viewModel::addHabit,
            saveHabit = viewModel::saveHabit,
            toggleHabit = viewModel::toggleHabit,
            removeHabit = viewModel::removeHabit,
            saveProfile = viewModel::saveProfile,
            setTheme = viewModel::setTheme,
            selectTheme = viewModel::selectTheme,
            setNotificationSettings = viewModel::setNotificationSettings,
            setNotificationPlan = viewModel::setNotificationPlan,
            saveDiary = viewModel::saveDiary,
            saveAvatarPath = viewModel::saveAvatarPath,
            removeAvatar = viewModel::removeAvatar,
            logout = viewModel::logout,
            setChallengeGoal = viewModel::setChallengeGoal,
            claimMissionXp = viewModel::claimMissionXp,
            importBackup = viewModel::importBackup,
            exportBackup = viewModel::exportBackup,
            exportBackupJson = viewModel::exportBackupJson,
        )
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    HabitTheme(darkTheme = state.darkMode) {
        if (!state.loggedIn) {
            AuthShell {
                LoginScreen(state = state, actions = actions)
            }
            return@HabitTheme
        }
        if (!state.dailySetupComplete) {
            AuthShell {
                DailySetupScreen(state = state, actions = actions)
            }
            return@HabitTheme
        }
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                val backStack by navController.currentBackStackEntryAsState()
                val destination = backStack?.destination
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    bottomRoutes.forEach { item ->
                        val selected = destination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(HabitRoute.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(painterResource(item.icon), contentDescription = item.label) },
                            label = { Text(item.label, maxLines = 1) },
                        )
                    }
                }
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Brush.linearGradient(screenGradient(state.darkMode))),
            ) {
                LaunchedEffect(requestedRoute) {
                    val route = requestedRoute
                    if (!route.isNullOrBlank()) {
                        navController.navigate(route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                        onRouteConsumed()
                    }
                }
                NavHost(navController = navController, startDestination = HabitRoute.Home.route) {
                    composable(HabitRoute.Home.route) { HomeScreen(state, actions, navController::navigate) }
                    composable(HabitRoute.Water.route) { WaterScreen(state, actions) }
                    composable(HabitRoute.Routine.route) { RoutineScreen(state, actions) }
                    composable(HabitRoute.Focus.route) { FocusScreen(state, timer, actions) }
                    composable(HabitRoute.Habits.route) { HabitsScreen(state, actions) }
                    composable(HabitRoute.Progress.route) { ProgressScreen(state, actions, navController::navigate) }
                    composable(HabitRoute.More.route) { MoreScreen(state, navController::navigate) }
                    composable(HabitRoute.Goals.route) { GoalsScreen(state, actions) }
                    composable(HabitRoute.Profile.route) { ProfileScreen(state, actions) }
                    composable(HabitRoute.Settings.route) { SettingsScreen(state, actions) }
                    composable(HabitRoute.History.route) { HistoryScreen(state) }
                    composable(HabitRoute.Calendar.route) { CalendarScreen(state) }
                    composable(HabitRoute.Achievements.route) { AchievementsScreen(state) }
                    composable(HabitRoute.Missions.route) { MissionsScreen(state, actions) }
                    composable(HabitRoute.Report.route) { ReportScreen(state) }
                    composable(HabitRoute.Backup.route) { BackupScreen(state, actions) }
                    composable(HabitRoute.Diary.route) { DiaryScreen(state, actions) }
                    composable(HabitRoute.Appearance.route) { AppearanceScreen(state, actions, navController::navigate) }
                    composable(HabitRoute.Themes.route) { ThemesScreen(state, actions, navController::navigate) }
                    composable(HabitRoute.Challenges.route) { ChallengesScreen(state, actions) }
                    composable(HabitRoute.Stats.route) { StatsScreen(state) }
                }
            }
        }
    }
}

@Stable
private data class HabitActions(
    val login: (String, String, String) -> Unit,
    val completeDailySetup: (String, String, String, String, Int, Int, Int, Int) -> Unit,
    val addWater: (Int) -> Unit,
    val undoWater: () -> Unit,
    val resetWater: () -> Unit,
    val setWaterGoal: (Int) -> Unit,
    val setFocusGoal: (Int) -> Unit,
    val addManualFocus: (Int) -> Unit,
    val setSessionMinutes: (Int) -> Unit,
    val toggleTimer: () -> Unit,
    val resetTimer: () -> Unit,
    val setChecklist: (Int, Boolean) -> Unit,
    val setRoutinePeriod: (String) -> Unit,
    val setRoutineBlock: (Int, Boolean) -> Unit,
    val completeRoutine: () -> Unit,
    val setMood: (Int) -> Unit,
    val setEnergy: (Int) -> Unit,
    val addHabit: (String) -> Unit,
    val saveHabit: (HabitDraft, String?) -> Unit,
    val toggleHabit: (String) -> Unit,
    val removeHabit: (String) -> Unit,
    val saveProfile: (String, String, String, String, String) -> Unit,
    val setTheme: (Boolean, String) -> Unit,
    val selectTheme: (String) -> Unit,
    val setNotificationSettings: (Boolean, Boolean, Boolean) -> Unit,
    val setNotificationPlan: (Boolean, Boolean, Boolean, Boolean, String, Int, String, String) -> Unit,
    val saveDiary: (String) -> Unit,
    val saveAvatarPath: (String) -> Unit,
    val removeAvatar: () -> Unit,
    val logout: () -> Unit,
    val setChallengeGoal: (Int) -> Unit,
    val claimMissionXp: () -> Unit,
    val importBackup: (String) -> Unit,
    val exportBackup: () -> String,
    val exportBackupJson: () -> String,
)

@Composable
private fun AuthShell(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(Color(0xFF263494), Color(0xFF0EA5E9), Color(0xFFF8FAFC)))),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(22.dp),
            verticalArrangement = Arrangement.Center,
            content = content,
        )
    }
}

@Composable
private fun LoginScreen(state: HabitDashboardState, actions: HabitActions) {
    var name by remember(state.name) { mutableStateOf(if (state.name == "Guerreiro") "" else state.name) }
    var email by remember(state.email) { mutableStateOf(state.email) }
    var password by remember { mutableStateOf("") }

    HeroCard(
        title = "HabitApp",
        subtitle = "Entre para recuperar seu perfil, metas e rotina diária.",
        icon = R.drawable.ic_app_icon_round,
        gradient = listOf(Color(0xFF263494), Color(0xFF4353D8), Water),
    ) {
        Text("O fluxo de login voltou para manter a experiência completa da versão anterior.", color = Color.White.copy(alpha = 0.86f))
    }
    Spacer(Modifier.height(14.dp))
    ElevatedPanel {
        OutlinedTextField(name, { name = it.take(32) }, label = { Text("Nome") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(email, { email = it.take(64) }, label = { Text("E-mail") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(password, { password = it.take(32) }, label = { Text("Senha opcional") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Button(onClick = { actions.login(name, email, password) }, modifier = Modifier.fillMaxWidth()) {
            Text("Entrar")
        }
    }
}

@Composable
private fun DailySetupScreen(state: HabitDashboardState, actions: HabitActions) {
    var name by remember(state.name) { mutableStateOf(state.name) }
    var objective by remember(state.objective) { mutableStateOf(state.objective) }
    var routine by remember(state.routine) { mutableStateOf(state.routine) }
    var period by remember(state.bestPeriod) { mutableStateOf(if (state.bestPeriod == "hoje") "Manhã" else state.bestPeriod) }
    var currentWater by remember(state.waterMl) { mutableStateOf(state.waterMl.toString()) }
    var waterGoal by remember(state.waterGoalMl) { mutableStateOf(state.waterGoalMl.toString()) }
    var focusGoal by remember(state.focusGoalMinutes) { mutableStateOf(state.focusGoalMinutes.toString()) }
    var session by remember(state.sessionMinutes) { mutableStateOf(state.sessionMinutes.toString()) }

    HeroCard(
        title = "Configuração do dia",
        subtitle = "A versão antiga tinha onboarding diário; ele voltou em Compose.",
        icon = R.drawable.ic_nav_routine,
        gradient = listOf(Success, Water, Study),
    ) {
        Text("Defina metas realistas antes de abrir o dashboard.", color = Color.White.copy(alpha = 0.86f))
    }
    Spacer(Modifier.height(14.dp))
    ElevatedPanel {
        OutlinedTextField(name, { name = it.take(32) }, label = { Text("Como quer ser chamado?") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(objective, { objective = it.take(64) }, label = { Text("Foco principal") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("Corrida", "Equilibrada", "Tranquila").forEach { value ->
                FilterChip(selected = routine == value, onClick = { routine = value }, label = { Text(value) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("Manhã", "Tarde", "Noite").forEach { value ->
                FilterChip(selected = period == value, onClick = { period = value }, label = { Text(value) })
            }
        }
        OutlinedTextField(currentWater, { currentWater = it.filter(Char::isDigit).take(4) }, label = { Text("Água já bebida em ml") }, singleLine = true, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        OutlinedTextField(waterGoal, { waterGoal = it.filter(Char::isDigit).take(4) }, label = { Text("Meta de água em ml") }, singleLine = true, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        OutlinedTextField(focusGoal, { focusGoal = it.filter(Char::isDigit).take(3) }, label = { Text("Meta de foco em min") }, singleLine = true, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        OutlinedTextField(session, { session = it.filter(Char::isDigit).take(3) }, label = { Text("Sessão de foco em min") }, singleLine = true, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                actions.completeDailySetup(
                    name,
                    objective,
                    routine,
                    period,
                    currentWater.toIntOrNull() ?: 0,
                    waterGoal.toIntOrNull() ?: 0,
                    focusGoal.toIntOrNull() ?: 0,
                    session.toIntOrNull() ?: 0,
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Criar minha rotina")
        }
    }
}

@Composable
private fun HomeScreen(state: HabitDashboardState, actions: HabitActions, navigate: (String) -> Unit) {
    ScreenColumn {
        HeroCard(
            title = greeting(state.name),
            subtitle = "Score ${state.score}% hoje | ${state.streak} dias fortes | nível ${state.xpLevel}",
            icon = R.drawable.ic_nav_home,
            gradient = listOf(Color(0xFF263494), Color(0xFF4353D8), Color(0xFF0EA5E9)),
        ) {
            ScoreRing(score = state.score, label = nextActionText(state))
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PrimaryActionButton("Água", R.drawable.ic_nav_water, Modifier.weight(1f)) { navigate(HabitRoute.Water.route) }
                PrimaryActionButton("Foco", R.drawable.ic_nav_focus, Modifier.weight(1f)) { navigate(HabitRoute.Focus.route) }
            }
        }

        ScreenSection("Resumo do dia", "O que mais pesa no progresso de hoje.")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("Água", "${state.waterMl}/${state.waterGoalMl} ml", state.waterPercent, Water, Modifier.weight(1f))
            MetricCard("Foco", "${state.focusMinutes}/${state.focusGoalMinutes} min", state.focusPercent, Study, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("Checklist", "${state.checklistDone}/3 feitos", state.checklistDone * 33, Success, Modifier.weight(1f))
            MetricCard("Semana", "${state.weeklyAverage}% média", state.weeklyAverage, Coral, Modifier.weight(1f))
        }

        ElevatedPanel {
            ScreenSection("Plano inteligente", "Baseado no que ainda falta hoje.")
            Text(progressInsight(state), color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 21.sp)
            Spacer(Modifier.height(14.dp))
            Button(onClick = { navigate(nextRoute(state)) }, modifier = Modifier.fillMaxWidth()) {
                Text(nextActionText(state))
            }
        }

        ScreenSection("Checklist rápido", "Marque o básico sem sair da home.")
        ChecklistCard(state, actions)

        ScreenSection("Check-in", "Humor e energia ajudam a calibrar o dia.")
        MoodEnergyCard(state, actions)

        ScreenSection("Hábitos de hoje", "Toque para concluir ou reabrir.")
        if (state.habits.isEmpty()) {
            EmptyPanel("Nenhum hábito ainda", "Crie o primeiro hábito e acompanhe streak, semana e score.")
        } else {
            state.habits.take(4).forEach { habit ->
                HabitRow(habit = habit, onToggle = { actions.toggleHabit(habit.name) }, onRemove = null)
            }
            TextButton(onClick = { navigate(HabitRoute.Habits.route) }, modifier = Modifier.align(Alignment.End)) {
                Text("Ver todos")
            }
        }
    }
}

@Composable
private fun WaterScreen(state: HabitDashboardState, actions: HabitActions) {
    var goalText by remember(state.waterGoalMl) { mutableStateOf(state.waterGoalMl.toString()) }
    var customAmount by remember { mutableStateOf("") }

    ScreenColumn {
        HeroCard(
            title = "Hidratação",
            subtitle = "${formatLiters(state.waterMl)} de ${formatLiters(state.waterGoalMl)} | ${state.cupsLeft} copos restantes",
            icon = R.drawable.ic_nav_water,
            gradient = listOf(Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFF14B8A6)),
        ) {
            ScoreRing(score = state.waterPercent, label = if (state.waterPercent >= 100) "Meta fechada" else "Ritmo de água")
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                PrimaryActionButton("+250 ml", R.drawable.ic_nav_water, Modifier.weight(1f)) { actions.addWater(250) }
                PrimaryActionButton("+500 ml", R.drawable.ic_nav_water, Modifier.weight(1f)) { actions.addWater(500) }
            }
        }

        ElevatedPanel {
            ScreenSection("Ajuste fino", "Use um registro rápido ou altere a meta diária.")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { actions.addWater(100) }, modifier = Modifier.weight(1f)) { Text("+100 ml") }
                OutlinedButton(onClick = actions.undoWater, modifier = Modifier.weight(1f)) { Text("Desfazer") }
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = customAmount,
                    onValueChange = { customAmount = it.filter(Char::isDigit).take(4) },
                    label = { Text("Quantidade em ml") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = {
                    actions.addWater(customAmount.toIntOrNull() ?: 0)
                    customAmount = ""
                }) { Text("Adicionar") }
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = goalText,
                    onValueChange = { goalText = it.filter(Char::isDigit).take(4) },
                    label = { Text("Meta em ml") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = { actions.setWaterGoal(goalText.toIntOrNull() ?: 0) }) { Text("Salvar") }
            }
            TextButton(onClick = actions.resetWater, modifier = Modifier.align(Alignment.End)) {
                Text("Zerar água de hoje")
            }
        }

        ScreenSection("Registros de hoje", "Últimos lançamentos de hidratação.")
        LogList(state.waterLog, emptyText = "Nenhum registro de água hoje.")
    }
}

@Composable
private fun RoutineScreen(state: HabitDashboardState, actions: HabitActions) {
    ScreenColumn {
        HeroCard(
            title = "Rotina",
            subtitle = "${state.routineBlocks.count { it }} de 4 blocos concluídos no período ${state.routinePeriod.lowercase(Locale.ROOT)}.",
            icon = R.drawable.ic_nav_routine,
            gradient = listOf(Success, Water, Color(0xFF14B8A6)),
        ) {
            ScoreRing(score = state.routinePercent, label = "rotina de hoje")
            Spacer(Modifier.height(14.dp))
            Text(state.routinePlan, color = Color.White.copy(alpha = 0.84f))
        }

        ElevatedPanel {
            ScreenSection("Período principal", "O app usa isso para sugerir seu melhor bloco.")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("Manhã", "Tarde", "Noite").forEach { period ->
                    FilterChip(
                        selected = state.routinePeriod == period,
                        onClick = { actions.setRoutinePeriod(period) },
                        label = { Text(period) },
                    )
                }
            }
        }

        ElevatedPanel {
            ScreenSection("Blocos do dia", "A função antiga de rotina voltou em Compose.")
            val items = listOf(
                "Manhã preparada" to "Abrir o dia com intenção.",
                "Alimentação" to "Proteger energia e hidratação.",
                "Movimento" to "Treino, caminhada ou alongamento.",
                "Sono" to "Fechar telas e preparar descanso.",
            )
            items.forEachIndexed { index, item ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Checkbox(
                        checked = state.routineBlocks.getOrElse(index) { false },
                        onCheckedChange = { actions.setRoutineBlock(index, it) },
                    )
                    Column(Modifier.weight(1f)) {
                        Text(item.first, fontWeight = FontWeight.Bold)
                        Text(item.second, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Button(onClick = actions.completeRoutine, modifier = Modifier.fillMaxWidth()) {
                Text("Marcar rotina completa")
            }
        }
    }
}

@Composable
private fun FocusScreen(state: HabitDashboardState, timer: FocusTimerState, actions: HabitActions) {
    ScreenColumn {
        HeroCard(
            title = "Foco profundo",
            subtitle = "${state.focusMinutes}/${state.focusGoalMinutes} min hoje | ${state.focusSessions} sessões",
            icon = R.drawable.ic_nav_focus,
            gradient = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED), Color(0xFFF97316)),
        ) {
            val progress = 1f - (timer.remainingMs / (timer.sessionMinutes * 60_000f))
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                CircularProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.size(178.dp),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.22f),
                    strokeWidth = 12.dp,
                    strokeCap = StrokeCap.Round,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(formatTimer(timer.remainingMs), color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black)
                    Text(if (timer.running) "focando agora" else "pronto para iniciar", color = Color.White.copy(alpha = 0.78f))
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                PrimaryActionButton(if (timer.running) "Pausar" else "Iniciar", R.drawable.ic_nav_focus, Modifier.weight(1f), actions.toggleTimer)
                PrimaryActionButton("Reset", R.drawable.ic_clock_history, Modifier.weight(1f), actions.resetTimer)
            }
        }

        ElevatedPanel {
            ScreenSection("Duração da sessão", "Escolha um bloco realista para hoje.")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(15, 25, 45).forEach { minutes ->
                    FilterChip(selected = timer.sessionMinutes == minutes, onClick = { actions.setSessionMinutes(minutes) }, label = { Text("$minutes min") })
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Meta diária: ${state.focusGoalMinutes} min", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Slider(
                value = state.focusGoalMinutes.toFloat(),
                onValueChange = { actions.setFocusGoal(it.roundToInt().coerceIn(15, 240)) },
                valueRange = 15f..240f,
                steps = 14,
            )
            LinearProgressIndicator(
                progress = { state.focusPercent / 100f },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(100)),
                color = Study,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = { actions.addManualFocus(10) }, modifier = Modifier.fillMaxWidth()) {
                Text("Registrar +10 min manualmente")
            }
        }

        ScreenSection("Histórico de foco", "Últimos blocos registrados.")
        LogList(state.focusLog, emptyText = "Nenhuma sessão registrada hoje.")
    }
}

@Composable
private fun HabitsScreen(state: HabitDashboardState, actions: HabitActions) {
    var newHabit by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Rotina") }
    var frequency by remember { mutableStateOf("Diário") }
    var time by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("Azul") }
    var icon by remember { mutableStateOf("Livro") }
    var editingName by remember { mutableStateOf<String?>(null) }

    ScreenColumn {
        HeroCard(
            title = if (editingName == null) "Hábitos" else "Editando hábito",
            subtitle = "${state.habits.count { it.done }} de ${state.habits.size} concluídos hoje",
            icon = R.drawable.ic_nav_goals,
            gradient = listOf(Color(0xFF16A34A), Color(0xFF14B8A6), Color(0xFF4353D8)),
        ) {
            Text("Cadastro completo com descrição, categoria, frequência e horário voltou em Compose.", color = Color.White.copy(alpha = 0.84f))
        }

        ElevatedPanel {
            OutlinedTextField(newHabit, { newHabit = it.take(42) }, label = { Text("Nome do hábito") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(description, { description = it.take(120) }, label = { Text("Descrição") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("Saúde", "Foco", "Movimento", "Sono", "Rotina").forEach { value ->
                    FilterChip(selected = category == value, onClick = { category = value }, label = { Text(value) })
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(frequency, { frequency = it.take(24) }, label = { Text("Frequência") }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(time, { time = it.take(5) }, label = { Text("Horário") }, singleLine = true, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(color, { color = it.take(18) }, label = { Text("Cor") }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(icon, { icon = it.take(18) }, label = { Text("Ícone") }, singleLine = true, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = {
                    actions.saveHabit(
                        HabitDraft(
                            name = newHabit,
                            description = description.ifBlank { "Pequena ação para manter consistência hoje." },
                            category = category,
                            frequency = frequency.ifBlank { "Diário" },
                            time = time,
                            colorName = color.ifBlank { "Azul" },
                            iconName = icon.ifBlank { "Livro" },
                        ),
                        editingName,
                    )
                    newHabit = ""
                    description = ""
                    category = "Rotina"
                    frequency = "Diário"
                    time = ""
                    color = "Azul"
                    icon = "Livro"
                    editingName = null
                }, modifier = Modifier.weight(1f)) { Text(if (editingName == null) "Criar" else "Salvar") }
                if (editingName != null) {
                    OutlinedButton(onClick = {
                        newHabit = ""
                        description = ""
                        editingName = null
                    }, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                }
            }
        }

        ScreenSection("Lista ativa", "Organizada por pendentes e concluídos.")
        if (state.habits.isEmpty()) {
            EmptyPanel("Sua lista está vazia", "Crie um hábito pequeno. O app sugere categoria, cor e descrição automaticamente.")
        } else {
            val pending = state.habits.filterNot { it.done }
            val done = state.habits.filter { it.done }
            if (pending.isNotEmpty()) {
                SmallLabel("Pendentes")
                pending.forEach { habit ->
                    HabitRow(
                        habit = habit,
                        onToggle = { actions.toggleHabit(habit.name) },
                        onEdit = {
                            editingName = habit.name
                            newHabit = habit.name
                            description = habit.description
                            category = habit.category
                            frequency = habit.frequency
                            time = habit.time
                            color = habit.colorName
                            icon = habit.iconName
                        },
                        onRemove = { actions.removeHabit(habit.name) },
                    )
                }
            }
            if (done.isNotEmpty()) {
                SmallLabel("Concluídos")
                done.forEach { habit ->
                    HabitRow(
                        habit = habit,
                        onToggle = { actions.toggleHabit(habit.name) },
                        onEdit = {
                            editingName = habit.name
                            newHabit = habit.name
                            description = habit.description
                            category = habit.category
                            frequency = habit.frequency
                            time = habit.time
                            color = habit.colorName
                            icon = habit.iconName
                        },
                        onRemove = { actions.removeHabit(habit.name) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressScreen(state: HabitDashboardState, actions: HabitActions, navigate: (String) -> Unit) {
    ScreenColumn {
        HeroCard(
            title = "Progresso",
            subtitle = "${state.weeklyAverage}% na semana | ${state.levelName} | ${state.xp} XP",
            icon = R.drawable.ic_nav_chart,
            gradient = listOf(Color(0xFF263494), Color(0xFF7C3AED), Color(0xFF16A34A)),
        ) {
            ScoreRing(score = state.score, label = "placar de hoje")
            Spacer(Modifier.height(12.dp))
            Text("Faltam ${state.xpToNext} XP para o próximo nível.", color = Color.White.copy(alpha = 0.82f))
            Spacer(Modifier.height(10.dp))
            PrimaryActionButton("Resgatar XP", R.drawable.ic_mission_flag, Modifier.fillMaxWidth(), actions.claimMissionXp)
        }

        ScreenSection("Semana", "Leitura visual dos últimos 7 dias.")
        ElevatedPanel { WeekBars(scores = state.weekScores) }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("Água", "${state.waterPercent}%", state.waterPercent, Water, Modifier.weight(1f))
            MetricCard("Foco", "${state.focusPercent}%", state.focusPercent, Study, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            val habitPercent = if (state.habits.isEmpty()) 0 else state.habits.count { it.done } * 100 / state.habits.size
            MetricCard("Hábitos", "${state.habits.count { it.done }}/${state.habits.size}", habitPercent, Success, Modifier.weight(1f))
            MetricCard("Checklist", "${state.checklistDone}/3", state.checklistDone * 33, Coral, Modifier.weight(1f))
        }

        ElevatedPanel {
            ScreenSection("Insight", "Melhor próximo movimento.")
            Text(progressInsight(state), color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 21.sp)
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = { navigate(nextRoute(state)) }, modifier = Modifier.fillMaxWidth()) {
                Text(nextActionText(state))
            }
        }
    }
}

@Composable
private fun MoreScreen(state: HabitDashboardState, navigate: (String) -> Unit) {
    ScreenColumn {
        HeroCard(
            title = "Mais",
            subtitle = "Nível ${state.xpLevel} | ${state.score}% hoje | tema ${state.accentTheme}",
            icon = R.drawable.ic_nav_more,
            gradient = listOf(Color(0xFFF97316), Color(0xFF7C3AED), Color(0xFF14B8A6)),
        ) {
            Text("Metas, relatórios, backup e preferências em um só lugar.", color = Color.White.copy(alpha = 0.84f))
        }

        ScreenSection("Ações principais", "Atalhos funcionais do app.")
        moreItems.forEach { route ->
            MoreAction(route = route, onClick = { navigate(route.route) })
        }
    }
}

@Composable
private fun GoalsScreen(state: HabitDashboardState, actions: HabitActions) {
    var waterGoal by remember(state.waterGoalMl) { mutableStateOf(state.waterGoalMl.toString()) }
    var focusGoal by remember(state.focusGoalMinutes) { mutableStateOf(state.focusGoalMinutes.toString()) }
    var session by remember(state.sessionMinutes) { mutableStateOf(state.sessionMinutes.toString()) }

    ScreenColumn {
        SimpleHero("Metas", "Ajuste água, foco diário e duração das sessões.", R.drawable.ic_nav_goals, listOf(Success, Study))
        ElevatedPanel {
            GoalInput("Meta de água em ml", waterGoal, { waterGoal = it.filter(Char::isDigit).take(4) }) {
                actions.setWaterGoal(waterGoal.toIntOrNull() ?: 0)
            }
            GoalInput("Meta de foco em min", focusGoal, { focusGoal = it.filter(Char::isDigit).take(3) }) {
                actions.setFocusGoal(focusGoal.toIntOrNull() ?: 0)
            }
            GoalInput("Sessão Pomodoro em min", session, { session = it.filter(Char::isDigit).take(3) }) {
                actions.setSessionMinutes(session.toIntOrNull() ?: 0)
            }
        }
    }
}

@Composable
private fun ProfileScreen(state: HabitDashboardState, actions: HabitActions) {
    val context = LocalContext.current
    var name by remember(state.name) { mutableStateOf(state.name) }
    var email by remember(state.email) { mutableStateOf(state.email) }
    var objective by remember(state.objective) { mutableStateOf(state.objective) }
    var routine by remember(state.routine) { mutableStateOf(state.routine) }
    var period by remember(state.bestPeriod) { mutableStateOf(state.bestPeriod) }
    val avatarLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { saveAvatarFromUri(context, it)?.let(actions.saveAvatarPath) }
    }

    ScreenColumn {
        SimpleHero("Perfil", "Dados pessoais e contexto da rotina.", R.drawable.ic_nav_profile, listOf(Color(0xFF4353D8), Water))
        ElevatedPanel {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                AvatarPreview(state.avatarPath)
                Column(Modifier.weight(1f)) {
                    Text("Perfil de ${state.name}", fontWeight = FontWeight.Black)
                    Text("${state.achievements.count { it.unlocked }} medalhas | ${state.streak} dias | média ${state.weeklyAverage}%", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${state.totalWaterMl} ml água | ${state.totalFocusMinutes} min foco", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { avatarLauncher.launch("image/*") }, modifier = Modifier.weight(1f)) { Text("Trocar foto") }
                OutlinedButton(onClick = actions.removeAvatar, modifier = Modifier.weight(1f)) { Text("Remover") }
            }
        }
        ElevatedPanel {
            OutlinedTextField(name, { name = it.take(32) }, label = { Text("Nome") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(email, { email = it.take(64) }, label = { Text("E-mail") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(objective, { objective = it.take(64) }, label = { Text("Objetivo principal") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(routine, { routine = it.take(32) }, label = { Text("Ritmo de rotina") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(period, { period = it.take(32) }, label = { Text("Melhor horário") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            Button(onClick = { actions.saveProfile(name, email, objective, routine, period) }, modifier = Modifier.fillMaxWidth()) {
                Text("Salvar perfil")
            }
            TextButton(onClick = actions.logout, modifier = Modifier.fillMaxWidth()) {
                Text("Sair da conta")
            }
        }
    }
}

@Composable
private fun SettingsScreen(state: HabitDashboardState, actions: HabitActions) {
    val context = LocalContext.current
    var dark by remember(state.darkMode) { mutableStateOf(state.darkMode) }
    var accent by remember(state.accentTheme) { mutableStateOf(state.accentTheme) }
    var notifications by remember(state.notificationsEnabled) { mutableStateOf(state.notificationsEnabled) }
    var waterReminder by remember(state.waterReminderEnabled) { mutableStateOf(state.waterReminderEnabled) }
    var focusReminder by remember(state.focusReminderEnabled) { mutableStateOf(state.focusReminderEnabled) }
    var routineReminder by remember(state.routineReminderEnabled) { mutableStateOf(state.routineReminderEnabled) }
    var waterTime by remember(state.waterReminderTime) { mutableStateOf(state.waterReminderTime) }
    var waterInterval by remember(state.waterReminderIntervalHours) { mutableStateOf(state.waterReminderIntervalHours.toString()) }
    var focusTime by remember(state.focusReminderTime) { mutableStateOf(state.focusReminderTime) }
    var routineTime by remember(state.routineReminderTime) { mutableStateOf(state.routineReminderTime) }

    ScreenColumn {
        SimpleHero("Configurações", "Tema, lembretes e preferências persistidas.", R.drawable.ic_notification, listOf(Study, Coral))
        ElevatedPanel {
            SettingSwitch("Modo escuro", "Alterna o tema Material 3.", dark) {
                dark = it
                actions.setTheme(dark, accent)
            }
            OutlinedTextField(accent, { accent = it.take(24) }, label = { Text("Tema/acento") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Button(onClick = { actions.setTheme(dark, accent) }, modifier = Modifier.fillMaxWidth()) { Text("Salvar tema") }
        }
        ElevatedPanel {
            ScreenSection("Lembretes", "Água, foco e rotina com horários editáveis.")
            SettingSwitch("Notificações", "Liga ou desliga lembretes do app.", notifications) {
                notifications = it
            }
            SettingSwitch("Lembrete de água", "Sugestão diária de hidratação.", waterReminder) {
                waterReminder = it
            }
            SettingSwitch("Lembrete de foco", "Sugestão diária de Pomodoro.", focusReminder) {
                focusReminder = it
            }
            SettingSwitch("Lembrete de rotina", "Fechamento do dia e checklist.", routineReminder) {
                routineReminder = it
            }
            HorizontalDivider(Modifier.padding(vertical = 14.dp))
            OutlinedTextField(waterTime, { waterTime = it.take(5) }, label = { Text("Água a partir de HH:mm") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(waterInterval, { waterInterval = it.filter(Char::isDigit).take(1) }, label = { Text("Intervalo de água em horas") }, singleLine = true, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(focusTime, { focusTime = it.take(5) }, label = { Text("Foco às HH:mm") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(routineTime, { routineTime = it.take(5) }, label = { Text("Rotina às HH:mm") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    actions.setNotificationPlan(
                        notifications,
                        waterReminder,
                        focusReminder,
                        routineReminder,
                        waterTime,
                        waterInterval.toIntOrNull() ?: 0,
                        focusTime,
                        routineTime,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Salvar lembretes") }
            OutlinedButton(onClick = { NotificationHelper.showReminder(context, ReminderScheduler.TYPE_WATER) }, modifier = Modifier.fillMaxWidth()) {
                Text("Enviar notificação de teste")
            }
        }
    }
}

@Composable
private fun HistoryScreen(state: HabitDashboardState) {
    ScreenColumn {
        SimpleHero("Histórico", "Últimos registros de água, foco e score.", R.drawable.ic_history, listOf(Coral, Study))
        LogList(state.waterLog, "Nenhum registro de água hoje.")
        LogList(state.focusLog, "Nenhuma sessão de foco hoje.")
        ElevatedPanel {
            state.weekScores.forEachIndexed { index, score ->
                HistoryRow(label = if (index == state.weekScores.lastIndex) "Hoje" else "-${state.weekScores.lastIndex - index} dias", score = score)
            }
        }
    }
}

@Composable
private fun CalendarScreen(state: HabitDashboardState) {
    ScreenColumn {
        SimpleHero("Calendário", "Mapa dos últimos 30 dias por desempenho.", R.drawable.ic_clock_history, listOf(Water, Success))
        ElevatedPanel {
            state.monthScores.chunked(7).forEachIndexed { rowIndex, row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEachIndexed { columnIndex, score ->
                        val day = rowIndex * 7 + columnIndex + 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(scoreColor(score)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(day.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    repeat(7 - row.size) { Spacer(Modifier.weight(1f).height(42.dp)) }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AchievementsScreen(state: HabitDashboardState) {
    ScreenColumn {
        SimpleHero("Conquistas", "${state.achievements.count { it.unlocked }} de ${state.achievements.size} desbloqueadas.", R.drawable.ic_premium_trophy, listOf(Warning, Coral))
        state.achievements.forEach { achievement ->
            ElevatedPanel {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(if (achievement.unlocked) "OK" else "--", if (achievement.unlocked) Success else MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(achievement.title, fontWeight = FontWeight.Black)
                        Text(achievement.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun MissionsScreen(state: HabitDashboardState, actions: HabitActions) {
    ScreenColumn {
        SimpleHero("Missões", "Tarefas diárias com XP real.", R.drawable.ic_mission_flag, listOf(Success, Water))
        Button(onClick = actions.claimMissionXp, modifier = Modifier.fillMaxWidth()) { Text("Resgatar XP disponível") }
        state.missions.forEach { mission ->
            ElevatedPanel {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(if (mission.done) "+${mission.xp} XP" else "${mission.xp} XP", if (mission.done) Success else Warning)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(mission.title, fontWeight = FontWeight.Black)
                        Text(mission.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportScreen(state: HabitDashboardState) {
    val context = LocalContext.current
    val report = remember(state) { buildReport(state) }
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) {
            runCatching { ReportPdfExporter.exportWeeklyReport(context, state, uri) }
                .onSuccess { Toast.makeText(context, "PDF exportado", Toast.LENGTH_SHORT).show() }
                .onFailure { Toast.makeText(context, "Não consegui exportar o PDF", Toast.LENGTH_SHORT).show() }
        }
    }
    ScreenColumn {
        SimpleHero("Relatório", "Resumo semanal pronto para copiar ou exportar em PDF.", R.drawable.ic_nav_chart, listOf(Color(0xFF263494), Success))
        ElevatedPanel {
            Text(report, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 22.sp)
            Spacer(Modifier.height(12.dp))
            Button(onClick = { copyToClipboard(context, "Relatório HabitApp", report) }, modifier = Modifier.fillMaxWidth()) {
                Text("Copiar relatório")
            }
            OutlinedButton(onClick = { pdfLauncher.launch("habitapp-relatorio-${System.currentTimeMillis()}.pdf") }, modifier = Modifier.fillMaxWidth()) {
                Text("Exportar PDF")
            }
        }
    }
}

@Composable
private fun BackupScreen(state: HabitDashboardState, actions: HabitActions) {
    val context = LocalContext.current
    var importText by remember { mutableStateOf("") }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            writeTextToUri(context, uri, actions.exportBackupJson())
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            readTextFromUri(context, uri)?.let {
                importText = it
                actions.importBackup(it)
            }
        }
    }
    ScreenColumn {
        SimpleHero("Backup", "Exporte, restaure ou copie os dados locais do app.", R.drawable.ic_backup, listOf(Study, Water))
        ElevatedPanel {
            Text("Prévia", fontWeight = FontWeight.Black)
            Text(state.backupPreview.ifBlank { "Sem dados para exportar." }, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 8, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(12.dp))
            Button(onClick = { copyToClipboard(context, "Backup HabitApp", actions.exportBackup()) }, modifier = Modifier.fillMaxWidth()) {
                Text("Copiar backup completo")
            }
            OutlinedButton(onClick = { exportLauncher.launch("habitapp-backup-${System.currentTimeMillis()}.json") }, modifier = Modifier.fillMaxWidth()) {
                Text("Exportar arquivo JSON")
            }
        }
        ElevatedPanel {
            OutlinedTextField(importText, { importText = it }, label = { Text("Cole o backup aqui") }, minLines = 4, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            Button(onClick = { actions.importBackup(importText) }, modifier = Modifier.fillMaxWidth()) {
                Text("Restaurar backup")
            }
            OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/*")) }, modifier = Modifier.fillMaxWidth()) {
                Text("Importar arquivo")
            }
        }
    }
}

@Composable
private fun DiaryScreen(state: HabitDashboardState, actions: HabitActions) {
    var note by remember(state.diaryNote) { mutableStateOf(state.diaryNote) }
    ScreenColumn {
        SimpleHero("Diário", "Uma nota curta para fechar o dia.", R.drawable.ic_premium_book, listOf(Coral, Warning))
        ElevatedPanel {
            OutlinedTextField(note, { note = it.take(800) }, label = { Text("Nota do dia") }, minLines = 6, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            Button(onClick = { actions.saveDiary(note) }, modifier = Modifier.fillMaxWidth()) { Text("Salvar diário") }
        }
    }
}

@Composable
private fun AppearanceScreen(state: HabitDashboardState, actions: HabitActions, navigate: (String) -> Unit) {
    ScreenColumn {
        SimpleHero(
            "Aparência",
            "Modo ${if (state.darkMode) "escuro" else "claro"} com tema ${state.accentTheme}.",
            R.drawable.ic_theme_palette,
            listOf(Color(0xFF263494), Study),
        )
        ElevatedPanel {
            SettingSwitch("Modo claro", "Visual limpo para usar durante o dia.", !state.darkMode) {
                actions.setTheme(!it, state.accentTheme)
            }
            SettingSwitch("Modo escuro", "Menos brilho para usar à noite.", state.darkMode) {
                actions.setTheme(it, state.accentTheme)
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = { navigate(HabitRoute.Themes.route) }, modifier = Modifier.fillMaxWidth()) {
                Text("Abrir loja de temas")
            }
        }
    }
}

@Composable
private fun ThemesScreen(state: HabitDashboardState, actions: HabitActions, navigate: (String) -> Unit) {
    ScreenColumn {
        SimpleHero(
            "Loja de temas",
            "Desbloqueie estilos conforme ganha XP.",
            R.drawable.ic_theme_palette,
            listOf(Warning, Success),
        )
        state.themeOptions.forEach { option ->
            ElevatedPanel {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(
                        text = when {
                            option.selected -> "Ativo"
                            option.unlocked -> "Liberado"
                            else -> "Nível ${option.requiredLevel}"
                        },
                        color = if (option.unlocked) Success else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(option.name, fontWeight = FontWeight.Black)
                        Text(
                            if (option.unlocked) "Toque para usar este estilo." else "Ganhe mais XP para desbloquear.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Button(onClick = { actions.selectTheme(option.name) }, enabled = option.unlocked) {
                        Text(if (option.selected) "Atual" else "Usar")
                    }
                }
            }
        }
        TextButton(onClick = { navigate(HabitRoute.Appearance.route) }, modifier = Modifier.fillMaxWidth()) {
            Text("Voltar para aparência")
        }
    }
}

@Composable
private fun ChallengesScreen(state: HabitDashboardState, actions: HabitActions) {
    ScreenColumn {
        SimpleHero("Desafios", "Ciclos de consistência para manter ritmo.", R.drawable.ic_premium_fire, listOf(Coral, Study))
        ElevatedPanel {
            Text("Dia ${state.challengeDay} de ${state.challengeGoalDays}", fontSize = 24.sp, fontWeight = FontWeight.Black)
            LinearProgressIndicator(
                progress = { (state.challengeDay.toFloat() / state.challengeGoalDays).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(100)),
                color = Coral,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(7, 14, 30, 60).forEach { days ->
                    FilterChip(selected = state.challengeGoalDays == days, onClick = { actions.setChallengeGoal(days) }, label = { Text("$days dias") })
                }
            }
        }
    }
}

@Composable
private fun StatsScreen(state: HabitDashboardState) {
    ScreenColumn {
        SimpleHero("Estatísticas", "Leitura avançada de água, foco, XP e score.", R.drawable.ic_premium_equalizer, listOf(Water, Study))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("XP", state.xp.toString(), (state.xp % 500) * 100 / 500, Warning, Modifier.weight(1f))
            MetricCard("Nível", state.xpLevel.toString(), state.weeklyAverage, Success, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("Água", "${state.waterMl} ml", state.waterPercent, Water, Modifier.weight(1f))
            MetricCard("Foco", "${state.focusMinutes} min", state.focusPercent, Study, Modifier.weight(1f))
        }
        ElevatedPanel {
            ScreenSection("Tendência mensal", "Últimos 30 dias em barras compactas.")
            WeekBars(state.monthScores.takeLast(7))
        }
    }
}

@Composable
private fun ChecklistCard(state: HabitDashboardState, actions: HabitActions) {
    ElevatedPanel {
        val items = listOf("Planejamento" to "Definir a prioridade do dia.", "Movimento" to "Mover o corpo por alguns minutos.", "Sono" to "Proteger a rotina de descanso.")
        items.forEachIndexed { index, item ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = state.checklist.getOrElse(index) { false }, onCheckedChange = { actions.setChecklist(index, it) })
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.first, fontWeight = FontWeight.Bold)
                    Text(item.second, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun MoodEnergyCard(state: HabitDashboardState, actions: HabitActions) {
    ElevatedPanel {
        Text("Humor: ${levelLabel(state.mood)}", fontWeight = FontWeight.Bold)
        LevelChips(selected = state.mood, onSelect = actions.setMood)
        Spacer(Modifier.height(10.dp))
        Text("Energia: ${levelLabel(state.energy)}", fontWeight = FontWeight.Bold)
        LevelChips(selected = state.energy, onSelect = actions.setEnergy)
    }
}

@Composable
private fun AvatarPreview(path: String) {
    val bitmap = remember(path) {
        if (path.isBlank()) null else runCatching { BitmapFactory.decodeFile(path) }.getOrNull()
    }
    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Foto do perfil",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                painterResource(R.drawable.ic_nav_profile),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(34.dp),
            )
        }
    }
}

@Composable
private fun LevelChips(selected: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        listOf("Baixo", "Ok", "Alto").forEachIndexed { index, label ->
            FilterChip(selected = selected == index, onClick = { onSelect(index) }, label = { Text(label) })
        }
    }
}

@Composable
private fun HabitRow(
    habit: HabitUiState,
    onToggle: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onRemove: (() -> Unit)? = null,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = if (habit.done) Success.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (habit.done) Success else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center,
            ) {
                Text(if (habit.done) "OK" else "+", color = if (habit.done) Color.White else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(habit.name, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(habit.description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text(habit.category) })
                    AssistChip(onClick = {}, label = { Text("${habit.streak}d") })
                    AssistChip(onClick = {}, label = { Text("${habit.weekPercent}%") })
                }
            }
            if (onEdit != null) {
                IconButton(onClick = onEdit) {
                    Icon(painterResource(R.drawable.ic_edit), contentDescription = "Editar")
                }
            }
            if (onRemove != null) {
                IconButton(onClick = onRemove) {
                    Icon(painterResource(R.drawable.ic_delete), contentDescription = "Remover")
                }
            }
        }
    }
}

@Composable
private fun LogList(items: List<String>, emptyText: String) {
    ElevatedPanel {
        if (items.isEmpty()) {
            Text(emptyText, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            items.take(8).forEachIndexed { index, item ->
                Text(item, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                if (index != items.lastIndex) HorizontalDivider(Modifier.padding(vertical = 10.dp))
            }
        }
    }
}

@Composable
private fun WeekBars(scores: List<Int>) {
    Row(
        modifier = Modifier.fillMaxWidth().height(156.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        scores.forEachIndexed { index, score ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(maxOf(18, score).dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, Success))),
                )
                Spacer(Modifier.height(8.dp))
                Text(if (index == scores.lastIndex) "Hoje" else "-${scores.lastIndex - index}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun MoreAction(route: HabitRoute, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            RoundIcon(icon = route.icon, tint = MaterialTheme.colorScheme.primary, background = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(route.label, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black)
                Text(subtitleFor(route), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
            Text(">", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, fontSize = 18.sp)
        }
    }
}

@Composable
private fun GoalInput(label: String, value: String, onChange: (String) -> Unit, onSave: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(value, onChange, label = { Text(label) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f))
        Button(onClick = onSave) { Text("Salvar") }
    }
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun SettingSwitch(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun HistoryRow(label: String, score: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, modifier = Modifier.width(72.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        LinearProgressIndicator(progress = { score / 100f }, modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(100)), color = scoreColor(score))
        Text("$score%", modifier = Modifier.width(52.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

@Composable
private fun SimpleHero(title: String, subtitle: String, icon: Int, colors: List<Color>) {
    HeroCard(title = title, subtitle = subtitle, icon = icon, gradient = colors) {
        Text(progressLine(title), color = Color.White.copy(alpha = 0.84f), lineHeight = 20.sp)
    }
}

@Composable
private fun SmallLabel(text: String) {
    Text(
        text = text.uppercase(Locale.ROOT),
        color = MaterialTheme.colorScheme.primary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier.padding(top = 4.dp, start = 2.dp),
    )
}

@Composable
private fun ScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content,
    )
}

private val moreItems = listOf(
    HabitRoute.Water,
    HabitRoute.Routine,
    HabitRoute.Goals,
    HabitRoute.Profile,
    HabitRoute.Settings,
    HabitRoute.History,
    HabitRoute.Calendar,
    HabitRoute.Achievements,
    HabitRoute.Missions,
    HabitRoute.Report,
    HabitRoute.Backup,
    HabitRoute.Diary,
    HabitRoute.Appearance,
    HabitRoute.Themes,
    HabitRoute.Challenges,
    HabitRoute.Stats,
)

private fun subtitleFor(route: HabitRoute): String = when (route) {
    HabitRoute.Water -> "Meta diária, histórico e ajuste em ml."
    HabitRoute.Routine -> "Blocos do dia, período e plano rápido."
    HabitRoute.Goals -> "Água, foco e tempo de sessão."
    HabitRoute.Profile -> "Nome, e-mail e objetivo."
    HabitRoute.Settings -> "Tema escuro e notificações."
    HabitRoute.History -> "Registros e scores recentes."
    HabitRoute.Calendar -> "Mapa de desempenho mensal."
    HabitRoute.Achievements -> "Marcos desbloqueados."
    HabitRoute.Missions -> "Tarefas diárias com XP."
    HabitRoute.Report -> "Resumo semanal copiável."
    HabitRoute.Backup -> "Exportar e restaurar dados."
    HabitRoute.Diary -> "Nota rápida do dia."
    HabitRoute.Appearance -> "Alternar claro/escuro."
    HabitRoute.Themes -> "Temas desbloqueados por XP."
    HabitRoute.Challenges -> "Ciclos de consistência."
    HabitRoute.Stats -> "Leitura avançada do progresso."
    else -> "Abrir tela."
}

private fun greeting(name: String): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val prefix = when {
        hour < 12 -> "Bom dia"
        hour < 18 -> "Boa tarde"
        else -> "Boa noite"
    }
    return "$prefix, $name"
}

private fun nextRoute(state: HabitDashboardState): String = when {
    state.waterPercent < 100 -> HabitRoute.Water.route
    state.focusPercent < 100 -> HabitRoute.Focus.route
    state.habits.any { !it.done } -> HabitRoute.Habits.route
    else -> HabitRoute.Progress.route
}

private fun nextActionText(state: HabitDashboardState): String = when {
    state.waterPercent < 100 -> "Registrar água"
    state.focusPercent < 100 -> "Abrir foco"
    state.habits.any { !it.done } -> "Concluir hábitos"
    else -> "Ver progresso"
}

private fun progressInsight(state: HabitDashboardState): String = when {
    state.score >= 95 -> "Dia praticamente fechado. Mantenha o ritmo e evite inventar complexidade."
    state.waterPercent < 70 -> "Seu painel melhora rápido com um copo de água agora."
    state.focusPercent < 70 -> "Um bloco curto de foco é suficiente para puxar o score para cima."
    state.habits.any { !it.done } -> "Escolha um hábito pendente e registre uma vitória pequena."
    state.checklistDone < 3 -> "Feche o checklist para transformar intenção em rotina."
    else -> "O dia está bem encaminhado. Use o progresso para repetir amanhã."
}

private fun formatLiters(ml: Int): String = String.format(Locale.getDefault(), "%.2f L", ml / 1000f)

private fun formatTimer(ms: Long): String {
    val seconds = (ms / 1000).toInt()
    return "%02d:%02d".format(seconds / 60, seconds % 60)
}

private fun levelLabel(value: Int): String = when (value) {
    0 -> "baixo"
    1 -> "ok"
    2 -> "alto"
    else -> "sem registro"
}

private fun scoreColor(score: Int): Color = when {
    score >= 80 -> Success
    score >= 50 -> Warning
    score > 0 -> Coral
    else -> Color(0xFF64748B)
}

private fun progressLine(title: String): String = when (title) {
    "Metas" -> "Metas claras deixam o app mais previsível e o dia mais fácil de cumprir."
    "Perfil" -> "Essas informações personalizam o painel e os lembretes."
    "Configurações" -> "Preferências ficam salvas localmente no aparelho."
    "Backup" -> "O backup usa os dados locais atuais do aplicativo."
    else -> "Tudo aqui reflete dados reais salvos no app."
}

private fun buildReport(state: HabitDashboardState): String = buildString {
    appendLine("Relatório HabitApp")
    appendLine("Score hoje: ${state.score}%")
    appendLine("Média semanal: ${state.weeklyAverage}%")
    appendLine("Água: ${state.waterMl}/${state.waterGoalMl} ml")
    appendLine("Foco: ${state.focusMinutes}/${state.focusGoalMinutes} min")
    appendLine("Hábitos: ${state.habits.count { it.done }}/${state.habits.size}")
    appendLine("Checklist: ${state.checklistDone}/3")
    appendLine("Nível: ${state.levelName} | XP: ${state.xp}")
    appendLine("Insight: ${progressInsight(state)}")
}

private fun saveAvatarFromUri(context: Context, uri: Uri): String? {
    return runCatching {
        val avatar = File(context.filesDir, "profile_avatar.img")
        context.contentResolver.openInputStream(uri)?.use { input ->
            avatar.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        avatar.absolutePath
    }.getOrNull()
}

private fun writeTextToUri(context: Context, uri: Uri, text: String) {
    runCatching {
        context.contentResolver.openOutputStream(uri)?.use { output ->
            output.write(text.toByteArray(Charsets.UTF_8))
        }
    }.onSuccess {
        Toast.makeText(context, "Arquivo exportado", Toast.LENGTH_SHORT).show()
    }.onFailure {
        Toast.makeText(context, "Não consegui exportar o arquivo", Toast.LENGTH_SHORT).show()
    }
}

private fun readTextFromUri(context: Context, uri: Uri): String? {
    return runCatching {
        context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
    }.onFailure {
        Toast.makeText(context, "Não consegui ler o arquivo", Toast.LENGTH_SHORT).show()
    }.getOrNull()
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    manager.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "$label copiado", Toast.LENGTH_SHORT).show()
}
