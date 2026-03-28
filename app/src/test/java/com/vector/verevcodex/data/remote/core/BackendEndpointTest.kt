package com.vector.verevcodex.data.remote.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackendEndpointTest {

    @Test
    fun `configured http endpoint is normalized`() {
        val endpoint = BackendEndpoint.from("10.0.2.2:8080")

        assertTrue(endpoint.isConfigured)
        assertEquals("http://10.0.2.2:8080/", endpoint.httpBaseUrl)
        assertEquals("ws://10.0.2.2:8080/ws", endpoint.webSocketUrl)
        assertEquals("10.0.2.2", endpoint.socketHost)
    }

    @Test
    fun `blank endpoint falls back to invalid sentinel`() {
        val endpoint = BackendEndpoint.from("")

        assertFalse(endpoint.isConfigured)
        assertEquals("http://backend.invalid/", endpoint.httpBaseUrl)
        assertEquals("ws://backend.invalid/ws", endpoint.webSocketUrl)
        assertEquals("backend.invalid", endpoint.socketHost)
    }

    @Test
    fun `relative download path resolves against backend base`() {
        val endpoint = BackendEndpoint.from("https://api.example.com/base-path")

        assertEquals(
            "https://api.example.com/reports/download/123",
            resolveBackendAbsoluteUrl(endpoint, "/reports/download/123"),
        )
    }
}
