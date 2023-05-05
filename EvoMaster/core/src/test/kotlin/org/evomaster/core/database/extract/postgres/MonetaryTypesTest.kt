package org.evomaster.core.database.extract.postgres

import org.evomaster.client.java.controller.api.dto.database.schema.DatabaseType
import org.evomaster.client.java.controller.db.SqlScriptRunner
import org.evomaster.client.java.controller.internal.db.SchemaExtractor
import org.evomaster.core.database.DbActionTransformer
import org.evomaster.core.database.SqlInsertBuilder
import org.evomaster.core.search.gene.numeric.BigDecimalGene
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Created by jgaleotti on 07-May-19.
 */
class MonetaryTypesTest : ExtractTestBasePostgres() {

    override fun getSchemaLocation() = "/sql_schema/postgres_monetary_types.sql"


    @Test
    fun testMonetaryTypes() {

        val schema = SchemaExtractor.extract(connection)

        assertNotNull(schema)

        assertEquals("public", schema.name.lowercase())
        assertEquals(DatabaseType.POSTGRES, schema.databaseType)

        val builder = SqlInsertBuilder(schema)
        val actions = builder.createSqlInsertionAction("MonetaryTypes", setOf("moneyColumn"))

        val genes = actions[0].seeTopGenes()

        assertEquals(1, genes.size)
        assertTrue(genes[0] is BigDecimalGene) // money
        assertEquals(2, (genes[0] as BigDecimalGene).scale)

        val dbCommandDto = DbActionTransformer.transform(actions)
        SqlScriptRunner.execInsert(connection, dbCommandDto.insertions)

    }
}