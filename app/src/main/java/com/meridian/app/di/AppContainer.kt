package com.meridian.app.di

import android.content.Context
import com.meridian.app.BuildConfig
import com.meridian.app.data.auth.CredentialManagerGoogleIdTokenProvider
import com.meridian.app.data.auth.DemoGoogleIdTokenProvider
import com.meridian.app.data.auth.GoogleIdTokenProvider
import com.meridian.app.data.local.MeridianDatabase
import com.meridian.app.data.local.meridianDataStore
import com.meridian.app.data.remote.*
import com.meridian.app.data.repository.*
import com.meridian.app.data.security.KeystoreTokenStore
import com.meridian.app.device.GeofenceManager
import com.meridian.app.device.NotificationHelper
import com.meridian.app.device.OfficeEventHandler
import com.meridian.app.device.SyncScheduler
import com.meridian.app.domain.repository.*
import com.meridian.app.domain.usecase.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.time.Clock

/**
 * Inyección de dependencias MANUAL (como en los ejemplos de clase): sin Hilt/Koin.
 * Los repositorios se pasan a los ViewModels por constructor desde [com.meridian.app.presentation.ViewModelFactory].
 */
class AppContainer(private val context: Context) {
    val clock: Clock = Clock.systemDefaultZone()
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Infraestructura
    private val database by lazy { MeridianDatabase.create(context) }
    private val tokenStore by lazy { KeystoreTokenStore(context) }
    private val httpClient by lazy { RetrofitFactory.createClient(tokenStore) }
    val notifications by lazy { NotificationHelper(context) }
    val syncScheduler by lazy { SyncScheduler(context) }
    val syncTrigger: SyncTrigger get() = syncScheduler
    val geofencing: OfficeGeofencing by lazy { GeofenceManager(context) }

    private val remote: RemoteDataSource by lazy {
        if (BuildConfig.USE_FAKE_BACKEND) FakeRemoteDataSource(clock)
        else RetrofitRemoteDataSource(RetrofitFactory.createApi(httpClient))
    }
    private val realtimeSource: TeamRealtimeSource by lazy {
        if (BuildConfig.USE_FAKE_BACKEND) NoOpTeamRealtimeSource() else OkHttpTeamRealtimeSource(httpClient, tokenStore)
    }
    val googleIdTokenProvider: GoogleIdTokenProvider by lazy {
        if (BuildConfig.USE_FAKE_BACKEND) DemoGoogleIdTokenProvider()
        else CredentialManagerGoogleIdTokenProvider(BuildConfig.GOOGLE_SERVER_CLIENT_ID)
    }

    // Repositorios
    val preferencesRepository: PreferencesRepository by lazy { PreferencesRepositoryImpl(context.meridianDataStore) }
    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(context.meridianDataStore, remote, tokenStore, geofencing)
    }
    val clockRepository: ClockRepository by lazy { ClockRepositoryImpl(database.clockEventDao()) }
    val teamRepository: TeamRepository by lazy { TeamRepositoryImpl(database) }
    val officePlanRepository: OfficePlanRepository by lazy { OfficePlanRepositoryImpl(database.officePlanDao()) }
    val connectivity: ConnectivityRepository by lazy { ConnectivityRepositoryImpl(context) }
    val syncRepository: SyncRepository by lazy {
        SyncRepositoryImpl(database, remote, authRepository, preferencesRepository, geofencing, clock)
    }
    val realtimeCoordinator by lazy { RealtimeCoordinator(authRepository, realtimeSource, database.teamDao()) }

    // Casos de uso
    val computeAvailability by lazy { ComputeAvailability() }
    val registerClockEvent by lazy {
        RegisterClockEventUseCase(authRepository, preferencesRepository, clockRepository, syncTrigger, clock)
    }
    val undoClockEvent by lazy { UndoClockEventUseCase(clockRepository, syncTrigger) }
    val observeOwnAvailability by lazy {
        ObserveOwnAvailabilityUseCase(preferencesRepository, clockRepository, teamRepository, computeAvailability, clock)
    }
    val observeOfficeWeek by lazy { ObserveOfficeWeekUseCase(teamRepository, officePlanRepository) }
    val toggleOfficeDay by lazy { ToggleOfficeDayUseCase(authRepository, officePlanRepository, syncTrigger) }
    val signInWithGoogle by lazy { SignInWithGoogleUseCase(authRepository) }

    val officeEventHandler by lazy {
        OfficeEventHandler(authRepository, preferencesRepository, clockRepository, notifications, clock)
    }
}
