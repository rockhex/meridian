package com.meridian.app.domain

import com.meridian.app.domain.model.*
import com.meridian.app.domain.repository.*
import com.meridian.app.domain.usecase.RegisterClockEventUseCase
import com.meridian.app.domain.usecase.RegisterResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.time.*

/** RF01 / RNF06: el fichaje se persiste como PENDING localmente, sin depender de la red. */
class RegisterClockEventUseCaseTest {

    private val session = Session("u1", "Carla Ruiz", "c@x.com", Role.LIDER, "t1", "UY")
    private val prefs = UserPreferences(WorkSchedule(LocalTime.of(9, 0), LocalTime.of(18, 0), "UTC"), true, true, true)
    private val fixedClock = Clock.fixed(Instant.parse("2026-11-18T12:00:00Z"), ZoneOffset.UTC)

    private class FakeAuth(private val s: Session?) : AuthRepository {
        override val session: Flow<Session?> = MutableStateFlow(s)
        override val googleStatus: Flow<GoogleAccountStatus> = MutableStateFlow(GoogleAccountStatus.CONNECTED)
        override suspend fun currentSession() = s
        override suspend fun signIn(idToken: String): SignInResult = TODO()
        override suspend fun reconnectGoogle(idToken: String): Boolean = TODO()
        override suspend fun setGoogleStatus(status: GoogleAccountStatus) = TODO()
        override suspend fun signOut() = TODO()
    }

    private class FakePrefs(private val p: UserPreferences) : PreferencesRepository {
        override val preferences: Flow<UserPreferences> = MutableStateFlow(p)
        override val lastSync: Flow<Instant?> = MutableStateFlow(null)
        override suspend fun current() = p
        override suspend fun setSchedule(start: LocalTime, end: LocalTime, zoneId: String) = TODO()
        override suspend fun setSuggestClockIn(enabled: Boolean) = TODO()
        override suspend fun setExitReminder(enabled: Boolean) = TODO()
        override suspend fun setPendingSyncAlert(enabled: Boolean) = TODO()
        override suspend fun setLastSync(instant: Instant) = TODO()
        override suspend fun lastSuggestionDate(): String? = TODO()
        override suspend fun setLastSuggestionDate(date: String) = TODO()
    }

    private class FakeClockRepo : ClockRepository {
        val stored = MutableStateFlow<List<ClockEvent>>(emptyList())
        override fun observeDay(userId: String, date: LocalDate, zone: ZoneId): Flow<List<ClockEvent>> = stored
        override suspend fun eventsForDay(userId: String, date: LocalDate, zone: ZoneId) = stored.value
        override suspend fun register(event: ClockEvent) { stored.value = stored.value + event }
        override suspend fun void(id: String) { stored.value = stored.value.filterNot { it.id == id } }
        override fun observePendingCount(): Flow<Int> = stored.map { l -> l.count { it.syncState == SyncState.PENDING } }
    }

    private class FakeTrigger : SyncTrigger { var requests = 0; override fun requestSync() { requests++ } }

    private fun useCase(repo: FakeClockRepo, trigger: FakeTrigger, auth: AuthRepository = FakeAuth(session)) =
        RegisterClockEventUseCase(auth, FakePrefs(prefs), repo, trigger, fixedClock, idGenerator = { "id-${repo.stored.value.size}" })

    @Test fun `sin conexion el fichaje queda guardado como PENDING y pide sincronizar`() = runTest {
        val repo = FakeClockRepo(); val trigger = FakeTrigger()
        val result = useCase(repo, trigger)(ClockType.ENTRADA, Modality.PRESENCIAL)

        assertTrue(result is RegisterResult.Registered)
        val saved = repo.stored.value.single()
        assertEquals(SyncState.PENDING, saved.syncState)
        assertEquals(Instant.parse("2026-11-18T12:00:00Z"), saved.timestamp) // hora del dispositivo
        assertEquals(1, trigger.requests)
    }

    @Test fun `una secuencia invalida no se guarda`() = runTest {
        val repo = FakeClockRepo(); val trigger = FakeTrigger()
        val result = useCase(repo, trigger)(ClockType.SALIDA, Modality.REMOTO)

        assertTrue(result is RegisterResult.InvalidSequence)
        assertTrue(repo.stored.value.isEmpty())
        assertEquals(0, trigger.requests)
    }

    @Test fun `registra la jornada completa en orden`() = runTest {
        val repo = FakeClockRepo(); val uc = useCase(repo, FakeTrigger())
        listOf(ClockType.ENTRADA, ClockType.INICIO_DESCANSO, ClockType.FIN_DESCANSO, ClockType.SALIDA).forEach {
            assertTrue(uc(it, Modality.REMOTO) is RegisterResult.Registered)
        }
        assertEquals(4, repo.stored.value.size)
        assertTrue(uc(ClockType.ENTRADA, Modality.REMOTO) is RegisterResult.InvalidSequence)
    }

    @Test fun `sin sesion no registra`() = runTest {
        val repo = FakeClockRepo()
        assertEquals(RegisterResult.NotSignedIn, useCase(repo, FakeTrigger(), FakeAuth(null))(ClockType.ENTRADA, Modality.REMOTO))
    }
}
