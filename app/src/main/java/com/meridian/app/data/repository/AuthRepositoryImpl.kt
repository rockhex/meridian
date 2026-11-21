package com.meridian.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.meridian.app.data.local.Keys
import com.meridian.app.data.remote.RemoteDataSource
import com.meridian.app.data.remote.WrongDomainException
import com.meridian.app.data.security.KeystoreTokenStore
import com.meridian.app.domain.model.GoogleAccountStatus
import com.meridian.app.domain.model.Role
import com.meridian.app.domain.model.Session
import com.meridian.app.domain.repository.AuthRepository
import com.meridian.app.domain.repository.OfficeGeofencing
import com.meridian.app.domain.repository.SignInResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import com.meridian.app.data.remote.Dto.*
class AuthRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val remote: RemoteDataSource,
    private val tokenStore: KeystoreTokenStore,
    private val geofencing: OfficeGeofencing,
) : AuthRepository {

    override val session: Flow<Session?> = dataStore.data.map { p ->
        val id = p[Keys.USER_ID] ?: return@map null
        Session(
            userId = id,
            name = p[Keys.NAME].orEmpty(),
            email = p[Keys.EMAIL].orEmpty(),
            role = runCatching { Role.valueOf(p[Keys.ROLE].orEmpty()) }.getOrDefault(Role.EMPLEADO),
            teamId = p[Keys.TEAM_ID].orEmpty(),
            country = p[Keys.COUNTRY].orEmpty(),
        )
    }.distinctUntilChanged()

    override val googleStatus: Flow<GoogleAccountStatus> = dataStore.data.map { p ->
        p[Keys.GOOGLE_STATUS]?.let { runCatching { GoogleAccountStatus.valueOf(it) }.getOrNull() }
            ?: GoogleAccountStatus.CONNECTED
    }.distinctUntilChanged()

    override suspend fun currentSession(): Session? = session.first()

    override suspend fun signIn(idToken: String): SignInResult = try {
        val dto = remote.signIn(idToken)
        tokenStore.save(dto.token)
        dataStore.edit {
            it[Keys.USER_ID] = dto.userId
            it[Keys.NAME] = dto.name
            it[Keys.EMAIL] = dto.email
            it[Keys.ROLE] = dto.role
            it[Keys.TEAM_ID] = dto.teamId
            it[Keys.COUNTRY] = dto.country
            it[Keys.ZONE] = dto.zoneId
            it[Keys.GOOGLE_STATUS] = GoogleAccountStatus.CONNECTED.name
        }
        SignInResult.Success
    } catch (e: WrongDomainException) {
        SignInResult.WrongDomain
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        SignInResult.Offline
    } catch (e: Exception) {
        SignInResult.Error
    }

    override suspend fun reconnectGoogle(idToken: String): Boolean = try {
        remote.linkGoogle(idToken)
        setGoogleStatus(GoogleAccountStatus.CONNECTED)
        true
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        false
    }

    override suspend fun setGoogleStatus(status: GoogleAccountStatus) {
        dataStore.edit { it[Keys.GOOGLE_STATUS] = status.name }
    }

    /** Conserva los fichajes locales (RNF06): solo se borra la sesión y el token. */
    override suspend fun signOut() {
        tokenStore.clear()
        geofencing.unregister()
        dataStore.edit {
            it.remove(Keys.USER_ID); it.remove(Keys.NAME); it.remove(Keys.EMAIL)
            it.remove(Keys.ROLE); it.remove(Keys.TEAM_ID); it.remove(Keys.COUNTRY)
            it.remove(Keys.GOOGLE_STATUS)
        }
    }
}
