package org.evomaster.core.database.extract.postgres

import org.evomaster.client.java.controller.api.dto.database.schema.DatabaseType
import org.evomaster.client.java.controller.db.SqlScriptRunner
import org.evomaster.client.java.controller.internal.db.SchemaExtractor
import org.evomaster.core.database.DbActionTransformer
import org.evomaster.core.database.SqlInsertBuilder
import org.evomaster.core.search.gene.numeric.DoubleGene
import org.evomaster.core.search.gene.numeric.FloatGene
import org.evomaster.core.search.gene.numeric.IntegerGene
import org.evomaster.core.search.gene.numeric.LongGene
import org.evomaster.core.search.gene.sql.SqlAutoIncrementGene
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Created by jgaleotti on 07-May-19.
 */
class NumericTypesTest : ExtractTestBasePostgres() {

    override fun getSchemaLocation() = "/sql_schema/postgres_numeric_types.sql"


    @Test
    fun testNumericTypes() {

        val schema = SchemaExtractor.extract(connection)

        assertNotNull(schema)

        assertEquals("public", schema.name.lowercase())
        assertEquals(DatabaseType.POSTGRES, schema.databaseType)

        val builder = SqlInsertBuilder(schema)
        val actions = builder.createSqlInsertionAction("NumericTypes", setOf("smallintColumn",
                "integerColumn",
                "bigintColumn",
                "decimalColumn",
                "numericColumn",
                "realColumn",
                "doublePrecisionColumn",
                "smallserialColumn",
                "serialColumn",
                "bigserialColumn"))

        val genes = actions[0].seeTopGenes()

        assertEquals(10, genes.size)
        assertTrue(genes[0] is IntegerGene) //smallint
        assertTrue(genes[1] is IntegerGene) // integer
        assertTrue(genes[2] is LongGene) // bigint
        assertTrue(genes[3] is FloatGene) //decimal
        assertTrue(genes[4] is FloatGene) // numeric
        assertTrue(genes[5] is DoubleGene) //real
        assertTrue(genes[6] is DoubleGene) // double precision
        assertTrue(genes[7] is SqlAutoIncrementGene) // smallserial
        assertTrue(genes[8] is SqlAutoIncrementGene) // serial
        assertTrue(genes[9] is SqlAutoIncrementGene) // bigserial

        val dbCommandDto = DbActionTransformer.transform(actions)
        SqlScriptRunner.execInsert(connection, dbCommandDto.insertions)

    }

    @Test
    fun testRealTypeMaximumValue() {

        val schema = SchemaExtractor.extract(connection)

        assertNotNull(schema)

        assertEquals("public", schema.name.lowercase())
        assertEquals(DatabaseType.POSTGRES, schema.databaseType)

        val builder = SqlInsertBuilder(schema)
        val actions = builder.createSqlInsertionAction("NumericTypes", setOf("smallintColumn",
                "integerColumn",
                "bigintColumn",
                "decimalColumn",
                "numericColumn",
                "realColumn",
                "doublePrecisionColumn",
                "smallserialColumn",
                "serialColumn",
                "bigserialColumn"))

        val genes = actions[0].seeTopGenes()

        assertEquals(10, genes.size)
        assertTrue(genes[5] is DoubleGene)

        val realColumnGene = genes[5] as DoubleGene
        assertEquals("realColumn".lowercase(), realColumnGene.name.lowercase())


        assertTrue(genes[6] is DoubleGene)
        val doublePrecisionColumnGene = genes[6] as DoubleGene
        assertEquals("doublePrecisionColumn".lowercase(), doublePrecisionColumnGene.name.lowercase())

        // check max values
        assertEquals("1E+38".toDouble(), realColumnGene.max)
        assertEquals("1E+308".toDouble(), doublePrecisionColumnGene.max)

        realColumnGene.value = "1E+38".toDouble()
        doublePrecisionColumnGene.value = "1E+308".toDouble()

        var dbCommandDto = DbActionTransformer.transform(actions)
        SqlScriptRunner.execInsert(connection, dbCommandDto.insertions)

        realColumnGene.value = "-1E+38".toDouble()
        doublePrecisionColumnGene.value = "-1E+308".toDouble()

        dbCommandDto = DbActionTransformer.transform(actions)
        SqlScriptRunner.execInsert(connection, dbCommandDto.insertions)

    }


}