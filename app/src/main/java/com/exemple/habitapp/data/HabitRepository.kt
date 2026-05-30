package com.exemple.habitapp.data

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val DAY_MILLIS = 1000L * 60L * 60L * 24L
private const val ONBOARDING_VERSION = 4

data class HabitDashboardState(
    val loggedIn: Boolean = false,
    val dailySetupComplete: Boolean = false,
    val name: String = "Guerreiro",
    val email: String = "",
    val objective: String = "Mais disciplina",
    val routine: String = "Equilibrada",
    val bestPeriod: String = "hoje",
    val avatarPath: String = "",
    val score: Int = 0,
    val streak: Int = 0,
    val weeklyAverage: Int = 0,
    val levelName: String = "Inicial",
    val xpLevel: Int = 1,
    val xp: Int = 0,
    val xpToNext: Int = 500,
    val waterMl: Int = 0,
    val waterGoalMl: Int = 2000,
    val waterPercent: Int = 0,
    val cupsLeft: Int = 8,
    val focusMinutes: Int = 0,
    val focusGoalMinutes: Int = 60,
    val focusPercent: Int = 0,
    val focusSessions: Int = 0,
    val sessionMinutes: Int = 25,
    val checklistDone: Int = 0,
    val checklist: List<Boolean> = listOf(false, false, false),
    val routinePeriod: String = "Manhã",
    val routineBlocks: List<Boolean> = listOf(false, false, false, false),
    val routinePercent: Int = 0,
    val routinePlan: String = "",
    val mood: Int = -1,
    val energy: Int = -1,
    val habits: List<HabitUiState> = emptyList(),
    val weekScores: List<Int> = List(7) { 0 },
    val monthScores: List<Int> = List(30) { 0 },
    val waterLog: List<String> = emptyList(),
    val focusLog: List<String> = emptyList(),
    val diaryNote: String = "",
    val challengeDay: Int = 1,
    val challengeGoalDays: Int = 30,
    val darkMode: Boolean = false,
    val accentTheme: String = "Clássico",
    val notificationsEnabled: Boolean = true,
    val waterReminderEnabled: Boolean = true,
    val focusReminderEnabled: Boolean = true,
    val routineReminderEnabled: Boolean = true,
    val waterReminderTime: String = "10:00",
    val waterReminderIntervalHours: Int = 2,
    val focusReminderTime: String = "16:30",
    val routineReminderTime: String = "21:00",
    val themeOptions: List<ThemeOptionUiState> = emptyList(),
    val achievements: List<AchievementUiState> = emptyList(),
    val missions: List<MissionUiState> = emptyList(),
    val totalWaterMl: Int = 0,
    val totalFocusMinutes: Int = 0,
    val backupPreview: String = "",
)

data class HabitUiState(
    val name: String,
    val description: String,
    val category: String,
    val frequency: String,
    val time: String,
    val colorName: String,
    val iconName: String,
    val done: Boolean,
    val streak: Int,
    val weekPercent: Int,
)

data class AchievementUiState(
    val title: String,
    val description: String,
    val unlocked: Boolean,
)

data class MissionUiState(
    val title: String,
    val description: String,
    val xp: Int,
    val done: Boolean,
)

data class ThemeOptionUiState(
    val name: String,
    val requiredLevel: Int,
    val unlocked: Boolean,
    val selected: Boolean,
)

data class HabitDraft(
    val name: String,
    val description: String,
    val category: String,
    val frequency: String = "Diário",
    val time: String = "",
    val colorName: String = "Azul",
    val iconName: String = "Livro",
)

class HabitRepository(context: Context) {
    private val appContext = context.applicationContext
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences("habit_data", Context.MODE_PRIVATE)

