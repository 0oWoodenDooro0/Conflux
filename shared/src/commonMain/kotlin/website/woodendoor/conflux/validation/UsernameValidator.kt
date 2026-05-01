package website.woodendoor.conflux.validation

object UsernameValidator {
    fun validateUsername(username: String): ValidationResult {
        if (username.isBlank()) {
            return ValidationResult.Error("Username cannot be empty")
        }
        if (username.length < 3) {
            return ValidationResult.Error("Username must be at least 3 characters")
        }
        if (username.length > 20) {
            return ValidationResult.Error("Username must be 20 characters or less")
        }
        val regex = Regex("^[a-zA-Z0-9]+$")
        if (!regex.matches(username)) {
            return ValidationResult.Error("Username can only contain alphanumeric characters")
        }
        return ValidationResult.Success
    }
}
