package website.woodendoor.conflux.controller

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OperationResultTest {

    @Test
    fun `test success result`() {
        val result = OperationResult.Success("Data")
        assertTrue(result is OperationResult.Success)
        assertEquals("Data", (result as OperationResult.Success).data)
    }

    @Test
    fun `test failure results`() {
        val notFound = OperationResult.Failure.NotFound("Not found")
        assertTrue(notFound is OperationResult.Failure.NotFound)
        assertEquals("Not found", notFound.message)

        val unauthorized = OperationResult.Failure.Unauthorized("Unauthorized")
        assertTrue(unauthorized is OperationResult.Failure.Unauthorized)
        assertEquals("Unauthorized", unauthorized.message)

        val forbidden = OperationResult.Failure.Forbidden("Forbidden")
        assertTrue(forbidden is OperationResult.Failure.Forbidden)
        assertEquals("Forbidden", forbidden.message)

        val badRequest = OperationResult.Failure.BadRequest("Bad request")
        assertTrue(badRequest is OperationResult.Failure.BadRequest)
        assertEquals("Bad request", badRequest.message)
    }
}
