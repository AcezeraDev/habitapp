package com.exemple.habitapp.ui.compose

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HabitAppRoot(viewModel: HabitMainViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val timer by viewModel.timer.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val navController = rememberNavController()
    val actions = remember(viewModel) {
        HabitActions(
            addWater = viewModel::addWater,
            undoWater = viewModel::undoWater,
            setWaterGoal = viewModel::setWaterGoal,
            setFocusGoal = viewModel::setFocusGoal,
            addManualFocus = viewModel::addManualFocus,
            setSessionMinutes = viewModel::setSessionMinutes,
            toggleTimer = viewModel::toggleTimer,
            resetTimer = viewModel::resetTimer,
            setChecklist = viewModel::setChecklist,
            setMood = viewModel::setMood,
            setEnergy = viewModel::setEnergy,
            addHabit = viewModel::addHabit,
            toggleHabit = viewModel::toggleHabit,
            removeHabit = viewModel::removeHabit,
            saveProfile = viewModel::saveProfile,
            setTheme = viewModel::setTheme,
            setNotificationSettings = viewModel::setNotificationSettings,
            saveDiary = viewModel::saveDiary,
            setChallengeGoal = viewModel::setChallengeGoal,
            claimMissionXp = viewModel::claimMissionXp,
            importBackup = viewModel::importBackup,
            exportBackup = viewModel::exportBackup,
        )
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    HabitTheme(darkTheme = state.darkMode) {
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
                NavHost(navController = navController, startDestination = HabitRoute.Home.route) {
                    composable(HabitRoute.Home.route) { HomeScreen(state, actions, navController::navigate) }
                    composable(HabitRoute.Water.route) { WaterScreen(state, actions) }
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
                    composable(HabitRoute.Challenges.route) { ChallengesScreen(state, actions) }
                    composable(HabitRoute.Stats.route) { StatsScreen(state) }
                }
            }
        }
    }
}

@Stable
private data class HabitActions(
    val addWater: (Int) -> Unit,
    val undoWater: () -> Unit,
    val setWaterGoal: (Int) -> Unit,
    val setFocusGoal: (Int) -> Unit,
    val addManualFocus: (Int) -> Unit,
    val setSessionMinutes: (Int) -> Unit,
    val toggleTimer: () -> Unit,
    val resetTimer: () -> Unit,
    val setChecklist: (Int, Boolean) -> Unit,
    val setMood: (Int) -> Unit,
    val setEnergy: (Int) -> Unit,
    val addHabit: (String) -> Unit,
    val toggleHabit: (String) -> Unit,
    val removeHabit: (String) -> Unit,
    val saveProfile: (String, String, String, String, String) -> Unit,
    val setTheme: (Boolean, String) -> Unit,
    val setNotificationSettings: (Boolean, Boolean, Boolean) -> Unit,
    val saveDiary: (String) -> Unit,
    val setChallengeGoal: (Int) -> Unit,
    val claimMissionXp: () -> Unit,
    val importBackup: (String) -> Unit,
    val exportBackup: () -> String,
)

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
                    value = goalText,
                    onValueChange = { goalText = it.filter(Char::isDigit).take(4) },
                    label = { Text("Meta em ml") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = { actions.setWaterGoal(goalText.toIntOrNull() ?: 0) }) { Text("Salvar") }
            }
        }

        ScreenSection("Registros de hoje", "Últimos lançamentos de hidratação.")
        LogList(state.waterLog, emptyText = "Nenhum registro de água hoje.")
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

    ScreenColumn {
        HeroCard(
            title = "Hábitos",
            subtitle = "${state.habits.count { it.done }} de ${state.habits.size} concluídos hoje",
            icon = R.drawable.ic_nav_goals,
            gradient = listOf(Color(0xFF16A34A), Color(0xFF14B8A6), Color(0xFF4353D8)),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = newHabit,
                    onValueChange = { newHabit = it.take(42) },
                    label = { Text("Novo hábito") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = {
                    actions.addHabit(newHabit)
                    newHabit = ""
                }) { Text("Criar") }
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
                pending.forEach { habit -> HabitRow(habit, { actions.toggleHabit(habit.name) }, { actions.removeHabit(habit.name) }) }
            }
            if (done.isNotEmpty()) {
                SmallLabel("Concluídos")
                done.forEach { habit -> HabitRow(habit, { actions.toggleHabit(habit.name) }, { actions.removeHabit(habit.name) }) }
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
    var name by remember(state.name) { mutableStateOf(state.name) }
    var email by remember(state.email) { mutableStateOf(state.email) }
    var objective by remember(state.objective) { mutableStateOf(state.objective) }
    var routine by remember(state.routine) { mutableStateOf(state.routine) }
    var period by remember(state.bestPeriod) { mutableStateOf(state.bestPeriod) }

    ScreenColumn {
        SimpleHero("Perfil", "Dados pessoais e contexto da rotina.", R.drawable.ic_nav_profile, listOf(Color(0xFF4353D8), Water))
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
        }
    }
}

