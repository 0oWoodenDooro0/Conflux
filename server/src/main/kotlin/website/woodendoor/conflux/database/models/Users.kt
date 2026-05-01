package website.woodendoor.conflux.database.models

import org.jetbrains.exposed.v1.core.Table

object Users : Table("users") {
    val id = varchar("id", 36)
    val username = varchar("username", 32)
    val discriminator = varchar("discriminator", 4)
    val avatar = varchar("avatar", 255).nullable()

    override val primaryKey = PrimaryKey(id)
}
