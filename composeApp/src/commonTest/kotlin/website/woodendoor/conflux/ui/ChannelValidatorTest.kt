package website.woodendoor.conflux.ui

import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ChannelValidatorTest {
    @Test
    fun testEmptyName() {
        val result = ChannelValidator.validateName("")
        assertIs<ValidationResult.Error>(result)
        assertTrue(result.message.contains("empty", ignoreCase = true))
    }

    @Test
    fun testLongName() {
        val result = ChannelValidator.validateName("a".repeat(33))
        assertIs<ValidationResult.Error>(result)
        assertTrue(result.message.contains("32", ignoreCase = true))
    }

    @Test
    fun testInvalidCharacters() {
        val result = ChannelValidator.validateName("general!")
        assertIs<ValidationResult.Error>(result)
        assertTrue(result.message.contains("alphanumeric", ignoreCase = true))
    }

    @Test
    fun testValidName() {
        val result = ChannelValidator.validateName("general-channel")
        assertIs<ValidationResult.Success>(result)
    }
}
