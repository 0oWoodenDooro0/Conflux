package website.woodendoor.conflux.database

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import kotlin.test.Test
import kotlin.test.assertNotNull

class DatabaseFactoryTest {
    @Test
    fun testDatabaseInitialization() {
        DatabaseFactory.init()
        runBlocking {
            DatabaseFactory.dbQuery {
                assertNotNull(TransactionManager.current())
            }
        }
    }
}
