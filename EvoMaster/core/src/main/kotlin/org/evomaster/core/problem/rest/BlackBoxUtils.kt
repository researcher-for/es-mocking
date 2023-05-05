package org.evomaster.core.problem.rest


import org.evomaster.core.EMConfig
import org.evomaster.core.logging.LoggingUtil
import org.evomaster.core.problem.rest.service.AbstractRestSampler
import org.evomaster.core.remote.SutProblemException
import org.evomaster.core.search.service.Sampler
import java.net.URL


object BlackBoxUtils {

    /**
     * Return the URL of the SUT.
     * This might depend on the several factors, like REST vs GraphQL, and whether
     * the info is overridden compared to what provided in the schema (eg for REST).
     */
    fun targetUrl(config: EMConfig, sampler: Sampler<*>? = null): String {

        /*
            Note: bbTargetUrl and bbSwaggerUrl are already validated
            in EMConfig (but they can be blank)
         */

        if (config.bbTargetUrl.isNotBlank()) {
            //this has the priority
            return config.bbTargetUrl
        } else {

            when (config.problemType) {
                //should had been already been validated in EMConfig
                EMConfig.ProblemType.GRAPHQL -> throw IllegalStateException("BUG: no target for GQL is defined")
                EMConfig.ProblemType.REST -> {
                    val schema = (sampler as AbstractRestSampler).swagger
                    return if (schema.servers == null || schema.servers.isEmpty() || schema.servers[0].url == "/") {
                        // "/" is default if value missing in schema
                        LoggingUtil.uniqueUserWarn("Schema has no info on where the API is, eg 'host' in v2 and 'servers' in v3." +
                                " Going to use same location as where the schema was downloaded from")
                        //try to infer it from Swagger URL
                        extractTarget(config.bbSwaggerUrl)
                    } else {
                        val url = schema.servers[0].url
                        if(url.startsWith("//")){
                            // this can happen if 'scheme' is missing in V2, resulting in an invalid URL in current parser
                            extractTarget("http:$url")
                        } else {
                            extractTarget(url)
                        }
                    }
                }
                else -> throw IllegalStateException("Black-box testing is currently not supported for ${config.problemType}")
            }
        }
    }

    private fun extractTarget(fullUrl: String): String {
        val url = try {
            URL(fullUrl)
        }catch (e: Exception){
            throw SutProblemException("Invalid URL: $fullUrl")
        }

        if (url.protocol == "file") {
            throw IllegalStateException("If the schema is read from local file system, and" +
                    " there is no info on host:port, then you MUST use --bbTargetUrl option to specify it")
        }

        val protocol = if (url.protocol == null || url.protocol.isEmpty()) {
            "http" //defaulting to it
        } else if (url.protocol == "http" || url.protocol == "https") {
            url.protocol
        } else {
            throw IllegalStateException("Not supported protocol: ${url.protocol}")
        }
        val port = if (url.port > 0) ":${url.port}" else ""
        return protocol + "://" + url.host + port
    }

}