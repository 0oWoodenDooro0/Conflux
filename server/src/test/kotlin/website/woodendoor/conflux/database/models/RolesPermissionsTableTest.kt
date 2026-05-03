package website.woodendoor.conflux.database.models

import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import website.woodendoor.conflux.database.DatabaseFactory
import kotlin.test.BeforeTest
import kotlin.test.Test

class RolesPermissionsTableTest {
    @BeforeTest
    fun setup() {
        DatabaseFactory.init()
    }

    @Test
    fun testRolesTableHasPriorityLevel() {
        transaction {
            SchemaUtils.create(Roles)
            // This will fail to compile if priorityLevel is not defined in Roles
            val priority = Roles.priorityLevel
        }
    }

    @Test
    fun testMemberRolesTableCreation() {
        transaction {
            SchemaUtils.create(MemberRoles)
        }
    }
}