    fun snapshot(): HabitDashboardState {
        ensureToday()
        saveTodaySnapshot()

        val today = todayKey()
        val waterMl = getWaterMl()
        val waterGoalMl = max(1, getWaterGoalMl())
        val focusMinutes = prefs.getInt("estudos_concluidos_min", 0)
        val focusGoalMinutes = max(1, prefs.getInt("meta_estudos_min", 60))
        val routineBlocks = getRoutineBlocks(today)
        val routineDone = routineBlocks.count { it }
        val routinePeriod = prefs.getString("rotina_periodo_principal", prefs.getString("melhor_horario", "Manhã"))
            .orEmpty()
            .ifBlank { "Manhã" }
        val checklist = listOf(
            prefs.getBoolean("check_planejamento_$today", false),
            prefs.getBoolean("check_treino_$today", false),
            prefs.getBoolean("check_sono_$today", false),
        )
        val habits = getHabitRecords()
        val weekScores = (6 downTo 0).map { getScoreForDay(dayKey(-it)) }
        val monthScores = (29 downTo 0).map { getScoreForDay(dayKey(-it)) }
        val xp = getXp()
        val score = getTodayScore()
        val level = max(1, xp / 500 + 1)
        val accent = prefs.getString("theme_accent_name", "Clássico").orEmpty().ifBlank { "Clássico" }

        return HabitDashboardState(
            loggedIn = prefs.getBoolean("perfil_logado", false),
            dailySetupComplete = prefs.getLong("daily_setup_day", -1) == today && prefs.getInt("onboarding_version", 0) >= ONBOARDING_VERSION,
            name = prefs.getString("nome_usuario", "Guerreiro").orEmpty().ifBlank { "Guerreiro" },
            email = prefs.getString("email_usuario", "").orEmpty(),
            objective = prefs.getString("objetivo_principal", "Mais disciplina").orEmpty().ifBlank { "Mais disciplina" },
            routine = prefs.getString("ritmo_rotina", "Equilibrada").orEmpty().ifBlank { "Equilibrada" },
            bestPeriod = prefs.getString("melhor_horario", "hoje").orEmpty().ifBlank { "hoje" },
            avatarPath = prefs.getString("perfil_foto_path", "").orEmpty(),
            score = score,
            streak = getStreak(),
            weeklyAverage = weekScores.average().roundToInt(),
            levelName = getLevelName(),
            xpLevel = level,
            xp = xp,
            xpToNext = 500 - (xp % 500).let { if (it == 0) 0 else it },
            waterMl = waterMl,
            waterGoalMl = waterGoalMl,
            waterPercent = percent(waterMl, waterGoalMl),
            cupsLeft = ceil(max(0, waterGoalMl - waterMl) / 250.0).roundToInt(),
            focusMinutes = focusMinutes,
            focusGoalMinutes = focusGoalMinutes,
            focusPercent = percent(focusMinutes, focusGoalMinutes),
            focusSessions = prefs.getInt("sessoes_foco_concluidas", 0),
            sessionMinutes = prefs.getInt("foco_minutos", 25).coerceIn(5, 180),
            checklistDone = checklist.count { it },
            checklist = checklist,
            routinePeriod = routinePeriod,
            routineBlocks = routineBlocks,
            routinePercent = percent(routineDone, 4),
            routinePlan = routinePlan(routinePeriod, routineDone),
            mood = prefs.getInt("mood_$today", -1),
            energy = prefs.getInt("energy_$today", -1),
            habits = habits,
            weekScores = weekScores,
            monthScores = monthScores,
            waterLog = readLog(todayWaterLogKey()),
            focusLog = readLog(todayFocusLogKey()),
            diaryNote = prefs.getString("diary_note_$today", "").orEmpty(),
            challengeDay = prefs.getInt("challenge_day", 1),
            challengeGoalDays = prefs.getInt("challenge_goal_days", 30),
            darkMode = prefs.getBoolean("theme_dark_mode", false),
            accentTheme = accent,
            notificationsEnabled = prefs.getBoolean("notifications_enabled", true),
            waterReminderEnabled = prefs.getBoolean("reminder_water_enabled", prefs.getBoolean("water_reminder_enabled", true)),
            focusReminderEnabled = prefs.getBoolean("reminder_focus_enabled", prefs.getBoolean("focus_reminder_enabled", true)),
            routineReminderEnabled = prefs.getBoolean("reminder_routine_enabled", true),
            waterReminderTime = formatTime(prefs.getInt("reminder_water_start_hour", 10), prefs.getInt("reminder_water_start_minute", 0)),
            waterReminderIntervalHours = prefs.getInt("reminder_water_interval_hours", 2).coerceIn(1, 8),
            focusReminderTime = formatTime(prefs.getInt("reminder_focus_hour", 16), prefs.getInt("reminder_focus_minute", 30)),
            routineReminderTime = formatTime(prefs.getInt("reminder_routine_hour", 21), prefs.getInt("reminder_routine_minute", 0)),
            themeOptions = themeOptions(level, accent),
            achievements = achievements(score, weekScores, habits),
            missions = missions(waterMl, waterGoalMl, focusMinutes, focusGoalMinutes, checklist.count { it }, habits),
            totalWaterMl = prefs.getInt("total_agua_ml_registrado", 0),
            totalFocusMinutes = prefs.getInt("total_foco_min_registrado", 0),
            backupPreview = exportBackupText(limitKeys = 12),
        )
    }

    fun login(name: String, email: String, password: String): Boolean {
        val cleanName = name.trim()
        val cleanEmail = email.trim()
        if (cleanName.isBlank() || !isValidEmail(cleanEmail)) return false
        if (password.isNotBlank() && password.length < 4) return false
        prefs.edit()
            .putBoolean("perfil_logado", true)
            .putString("nome_usuario", cleanName)
            .putString("email_usuario", cleanEmail)
            .putBoolean("perfil_tem_senha", password.isNotBlank())
            .apply()
        return true
    }

