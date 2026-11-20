package com.meridian.app.data.remote

import java.io.IOException

data class SignInRequest(val idToken: String)
data class LinkGoogleRequest(val idToken: String)

/** status: ACCEPTED | DUPLICATE | FLAGGED (secuencia inválida, guardado para revisión). */
data class ClockPushResponse(val status: String)

class UnauthorizedException : IOException("Sesión vencida")
class WrongDomainException : IOException("Cuenta fuera del dominio")
class GoogleTokenExpiredException : IOException("Token de Google vencido o revocado")
