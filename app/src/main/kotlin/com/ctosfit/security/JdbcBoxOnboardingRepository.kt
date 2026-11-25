package com.ctosfit.security

import com.ctosfit.box.application.port.out.BoxOnboardingRepository
import com.ctosfit.box.domain.AuthenticatedUserId
import com.ctosfit.box.domain.BoxProfile
import java.sql.Connection
import java.util.UUID
import javax.sql.DataSource

class JdbcBoxOnboardingRepository(private val dataSource: DataSource) : BoxOnboardingRepository {
    override fun createWithOwner(box: BoxProfile, ownerId: AuthenticatedUserId): BoxProfile =
        dataSource.connection.use { connection ->
            val oldAutoCommit = connection.autoCommit
            connection.autoCommit = false
            try {
                connection.prepareStatement("INSERT INTO boxes (id, name, time_zone) VALUES (?, ?, ?)").use { statement ->
                    statement.setObject(1, UUID.fromString(box.id.value))
                    statement.setString(2, box.name)
                    statement.setString(3, box.timeZone)
                    statement.executeUpdate()
                }
                connection.prepareStatement(
                    "INSERT INTO box_memberships (id, user_id, box_id, role, status) VALUES (?, ?, ?, 'BOX_OWNER', 'ACTIVE')",
                ).use { statement ->
                    statement.setObject(1, UUID.randomUUID())
                    statement.setString(2, ownerId.value)
                    statement.setObject(3, UUID.fromString(box.id.value))
                    statement.executeUpdate()
                }
                connection.commit()
                box
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = oldAutoCommit
            }
        }
}