@Composable
private fun SettingsScreen(state: HabitDashboardState, actions: HabitActions) {
    var dark by remember(state.darkMode) { mutableStateOf(state.darkMode) }
    var accent by remember(state.accentTheme) { mutableStateOf(state.accentTheme) }
    var notifications by remember(state.notificationsEnabled) { mutableStateOf(state.notificationsEnabled) }
    var waterReminder by remember(state.waterReminderEnabled) { mutableStateOf(state.waterReminderEnabled) }
    var focusReminder by remember(state.focusReminderEnabled) { mutableStateOf(state.focusReminderEnabled) }

    ScreenColumn {
        SimpleHero("Configurações", "Tema, lembretes e preferências persistidas.", R.drawable.ic_notification, listOf(Study, Coral))
        ElevatedPanel {
            SettingSwitch("Modo escuro", "Alterna o tema Material 3.", dark) {
                dark = it
                actions.setTheme(dark, accent)
            }
            OutlinedTextField(accent, { accent = it.take(24) }, label = { Text("Tema/acento") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Button(onClick = { actions.setTheme(dark, accent) }, modifier = Modifier.fillMaxWidth()) { Text("Salvar tema") }
            HorizontalDivider(Modifier.padding(vertical = 14.dp))
            SettingSwitch("Notificações", "Liga ou desliga lembretes do app.", notifications) {
                notifications = it
                actions.setNotificationSettings(notifications, waterReminder, focusReminder)
            }
            SettingSwitch("Lembrete de água", "Sugestão diária de hidratação.", waterReminder) {
                waterReminder = it
                actions.setNotificationSettings(notifications, waterReminder, focusReminder)
            }
            SettingSwitch("Lembrete de foco", "Sugestão diária de Pomodoro.", focusReminder) {
                focusReminder = it
                actions.setNotificationSettings(notifications, waterReminder, focusReminder)
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
    ScreenColumn {
        SimpleHero("Relatório", "Resumo semanal pronto para copiar.", R.drawable.ic_nav_chart, listOf(Color(0xFF263494), Success))
        ElevatedPanel {
            Text(report, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 22.sp)
            Spacer(Modifier.height(12.dp))
            Button(onClick = { copyToClipboard(context, "Relatório HabitApp", report) }, modifier = Modifier.fillMaxWidth()) {
                Text("Copiar relatório")
            }
        }
    }
}

@Composable
private fun BackupScreen(state: HabitDashboardState, actions: HabitActions) {
    val context = LocalContext.current
    var importText by remember { mutableStateOf("") }
    ScreenColumn {
        SimpleHero("Backup", "Copie ou restaure dados locais do app.", R.drawable.ic_backup, listOf(Study, Water))
        ElevatedPanel {
            Text("Prévia", fontWeight = FontWeight.Black)
            Text(state.backupPreview.ifBlank { "Sem dados para exportar." }, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 8, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(12.dp))
            Button(onClick = { copyToClipboard(context, "Backup HabitApp", actions.exportBackup()) }, modifier = Modifier.fillMaxWidth()) {
                Text("Copiar backup completo")
            }
        }
        ElevatedPanel {
            OutlinedTextField(importText, { importText = it }, label = { Text("Cole o backup aqui") }, minLines = 4, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            Button(onClick = { actions.importBackup(importText) }, modifier = Modifier.fillMaxWidth()) {
                Text("Restaurar backup")
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
private fun LevelChips(selected: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        listOf("Baixo", "Ok", "Alto").forEachIndexed { index, label ->
            FilterChip(selected = selected == index, onClick = { onSelect(index) }, label = { Text(label) })
        }
    }
}

@Composable
private fun HabitRow(habit: HabitUiState, onToggle: () -> Unit, onRemove: (() -> Unit)?) {
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
    HabitRoute.Challenges,
    HabitRoute.Stats,
)

private fun subtitleFor(route: HabitRoute): String = when (route) {
    HabitRoute.Water -> "Meta diária, histórico e ajuste em ml."
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

private fun copyToClipboard(context: Context, label: String, text: String) {
    val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    manager.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "$label copiado", Toast.LENGTH_SHORT).show()
}
