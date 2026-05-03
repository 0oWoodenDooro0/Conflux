package website.woodendoor.conflux.validation

object ServerIdValidator {
    private val uuidRegex = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

    fun isValid(id: String): Boolean {
        return uuidRegex.matches(id)
    }
}
