package website.woodendoor.conflux.validation

object PasswordValidator {
    fun validatePassword(password: String): ValidationResult {
        if (password.isEmpty()) {
            return ValidationResult.Error("Password cannot be empty")
        }
        if (password.length < 6) {
            return ValidationResult.Error("Password must be at least 6 characters")
        }
        return ValidationResult.Success
    }
}
