# Meridian — App Android (Kotlin + Jetpack Compose)

Coordinación de equipos híbridos: fichaje rápido, disponibilidad de compañeros y plan de oficina, **sin monitoreo invasivo**.

## Cómo correrla
1. Abrir la carpeta en **Android Studio Koala (2024.1.1)+** (JDK 17). Dejar que sincronice Gradle.
2. Ejecutar en un dispositivo/emulador con Android 8.0+ (API 26+).
3. Modo demo por defecto (`USE_FAKE_BACKEND = true` en `app/build.gradle.kts`): backend simulado en memoria con la cuenta
   `carla.ruiz@meridian.example` (rol Líder, Equipo Backend, oficina de Montevideo).
4. Tests: `./gradlew testDebugUnitTest`.

Para el backend real (Spring Boot): `USE_FAKE_BACKEND=false`, completar `API_BASE_URL`, `WS_URL` y `GOOGLE_SERVER_CLIENT_ID`.
Los endpoints esperados están en `data/remote/MeridianApi.kt`.

## Arquitectura (MVVM + capas)
```
presentation/  Compose (Material 3) + ViewModels (StateFlow) + Navigation Compose
domain/        modelos, puertos (interfaces de repositorio) y casos de uso — Kotlin puro, testeable
data/          Room (fuente de verdad), DataStore, Retrofit/OkHttp, Keystore, repositorios
device/        geofencing, notificaciones accionables, WorkManager, widget Glance
di/            AppContainer: inyección de dependencias manual (sin Hilt)
```
Flujo: `Screen → ViewModel → UseCase → Repository(interface) ← RepositoryImpl → Room / Remote`.
La UI **solo lee Room**; la red solo sincroniza la caché (offline-first).

## Pantallas y estados (según los wireframes)
| Pantalla | Estados implementados |
|---|---|
| Login | normal · cargando · cuenta fuera del dominio · sin conexión |
| Inicio | sin fichar · disponible · en llamada · en descanso · fuera de horario · confirmación de salida (bottom sheet) · deshacer (snackbar) · pendiente de sincronizar · aviso de token Google vencido |
| Equipo | cargando (skeleton) · contenido (con resumen para líder y filtros) · vacío · desactualizado (>15 min) · sin conexión sin datos |
| Oficina | sin oficina asignada · sin planificación · contenido (fijos/feriados/quién va) · cambio pendiente |
| Perfil | permiso de ubicación rechazado · token de Google vencido/desconectado · horario y zona · notificaciones · cuentas |
| Widget | widget Glance con la acción principal + notificación accionable (Fichar entrada/salida) |

## Trazabilidad con los requisitos
| Req. | Dónde |
|---|---|
| RF01 fichaje con UUID e idempotencia | `RegisterClockEventUseCase`, `SyncRepositoryImpl.pushClockEvents` |
| RF02 estado de compañeros, 15 min | `TeamRules`, `TeamViewModel`, `RealtimeCoordinator` (WebSocket) |
| RF03 sugerencia al llegar, 1 vez/día | `GeofenceManager`, `OfficeEventHandler.onEnter` |
| RF04 plan de oficina colaborativo | `ObserveOfficeWeekUseCase`, `ToggleOfficeDayUseCase` |
| RNF01/06 offline y no perder fichajes | Room + `SyncWorker` (WorkManager sobrevive a reinicios) |
| RNF02 pocos toques | notificación/widget (1 toque); salida con confirmación |
| RNF03/05 sin vigilancia | solo ENTER/EXIT de geofence; del calendario solo el horario |
| RNF04 privacidad de estado | `ComputeAvailability` → `FUERA_DE_HORARIO` sin detalle |
| RNF07 token vencido sin bloquear | `GoogleAccountStatus` + banners con "Reconectar" |
| RNF08 accesibilidad | ícono+texto en estados, áreas ≥48 dp, `contentDescription` |
| RNF09 seguridad | token cifrado AES-GCM en Keystore, dominio validado por el servidor |

## Decisiones de diseño
- **Secuencia de fichajes** validada en dominio (`ClockRules`): entrada → (descanso → fin) → salida.
- **Deshacer**: marca el fichaje como `voided`; se informa al servidor (DELETE idempotente) y luego se borra localmente.
- **Estimación offline**: una llamada cuya hora de fin ya pasó se muestra como "Disponible".
- **Fila propia en Equipo**: se calcula localmente, así siempre refleja tu último fichaje.

## Limitaciones conocidas
- El código **no se compiló en este entorno** (no hay SDK de Android); puede requerir ajustes menores de versiones al sincronizar.
- Textos de UI en español dentro de los composables: pendiente extraerlos a `strings.xml` (i18n).
- Campana de notificaciones de la barra superior del mockup no incluida (no tenía destino definido).
- Google Sign-In real requiere un OAuth client ID; el modo demo usa un proveedor simulado.
- Widget: solo la acción principal; "Fichar salida" siempre pasa por la confirmación de la app.
