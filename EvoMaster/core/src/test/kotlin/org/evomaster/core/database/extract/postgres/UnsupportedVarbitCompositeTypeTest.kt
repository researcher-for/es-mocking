package org.evomaster.core.database.extract.postgres

import org.evomaster.client.java.controller.internal.db.SchemaExtractor
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Created by jgaleotti on 02-May-22.
 */
class UnsupportedVarbitCompositeTypeTest : ExtractTestBasePostgres() {

    override fun getSchemaLocation() = "/sql_schema/postgres_unsupported_composite_type_varbit.sql"

    @Test
    fun testFailureToExtractSchema() {
        assertThrows<UnsupportedOperationException> { SchemaExtractor.extract(connection) }

    }


}