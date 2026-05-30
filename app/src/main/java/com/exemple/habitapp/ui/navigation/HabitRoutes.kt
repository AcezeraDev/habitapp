package com.exemple.habitapp.ui.navigation

import androidx.annotation.DrawableRes
import com.exemple.habitapp.R

enum class HabitRoute(
    val route: String,
    val label: String,
    @param:DrawableRes val icon: Int,
) {
    Home("home", "Início", R.drawable.ic_nav_home),
    Habits("habits", "Hábitos", R.drawable.ic_nav_goals),
    Focus("focus", "Foco", R.drawable.ic_nav_focus),
    Progress("progress", "Progresso", R.drawable.ic_nav_chart),
    More("more", "Mais", R.drawable.ic_nav_more),
    Water("water", "Água", R.drawable.ic_nav_water),
    Routine("routine", "Rotina", R.drawable.ic_nav_routine),
    Goals("goals", "Metas", R.drawable.ic_nav_goals),
    Profile("profile", "Perfil", R.drawable.ic_nav_profile),
    Settings("settings", "Configurações", R.drawable.ic_notification),
    History("history", "Histórico", R.drawable.ic_history),
    Calendar("calendar", "Calendário", R.drawable.ic_clock_history),
    Achievements("achievements", "Conquistas", R.drawable.ic_premium_trophy),
    Missions("missions", "Missões", R.drawable.ic_mission_flag),
    Report("report", "Relatório", R.drawable.ic_nav_chart),
    Backup("backup", "Backup", R.drawable.ic_backup),
    Diary("diary", "Diário", R.drawable.ic_premium_book),
    Appearance("appearance", "Aparência", R.drawable.ic_theme_palette),
    Themes("themes", "Loja de temas", R.drawable.ic_theme_palette),
    Challenges("challenges", "Desafios", R.drawable.ic_premium_fire),
    Stats("stats", "Estatísticas", R.drawable.ic_premium_equalizer),
}

val bottomRoutes = listOf(
    HabitRoute.Home,
    HabitRoute.Habits,
    HabitRoute.Focus,
    HabitRoute.Progress,
    HabitRoute.More,
)
