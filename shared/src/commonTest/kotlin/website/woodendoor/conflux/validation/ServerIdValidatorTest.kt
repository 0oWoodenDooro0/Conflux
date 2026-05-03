package website.woodendoor.conflux.validation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ServerIdValidatorTest {

    @Test
    fun testValidUuid() {
        assertTrue(ServerIdValidator.isValid("550e8400-e29b-41d4-a716-446655440000"))
        assertTrue(ServerIdValidator.isValid("eb636906-89d5-450a-8677-e984950e42d7"))
    }

    @Test
    fun testInvalidUuid() {
        assertFalse(ServerIdValidator.isValid("not-a-uuid"))
        assertFalse(ServerIdValidator.isValid(""))
        assertFalse(ServerIdValidator.isValid("550e8400-e29b-41d4-a716"))
    }
}
