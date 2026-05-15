package com.example.killersudoku.viewmodel

import androidx.annotation.StringRes

data class UiMessage(
    @StringRes val resId: Int,
    val args: List<String> = emptyList(),
)
