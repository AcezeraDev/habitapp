package com.exemple.habitapp.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.habitapp.data.HabitDashboardState
import com.exemple.habitapp.data.HabitDraft
import com.exemple.habitapp.data.HabitRepository
import com.exemple.habitapp.notifications.ReminderScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FocusTimerState(
    val sessionMinutes: Int = 25,
    val remainingMs: Long = 25 * 60_000L,
    val running: Boolean = false,
)

class HabitMainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HabitRepository(application)
    private val prefs = application.getSharedPreferences("habit_data", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(repository.snapshot())
    val state: StateFlow<HabitDashboardState> = _state.asStateFlow()

    private val _timer = MutableStateFlow(FocusTimerState(sessionMinutes = _state.value.sessionMinutes, remainingMs = _state.value.sessionMinutes * 60_000L))
    val timer: StateFlow<FocusTimerState> = _timer.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    private var timerJob: Job? = null

    init {
        restoreTimerState()
    }

    fun refresh() {
        _state.value = repository.snapshot()
        val session = _state.value.sessionMinutes
        if (!_timer.value.running && _timer.value.sessionMinutes != session) {
            _timer.value = FocusTimerState(sessionMinutes = session, remainingMs = session * 60_000L)
        }
    }

    fun login(name: String, email: String, password: String) {
        val ok = repository.login(name, email, password)
        send(if (ok) "Login salvo" else "Revise nome, e-mail e senha")
        refresh()
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
    ) {
        val ok = repository.completeDailySetup(
            name = name,
            objective = objective,
            routine = routine,
            bestPeriod = bestPeriod,
            currentWaterMl = currentWaterMl,
            waterGoalMl = waterGoalMl,
            focusGoalMinutes = focusGoalMinutes,
            sessionMinutes = sessionMinutes,
        )
        if (ok) {
            ReminderScheduler.scheduleDefaultReminders(getApplication())
            _timer.value = FocusTimerState(sessionMinutes = sessionMinutes, remainingMs = sessionMinutes * 60_000L)
        }
        send(if (ok) "Rotina inicial criada" else "Revise as metas do onboarding")
        refresh()
    }

    fun addWater(amountMl: Int) {
        val added = repository.addWater(amountMl)
        send(if (added > 0) "+$added ml adicionados" else "Meta de água já concluída")
        refresh()
    }

    fun undoWater() {
        val removed = repository.undoWater()
        send(if (removed > 0) "-$removed ml removidos" else "Sem registro para desfazer")
        refresh()
    }

    fun resetWater() {
        repository.resetWater()
        send("Água zerada para hoje")
        refresh()
    }

    fun setWaterGoal(goalMl: Int) {
        val ok = repository.setWaterGoal(goalMl)
        send(if (ok) "Meta de água salva" else "Use uma meta entre 500 e 8000 ml")
        refresh()
    }

    fun setFocusGoal(minutes: Int) {
        val ok = repository.setFocusGoal(minutes)
        if (!ok) send("Use uma meta de foco válida")
        refresh()
    }

    fun addManualFocus(minutes: Int) {
        repository.addFocus(minutes)
        send("+$minutes min registrados")
        refresh()
    }

    fun setSessionMinutes(minutes: Int) {
        if (repository.setSessionMinutes(minutes)) {
            timerJob?.cancel()
            _timer.value = FocusTimerState(sessionMinutes = minutes, remainingMs = minutes * 60_000L)
            clearTimerState()
            refresh()
        } else {
            send("Use uma sessão entre 5 e 180 minutos")
        }
    }

    fun toggleTimer() {
        if (_timer.value.running) {
            timerJob?.cancel()
            _timer.value = _timer.value.copy(running = false)
            persistTimerState(running = false)
            send("Sessão pausada")
            return
        }

        if (_timer.value.remainingMs <= 0L) {
            _timer.value = _timer.value.copy(remainingMs = _timer.value.sessionMinutes * 60_000L)
        }
        _timer.value = _timer.value.copy(running = true)
        persistTimerState(running = true)
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timer.value.running && _timer.value.remainingMs > 0L) {
                delay(1000L)
                _timer.value = _timer.value.copy(remainingMs = (_timer.value.remainingMs - 1000L).coerceAtLeast(0L))
                persistTimerState(running = _timer.value.running)
            }
            if (_timer.value.running && _timer.value.remainingMs <= 0L) {
                val completed = _timer.value.sessionMinutes
                repository.completeFocusSession(completed)
                clearTimerState()
                _timer.value = FocusTimerState(sessionMinutes = completed, remainingMs = completed * 60_000L)
                refresh()
                send("Sessão concluída")
            }
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        _timer.value = FocusTimerState(sessionMinutes = _timer.value.sessionMinutes, remainingMs = _timer.value.sessionMinutes * 60_000L)
        clearTimerState()
        send("Timer reiniciado")
    }

    fun setChecklist(index: Int, checked: Boolean) {
        repository.setChecklist(index, checked)
        refresh()
    }

    fun setRoutinePeriod(period: String) {
        repository.setRoutinePeriod(period)
        send("Período definido: $period")
        refresh()
    }

    fun setRoutineBlock(index: Int, checked: Boolean) {
        repository.setRoutineBlock(index, checked)
        refresh()
    }

    fun completeRoutine() {
        repository.completeRoutine()
        send("Rotina marcada")
        refresh()
    }

    fun setMood(value: Int) {
        repository.setMood(value)
        refresh()
    }

    fun setEnergy(value: Int) {
        repository.setEnergy(value)
        refresh()
    }

    fun addHabit(name: String) {
        val ok = repository.addHabit(name)
        send(if (ok) "Hábito criado" else "Digite um hábito novo")
        refresh()
    }

    fun saveHabit(draft: HabitDraft, oldName: String? = null) {
        val ok = repository.saveHabit(draft, oldName)
        send(if (ok) "Hábito salvo" else "Revise o nome do hábito")
        refresh()
    }

    fun toggleHabit(name: String) {
        repository.toggleHabit(name)
        refresh()
    }

    fun removeHabit(name: String) {
        repository.removeHabit(name)
        send("Hábito removido")
        refresh()
    }

    fun saveProfile(name: String, email: String, objective: String, routine: String, bestPeriod: String) {
        repository.saveProfile(name, email, objective, routine, bestPeriod)
        send("Perfil salvo")
        refresh()
    }

    fun setTheme(dark: Boolean, accent: String) {
        repository.setDarkMode(dark)
        repository.setAccentTheme(accent)
        send("Tema atualizado")
        refresh()
    }

    fun selectTheme(theme: String) {
        val ok = repository.selectTheme(theme)
        send(if (ok) "Tema $theme selecionado" else "Tema bloqueado. Ganhe mais XP.")
        refresh()
    }

    fun setNotificationSettings(enabled: Boolean, water: Boolean, focus: Boolean) {
        repository.setNotificationSettings(enabled, water, focus)
        if (enabled) ReminderScheduler.scheduleDefaultReminders(getApplication()) else ReminderScheduler.cancelAll(getApplication())
        send("Notificações atualizadas")
        refresh()
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
    ) {
        val ok = repository.setNotificationPlan(enabled, water, focus, routine, waterStart, waterIntervalHours, focusTime, routineTime)
        if (ok) {
            if (enabled) ReminderScheduler.scheduleDefaultReminders(getApplication()) else ReminderScheduler.cancelAll(getApplication())
        }
        send(if (ok) "Lembretes salvos" else "Revise horários e intervalo")
        refresh()
    }

    fun saveDiary(note: String) {
        repository.saveDiary(note)
        send("Diário salvo")
        refresh()
    }

    fun saveAvatarPath(path: String) {
        repository.saveAvatarPath(path)
        send("Foto do perfil atualizada")
        refresh()
    }

    fun removeAvatar() {
        repository.removeAvatarPath()
        send("Foto removida")
        refresh()
    }

    fun logout() {
        repository.logout()
        send("Sessão encerrada")
        refresh()
    }

    fun setChallengeGoal(days: Int) {
        val ok = repository.setChallengeGoal(days)
        send(if (ok) "Desafio atualizado" else "Use entre 7 e 90 dias")
        refresh()
    }

    fun claimMissionXp() {
        val xp = repository.claimMissionXp()
        send(if (xp > 0) "+$xp XP resgatados" else "Nenhum XP novo disponível")
        refresh()
    }

    fun importBackup(text: String) {
        val count = repository.importBackupText(text)
        send(if (count > 0) "$count itens restaurados" else "Backup inválido")
        refresh()
    }

    fun exportBackup(): String = repository.exportBackupText()

    fun exportBackupJson(): String = repository.exportBackupJson()

    private fun send(message: String) {
        viewModelScope.launch {
            _events.emit(message)
        }
    }

    private fun restoreTimerState() {
        val fallbackSession = _state.value.sessionMinutes
        val totalMs = prefs.getLong("focus_timer_total_ms", fallbackSession * 60_000L).coerceAtLeast(5 * 60_000L)
        val sessionMinutes = (totalMs / 60_000L).toInt().coerceIn(5, 180)
        val running = prefs.getBoolean("focus_timer_running", false)
        val remaining = if (running) {
            (prefs.getLong("focus_timer_end_at_ms", 0L) - System.currentTimeMillis()).coerceAtLeast(0L)
        } else {
            prefs.getLong("focus_timer_remaining_ms", totalMs).coerceIn(0L, totalMs)
        }
        _timer.value = FocusTimerState(sessionMinutes = sessionMinutes, remainingMs = if (remaining > 0) remaining else totalMs)
        if (running && remaining > 0L) toggleTimer()
        if (running && remaining <= 0L) {
            repository.completeFocusSession(sessionMinutes)
            clearTimerState()
            refresh()
        }
    }

    private fun persistTimerState(running: Boolean) {
        val timer = _timer.value
        prefs.edit()
            .putBoolean("focus_timer_running", running)
            .putLong("focus_timer_total_ms", timer.sessionMinutes * 60_000L)
            .putLong("focus_timer_remaining_ms", timer.remainingMs)
            .putLong("focus_timer_end_at_ms", if (running) System.currentTimeMillis() + timer.remainingMs else 0L)
            .apply()
    }

    private fun clearTimerState() {
        prefs.edit()
            .remove("focus_timer_running")
            .remove("focus_timer_total_ms")
            .remove("focus_timer_remaining_ms")
            .remove("focus_timer_end_at_ms")
            .apply()
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
