package com.vector.verevcodex.data.repository.realtime

import com.google.gson.Gson
import java.nio.charset.StandardCharsets
import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Test

class RealtimeRepositoryHelpersTest {

    @Test
    fun `organization id is parsed from jwt payload`() {
        val header = """{"alg":"HS256","typ":"JWT"}""".encodeBase64Url()
        val payload = """{"organization_id":"org-123"}""".encodeBase64Url()
        val token = "$header.$payload.signature"

        assertEquals("org-123", token.extractOrganizationId(Gson()))
    }
}

private fun String.encodeBase64Url(): String =
    Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(toByteArray(StandardCharsets.UTF_8))
