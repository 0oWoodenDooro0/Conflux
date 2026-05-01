package website.woodendoor.conflux.state

import website.woodendoor.conflux.models.User

object LoginState {
    var currentUser: User? = null
        private set

    fun login(user: User) {
        currentUser = user
    }

    fun logout() {
        currentUser = null
    }
}
