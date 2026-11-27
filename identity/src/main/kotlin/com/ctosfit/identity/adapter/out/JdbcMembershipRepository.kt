package com.ctosfit.identity.adapter.out

import com.ctosfit.identity.application.port.out.ManageMembershipsRepository
import com.ctosfit.identity.application.port.out.MembershipRepository
import com.ctosfit.identity.domain.BoxId
import com.ctosfit.identity.domain.Membership
import com.ctosfit.identity.domain.MembershipId
import com.ctosfit.identity.domain.MembershipStatus
import com.ctosfit.identity.domain.Role
import com.ctosfit.identity.domain.UserId
import java.sql.Connection
import java.sql.ResultSet
import java.util.UUID
import javax.sql.DataSource

class JdbcMembershipRepository(private val dataSource: DataSource) : MembershipRepository, ManageMembershipsRepository {
    override fun findByUserAndBox(userId: UserId, boxId: BoxId): Membership? = dataSource.connection.use { find(it, userId, boxId) }

    override fun findAllByBox(boxId: BoxId): List<Membership> = dataSource.connection.use { connection ->
        connection.prepareStatement("SELECT id, user_id, box_id, role, status FROM box_memberships WHERE box_id = ? ORDER BY created_at, id").use { statement ->
            statement.setObject(1, UUID.fromString(boxId.value))
            statement.executeQuery().use { rows -> buildList { while (rows.next()) add(rows.toMembership()) } }
        }
    }

    override fun addOrReactivate(userId: UserId, boxId: BoxId, role: Role): Membership = inBoxTransaction(boxId) { connection ->
        val existing = find(connection, userId, boxId)
        if (existing == null) {
            val membership = Membership.active(MembershipId(UUID.randomUUID().toString()), userId, boxId, role)
            connection.prepareStatement("INSERT INTO box_memberships (id, user_id, box_id, role, status) VALUES (?, ?, ?, ?, 'ACTIVE')").use { statement ->
                statement.setObject(1, UUID.fromString(membership.id.value)); statement.setString(2, userId.value)
                statement.setObject(3, UUID.fromString(boxId.value)); statement.setString(4, role.name); statement.executeUpdate()
            }
            membership
        } else {
            if (existing.status == MembershipStatus.ACTIVE && existing.role == Role.BOX_OWNER && role != Role.BOX_OWNER) {
                ensureAnotherOwner(connection, boxId, userId)
            }
            connection.prepareStatement("UPDATE box_memberships SET role = ?, status = 'ACTIVE' WHERE id = ? AND box_id = ?").use { statement ->
                statement.setString(1, role.name); statement.setObject(2, UUID.fromString(existing.id.value)); statement.setObject(3, UUID.fromString(boxId.value)); statement.executeUpdate()
            }
            Membership.active(existing.id, userId, boxId, role)
        }
    }

    override fun changeRole(userId: UserId, boxId: BoxId, role: Role): Membership? = inBoxTransaction(boxId) { connection ->
        val existing = find(connection, userId, boxId)?.takeIf { it.status == MembershipStatus.ACTIVE } ?: return@inBoxTransaction null
        if (existing.role == Role.BOX_OWNER && role != Role.BOX_OWNER) ensureAnotherOwner(connection, boxId, userId)
        connection.prepareStatement("UPDATE box_memberships SET role = ? WHERE id = ? AND box_id = ? AND status = 'ACTIVE'").use { statement ->
            statement.setString(1, role.name); statement.setObject(2, UUID.fromString(existing.id.value)); statement.setObject(3, UUID.fromString(boxId.value))
            if (statement.executeUpdate() == 0) return@inBoxTransaction null
        }
        Membership.active(existing.id, userId, boxId, role)
    }

    override fun deactivate(userId: UserId, boxId: BoxId): Membership? = inBoxTransaction(boxId) { connection ->
        val existing = find(connection, userId, boxId)?.takeIf { it.status == MembershipStatus.ACTIVE } ?: return@inBoxTransaction null
        if (existing.role == Role.BOX_OWNER) ensureAnotherOwner(connection, boxId, userId)
        connection.prepareStatement("UPDATE box_memberships SET status = 'INACTIVE' WHERE id = ? AND box_id = ? AND status = 'ACTIVE'").use { statement ->
            statement.setObject(1, UUID.fromString(existing.id.value)); statement.setObject(2, UUID.fromString(boxId.value))
            if (statement.executeUpdate() == 0) return@inBoxTransaction null
        }
        existing.deactivate()
    }

    private fun ensureAnotherOwner(connection: Connection, boxId: BoxId, excluded: UserId) {
        connection.prepareStatement("SELECT count(*) FROM box_memberships WHERE box_id = ? AND role = 'BOX_OWNER' AND status = 'ACTIVE' AND user_id <> ?").use { statement ->
            statement.setObject(1, UUID.fromString(boxId.value)); statement.setString(2, excluded.value)
            statement.executeQuery().use { rows -> rows.next(); check(rows.getLong(1) > 0) { "A box must retain at least one active owner" } }
        }
    }

    private fun <T> inBoxTransaction(boxId: BoxId, work: (Connection) -> T): T = dataSource.connection.use { connection ->
        val oldAutoCommit = connection.autoCommit; connection.autoCommit = false
        try {
            connection.prepareStatement("SELECT id FROM boxes WHERE id = ? FOR UPDATE").use { statement ->
                statement.setObject(1, UUID.fromString(boxId.value)); statement.executeQuery().use { rows -> check(rows.next()) { "Box was not found" } }
            }
            work(connection).also { connection.commit() }
        } catch (error: Throwable) { connection.rollback(); throw error } finally { connection.autoCommit = oldAutoCommit }
    }

    private fun find(connection: Connection, userId: UserId, boxId: BoxId): Membership? = connection.prepareStatement("SELECT id, user_id, box_id, role, status FROM box_memberships WHERE user_id = ? AND box_id = ?").use { statement ->
        statement.setString(1, userId.value); statement.setObject(2, UUID.fromString(boxId.value))
        statement.executeQuery().use { rows -> if (rows.next()) rows.toMembership() else null }
    }

    private fun ResultSet.toMembership(): Membership {
        val active = Membership.active(MembershipId(getObject("id", UUID::class.java).toString()), UserId(getString("user_id")), BoxId(getObject("box_id", UUID::class.java).toString()), Role.valueOf(getString("role")))
        return if (getString("status") == MembershipStatus.ACTIVE.name) active else active.deactivate()
    }
}