    fun completeDailySetup(
        name: String,
        objective: String,
        routine: String,
        bestPeriod: String,
        currentWaterMl: Int,
        waterGoalMl: Int,
        focusGoalMinutes: Int,
        sessionMinutes: Int,
    ): Boolean {
        if (currentWaterMl < 0 || waterGoalMl !in 500..8000 || focusGoalMinutes !in 5..480 || sessionMinutes !in 5..180) {
            return false
        }
        prefs.edit()
            .putBoolean("perfil_logado", true)
            .putString("nome_usuario", name.ifBlank { "Guerreiro" })
            .putString("objetivo_principal", objective.ifBlank { "Mais disciplina" })
            .putString("ritmo_rotina", routine.ifBlank { "Equilibrada" })
            .putString("melhor_horario", bestPeriod.ifBlank { "Manhã" })
            .putString("rotina_periodo_principal", bestPeriod.ifBlank { "Manhã" })
            .putFloat("agua_litros", currentWaterMl / 1000f)
            .putFloat("meta_litros", waterGoalMl / 1000f)
            .putInt("meta_estudos_min", focusGoalMinutes)
            .putInt("foco_minutos", sessionMinutes)
            .putInt("onboarding_version", ONBOARDING_VERSION)
            .putLong("daily_setup_day", todayKey())
            .apply()
        ensureStarterHabits(objective)
        commitSnapshot()
        return true
    }

    fun addWater(amountMl: Int): Int {
        if (amountMl <= 0) return 0
        val waterMl = getWaterMl()
        val goalMl = getWaterGoalMl()
        if (waterMl >= goalMl) return 0

        val added = min(amountMl, goalMl - waterMl)
        val total = prefs.getInt("total_agua_ml_registrado", 0) + added
        prefs.edit()
            .putFloat("agua_litros", (waterMl + added) / 1000f)
            .putInt("total_agua_ml_registrado", total)
            .putString(todayWaterLogKey(), appendLog(todayWaterLogKey(), "${timeNow()} - $added ml"))
            .apply()
        commitSnapshot()
        return added
    }

    fun undoWater(): Int {
        val entries = readRawLog(todayWaterLogKey()).split("|").filter { it.isNotBlank() }
        val last = entries.lastOrNull() ?: return 0
        val removedMl = last.substringAfterLast("-").replace("ml", "").trim().toIntOrNull() ?: return 0
        val waterMl = max(0, getWaterMl() - removedMl)
        val total = max(0, prefs.getInt("total_agua_ml_registrado", 0) - removedMl)
        prefs.edit()
            .putFloat("agua_litros", waterMl / 1000f)
            .putInt("total_agua_ml_registrado", total)
            .putString(todayWaterLogKey(), entries.dropLast(1).joinToString("|"))
            .apply()
        commitSnapshot()
        return removedMl
    }

    fun resetWater() {
        prefs.edit()
            .putFloat("agua_litros", 0f)
            .remove(todayWaterLogKey())
            .apply()
        commitSnapshot()
    }

    fun setWaterGoal(goalMl: Int): Boolean {
        if (goalMl !in 500..8000) return false
        prefs.edit().putFloat("meta_litros", goalMl / 1000f).apply()
        commitSnapshot()
        return true
    }

    fun addFocus(minutes: Int, source: String = "Manual"): Boolean {
        if (minutes <= 0) return false
        prefs.edit()
            .putInt("estudos_concluidos_min", prefs.getInt("estudos_concluidos_min", 0) + minutes)
            .putInt("total_foco_min_registrado", prefs.getInt("total_foco_min_registrado", 0) + minutes)
            .putString(todayFocusLogKey(), appendLog(todayFocusLogKey(), "${timeNow()} - $source - $minutes min"))
            .apply()
        commitSnapshot()
        return true
    }

    fun completeFocusSession(minutes: Int): Boolean {
        if (minutes <= 0) return false
        prefs.edit()
            .putInt("sessoes_foco_concluidas", prefs.getInt("sessoes_foco_concluidas", 0) + 1)
            .apply()
        return addFocus(minutes, "Sessão concluída")
    }

    fun setFocusGoal(minutes: Int): Boolean {
        if (minutes !in 5..480) return false
        prefs.edit().putInt("meta_estudos_min", minutes).apply()
        commitSnapshot()
        return true
    }

    fun setSessionMinutes(minutes: Int): Boolean {
        if (minutes !in 5..180) return false
        prefs.edit().putInt("foco_minutos", minutes).apply()
        return true
    }

    fun setChecklist(index: Int, checked: Boolean) {
        val prefix = when (index) {
            0 -> "check_planejamento_"
            1 -> "check_treino_"
            else -> "check_sono_"
        }
        prefs.edit().putBoolean(prefix + todayKey(), checked).apply()
        commitSnapshot()
    }

    fun setRoutinePeriod(period: String) {
        val clean = when (normalize(period)) {
            "manha" -> "Manhã"
            "tarde" -> "Tarde"
            "noite" -> "Noite"
            else -> period.ifBlank { "Manhã" }
        }
        prefs.edit()
            .putString("rotina_periodo_principal", clean)
            .putString("melhor_horario", clean)
            .apply()
        commitSnapshot()
    }

