package website.woodendoor.conflux.database.models

import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import website.woodendoor.conflux.database.DatabaseFactory
import kotlin.test.BeforeTest
import kotlin.test.Test

class MessagesTableTest {
    @BeforeTest
    fun setup() {
        DatabaseFactory.init()
    }

    @Test
    fun testMessagesTableCreation() {
        transaction {
            // This will fail to compile if Messages is not defined
            SchemaUtils.create(Messages)
        }
    }
}
