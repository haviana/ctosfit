package com.ctosfit.security

import com.ctosfit.box.application.port.`in`.ResolveTenantContext
import com.ctosfit.box.application.port.`in`.CreateBox
import com.ctosfit.box.application.port.out.ActiveMembershipLookup
import com.ctosfit.box.application.port.out.AuthenticatedUserProvider
import com.ctosfit.box.application.port.out.BoxOnboardingRepository
import com.ctosfit.box.application.service.ResolveTenantContextService
import com.ctosfit.box.application.service.CreateBoxService
import com.ctosfit.box.domain.AuthenticatedUserId
import com.ctosfit.box.domain.BoxRole
import com.ctosfit.exercise.adapter.out.JdbcExerciseRepository
import com.ctosfit.exercise.application.port.`in`.ManageExercises
import com.ctosfit.exercise.application.port.out.ExerciseRepository
import com.ctosfit.exercise.application.port.out.CurrentTenantProvider
import com.ctosfit.exercise.application.service.ManageExercisesService
import com.ctosfit.exercise.domain.TenantId
import com.ctosfit.identity.adapter.out.JdbcMembershipRepository
import com.ctosfit.identity.application.port.`in`.FindActiveMembership
import com.ctosfit.identity.application.port.out.MembershipRepository
import com.ctosfit.identity.application.port.out.ManageMembershipsRepository
import com.ctosfit.identity.application.port.out.CurrentMembershipAdministrator
import com.ctosfit.identity.application.port.out.MembershipAdministratorContext
import com.ctosfit.identity.application.port.`in`.ManageBoxMemberships
import com.ctosfit.identity.application.service.FindActiveMembershipService
import com.ctosfit.identity.application.service.ManageBoxMembershipsService
import com.ctosfit.identity.domain.UserId
import com.ctosfit.programming.application.port.out.CoachContext
import com.ctosfit.programming.application.port.out.CurrentBoxContextProvider
import com.ctosfit.programming.application.port.out.CurrentCoachContextProvider
import com.ctosfit.programming.application.port.in.CreateWorkoutDraft
import com.ctosfit.programming.application.port.in.EditWorkoutDraft
import com.ctosfit.programming.application.port.in.GetPublishedWorkoutSnapshot
import com.ctosfit.programming.application.port.in.GetScheduledWorkoutSnapshots
import com.ctosfit.programming.application.port.in.PublishWorkout
import com.ctosfit.programming.application.port.out.WorkoutDraftRepository
import com.ctosfit.programming.application.port.out.WorkoutPublicationRepository
import com.ctosfit.programming.adapter.out.JdbcWorkoutRepositories
import com.ctosfit.programming.application.service.CreateWorkoutDraftService
import com.ctosfit.programming.application.service.EditWorkoutDraftService
import com.ctosfit.programming.application.service.GetPublishedWorkoutSnapshotService
import com.ctosfit.programming.application.service.GetScheduledWorkoutSnapshotsService
import com.ctosfit.programming.application.service.PublishWorkoutService
import com.ctosfit.programming.domain.BoxId as ProgrammingBoxId
import com.ctosfit.programming.domain.CoachId
import com.ctosfit.programming.domain.PublicationId as ProgrammingPublicationId
import com.ctosfit.results.application.port.out.AthleteContext
import com.ctosfit.results.application.port.out.CurrentAthleteContextProvider
import com.ctosfit.results.application.port.out.CurrentCoachBoxProvider
import com.ctosfit.results.application.port.`in`.GetMyWorkoutResults
import com.ctosfit.results.application.port.`in`.GetBoxWorkoutResults
import com.ctosfit.results.application.port.`in`.GetMyPersonalBests
import com.ctosfit.results.application.port.`in`.SubmitWorkoutResult
import com.ctosfit.results.application.port.out.PublishedWorkoutDetails
import com.ctosfit.results.application.port.out.PublishedWorkoutLookup
import com.ctosfit.results.application.port.out.WorkoutResultRepository
import com.ctosfit.results.adapter.out.JdbcWorkoutResultRepository
import com.ctosfit.results.application.service.GetMyWorkoutResultsService
import com.ctosfit.results.application.service.GetBoxWorkoutResultsService
import com.ctosfit.results.application.service.GetMyPersonalBestsService
import com.ctosfit.results.application.service.SubmitWorkoutResultService
import com.ctosfit.results.domain.AthleteId
import com.ctosfit.results.domain.BoxId as ResultsBoxId
import com.ctosfit.results.domain.MetricId
import com.ctosfit.results.domain.PrescriptionId
import com.ctosfit.results.domain.PublicationId as ResultsPublicationId
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.core.context.SecurityContextHolder
import javax.sql.DataSource