    fun setRoutineBlock(index: Int, checked: Boolean) {
        val prefix = when (index) {
            0 -> "rotina_bloco_manha_"
            1 -> "rotina_bloco_alimentacao_"
            2 -> "rotina_bloco_treino_"
            else -> "rotina_bloco_sono_"
        }
        prefs.edit().putBoolean(prefix + todayKey(), checked).apply()
        commitSnapshot()
    }

    fun completeRoutine() {
        val today = todayKey()
        prefs.edit()
            .putBoolean("rotina_bloco_manha_$today", true)
            .putBoolean("rotina_bloco_alimentacao_$today", true)
            .putBoolean("rotina_bloco_treino_$today", true)
            .putBoolean("rotina_bloco_sono_$today", true)
            .putBoolean("check_planejamento_$today", true)
            .putBoolean("check_sono_$today", true)
            .apply()
        commitSnapshot()
    }

    fun setMood(value: Int) {
        prefs.edit().putInt("mood_${todayKey()}", value.coerceIn(0, 2)).apply()
        commitSnapshot()
    }

    fun setEnergy(value: Int) {
        prefs.edit().putInt("energy_${todayKey()}", value.coerceIn(0, 2)).apply()
        commitSnapshot()
    }

    fun addHabit(name: String): Boolean {
        val clean = sanitizeHabitName(name)
        if (clean.isBlank()) return false
        val habits = getCustomHabits().toMutableList()
        if (habits.any { it.equals(clean, ignoreCase = true) }) return false
        habits.add(clean)
        saveCustomHabits(habits)
        saveHabitMeta(clean, suggestedHabit(clean))
        commitSnapshot()
        return true
    }

    fun saveHabit(draft: HabitDraft, oldName: String? = null): Boolean {
        val clean = sanitizeHabitName(draft.name)
        if (clean.isBlank()) return false
        val habits = getCustomHabits().toMutableList()
        val oldClean = sanitizeHabitName(oldName.orEmpty())
        val existingIndex = habits.indexOfFirst { it == oldClean }
        if (existingIndex >= 0) {
            habits[existingIndex] = clean
        } else if (habits.none { it.equals(clean, ignoreCase = true) }) {
            habits.add(clean)
        }
        saveCustomHabits(habits)
        saveHabitMeta(clean, draft.copy(name = clean))
        if (oldClean.isNotBlank() && oldClean != clean) removeHabitMeta(oldClean)
        commitSnapshot()
        return true
    }

    fun toggleHabit(name: String) {
        val key = habitDoneKey(name, todayKey())
        prefs.edit().putBoolean(key, !prefs.getBoolean(key, false)).apply()
        commitSnapshot()
    }

    fun removeHabit(name: String) {
        val clean = sanitizeHabitName(name)
        saveCustomHabits(getCustomHabits().filterNot { it == clean })
        removeHabitMeta(clean)
        prefs.edit().remove(habitDoneKey(clean, todayKey())).apply()
        commitSnapshot()
    }

    fun saveProfile(name: String, email: String, objective: String, routine: String, bestPeriod: String) {
        prefs.edit()
            .putBoolean("perfil_logado", true)
            .putString("nome_usuario", name.ifBlank { "Guerreiro" })
            .putString("email_usuario", email)
            .putString("objetivo_principal", objective.ifBlank { "Mais disciplina" })
            .putString("ritmo_rotina", routine.ifBlank { "Equilibrada" })
            .putString("melhor_horario", bestPeriod.ifBlank { "hoje" })
            .apply()
        commitSnapshot()
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("theme_dark_mode", enabled).apply()
    }

    fun setAccentTheme(theme: String) {
        prefs.edit().putString("theme_accent_name", theme.ifBlank { "Clássico" }).apply()
    }

    fun selectTheme(theme: String): Boolean {
        val level = max(1, getXp() / 500 + 1)
        val selected = prefs.getString("theme_accent_name", "Clássico").orEmpty()
        val option = themeOptions(level, selected).firstOrNull { it.name.equals(theme, ignoreCase = true) } ?: return false
        if (!option.unlocked) return false
        setAccentTheme(option.name)
        return true
    }

    fun setNotificationSettings(enabled: Boolean, water: Boolean, focus: Boolean) {
        prefs.edit()
            .putBoolean("notifications_enabled", enabled)
            .putBoolean("water_reminder_enabled", water)
            .putBoolean("focus_reminder_enabled", focus)
            .putBoolean("reminder_water_enabled", water)
            .putBoolean("reminder_focus_enabled", focus)
            .apply()
    }

