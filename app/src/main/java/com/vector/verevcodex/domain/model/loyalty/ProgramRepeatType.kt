package com.vector.verevcodex.domain.model.loyalty

enum class ProgramRepeatType {
    NONE,
    WEEKDAYS,
    SEASONAL,
    CUSTOM;

    companion object {
        fun from(raw: String?): ProgramRepeatType = when (raw?.trim()?.uppercase()) {
            "WEEKLY", "WEEKDAYS" -> WEEKDAYS
            "SEASONAL", "SEASON", "YEARLY" -> SEASONAL
            "CUSTOM", "MONTHLY" -> CUSTOM
            else -> NONE
        }
    }
}
