package org.evomaster.core.database.schema

import org.evomaster.client.java.controller.api.dto.database.schema.DatabaseType

/**
 *
 * Should be immutable
 */
data class Column(

        val name: String,

        val type: ColumnDataType,

        val size: Int = 0,

        val primaryKey: Boolean = false,

        val nullable: Boolean = true,

        val unique: Boolean = false,

        val autoIncrement: Boolean = false,

        val foreignKeyToAutoIncrement: Boolean = false,

        val lowerBound: Long? = null,

        val upperBound: Long? = null,

        val enumValuesAsStrings: List<String>? = null,

        val similarToPatterns: List<String>? = null,

        val likePatterns: List<String>? = null,

        val databaseType: DatabaseType,

        val isUnsigned : Boolean = false,

        /**
         * null means that the scale is not applicable
         */
        val scale: Int? = null,

        /**
         * A column with dimension > 0 represents arrays, matrices, etc.
         */
        val dimension: Int = 0,

        val compositeType: List<Column>? = null,

        val compositeTypeName: String? = null

        // public boolean identity;

        //TODO something for other constraints

)