    fun setNotificationPlan(
        enabled: Boolean,
        water: Boolean,
        focus: Boolean,
        routine: Boolean,
        waterStart: String,
        waterIntervalHours: Int,
        focusTime: String,
        routineTime: String,
    ): Boolean {
        val waterParsed = parseTime(waterStart) ?: return false
        val focusParsed = parseTime(focusTime) ?: return false
        val routineParsed = parseTime(routineTime) ?: return false
        if (waterIntervalHours !in 1..8) return false
        prefs.edit()
            .putBoolean("notifications_enabled", enabled)
            .putBoolean("water_reminder_enabled", water)
            .putBoolean("focus_reminder_enabled", focus)
            .putBoolean("reminder_water_enabled", water)
            .putBoolean("reminder_focus_enabled", focus)
            .putBoolean("reminder_routine_enabled", routine)
            .putInt("reminder_water_start_hour", waterParsed.first)
            .putInt("reminder_water_start_minute", waterParsed.second)
            .putInt("reminder_water_interval_hours", waterIntervalHours)
            .putInt("reminder_focus_hour", focusParsed.first)
            .putInt("reminder_focus_minute", focusParsed.second)
            .putInt("reminder_routine_hour", routineParsed.first)
            .putInt("reminder_routine_minute", routineParsed.second)
            .apply()
        return true
    }

    fun saveDiary(note: String) {
        prefs.edit().putString("diary_note_${todayKey()}", note.take(800)).apply()
    }

    fun saveAvatarPath(path: String) {
        prefs.edit().putString("perfil_foto_path", path).apply()
    }

    fun removeAvatarPath() {
        prefs.edit().remove("perfil_foto_path").apply()
    }

    fun logout() {
        prefs.edit().putBoolean("perfil_logado", false).apply()
    }

    fun setChallengeGoal(days: Int): Boolean {
        if (days !in 7..90) return false
        prefs.edit().putInt("challenge_goal_days", days).apply()
        return true
    }

    fun claimMissionXp(): Int {
        if (prefs.getLong("xp_claim_day", -1) == todayKey()) return 0
        val xp = snapshot().missions.filter { it.done }.sumOf { it.xp }
        if (xp <= 0) return 0
        prefs.edit()
            .putLong("xp_claim_day", todayKey())
            .putInt("xp_claimed_total", prefs.getInt("xp_claimed_total", 0) + xp)
            .apply()
        commitSnapshot()
        return xp
    }

    fun exportBackupText(limitKeys: Int = Int.MAX_VALUE): String {
        return prefs.all.entries
            .sortedBy { it.key }
            .take(limitKeys)
            .joinToString("\n") { (key, value) -> "$key\t${value?.javaClass?.simpleName ?: "String"}\t$value" }
    }

    fun exportBackupJson(): String {
        val root = JSONObject()
        val values = JSONObject()
        root.put("app", "HabitApp")
        root.put("version", 2)
        root.put("exported_at", System.currentTimeMillis())
        prefs.all.entries.sortedBy { it.key }.forEach { (key, value) ->
            if (value != null) {
                val item = JSONObject()
                item.put("type", value.javaClass.simpleName)
                item.put("value", value)
                values.put(key, item)
            }
        }
        root.put("values", values)
        return root.toString(2)
    }

    fun importBackupText(text: String): Int {
        val trimmed = text.trim()
        if (trimmed.startsWith("{")) return importBackupJson(trimmed)
        var imported = 0
        val editor = prefs.edit()
        text.lineSequence().forEach { line ->
            val parts = line.split("\t", limit = 3)
            if (parts.size != 3) return@forEach
            val key = parts[0]
            val type = parts[1]
            val value = parts[2]
            when (type) {
                "Boolean" -> editor.putBoolean(key, value.toBooleanStrictOrNull() ?: return@forEach)
                "Integer" -> editor.putInt(key, value.toIntOrNull() ?: return@forEach)
                "Long" -> editor.putLong(key, value.toLongOrNull() ?: return@forEach)
                "Float" -> editor.putFloat(key, value.toFloatOrNull() ?: return@forEach)
                else -> editor.putString(key, value)
            }
            imported++
        }
        editor.apply()
        ensureToday()
        return imported
    }

