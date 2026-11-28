package com.ctosfit.security

import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.hamcrest.Matchers.everyItem
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Cross-tenant denial against real PostgreSQL. Callers who belong to both boxes are used
 * for identifier and list checks, so a 404 or an empty list is data scoping and not a
 * missing-membership 403.
 */
@Tag("tenant-isolation")
@Testcontainers(disabledWithoutDocker = false)
@SpringBootTest
class PostgreSqlTenantIsolationTest @Autowired constructor(
    context: WebApplicationContext,
    private val jdbcTemplate: JdbcTemplate,
) {
    private val mockMvc: MockMvc =
        MockMvcBuilders.webAppContextSetup(context).apply<DefaultMockMvcBuilder>(springSecurity()).build()

    @BeforeEach
    fun setUp() {
        jdbcTemplate.update(
            """
            TRUNCATE TABLE
                workout_results,
                publication_prescriptions,
                publication_metrics,
                workout_publications,
                workout_drafts,
                exercise_equipment,
                exercises,
                membership_invitations,
                box_memberships,
                boxes
            RESTART IDENTITY CASCADE
            """.trimIndent(),
        )
        jdbcTemplate.update(
            "INSERT INTO boxes (id, name) VALUES (?, ?), (?, ?)",
            BOX_A,
            "North",
            BOX_B,
            "South",
        )
        insertMembership(OWNER_A, BOX_A, "BOX_OWNER")
        insertMembership(OWNER_B, BOX_B, "BOX_OWNER")
        insertMembership(OWNER_BOTH, BOX_A, "BOX_OWNER")
        insertMembership(OWNER_BOTH, BOX_B, "BOX_OWNER")
        insertMembership(COACH_A, BOX_A, "COACH")
        insertMembership(COACH_BOTH, BOX_A, "COACH")
        insertMembership(COACH_BOTH, BOX_B, "COACH")
        insertMembership(ATHLETE_A, BOX_A, "ATHLETE")
        insertMembership(ATHLETE_A2, BOX_A, "ATHLETE")
        insertMembership(ATHLETE_B, BOX_B, "ATHLETE")
        insertMembership(ATHLETE_BOTH, BOX_A, "ATHLETE")
        insertMembership(ATHLETE_BOTH, BOX_B, "ATHLETE")
        insertMembership(INACTIVE_MEMBER, BOX_A, "COACH", "INACTIVE")
        insertMembership(INACTIVE_MEMBER, BOX_B, "ATHLETE")
    }

    @Test
    fun `exercises deny cross-tenant search update archive and guessed identifiers`() {
        val north = createExercise(COACH_BOTH, BOX_A, "North-only snatch")
        val south = createExercise(COACH_BOTH, BOX_B, "South-only clean")

        mockMvc.perform(get("/api/coaches/exercises").with(coach(COACH_BOTH)).header(BOX_HEADER, BOX_A.toString()).param("query", "only"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[*].id", hasItem(north)))
            .andExpect(jsonPath("$[*].id", not(hasItem(south))))
            .andExpect(jsonPath("$[*].name", not(hasItem("South-only clean"))))

        mockMvc.perform(
            get("/api/coaches/exercises")
                .with(coach(COACH_BOTH))
                .header(BOX_HEADER, BOX_B.toString())
                .param("query", "North-only")
                .param("includeArchived", "true"),
        ).andExpect(status().isOk).andExpect(content().json("[]"))

        mockMvc.perform(
            put("/api/coaches/exercises/{id}", north)
                .with(coach(COACH_BOTH))
                .header(BOX_HEADER, BOX_B.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(exerciseBody("Changed from south")),
        ).andExpect(status().isNotFound)
        assertEquals("North-only snatch", exerciseName(north))

        mockMvc.perform(
            post("/api/coaches/exercises/{id}/archive", north)
                .with(coach(COACH_BOTH))
                .header(BOX_HEADER, BOX_B.toString()),
        ).andExpect(status().isNotFound)
        assertEquals(0, count("SELECT count(*) FROM exercises WHERE id = ? AND archived_at IS NOT NULL", UUID.fromString(north)))

        val guessed = UUID.randomUUID()
        mockMvc.perform(
            put("/api/coaches/exercises/{id}", guessed)
                .with(coach(COACH_BOTH))
                .header(BOX_HEADER, BOX_A.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(exerciseBody("Guessed")),
        ).andExpect(status().isNotFound)
        assertEquals(0, count("SELECT count(*) FROM exercises WHERE id = ?", guessed))

        mockMvc.perform(
            post("/api/coaches/exercises/{id}/archive", guessed)
                .with(coach(COACH_BOTH))
                .header(BOX_HEADER, BOX_A.toString()),
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `workout drafts deny cross-tenant writes nested block edits and guessed identifiers`() {
        val exerciseId = createExercise(COACH_BOTH, BOX_A, "Thruster")
        val draft = createDraft(COACH_BOTH, BOX_A, "North Fran", exerciseId)
        val versionBefore = draftVersion(draft.workoutId)

        listOf(
            put("/api/coaches/workouts/{id}/title", draft.workoutId).contentType(MediaType.APPLICATION_JSON).content("""{"title":"Stolen"}"""),
            post("/api/coaches/workouts/{id}/blocks", draft.workoutId).contentType(MediaType.APPLICATION_JSON).content(addBlockBody(exerciseId)),
            put("/api/coaches/workouts/{id}/blocks/{blockId}", draft.workoutId, draft.blockId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(blockBody(exerciseId, "Replaced")),
            patch("/api/coaches/workouts/{id}/blocks/{blockId}/position", draft.workoutId, draft.blockId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"position":0}"""),
            delete("/api/coaches/workouts/{id}/blocks/{blockId}", draft.workoutId, draft.blockId),
            post("/api/coaches/workouts/{id}/publish", draft.workoutId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"scheduledFor":"2026-11-24"}"""),
        ).forEach { request ->
            mockMvc.perform(request.with(coach(COACH_BOTH)).header(BOX_HEADER, BOX_B.toString()))
                .andExpect(status().isNotFound)
        }

        assertEquals("North Fran", draftTitle(draft.workoutId))
        assertEquals(versionBefore, draftVersion(draft.workoutId))
        assertEquals(0, count("SELECT count(*) FROM workout_publications WHERE workout_id = ?", UUID.fromString(draft.workoutId)))
        assertTrue(draftContent(draft.workoutId).contains("Metcon"))

        val guessed = UUID.randomUUID()
        mockMvc.perform(
            put("/api/coaches/workouts/{id}/title", guessed)
                .with(coach(COACH_BOTH))
                .header(BOX_HEADER, BOX_A.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"Guessed"}"""),
        ).andExpect(status().isNotFound)
        assertEquals(0, count("SELECT count(*) FROM workout_drafts WHERE id = ?", guessed))
    }

    @Test
    fun `publications deny cross-tenant lists reads guessed ids and foreign exercises`() {
        val northExercise = createExercise(COACH_BOTH, BOX_A, "North thruster")
        val southExercise = createExercise(COACH_BOTH, BOX_B, "South thruster")
        val north = publish(COACH_BOTH, BOX_A, "North Fran", northExercise, "2026-11-24")
        val south = publish(COACH_BOTH, BOX_B, "South Fran", southExercise, "2026-11-24")

        mockMvc.perform(
            get("/api/athletes/workouts").param("date", "2026-11-24").with(athlete(ATHLETE_BOTH)).header(BOX_HEADER, BOX_A.toString()),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[*].id", hasItem(north.publicationId)))
            .andExpect(jsonPath("$[*].id", not(hasItem(south.publicationId))))
            .andExpect(jsonPath("$[*].title", not(hasItem("South Fran"))))

        mockMvc.perform(
            get("/api/athletes/publications/{id}", north.publicationId)
                .with(athlete(ATHLETE_BOTH))
                .header(BOX_HEADER, BOX_A.toString()),
        ).andExpect(status().isOk).andExpect(jsonPath("$.title").value("North Fran"))

        mockMvc.perform(
            get("/api/athletes/publications/{id}", north.publicationId)
                .with(athlete(ATHLETE_BOTH))
                .header(BOX_HEADER, BOX_B.toString()),
        ).andExpect(status().isNotFound)

        mockMvc.perform(
            get("/api/athletes/publications/{id}", south.publicationId)
                .with(athlete(ATHLETE_A))
                .header(BOX_HEADER, BOX_A.toString()),
        ).andExpect(status().isNotFound)

        val guessed = UUID.randomUUID()
        mockMvc.perform(
            get("/api/athletes/publications/{id}", guessed)
                .with(athlete(ATHLETE_BOTH))
                .header(BOX_HEADER, BOX_A.toString()),
        ).andExpect(status().isNotFound)
        assertEquals(0, count("SELECT count(*) FROM workout_publications WHERE id = ?", guessed))

        val foreignDraft = createDraft(COACH_BOTH, BOX_A, "Foreign movement", southExercise)
        mockMvc.perform(
            post("/api/coaches/workouts/{id}/publish", foreignDraft.workoutId)
                .with(coach(COACH_BOTH))
                .header(BOX_HEADER, BOX_A.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"scheduledFor":"2026-11-25"}"""),
        ).andExpect(status().isBadRequest)
        assertEquals(
            0,
            count("SELECT count(*) FROM workout_publications WHERE workout_id = ?", UUID.fromString(foreignDraft.workoutId)),
        )
        assertEquals(BOX_B, exerciseBox(southExercise))
    }

    @Test
    fun `results deny cross-tenant submits lists personal bests and other athletes`() {
        val exerciseId = createExercise(COACH_BOTH, BOX_A, "Thruster")
        val north = publish(COACH_BOTH, BOX_A, "North Fran", exerciseId, "2026-11-24")
        val southExercise = createExercise(COACH_BOTH, BOX_B, "South burpee")
        publish(COACH_BOTH, BOX_B, "South chipper", southExercise, "2026-11-24")

        val northResult = submitResult(ATHLETE_BOTH, BOX_A, north.publicationId, exerciseId)
        val secondAthleteResult = submitResult(ATHLETE_A2, BOX_A, north.publicationId, exerciseId)

        mockMvc.perform(
            post("/api/athletes/results")
                .with(athlete(ATHLETE_BOTH))
                .header(BOX_HEADER, BOX_B.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(resultBody(north.publicationId, exerciseId)),
        ).andExpect(status().isNotFound)
        assertEquals(2, count("SELECT count(*) FROM workout_results WHERE publication_id = ?", UUID.fromString(north.publicationId)))
        assertEquals(BOX_A, resultBox(northResult))

        val guessedPublication = UUID.randomUUID()
        mockMvc.perform(
            post("/api/athletes/results")
                .with(athlete(ATHLETE_A))
                .header(BOX_HEADER, BOX_A.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(resultBody(guessedPublication.toString(), exerciseId)),
        ).andExpect(status().isNotFound)
        assertEquals(0, count("SELECT count(*) FROM workout_results WHERE publication_id = ?", guessedPublication))

        mockMvc.perform(get("/api/athletes/results").with(athlete(ATHLETE_BOTH)).header(BOX_HEADER, BOX_B.toString()))
            .andExpect(status().isOk)
            .andExpect(content().json("[]"))
        mockMvc.perform(get("/api/athletes/personal-bests").with(athlete(ATHLETE_BOTH)).header(BOX_HEADER, BOX_B.toString()))
            .andExpect(status().isOk)
            .andExpect(content().json("[]"))

        mockMvc.perform(get("/api/athletes/results").with(athlete(ATHLETE_BOTH)).header(BOX_HEADER, BOX_A.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[*].id", hasItem(northResult)))
            .andExpect(jsonPath("$[*].id", not(hasItem(secondAthleteResult))))
            .andExpect(jsonPath("$[*].athleteId", everyItem(org.hamcrest.Matchers.equalTo(ATHLETE_BOTH))))

        mockMvc.perform(get("/api/athletes/personal-bests").with(athlete(ATHLETE_A2)).header(BOX_HEADER, BOX_A.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[*].resultId", hasItem(secondAthleteResult)))
            .andExpect(jsonPath("$[*].resultId", not(hasItem(northResult))))

        mockMvc.perform(get("/api/coaches/results").with(coach(COACH_BOTH)).header(BOX_HEADER, BOX_A.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[*].id", hasItem(northResult)))
            .andExpect(jsonPath("$[*].id", hasItem(secondAthleteResult)))
        mockMvc.perform(get("/api/coaches/results").with(coach(COACH_BOTH)).header(BOX_HEADER, BOX_B.toString()))
            .andExpect(status().isOk)
            .andExpect(content().json("[]"))
        mockMvc.perform(get("/api/coaches/results").with(coach(COACH_A)).header(BOX_HEADER, BOX_B.toString()))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `memberships deny cross-tenant lists writes and guessed users`() {
        mockMvc.perform(get("/api/box-owner/memberships").with(owner(OWNER_BOTH)).header(BOX_HEADER, BOX_A.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[*].userId", hasItem(ATHLETE_A)))
            .andExpect(jsonPath("$[*].userId", not(hasItem(ATHLETE_B))))
            .andExpect(jsonPath("$[*].userId", not(hasItem(OWNER_B))))
            .andExpect(jsonPath("$[*].boxId", everyItem(org.hamcrest.Matchers.equalTo(BOX_A.toString()))))

        mockMvc.perform(get("/api/box-owner/memberships").with(owner(OWNER_BOTH)).header(BOX_HEADER, BOX_B.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[*].userId", hasItem(ATHLETE_B)))
            .andExpect(jsonPath("$[*].userId", not(hasItem(ATHLETE_A))))
            .andExpect(jsonPath("$[*].boxId", everyItem(org.hamcrest.Matchers.equalTo(BOX_B.toString()))))

        mockMvc.perform(get("/api/box-owner/memberships").with(owner(OWNER_A)).header(BOX_HEADER, BOX_B.toString()))
            .andExpect(status().isForbidden)

        mockMvc.perform(
            post("/api/box-owner/memberships")
                .with(owner(OWNER_A))
                .header(BOX_HEADER, BOX_B.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"userId":"intruder","role":"COACH"}"""),
        ).andExpect(status().isForbidden)
        assertEquals(0, count("SELECT count(*) FROM box_memberships WHERE user_id = ?", "intruder"))

        mockMvc.perform(
            post("/api/box-owner/memberships")
                .with(owner(OWNER_BOTH))
                .header(BOX_HEADER, BOX_A.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"userId":"scoped-coach","role":"COACH"}"""),
        ).andExpect(status().isCreated)
        assertEquals(1, count("SELECT count(*) FROM box_memberships WHERE user_id = ? AND box_id = ?", "scoped-coach", BOX_A))
        assertEquals(0, count("SELECT count(*) FROM box_memberships WHERE user_id = ? AND box_id = ?", "scoped-coach", BOX_B))

        mockMvc.perform(
            patch("/api/box-owner/memberships/{userId}", ATHLETE_B)
                .with(owner(OWNER_A))
                .header(BOX_HEADER, BOX_A.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"role":"COACH"}"""),
        ).andExpect(status().isNotFound)
        assertEquals("ATHLETE", membershipRole(ATHLETE_B, BOX_B))

        mockMvc.perform(
            delete("/api/box-owner/memberships/{userId}", ATHLETE_B)
                .with(owner(OWNER_BOTH))
                .header(BOX_HEADER, BOX_A.toString()),
        ).andExpect(status().isNotFound)
        assertEquals("ACTIVE", membershipStatus(ATHLETE_B, BOX_B))

        val guessedUser = "guessed-member"
        mockMvc.perform(
            patch("/api/box-owner/memberships/{userId}", guessedUser)
                .with(owner(OWNER_A))
                .header(BOX_HEADER, BOX_A.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"role":"ATHLETE"}"""),
        ).andExpect(status().isNotFound)
        assertEquals(0, count("SELECT count(*) FROM box_memberships WHERE user_id = ?", guessedUser))

        mockMvc.perform(
            delete("/api/box-owner/memberships/{userId}", OWNER_A)
                .with(owner(OWNER_A))
                .header(BOX_HEADER, BOX_A.toString()),
        ).andExpect(status().isForbidden)
        assertEquals("ACTIVE", membershipStatus(OWNER_A, BOX_A))
        assertEquals("BOX_OWNER", membershipRole(OWNER_A, BOX_A))

        mockMvc.perform(
            post("/api/box-owner/memberships")
                .with(athlete(ATHLETE_A))
                .header(BOX_HEADER, BOX_A.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"userId":"athlete-intruder","role":"COACH"}"""),
        ).andExpect(status().isForbidden)
        assertEquals(0, count("SELECT count(*) FROM box_memberships WHERE user_id = ?", "athlete-intruder"))
    }

    @Test
    fun `inactive memberships lose the selected box and keep an active membership elsewhere`() {
        mockMvc.perform(get("/api/coaches/exercises").with(coach(INACTIVE_MEMBER)).header(BOX_HEADER, BOX_A.toString()))
            .andExpect(status().isForbidden)
        mockMvc.perform(
            get("/api/athletes/workouts").param("date", "2026-11-24").with(athlete(INACTIVE_MEMBER)).header(BOX_HEADER, BOX_B.toString()),
        ).andExpect(status().isOk).andExpect(content().json("[]"))

        val exerciseId = createExercise(COACH_A, BOX_A, "Still north")
        mockMvc.perform(
            delete("/api/box-owner/memberships/{userId}", COACH_A)
                .with(owner(OWNER_A))
                .header(BOX_HEADER, BOX_A.toString()),
        ).andExpect(status().isOk)
        assertEquals("INACTIVE", membershipStatus(COACH_A, BOX_A))

        mockMvc.perform(get("/api/coaches/exercises").with(coach(COACH_A)).header(BOX_HEADER, BOX_A.toString()))
            .andExpect(status().isForbidden)
        mockMvc.perform(
            put("/api/coaches/exercises/{id}", exerciseId)
                .with(coach(COACH_A))
                .header(BOX_HEADER, BOX_A.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(exerciseBody("Tampered after deactivation")),
        ).andExpect(status().isForbidden)
        assertEquals("Still north", exerciseName(exerciseId))

        mockMvc.perform(get("/api/coaches/exercises").with(coach(COACH_BOTH)).header(BOX_HEADER, BOX_A.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[*].id", hasItem(exerciseId)))

        mockMvc.perform(
            post("/api/box-owner/memberships")
                .with(owner(OWNER_A))
                .header(BOX_HEADER, BOX_A.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"userId":"$COACH_A","role":"COACH"}"""),
        ).andExpect(status().isCreated)
        mockMvc.perform(get("/api/coaches/exercises").with(coach(COACH_A)).header(BOX_HEADER, BOX_A.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[*].name", hasItem("Still north")))
    }

    @Test
    fun `an athlete token does not grant athlete routes to an owner or coach membership`() {
        val exerciseId = createExercise(COACH_BOTH, BOX_A, "Role boundary thruster")
        val published = publish(COACH_BOTH, BOX_A, "Role boundary Fran", exerciseId, "2026-11-24")
        val resultsBefore = count("SELECT count(*) FROM workout_results")

        listOf(OWNER_A, COACH_A).forEach { userId ->
            mockMvc.perform(
                get("/api/athletes/workouts").param("date", "2026-11-24").with(athlete(userId)).header(BOX_HEADER, BOX_A.toString()),
            ).andExpect(status().isForbidden)
            mockMvc.perform(
                get("/api/athletes/publications/{id}", published.publicationId)
                    .with(athlete(userId))
                    .header(BOX_HEADER, BOX_A.toString()),
            ).andExpect(status().isForbidden)
            mockMvc.perform(
                post("/api/athletes/results")
                    .with(athlete(userId))
                    .header(BOX_HEADER, BOX_A.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(resultBody(published.publicationId, exerciseId)),
            ).andExpect(status().isForbidden)
        }

        mockMvc.perform(
            delete("/api/box-owner/memberships/{userId}", ATHLETE_A)
                .with(coach(COACH_A))
                .header(BOX_HEADER, BOX_A.toString()),
        ).andExpect(status().isForbidden)
        assertEquals("ACTIVE", membershipStatus(ATHLETE_A, BOX_A))
        assertEquals(resultsBefore, count("SELECT count(*) FROM workout_results"))
    }

    @Test
    fun `selected box header is required and unknown boxes are not authorized`() {
        mockMvc.perform(get("/api/coaches/exercises").with(coach(COACH_BOTH)))
            .andExpect(status().isBadRequest)
        mockMvc.perform(get("/api/coaches/exercises").with(coach(COACH_BOTH)).header(BOX_HEADER, "not-a-uuid"))
            .andExpect(status().isBadRequest)
        mockMvc.perform(get("/api/coaches/exercises").with(coach(COACH_BOTH)).header(BOX_HEADER, UUID.randomUUID().toString()))
            .andExpect(status().isForbidden)
        mockMvc.perform(get("/api/coaches/exercises").with(coach("nobody")).header(BOX_HEADER, BOX_A.toString()))
            .andExpect(status().isForbidden)
    }

    @ParameterizedTest(name = "{0} {1} token={2} user={3} -> {5}")
    @MethodSource("roleBoundaries")
    fun `owner coach and athlete boundaries follow token role and active membership`(
        httpMethod: String,
        path: String,
        tokenRole: String,
        userId: String,
        boxId: UUID,
        expectedStatus: Int,
    ) {
        val request = when (httpMethod) {
            "GET" -> get(path)
            "POST" -> post(path)
            "DELETE" -> delete(path)
            else -> error("Unsupported method $httpMethod")
        }
        mockMvc.perform(request.with(token(userId, tokenRole)).header(BOX_HEADER, boxId.toString()))
            .andExpect(status().`is`(expectedStatus))
    }

    private fun createExercise(userId: String, boxId: UUID, name: String): String {
        val body = mockMvc.perform(
            post("/api/coaches/exercises")
                .with(coach(userId))
                .header(BOX_HEADER, boxId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(exerciseBody(name)),
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        return extractId(body)
    }

    private fun createDraft(userId: String, boxId: UUID, title: String, exerciseId: String): DraftRef {
        val body = mockMvc.perform(
            post("/api/coaches/workouts")
                .with(coach(userId))
                .header(BOX_HEADER, boxId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"$title","blocks":[${blockBody(exerciseId, "Metcon")}]}"""),
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        return DraftRef(extractId(body), extractBlockId(body))
    }

    private fun publish(userId: String, boxId: UUID, title: String, exerciseId: String, date: String): PublishedRef {
        val draft = createDraft(userId, boxId, title, exerciseId)
        val body = mockMvc.perform(
            post("/api/coaches/workouts/{id}/publish", draft.workoutId)
                .with(coach(userId))
                .header(BOX_HEADER, boxId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"scheduledFor":"$date"}"""),
        ).andExpect(status().isOk).andReturn().response.contentAsString
        return PublishedRef(draft.workoutId, draft.blockId, extractId(body))
    }

    private fun submitResult(userId: String, boxId: UUID, publicationId: String, metricId: String): String {
        val body = mockMvc.perform(
            post("/api/athletes/results")
                .with(athlete(userId))
                .header(BOX_HEADER, boxId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(resultBody(publicationId, metricId)),
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        assertTrue(body.contains(userId))
        return extractId(body)
    }

    private fun exerciseBody(name: String) =
        """{"name":"$name","category":"Strength","equipment":["Barbell"]}"""

    private fun movement(exerciseId: String) =
        """{"exerciseId":"$exerciseId","movementName":"Thruster","variants":{"RX":[{"kind":"REPETITIONS","value":"21"}],"SCALED":[{"kind":"REPETITIONS","value":"15"}],"BEGINNER":[{"kind":"REPETITIONS","value":"9"}]}}"""

    private fun blockBody(exerciseId: String, title: String) =
        """{"title":"$title","intent":"CONDITIONING","format":{"kind":"FOR_TIME","timeCapSeconds":600},"movements":[${movement(exerciseId)}]}"""

    private fun addBlockBody(exerciseId: String) = """{"block":${blockBody(exerciseId, "Accessory")},"position":1}"""

    private fun resultBody(publicationId: String, metricId: String) =
        """{"publicationId":"$publicationId","metricId":"$metricId","prescriptionId":"RX","score":{"type":"TIME","elapsedSeconds":180,"timeOutcome":"COMPLETED"}}"""

    private fun insertMembership(userId: String, boxId: UUID, role: String, status: String = "ACTIVE") {
        jdbcTemplate.update(
            "INSERT INTO box_memberships (id, user_id, box_id, role, status) VALUES (?, ?, ?, ?, ?)",
            UUID.randomUUID(),
            userId,
            boxId,
            role,
            status,
        )
    }

    private fun count(sql: String, vararg args: Any): Int =
        jdbcTemplate.queryForObject(sql, Int::class.java, *args)!!

    private fun exerciseName(exerciseId: String): String =
        jdbcTemplate.queryForObject(
            "SELECT name FROM exercises WHERE id = ?",
            String::class.java,
            UUID.fromString(exerciseId),
        )!!

    private fun exerciseBox(exerciseId: String): UUID =
        jdbcTemplate.queryForObject(
            "SELECT box_id FROM exercises WHERE id = ?",
            UUID::class.java,
            UUID.fromString(exerciseId),
        )!!

    private fun draftTitle(workoutId: String): String =
        jdbcTemplate.queryForObject(
            "SELECT title FROM workout_drafts WHERE id = ?",
            String::class.java,
            UUID.fromString(workoutId),
        )!!

    private fun draftVersion(workoutId: String): Int =
        jdbcTemplate.queryForObject(
            "SELECT version FROM workout_drafts WHERE id = ?",
            Int::class.java,
            UUID.fromString(workoutId),
        )!!

    private fun draftContent(workoutId: String): String =
        jdbcTemplate.queryForObject(
            "SELECT content_json::text FROM workout_drafts WHERE id = ?",
            String::class.java,
            UUID.fromString(workoutId),
        )!!

    private fun resultBox(resultId: String): UUID =
        jdbcTemplate.queryForObject(
            "SELECT box_id FROM workout_results WHERE id = ?",
            UUID::class.java,
            UUID.fromString(resultId),
        )!!

    private fun membershipRole(userId: String, boxId: UUID): String =
        jdbcTemplate.queryForObject(
            "SELECT role FROM box_memberships WHERE user_id = ? AND box_id = ?",
            String::class.java,
            userId,
            boxId,
        )!!

    private fun membershipStatus(userId: String, boxId: UUID): String =
        jdbcTemplate.queryForObject(
            "SELECT status FROM box_memberships WHERE user_id = ? AND box_id = ?",
            String::class.java,
            userId,
            boxId,
        )!!

    private fun extractId(json: String): String = Regex(""""id"\s*:\s*"([^"]+)"""").find(json)!!.groupValues[1]

    private fun extractBlockId(json: String): String =
        Regex(""""blocks"\s*:\s*\[\s*\{\s*"id"\s*:\s*"([^"]+)"""").find(json)!!.groupValues[1]

    private fun token(userId: String, role: String) =
        jwt().jwt { it.subject(userId) }.authorities(SimpleGrantedAuthority(role))

    private fun coach(userId: String) = token(userId, "COACH")

    private fun athlete(userId: String) = token(userId, "ATHLETE")

    private fun owner(userId: String) = token(userId, "BOX_OWNER")

    private data class DraftRef(val workoutId: String, val blockId: String)

    private data class PublishedRef(val workoutId: String, val blockId: String, val publicationId: String)

    companion object {
        private const val BOX_HEADER = "X-Box-Id"
        private const val OWNER_A = "owner-a"
        private const val OWNER_B = "owner-b"
        private const val OWNER_BOTH = "owner-both"
        private const val COACH_A = "coach-a"
        private const val COACH_BOTH = "coach-both"
        private const val ATHLETE_A = "athlete-a"
        private const val ATHLETE_A2 = "athlete-a2"
        private const val ATHLETE_B = "athlete-b"
        private const val ATHLETE_BOTH = "athlete-both"
        private const val INACTIVE_MEMBER = "inactive-member"
        private val BOX_A = UUID.fromString("00000000-0000-0000-0000-0000000000a1")
        private val BOX_B = UUID.fromString("00000000-0000-0000-0000-0000000000b2")

        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer<Nothing>("postgres:17")

        @DynamicPropertySource
        @JvmStatic
        fun databaseProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("ctosfit.security.issuer-uri") { "https://test.invalid/issuer" }
            registry.add("ctosfit.security.jwk-set-uri") { "https://test.invalid/jwks" }
        }

        @JvmStatic
        fun roleBoundaries(): List<Arguments> {
            val workouts = "/api/athletes/workouts?date=2026-11-24"
            return listOf(
                Arguments.of("GET", "/api/coaches/exercises", "ATHLETE", ATHLETE_A, BOX_A, 403),
                Arguments.of("GET", "/api/coaches/exercises", "COACH", ATHLETE_A, BOX_A, 403),
                Arguments.of("GET", "/api/box-owner/memberships", "BOX_OWNER", COACH_A, BOX_A, 403),
                Arguments.of("GET", workouts, "COACH", COACH_A, BOX_A, 403),
                Arguments.of("POST", "/api/coaches/workouts", "ATHLETE", ATHLETE_A, BOX_A, 403),
                Arguments.of("DELETE", "/api/box-owner/memberships/$ATHLETE_A", "COACH", COACH_A, BOX_A, 403),
                Arguments.of("POST", "/api/athletes/results", "BOX_OWNER", OWNER_A, BOX_A, 403),
                Arguments.of("GET", "/api/coaches/results", "ATHLETE", ATHLETE_A, BOX_A, 403),
                Arguments.of("GET", "/api/athletes/personal-bests", "COACH", COACH_BOTH, BOX_B, 403),
                Arguments.of("GET", "/api/box-owner/memberships", "COACH", OWNER_A, BOX_A, 200),
                Arguments.of("GET", "/api/coaches/exercises", "BOX_OWNER", OWNER_A, BOX_A, 200),
                Arguments.of("GET", "/api/coaches/exercises", "COACH", COACH_A, BOX_A, 200),
                Arguments.of("GET", workouts, "ATHLETE", ATHLETE_A, BOX_A, 200),
                Arguments.of("GET", "/api/coaches/exercises", "COACH", COACH_BOTH, BOX_B, 200),
                Arguments.of("GET", "/api/coaches/exercises", "COACH", COACH_A, BOX_B, 403),
                Arguments.of("GET", "/api/coaches/exercises", "BOX_OWNER", OWNER_A, BOX_B, 403),
                Arguments.of("GET", workouts, "ATHLETE", INACTIVE_MEMBER, BOX_A, 403),
                Arguments.of("GET", workouts, "ATHLETE", INACTIVE_MEMBER, BOX_B, 200),
            )
        }
    }
}
