package com.vector.verevcodex.presentation.scan

import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction
import com.vector.verevcodex.presentation.programs.ProgramInactiveReason
import com.vector.verevcodex.presentation.programs.toOperationalSnapshot
import java.time.LocalDate

internal data class ScanProgramAvailability(
    val livePrograms: List<RewardProgram>,
    val primaryInactiveReason: ProgramInactiveReason?,
)

internal fun resolveScanAvailableActions(
    livePrograms: List<RewardProgram>,
    remoteActions: List<RewardProgramScanAction>,
): List<RewardProgramScanAction> {
    if (livePrograms.isEmpty()) return emptyList()

    val liveProgramActions = livePrograms
        .asSequence()
        .flatMap { it.configuration.scanActions.asSequence() }
        .distinct()
        .toList()

    val resolvedActions = if (remoteActions.isEmpty()) {
        liveProgramActions
    } else {
        val remoteActionSet = remoteActions.toSet()
        liveProgramActions.filter { it in remoteActionSet }
    }.filterNot { action ->
        action == RewardProgramScanAction.TRACK_TIER_PROGRESS
    }

    val actionableWithoutCheckIn = resolvedActions.filterNot { it == RewardProgramScanAction.CHECK_IN }
    val visibleActions = if (
        RewardProgramScanAction.CHECK_IN in resolvedActions &&
        actionableWithoutCheckIn.isNotEmpty()
    ) {
        actionableWithoutCheckIn
    } else {
        resolvedActions
    }

    return visibleActions.sortedBy { action ->
        when (action) {
            RewardProgramScanAction.EARN_POINTS -> 0
            RewardProgramScanAction.REDEEM_REWARDS -> 1
            RewardProgramScanAction.CHECK_IN -> 2
            RewardProgramScanAction.TRACK_TIER_PROGRESS -> 3
        }
    }
}

internal fun resolveScanProgramAvailability(
    programs: List<RewardProgram>,
    remoteActions: List<RewardProgramScanAction>,
    today: LocalDate = LocalDate.now(),
): ScanProgramAvailability {
    val storePrograms = programs.filter { it.active }
    if (storePrograms.isEmpty()) {
        return ScanProgramAvailability(
            livePrograms = emptyList(),
            primaryInactiveReason = null,
        )
    }

    val snapshots = storePrograms.map { program ->
        program to program.toOperationalSnapshot(
            existingPrograms = storePrograms,
            campaigns = emptyList(),
            activeScanActions = remoteActions,
            today = today,
        )
    }

    val livePrograms = snapshots
        .filter { (_, snapshot) -> snapshot.affectsScansNow }
        .map { (program, _) -> program }

    val primaryInactiveReason = if (livePrograms.isEmpty()) {
        snapshots
            .mapNotNull { (_, snapshot) -> snapshot.inactiveReasons.firstOrNull() }
            .sortedWith(compareBy(::inactiveReasonPriority))
            .firstOrNull()
    } else {
        null
    }

    return ScanProgramAvailability(
        livePrograms = livePrograms,
        primaryInactiveReason = primaryInactiveReason,
    )
}

private fun inactiveReasonPriority(reason: ProgramInactiveReason): Int = when (reason) {
    is ProgramInactiveReason.StartsLater -> 0
    is ProgramInactiveReason.Ended -> 1
    ProgramInactiveReason.NoActiveScanCoverage -> 2
    ProgramInactiveReason.NoScanActions -> 3
    ProgramInactiveReason.ReferralOnly -> 4
    ProgramInactiveReason.Disabled -> 5
}