    private fun importBackupJson(text: String): Int {
        return try {
            val values = JSONObject(text).getJSONObject("values")
            val editor = prefs.edit().clear()
            var imported = 0
            val keys = values.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val item = values.getJSONObject(key)
                when (item.getString("type")) {
                    "Boolean" -> editor.putBoolean(key, item.getBoolean("value"))
                    "Integer" -> editor.putInt(key, item.getInt("value"))
                    "Long" -> editor.putLong(key, item.getLong("value"))
                    "Float" -> editor.putFloat(key, item.getDouble("value").toFloat())
                    else -> editor.putString(key, item.optString("value", ""))
                }
                imported++
            }
            editor.apply()
            ensureToday()
            imported
        } catch (_: Exception) {
            0
        }
    }

    private fun ensureToday() {
        val today = todayKey()
        val lastActiveDay = prefs.getLong("last_active_day", -1)
        if (lastActiveDay == -1L) {
            prefs.edit().putLong("last_active_day", today).apply()
            saveTodaySnapshot()
            return
        }
        if (lastActiveDay != today) {
            saveSnapshotForDay(lastActiveDay)
            prefs.edit()
                .putFloat("agua_litros", 0f)
                .putInt("estudos_concluidos_min", 0)
                .putInt("sessoes_foco_concluidas", 0)
                .putLong("last_active_day", today)
                .apply()
            saveTodaySnapshot()
        }
    }

    private fun commitSnapshot() {
        saveTodaySnapshot()
    }

    private fun saveTodaySnapshot() {
        saveSnapshotForDay(todayKey())
    }

    private fun saveSnapshotForDay(day: Long) {
        val habits = getCustomHabits()
        val waterPercent = percent(getWaterMl(), getWaterGoalMl())
        val focusPercent = percent(prefs.getInt("estudos_concluidos_min", 0), prefs.getInt("meta_estudos_min", 60))
        val checklistPercent = percent(checklistDone(day), 3)
        val habitsPercent = if (habits.isEmpty()) 100 else percent(habits.count { prefs.getBoolean(habitDoneKey(it, day), false) }, habits.size)
        val score = if (habits.isEmpty()) {
            (waterPercent + focusPercent + checklistPercent) / 3
        } else {
            (waterPercent + focusPercent + checklistPercent + habitsPercent) / 4
        }

        prefs.edit()
            .putInt("score_day_$day", score)
            .putInt("agua_ml_day_$day", getWaterMl())
            .putInt("estudos_min_day_$day", prefs.getInt("estudos_concluidos_min", 0))
            .putInt("checklist_day_$day", checklistDone(day))
            .putInt("habitos_done_day_$day", habits.count { prefs.getBoolean(habitDoneKey(it, day), false) })
            .apply()
    }

    private fun getTodayScore(): Int = getScoreForDay(todayKey())

    private fun getScoreForDay(day: Long): Int {
        if (day != todayKey()) return prefs.getInt("score_day_$day", 0)
        val habits = getCustomHabits()
        val waterPercent = percent(getWaterMl(), getWaterGoalMl())
        val focusPercent = percent(prefs.getInt("estudos_concluidos_min", 0), prefs.getInt("meta_estudos_min", 60))
        val checklistPercent = percent(checklistDone(day), 3)
        val habitsPercent = if (habits.isEmpty()) 100 else percent(habits.count { prefs.getBoolean(habitDoneKey(it, day), false) }, habits.size)
        return if (habits.isEmpty()) {
            (waterPercent + focusPercent + checklistPercent) / 3
        } else {
            (waterPercent + focusPercent + checklistPercent + habitsPercent) / 4
        }
    }

    private fun getStreak(): Int {
        var streak = 0
        for (index in 0 until 90) {
            if (getScoreForDay(dayKey(-index)) >= 80) streak++ else break
        }
        return streak
    }

    private fun getLevelName(): String {
        val streak = getStreak()
        val average = (6 downTo 0).map { getScoreForDay(dayKey(-it)) }.average()
        return when {
            streak >= 14 && average >= 85 -> "Elite"
            streak >= 7 && average >= 75 -> "Avançado"
            streak >= 3 || average >= 60 -> "Consistente"
            else -> "Inicial"
        }
    }

    private fun getXp(): Int {
        val totalWater = prefs.getInt("total_agua_ml_registrado", 0) / 50
        val totalFocus = prefs.getInt("total_foco_min_registrado", 0) * 2
        val streak = getStreak() * 120
        val weekly = (6 downTo 0).map { getScoreForDay(dayKey(-it)) }.average().roundToInt() * 4
        val achievements = achievements(getTodayScore(), (6 downTo 0).map { getScoreForDay(dayKey(-it)) }, getHabitRecords()).count { it.unlocked } * 90
        return totalWater + totalFocus + streak + weekly + achievements + prefs.getInt("xp_claimed_total", 0)
    }

    private fun achievements(score: Int, weekScores: List<Int>, habits: List<HabitUiState>): List<AchievementUiState> {
        val waterMl = getWaterMl()
        val focus = prefs.getInt("estudos_concluidos_min", 0)
        val sessions = prefs.getInt("sessoes_foco_concluidas", 0)
        val streak = getStreak()
        return listOf(
            AchievementUiState("Primeiro passo", "Score acima de 30% no dia.", score >= 30),
            AchievementUiState("Hidratação completa", "Meta de água fechada.", waterMl >= getWaterGoalMl()),
            AchievementUiState("Foco protegido", "Meta diária de estudos concluída.", focus >= prefs.getInt("meta_estudos_min", 60)),
            AchievementUiState("Dia forte", "Score acima de 80%.", score >= 80),
            AchievementUiState("Semana estável", "Média semanal acima de 70%.", weekScores.average() >= 70),
            AchievementUiState("Três sessões", "3 sessões de foco registradas.", sessions >= 3),
            AchievementUiState("Sequência", "3 dias fortes seguidos.", streak >= 3),
            AchievementUiState("Todos os hábitos", "Todos os hábitos extras concluídos.", habits.isNotEmpty() && habits.all { it.done }),
        )
    }

    private fun missions(waterMl: Int, waterGoalMl: Int, focusMinutes: Int, focusGoalMinutes: Int, checklistDone: Int, habits: List<HabitUiState>): List<MissionUiState> {
        return listOf(
            MissionUiState("Fechar hidratação", "Bata a meta de água do dia.", 80, waterMl >= waterGoalMl),
            MissionUiState("Bloco de foco", "Conclua sua meta diária de estudo.", 100, focusMinutes >= focusGoalMinutes),
            MissionUiState("Checklist essencial", "Finalize planejamento, movimento e sono.", 70, checklistDone >= 3),
            MissionUiState("Vitória pequena", "Conclua pelo menos um hábito extra.", 50, habits.any { it.done }),
        )
    }

    private fun getWaterMl(): Int = (prefs.getFloat("agua_litros", 0f) * 1000).roundToInt()

    private fun getWaterGoalMl(): Int = max(1, (prefs.getFloat("meta_litros", 2.0f) * 1000).roundToInt())

    private fun checklistDone(day: Long): Int {
        var total = 0
        if (prefs.getBoolean("check_planejamento_$day", false)) total++
        if (prefs.getBoolean("check_treino_$day", false)) total++
        if (prefs.getBoolean("check_sono_$day", false)) total++
        return total
    }

    private fun getRoutineBlocks(day: Long): List<Boolean> = listOf(
        prefs.getBoolean("rotina_bloco_manha_$day", false),
        prefs.getBoolean("rotina_bloco_alimentacao_$day", false),
        prefs.getBoolean("rotina_bloco_treino_$day", false),
        prefs.getBoolean("rotina_bloco_sono_$day", false),
    )

    private fun routinePlan(period: String, done: Int): String {
        return when {
            done == 0 -> "Comece pequeno: escolha um bloco de 10 minutos para ativar o dia."
            done < 4 -> "Foque no próximo bloco do período ${period.lowercase(Locale.ROOT)}. Rotina forte nasce de repetição simples."
            else -> "Rotina fechada. Agora deixe o ambiente pronto para repetir amanhã."
        }
    }

    private fun getHabitRecords(): List<HabitUiState> {
        return getCustomHabits().map { name ->
            val category = prefs.getString(metaKey(name, "category"), inferCategory(name)).orEmpty()
            val frequency = prefs.getString(metaKey(name, "frequency"), "Diário").orEmpty()
            HabitUiState(
                name = name,
                description = prefs.getString(metaKey(name, "description"), defaultDescription(name, category)).orEmpty(),
                category = category,
                frequency = frequency,
                time = prefs.getString(metaKey(name, "time"), "").orEmpty(),
                colorName = prefs.getString(metaKey(name, "color"), defaultColorForCategory(category)).orEmpty(),
                iconName = prefs.getString(metaKey(name, "icon"), defaultIconForCategory(category)).orEmpty(),
                done = prefs.getBoolean(habitDoneKey(name, todayKey()), false),
                streak = habitStreak(name),
                weekPercent = percent((0 until 7).count { prefs.getBoolean(habitDoneKey(name, dayKey(-it)), false) }, 7),
            )
        }
    }

    private fun suggestedHabit(name: String): HabitDraft {
        val category = inferCategory(name)
        return HabitDraft(
            name = name,
            description = defaultDescription(name, category),
            category = category,
            frequency = if (category == "Movimento") "3x semana" else "Diário",
            time = when (category) {
                "Saúde" -> "10:00"
                "Foco" -> "16:30"
                "Sono" -> "22:30"
                else -> ""
            },
            colorName = defaultColorForCategory(category),
            iconName = defaultIconForCategory(category),
        )
    }

    private fun saveHabitMeta(name: String, draft: HabitDraft) {
        prefs.edit()
            .putString(metaKey(name, "description"), draft.description)
            .putString(metaKey(name, "category"), draft.category)
            .putString(metaKey(name, "frequency"), draft.frequency)
            .putString(metaKey(name, "time"), draft.time)
            .putString(metaKey(name, "color"), draft.colorName)
            .putString(metaKey(name, "icon"), draft.iconName)
            .apply()
    }

    private fun removeHabitMeta(name: String) {
        prefs.edit()
            .remove(metaKey(name, "description"))
            .remove(metaKey(name, "category"))
            .remove(metaKey(name, "frequency"))
            .remove(metaKey(name, "time"))
            .remove(metaKey(name, "color"))
            .remove(metaKey(name, "icon"))
            .apply()
    }

    private fun getCustomHabits(): List<String> {
        val saved = prefs.getString("custom_habits", "").orEmpty()
        if (saved.isBlank()) return emptyList()
        return saved.split("|").map { sanitizeHabitName(it) }.filter { it.isNotBlank() }
    }

    private fun saveCustomHabits(habits: List<String>) {
        prefs.edit().putString("custom_habits", TextUtils.join("|", habits.map(::sanitizeHabitName))).apply()
    }

    private fun ensureStarterHabits(objective: String) {
        if (getCustomHabits().isNotEmpty()) return
        val target = normalize(objective)
        val starters = listOf(
            if (target.contains("agua")) "Beber 2L de água" else "Planejar o dia",
            if (target.contains("estud")) "Estudar 30 min" else "Fazer 15 min de foco",
            if (target.contains("agua")) "Alongar por 5 min" else "Beber água",
        )
        starters.forEach { addHabit(it) }
    }

    private fun habitStreak(name: String): Int {
        var streak = 0
        for (index in 0 until 90) {
            if (prefs.getBoolean(habitDoneKey(name, dayKey(-index)), false)) streak++ else break
        }
        return streak
    }

    private fun inferCategory(name: String): String {
        val value = normalize(name)
        return when {
            value.contains("agua") || value.contains("beber") || value.contains("hidrata") -> "Saúde"
            value.contains("estud") || value.contains("foco") || value.contains("ler") -> "Foco"
            value.contains("trein") || value.contains("exerc") || value.contains("along") || value.contains("caminh") -> "Movimento"
            value.contains("sono") || value.contains("dorm") -> "Sono"
            else -> "Rotina"
        }
    }

    private fun defaultDescription(name: String, category: String): String {
        return when (category) {
            "Saúde" -> "Cuide da energia do corpo com uma meta simples e visível."
            "Foco" -> "Proteja um bloco curto de atenção profunda."
            "Movimento" -> "Movimente o corpo por poucos minutos para manter constância."
            "Sono" -> "Prepare uma noite melhor com uma rotina pequena."
            else -> "Pequena ação para manter consistência hoje."
        }
    }

    private fun defaultColorForCategory(category: String): String = when (category) {
        "Saúde" -> "Água"
        "Foco" -> "Roxo"
        "Movimento" -> "Coral"
        "Sono" -> "Verde"
        else -> "Azul"
    }

    private fun defaultIconForCategory(category: String): String = when (category) {
        "Saúde" -> "Água"
        "Foco" -> "Foco"
        "Movimento" -> "Movimento"
        "Sono" -> "Sono"
        else -> "Livro"
    }

    private fun metaKey(name: String, field: String): String = "habit_meta_${field}_${sanitizeHabitName(name).hashCode()}"

    private fun habitDoneKey(name: String, day: Long): String = "custom_habit_${day}_${sanitizeHabitName(name).hashCode()}"

    private fun sanitizeHabitName(value: String): String = value.trim().replace("\n", " ").replace("|", "/")

    private fun normalize(value: String): String = value.lowercase(Locale.ROOT)
        .replace("á", "a")
        .replace("à", "a")
        .replace("ã", "a")
        .replace("â", "a")
        .replace("é", "e")
        .replace("ê", "e")
        .replace("í", "i")
        .replace("ó", "o")
        .replace("ô", "o")
        .replace("õ", "o")
        .replace("ú", "u")
        .replace("ç", "c")

    private fun readLog(key: String): List<String> =
        readRawLog(key).split("|").map { it.trim() }.filter { it.isNotBlank() }.asReversed()

    private fun readRawLog(key: String): String = prefs.getString(key, "").orEmpty()

    private fun appendLog(key: String, entry: String): String {
        val current = readRawLog(key)
        return if (current.isBlank()) entry else "$current|$entry"
    }

    private fun todayWaterLogKey(): String = "agua_log_${todayKey()}"

    private fun todayFocusLogKey(): String = "focus_log_${todayKey()}"

    private fun timeNow(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    private fun percent(current: Int, target: Int): Int {
        if (target <= 0) return 0
        return ((current.toDouble() / target) * 100).roundToInt().coerceIn(0, 100)
    }

    private fun themeOptions(level: Int, selected: String): List<ThemeOptionUiState> {
        val names = listOf("Clássico", "Oceano", "Foco Neon", "Floresta", "Solar")
        val levels = listOf(1, 2, 3, 5, 8)
        return names.mapIndexed { index, name ->
            ThemeOptionUiState(
                name = name,
                requiredLevel = levels[index],
                unlocked = level >= levels[index],
                selected = name.equals(selected, ignoreCase = true),
            )
        }
    }

    private fun parseTime(value: String): Pair<Int, Int>? {
        val parts = value.trim().split(":")
        if (parts.size != 2) return null
        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        if (hour !in 0..23 || minute !in 0..59) return null
        return hour to minute
    }

    private fun formatTime(hour: Int, minute: Int): String = String.format(Locale.getDefault(), "%02d:%02d", hour.coerceIn(0, 23), minute.coerceIn(0, 59))

    private fun isValidEmail(value: String): Boolean = value.contains("@") && value.substringAfter("@").contains(".")

    private fun todayKey(): Long = dayKey(0)

    private fun dayKey(offset: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, offset)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis / DAY_MILLIS
    }
}
