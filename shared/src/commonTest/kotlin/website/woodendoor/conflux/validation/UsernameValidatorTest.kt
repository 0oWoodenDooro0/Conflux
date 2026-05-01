package website.woodendoor.conflux.validation

import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class UsernameValidatorTest {
    @Test
    fun testEmptyUsername() {
        val result = UsernameValidator.validateUsername("")
        assertIs<ValidationResult.Error>(result)
        assertTrue(result.message.contains("empty", ignoreCase = true))
    }

    @Test
    fun testShortUsername() {
        val result = UsernameValidator.validateUsername("ab")
        assertIs<ValidationResult.Error>(result)
        assertTrue(result.message.contains("3", ignoreCase = true))
    }

    @Test
    fun testLongUsername() {
        val result = UsernameValidator.validateUsername("a".repeat(21))
        assertIs<ValidationResult.Error>(result)
        assertTrue(result.message.contains("20", ignoreCase = true))
    }

    @Test
    fun testInvalidCharacters() {
        val result = UsernameValidator.validateUsername("user!")
        assertIs<ValidationResult.Error>(result)
        assertTrue(result.message.contains("alphanumeric", ignoreCase = true))
    }

    @Test
    fun testValidUsername() {
        val result = UsernameValidator.validateUsername("confluxUser123")
        assertIs<ValidationResult.Success>(result)
    }
}
