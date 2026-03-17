package com.vector.verevcodex.data.remote.engagement

import com.vector.verevcodex.data.remote.api.engagement.CheckInRequestDto
import com.vector.verevcodex.data.remote.api.engagement.CheckInResponseDto
import com.vector.verevcodex.data.remote.api.engagement.VerevCheckInsApi
import com.vector.verevcodex.data.remote.core.RemoteIdempotencyAction
import com.vector.verevcodex.data.remote.core.RemoteIdempotencyDomain
import com.vector.verevcodex.data.remote.core.buildRemoteIdempotencyKey
import com.vector.verevcodex.data.remote.core.unwrap
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

data class CheckInResult(
    val deduplicated: Boolean,
    val rewardIssued: Boolean,
    val rewardPoints: Int,
)

@Singleton
class CheckInRemoteDataSource @Inject constructor(
    private val api: VerevCheckInsApi,
) {
    suspend fun create(storeId: String, customerId: String): Result<CheckInResult> = runCatching {
        val occurredAt = Instant.now().toString()
        api.create(
            CheckInRequestDto(
                storeId = storeId,
                customerId = customerId,
                occurredAt = occurredAt,
            ),
            idempotencyKey = checkInIdempotencyKey(
                action = RemoteIdempotencyAction.CREATE,
                storeId,
                customerId,
                occurredAt,
            ),
        ).unwrap { dto -> dto.toDomain() }
    }

    private fun checkInIdempotencyKey(
        action: RemoteIdempotencyAction,
        vararg parts: String?,
    ): String = buildRemoteIdempotencyKey(RemoteIdempotencyDomain.CHECK_IN, action, *parts)
}

private fun CheckInResponseDto.toDomain() = CheckInResult(
    deduplicated = deduplicated == true,
    rewardIssued = rewardIssued == true,
    rewardPoints = rewardPoints ?: 0,
)
