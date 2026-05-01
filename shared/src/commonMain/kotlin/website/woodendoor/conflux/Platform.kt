package website.woodendoor.conflux

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform