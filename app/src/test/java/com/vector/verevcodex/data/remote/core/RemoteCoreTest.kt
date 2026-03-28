package com.vector.verevcodex.data.remote.core

import com.vector.verevcodex.data.remote.api.ApiEnvelope
import java.net.ConnectException
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class RemoteCoreTest {

    @Test
    fun `unwrap exposes backend code status and request id from error envelope`() {
        val response = Response.error<ApiEnvelope<Unit>>(
            409,
            """
            {
              "meta": {
                "requestId": "req-123"
              },
              "error": {
                "code": "CONFLICT_USER_EMAIL",
                "message": "Email already exists"
              }
            }
            """.trimIndent().toResponseBody("application/json".toMediaType()),
        )

        val error = runCatching {
            response.unwrap { it }
        }.exceptionOrNull() as? RemoteException

        requireNotNull(error)
        assertEquals(RemoteException.Kind.Api, error.kind)
        assertEquals("CONFLICT_USER_EMAIL", error.backendCode)
        assertEquals(409, error.httpStatus)
        assertEquals("req-123", error.requestId)
        assertEquals("Email already exists", error.message)
    }

    @Test
    fun `remote result maps connectivity failures consistently`() = runBlocking {
        val result = remoteResult<Unit> {
            throw ConnectException("boom")
        }

        val error = result.exceptionOrNull() as? RemoteException

        requireNotNull(error)
        assertEquals(RemoteException.Kind.Connectivity, error.kind)
        assertNull(error.backendCode)
        assertEquals("Cannot reach the server. Check your connection and try again.", error.message)
    }

    @Test
    fun `unwrap rejects successful response without data as data failure`() {
        val response = Response.success(
            ApiEnvelope<Unit>(
                data = null,
                meta = null,
                error = null,
            ),
        )

        val error = runCatching {
            response.unwrap { it }
        }.exceptionOrNull() as? RemoteException

        requireNotNull(error)
        assertEquals(RemoteException.Kind.Data, error.kind)
        assertTrue(error.message.contains("without data"))
    }
}
