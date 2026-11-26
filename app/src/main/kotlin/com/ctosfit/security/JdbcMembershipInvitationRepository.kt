package com.ctosfit.security

import com.ctosfit.identity.application.port.out.MembershipInvitationRepository
import com.ctosfit.identity.application.port.out.StoredMembershipInvitation
import com.ctosfit.identity.domain.BoxId
import com.ctosfit.identity.domain.Role
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID
import javax.sql.DataSource

class JdbcMembershipInvitationRepository(private val dataSource: DataSource) : MembershipInvitationRepository {
    override fun create(boxId: BoxId, email: String, role: Role, tokenHash: String, expiresAt: Instant, invitedBy: String): StoredMembershipInvitation = dataSource.connection.use { connection ->
        val id = UUID.randomUUID()
        connection.prepareStatement("INSERT INTO membership_invitations (id, box_id, email, role, token_hash, expires_at, invited_by) VALUES (?, ?, ?, ?, ?, ?, ?)").use { statement ->
            statement.setObject(1, id); statement.setObject(2, UUID.fromString(boxId.value)); statement.setString(3, email); statement.setString(4, role.name); statement.setString(5, tokenHash); statement.setTimestamp(6, Timestamp.from(expiresAt)); statement.setString(7, invitedBy); statement.executeUpdate()
        }
        StoredMembershipInvitation(id.toString(), boxId, email, role, tokenHash, expiresAt, null)
    }

    override fun findPendingByTokenHash(tokenHash: String, now: Instant): StoredMembershipInvitation? = dataSource.connection.use { connection ->
        connection.prepareStatement("SELECT id, box_id, email, role, token_hash, expires_at, accepted_at FROM membership_invitations WHERE token_hash = ? AND accepted_at IS NULL AND expires_at > ?").use { statement ->
            statement.setString(1, tokenHash); statement.setTimestamp(2, Timestamp.from(now))
            statement.executeQuery().use { rows -> if (rows.next()) rows.toInvitation() else null }
        }
    }

    override fun markAccepted(id: String, acceptedAt: Instant): Boolean = dataSource.connection.use { connection ->
        connection.prepareStatement("UPDATE membership_invitations SET accepted_at = ? WHERE id = ? AND accepted_at IS NULL").use { statement ->
            statement.setTimestamp(1, Timestamp.from(acceptedAt)); statement.setObject(2, UUID.fromString(id)); statement.executeUpdate() == 1
        }
    }

    private fun ResultSet.toInvitation() = StoredMembershipInvitation(
        getObject("id", UUID::class.java).toString(), BoxId(getObject("box_id", UUID::class.java).toString()), getString("email"), Role.valueOf(getString("role")), getString("token_hash"), getTimestamp("expires_at").toInstant(), getTimestamp("accepted_at")?.toInstant(),
    )
}
