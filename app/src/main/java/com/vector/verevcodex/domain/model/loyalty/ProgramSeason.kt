package com.vector.verevcodex.domain.model.loyalty

enum class ProgramSeason(val months: Set<Int>) {
    WINTER(setOf(12, 1, 2)),
    SPRING(setOf(3, 4, 5)),
    SUMMER(setOf(6, 7, 8)),
    AUTUMN(setOf(9, 10, 11));

    companion object {
        fun from(raw: String?): ProgramSeason? = entries.firstOrNull { it.name == raw?.trim()?.uppercase() }
    }
}
