package website.woodendoor.conflux.validation

object UsernameValidator {
    fun validateCharacters(username: String): ValidationResult {
        val regex = Regex("^[a-zA-Z0-9]*$") // Use * to allow empty string during typing without character error
        if (!regex.matches(username)) {
            return ValidationResult.Error("Username can only contain alphanumeric characters")
        }
        return ValidationResult.Success
    }

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
        return validateCharacters(username)
    }
}
