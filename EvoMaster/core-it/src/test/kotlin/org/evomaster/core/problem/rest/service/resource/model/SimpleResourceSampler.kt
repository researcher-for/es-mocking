package org.evomaster.core.problem.rest.service.resource.model

import io.swagger.parser.OpenAPIParser
import org.evomaster.core.EMConfig
import org.evomaster.core.database.SqlInsertBuilder
import org.evomaster.core.problem.rest.RestActionBuilderV3
import org.evomaster.core.problem.rest.RestCallAction
import org.evomaster.core.problem.rest.service.ResourceSampler
import javax.annotation.PostConstruct

class SimpleResourceSampler : ResourceSampler() {

    fun initialize(swaggerPath : String, config: EMConfig, skipAction: List<String> = listOf(), sqlInsertBuilder: SqlInsertBuilder?) {
        val swagger = OpenAPIParser().readLocation(swaggerPath, null, null).openAPI
        actionCluster.clear()
        RestActionBuilderV3.addActionsFromSwagger(swagger, actionCluster, skipAction, config.doesApplyNameMatching,config.enableSchemaConstraintHandling)


        if (sqlInsertBuilder != null){
            this.sqlInsertBuilder = sqlInsertBuilder
            existingSqlData = sqlInsertBuilder.extractExistingPKs()
        }

        initAdHocInitialIndividuals()
        postInits()
    }

    @PostConstruct
    override fun initialize(){
    }

    override fun getExcludedActions(): List<RestCallAction> = listOf()
}