@Configuration
class TenantContextConfiguration {
    @Bean
    fun boxOnboardingRepository(dataSource: DataSource): BoxOnboardingRepository =
        JdbcBoxOnboardingRepository(dataSource)

    @Bean
    fun createBox(
        authenticatedUserProvider: AuthenticatedUserProvider,
        boxOnboardingRepository: BoxOnboardingRepository,
    ): CreateBox = CreateBoxService(authenticatedUserProvider, boxOnboardingRepository)

    @Bean
    fun membershipRepository(dataSource: DataSource): MembershipRepository = JdbcMembershipRepository(dataSource)

    @Bean
    fun manageMembershipsRepository(dataSource: DataSource): ManageMembershipsRepository =
        JdbcMembershipRepository(dataSource)

    @Bean
    fun exerciseRepository(dataSource: DataSource): ExerciseRepository = JdbcExerciseRepository(dataSource)

    @Bean
    fun workoutRepositories(dataSource: DataSource): JdbcWorkoutRepositories = JdbcWorkoutRepositories(dataSource)

    @Bean
    fun workoutResultRepository(dataSource: DataSource): WorkoutResultRepository =
        JdbcWorkoutResultRepository(dataSource)

    @Bean
    fun manageExercises(
        exerciseRepository: ExerciseRepository,
        currentTenantProvider: CurrentTenantProvider,
    ): ManageExercises = ManageExercisesService(exerciseRepository, currentTenantProvider)

    @Bean
    fun findActiveMembership(membershipRepository: MembershipRepository): FindActiveMembership =
        FindActiveMembershipService(membershipRepository)

    @Bean
    fun currentMembershipAdministrator(resolver: RequestTenantContextResolver): CurrentMembershipAdministrator =
        CurrentMembershipAdministrator {
            val context = resolver.currentOwner()
            MembershipAdministratorContext(
                UserId(context.userId.value),
                com.ctosfit.identity.domain.BoxId(context.boxId.value),
                com.ctosfit.identity.domain.Role.valueOf(context.role.name),
            )
        }

    @Bean
    fun manageBoxMemberships(
        currentMembershipAdministrator: CurrentMembershipAdministrator,
        manageMembershipsRepository: ManageMembershipsRepository,
    ): ManageBoxMemberships = ManageBoxMembershipsService(currentMembershipAdministrator, manageMembershipsRepository)

    @Bean
    fun createWorkoutDraft(
        currentCoachContextProvider: CurrentCoachContextProvider,
        workoutDraftRepository: WorkoutDraftRepository,
    ): CreateWorkoutDraft = CreateWorkoutDraftService(currentCoachContextProvider, workoutDraftRepository)

    @Bean
    fun editWorkoutDraft(
        workoutDraftRepository: WorkoutDraftRepository,
        currentCoachContextProvider: CurrentCoachContextProvider,
    ): EditWorkoutDraft = EditWorkoutDraftService(workoutDraftRepository, currentCoachContextProvider)

    @Bean
    fun publishWorkout(
        workoutDraftRepository: WorkoutDraftRepository,
        workoutPublicationRepository: WorkoutPublicationRepository,
        currentCoachContextProvider: CurrentCoachContextProvider,
    ): PublishWorkout = PublishWorkoutService(
        workoutDraftRepository,
        workoutPublicationRepository,
        currentCoachContextProvider,
    )

    @Bean
    fun getPublishedWorkoutSnapshot(
        workoutPublicationRepository: WorkoutPublicationRepository,
    ): GetPublishedWorkoutSnapshot = GetPublishedWorkoutSnapshotService(workoutPublicationRepository)

    @Bean
    fun getScheduledWorkoutSnapshots(
        workoutPublicationRepository: WorkoutPublicationRepository,
    ): GetScheduledWorkoutSnapshots = GetScheduledWorkoutSnapshotsService(workoutPublicationRepository)

