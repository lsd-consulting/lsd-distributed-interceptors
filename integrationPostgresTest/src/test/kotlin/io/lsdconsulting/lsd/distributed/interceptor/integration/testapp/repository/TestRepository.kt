package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.repository

import com.fasterxml.jackson.databind.ObjectMapper
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.interceptor.config.log
import io.lsdconsulting.lsd.distributed.postgres.repository.toInterceptedInteraction
import org.postgresql.util.PSQLException
import org.springframework.stereotype.Repository
import javax.sql.DataSource

private const val QUERY = "select * from lsd.intercepted_interactions o where o.trace_id = ? order by o.created_at"

@Repository
open class TestRepository(
    private val dataSource: DataSource,
    private val objectMapper: ObjectMapper,
) {

    open fun createTable(dataSource: DataSource) {
        val prepareDatabaseQuery = javaClass.getResourceAsStream("/db/prepareDatabase.sql")?.bufferedReader()?.readText()
        dataSource.connection.use { con ->
            con.prepareStatement(prepareDatabaseQuery).use { pst ->
                pst.executeUpdate()
            }
        }
    }

    open fun clearTable(dataSource: DataSource) {
        dataSource.connection.use { con ->
            con.prepareStatement("truncate lsd.intercepted_interactions").use { pst ->
                pst.executeUpdate()
            }
        }
    }

    open fun findAll(traceId: String): List<InterceptedInteraction> {
        log().info("Retrieving interceptedInteractions for traceId:{}", traceId)
        val interceptedInteractions: MutableList<InterceptedInteraction> = mutableListOf()
        try {
            dataSource.connection.use { con ->
                val prepareStatement = con.prepareStatement(QUERY)
                prepareStatement.setString(1, traceId)
                prepareStatement.use { pst ->
                    pst.executeQuery().use { rs ->
                        while (rs.next()) {
                            val interceptedInteraction = rs.toInterceptedInteraction(objectMapper)
                            interceptedInteractions.add(interceptedInteraction)
                        }
                    }
                }
            }
        } catch (e: PSQLException) {
            log().error("Failed to retrieve interceptedInteractions - message:${e.message}", e.stackTrace)
        }
        return interceptedInteractions
    }
}
