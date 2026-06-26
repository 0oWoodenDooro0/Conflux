package website.woodendoor.conflux.validation

import kotlin.test.Test
import kotlin.test.assertIs

class PasswordValidatorTest {

    @Test
    fun testValidatePasswordEmpty() {
        val result = PasswordValidator.validatePassword("")
        assertIs<ValidationResult.Error>(result)
        assertIs<String>(result.message)
    }

    @Test
    fun testValidatePasswordTooShort() {
        val result = PasswordValidator.validatePassword("12345")
        assertIs<ValidationResult.Error>(result)
    }

    @Test
    fun testValidatePasswordValid() {
        val result = PasswordValidator.validatePassword("123456")
        assertIs<ValidationResult.Success>(result)
    }
}
