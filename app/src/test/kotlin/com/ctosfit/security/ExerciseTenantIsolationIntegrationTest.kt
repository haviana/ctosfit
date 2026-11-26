package com.ctosfit.security

import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.web.context.WebApplicationContext
import kotlin.test.assertContains
import kotlin.test.assertEquals
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest
class ExerciseTenantIsolationIntegrationTest @Autowired constructor(
    context: WebApplicationContext,
    private val jdbcTemplate: JdbcTemplate,
) {
    private val mockMvc: MockMvc = MockMvcBuilders.webAppContextSetup(context).apply<DefaultMockMvcBuilder>(springSecurity()).build()
    @BeforeEach
    fun setUp() {
        jdbcTemplate.update("TRUNCATE TABLE exercise_equipment, exercises, box_memberships, boxes CASCADE")
        jdbcTemplate.update("INSERT INTO boxes (id, name) VALUES (?, ?), (?, ?)", BOX_A, "North", BOX_B, "South")
        insertMembership(USER_A, BOX_A)
        insertMembership(USER_A, BOX_B)
        insertMembership(USER_ATHLETE, BOX_A, "ATHLETE")
    }

    @Test
    fun `exercise lists and mutations remain within selected membership box`() {
        val createResponse = mockMvc.perform(
            post("/api/coaches/exercises")
                .with(coach(USER_A))
                .header(BOX_HEADER, BOX_A.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Back squat","category":"Strength","equipment":["Barbell"]}"""),
        )
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .contentAsString
        val exerciseId = Regex(""""id":"([^"]+)"""").find(createResponse)!!.groupValues[1]

        mockMvc.perform(
            get("/api/coaches/exercises")
                .with(coach(USER_A))
                .header(BOX_HEADER, BOX_B.toString()),
        )
            .andExpect(status().isOk)
            .andExpect(content().json("[]"))

        mockMvc.perform(
            put("/api/coaches/exercises/{id}", exerciseId)
                .with(coach(USER_A))
                .header(BOX_HEADER, BOX_B.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Changed","category":"Strength"}"""),
        ).andExpect(status().isNotFound)

        mockMvc.perform(
            get("/api/coaches/exercises")
                .with(coach("user-without-membership"))
                .header(BOX_HEADER, BOX_A.toString()),
        ).andExpect(status().isForbidden)

        val storedName = jdbcTemplate.queryForObject(
            "SELECT name FROM exercises WHERE id = ? AND box_id = ?",
            String::class.java,
            UUID.fromString(exerciseId),
            BOX_A,
        )
        assertEquals("Back squat", storedName)
    }

    @Test
    fun `coach publication is visible to box athletes and results remain linked to that version`() {
        val exerciseResponse = mockMvc.perform(
            post("/api/coaches/exercises")
                .with(coach(USER_A))
                .header(BOX_HEADER, BOX_A.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Thruster","category":"Weightlifting","equipment":["Barbell"]}"""),
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        val exerciseId = extractId(exerciseResponse)

        val draftResponse = mockMvc.perform(
            post("/api/coaches/workouts")
                .with(coach(USER_A))
                .header(BOX_HEADER, BOX_A.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """{"title":"Fran","blocks":[{"title":"Metcon","intent":"CONDITIONING","format":{"kind":"FOR_TIME","timeCapSeconds":600},"movements":[{"exerciseId":"$exerciseId","movementName":"Thruster","variants":{"RX":[{"kind":"REPETITIONS","value":"21"}],"SCALED":[{"kind":"REPETITIONS","value":"15"}],"BEGINNER":[{"kind":"REPETITIONS","value":"9"}]}}]}]}""",
                ),
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        val workoutId = extractId(draftResponse)

        val publicationResponse = mockMvc.perform(
            post("/api/coaches/workouts/{id}/publish", workoutId)
                .with(coach(USER_A))
                .header(BOX_HEADER, BOX_A.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"scheduledFor":"2026-11-24"}"""),
        ).andExpect(status().isOk).andReturn().response.contentAsString
        val publicationId = extractId(publicationResponse)
        assertContains(publicationResponse, "\"publishedBy\":\"$USER_A\"")

        mockMvc.perform(
            get("/api/athletes/workouts")
                .param("date", "2026-11-24")
                .with(athlete(USER_ATHLETE))
                .header(BOX_HEADER, BOX_A.toString()),
        ).andExpect(status().isOk).andExpect(content().json("""[{"id":"$publicationId"}]"""))

        mockMvc.perform(
            put("/api/coaches/workouts/{id}/title", workoutId)
                .with(coach(USER_A))
                .header(BOX_HEADER, BOX_A.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"Fran revised"}"""),
        ).andExpect(status().isOk)

        val publishedAfterEdit = mockMvc.perform(
            get("/api/athletes/publications/{id}", publicationId)
                .with(athlete(USER_ATHLETE))
                .header(BOX_HEADER, BOX_A.toString()),
        ).andExpect(status().isOk).andReturn().response.contentAsString
        assertContains(publishedAfterEdit, "\"title\":\"Fran\"")
        assertEquals(false, publishedAfterEdit.contains("Fran revised"))

        mockMvc.perform(
            get("/api/athletes/publications/{id}", publicationId)
                .with(athlete(USER_ATHLETE))
                .header(BOX_HEADER, BOX_B.toString()),
        ).andExpect(status().isForbidden)

        val resultResponse = mockMvc.perform(
            post("/api/athletes/results")
                .with(athlete(USER_ATHLETE))
                .header(BOX_HEADER, BOX_A.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """{"publicationId":"$publicationId","metricId":"$exerciseId","prescriptionId":"RX","score":{"type":"TIME","elapsedSeconds":180,"timeOutcome":"COMPLETED"}}""",
                ),
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        val resultId = extractId(resultResponse)

        val athleteHistory = mockMvc.perform(
            get("/api/athletes/results")
                .with(athlete(USER_ATHLETE))
                .header(BOX_HEADER, BOX_A.toString()),
        ).andExpect(status().isOk).andReturn().response.contentAsString
        assertContains(athleteHistory, publicationId)
        assertContains(athleteHistory, "\"athleteId\":\"$USER_ATHLETE\"")

        val personalBests = mockMvc.perform(
            get("/api/athletes/personal-bests")
                .with(athlete(USER_ATHLETE))
                .header(BOX_HEADER, BOX_A.toString()),
        ).andExpect(status().isOk).andReturn().response.contentAsString
        assertContains(personalBests, resultId)

        val coachResults = mockMvc.perform(
            get("/api/coaches/results")
                .with(coach(USER_A))
                .header(BOX_HEADER, BOX_A.toString()),
        ).andExpect(status().isOk).andReturn().response.contentAsString
        assertContains(coachResults, "\"athleteId\":\"$USER_ATHLETE\"")

        val otherBoxResults = mockMvc.perform(
            get("/api/coaches/results")
                .with(coach(USER_A))
                .header(BOX_HEADER, BOX_B.toString()),
        ).andExpect(status().isOk).andReturn().response.contentAsString
        assertEquals("[]", otherBoxResults)
    }

    private fun extractId(json: String): String = Regex(""""id":"([^"]+)"""").find(json)!!.groupValues[1]

    private fun insertMembership(userId: String, boxId: UUID, role: String = "COACH") {
        jdbcTemplate.update(
            "INSERT INTO box_memberships (id, user_id, box_id, role, status) VALUES (?, ?, ?, ?, 'ACTIVE')",
            UUID.randomUUID(),
            userId,
            boxId,
            role,
        )
    }

    private fun coach(userId: String) = jwt()
        .jwt { it.subject(userId) }
        .authorities(SimpleGrantedAuthority("COACH"))

    private fun athlete(userId: String) = jwt()
        .jwt { it.subject(userId) }
        .authorities(SimpleGrantedAuthority("ATHLETE"))

    companion object {
        private const val BOX_HEADER = "X-Box-Id"
        private const val USER_A = "oidc-user-a"
        private const val USER_ATHLETE = "oidc-athlete-a"
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
    }
}
