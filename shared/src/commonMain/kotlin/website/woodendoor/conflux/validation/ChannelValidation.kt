package website.woodendoor.conflux.validation

object ChannelValidator {
    fun validateName(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult.Error("Channel name cannot be empty")
        }
        if (name.length > 32) {
            return ValidationResult.Error("Channel name must be 32 characters or less")
        }
        val regex = Regex("^[a-zA-Z0-9-]+$")
        if (!regex.matches(name)) {
            return ValidationResult.Error("Channel name can only contain alphanumeric characters and hyphens")
        }
        return ValidationResult.Success
    }
}