    @Bean
    fun publishedWorkoutLookup(
        getPublishedWorkoutSnapshot: GetPublishedWorkoutSnapshot,
    ): PublishedWorkoutLookup = PublishedWorkoutLookup { boxId, publicationId ->
        getPublishedWorkoutSnapshot.get(
            ProgrammingBoxId(boxId.value),
            ProgrammingPublicationId(publicationId.value),
        )?.let { published ->
            PublishedWorkoutDetails(
                boxId = ResultsBoxId(published.boxId.value),
                publicationId = ResultsPublicationId(published.publicationId.value),
                metricIds = published.metricIds.mapTo(linkedSetOf(), ::MetricId),
                prescriptionIds = published.prescriptionIds.mapTo(linkedSetOf(), ::PrescriptionId),
            )
        }
    }

    @Bean
    fun submitWorkoutResult(
        currentAthleteContextProvider: CurrentAthleteContextProvider,
        publishedWorkoutLookup: PublishedWorkoutLookup,
        workoutResultRepository: WorkoutResultRepository,
    ): SubmitWorkoutResult = SubmitWorkoutResultService(
        currentAthleteContextProvider,
        publishedWorkoutLookup,
        workoutResultRepository,
    )

    @Bean
    fun getMyWorkoutResults(
        currentAthleteContextProvider: CurrentAthleteContextProvider,
        workoutResultRepository: WorkoutResultRepository,
    ): GetMyWorkoutResults = GetMyWorkoutResultsService(
        currentAthleteContextProvider,
        workoutResultRepository,
    )

    @Bean
    fun authenticatedUserProvider(): AuthenticatedUserProvider = AuthenticatedUserProvider {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated || authentication.name == "anonymousUser") {
            throw AuthenticationCredentialsNotFoundException("An authenticated user is required")
        }
        AuthenticatedUserId(authentication.name)
    }

    @Bean
    fun activeMembershipLookup(findActiveMembership: FindActiveMembership): ActiveMembershipLookup =
        ActiveMembershipLookup { userId, boxId ->
            findActiveMembership.find(UserId(userId.value), com.ctosfit.identity.domain.BoxId(boxId.value))
                ?.role
                ?.let { BoxRole.valueOf(it.name) }
        }

    @Bean
    fun resolveTenantContext(
        authenticatedUserProvider: AuthenticatedUserProvider,
        activeMembershipLookup: ActiveMembershipLookup,
    ): ResolveTenantContext = ResolveTenantContextService(authenticatedUserProvider, activeMembershipLookup)

    @Bean
    fun currentTenantProvider(resolver: RequestTenantContextResolver): CurrentTenantProvider =
        CurrentTenantProvider { TenantId(resolver.currentCoach().boxId.value) }

    @Bean
    fun currentCoachContextProvider(resolver: RequestTenantContextResolver): CurrentCoachContextProvider =
        CurrentCoachContextProvider {
            val context = resolver.currentCoach()
            CoachContext(ProgrammingBoxId(context.boxId.value), CoachId(context.userId.value))
        }

    @Bean
    fun currentBoxContextProvider(resolver: RequestTenantContextResolver): CurrentBoxContextProvider =
        CurrentBoxContextProvider { ProgrammingBoxId(resolver.currentAthlete().boxId.value) }

    @Bean
    fun currentAthleteContextProvider(resolver: RequestTenantContextResolver): CurrentAthleteContextProvider =
        CurrentAthleteContextProvider {
            val context = resolver.currentAthlete()
            AthleteContext(ResultsBoxId(context.boxId.value), AthleteId(context.userId.value))
        }

    @Bean
    fun currentCoachBoxProvider(resolver: RequestTenantContextResolver): CurrentCoachBoxProvider =
        CurrentCoachBoxProvider { ResultsBoxId(resolver.currentCoach().boxId.value) }

    @Bean
    fun getBoxWorkoutResults(
        currentCoachBoxProvider: CurrentCoachBoxProvider,
        workoutResultRepository: WorkoutResultRepository,
    ): GetBoxWorkoutResults = GetBoxWorkoutResultsService(currentCoachBoxProvider, workoutResultRepository)

    @Bean
    fun getMyPersonalBests(
        currentAthleteContextProvider: CurrentAthleteContextProvider,
        workoutResultRepository: WorkoutResultRepository,
    ): GetMyPersonalBests = GetMyPersonalBestsService(
        currentAthleteContextProvider,
        workoutResultRepository,
    )
}
