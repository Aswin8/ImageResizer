package ru.tech.imageresizershrinker.main_screen.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.Dp
import com.t8rin.dynamic.theme.ColorTuple

val LocalNightMode = compositionLocalOf<Int> { error("Not present Night Mode") }

val LocalDynamicColors = compositionLocalOf<Boolean> { error("Not present Dynamic Colors") }

val LocalAllowChangeColorByImage =
    compositionLocalOf<Boolean> { error("Not present Dynamic Colors") }

val LocalAmoledMode = compositionLocalOf<Boolean> { error("Not present Amoled Mode") }

val LocalAppColorTuple = compositionLocalOf<ColorTuple> { error("No color present") }

val LocalBorderWidth = compositionLocalOf<Dp> { error("No borders present") }

val LocalPresetsProvider = compositionLocalOf<List<Int>> { error("No LocalPresets present") }

val LocalEditPresets = compositionLocalOf { mutableStateOf(false) }

@Composable
fun Int.isNightMode(): Boolean = when (this) {
    0 -> true
    1 -> false
    else -> isSystemInDarkTheme()
}