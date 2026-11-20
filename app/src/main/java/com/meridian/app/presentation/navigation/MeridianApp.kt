package com.meridian.app.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.meridian.app.R
import com.meridian.app.di.AppContainer
import com.meridian.app.domain.model.Session
import com.meridian.app.presentation.ViewModelFactory
import com.meridian.app.presentation.home.HomeScreen
import com.meridian.app.presentation.home.HomeViewModel
import com.meridian.app.presentation.login.LoginScreen
import com.meridian.app.presentation.login.LoginViewModel
import com.meridian.app.presentation.office.OfficeScreen
import com.meridian.app.presentation.office.OfficeViewModel
import com.meridian.app.presentation.profile.ProfileScreen
import com.meridian.app.presentation.profile.ProfileViewModel
import com.meridian.app.presentation.team.TeamScreen
import com.meridian.app.presentation.team.TeamViewModel
import kotlinx.coroutines.flow.map

private sealed interface SessionState {
    data object Loading : SessionState
    data class Ready(val session: Session?) : SessionState
}

@Composable
fun MeridianApp(container: AppContainer) {
    val sessionFlow = remember { container.authRepository.session.map<Session?, SessionState> { SessionState.Ready(it) } }
    val state by sessionFlow.collectAsStateWithLifecycle(initialValue = SessionState.Loading)
    when (val s = state) {
        SessionState.Loading -> Box(Modifier.fillMaxSize())
        is SessionState.Ready -> AppNav(container, s.session)
    }
}

private data class TabItem(val route: String, val label: String, val icon: ImageVector)

private val tabItems = listOf(
    TabItem(Route.HOME, "Inicio", Icons.Outlined.Home),
    TabItem(Route.TEAM, "Equipo", Icons.Outlined.Groups),
    TabItem(Route.OFFICE, "Oficina", Icons.Outlined.Business),
    TabItem(Route.PROFILE, "Perfil", Icons.Outlined.Person),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppNav(container: AppContainer, session: Session?) {
    val navController = rememberNavController()
    val factory = remember { ViewModelFactory(container) }
    val snackbarHostState = remember { SnackbarHostState() }
    val startDestination = remember { if (session != null) Route.HOME else Route.LOGIN }
    val route = navController.currentBackStackEntryAsState().value?.destination?.route

    // La sesión manda: sin sesión => Login; al iniciar sesión => Inicio.
    LaunchedEffect(session?.userId) {
        val current = navController.currentDestination?.route ?: return@LaunchedEffect
        if (session == null && current != Route.LOGIN) {
            navController.navigate(Route.LOGIN) { popUpTo(navController.graph.id) { inclusive = true } }
        } else if (session != null && current == Route.LOGIN) {
            navController.navigate(Route.HOME) { popUpTo(Route.LOGIN) { inclusive = true } }
        }
    }

    fun goToTab(target: String) = navController.navigate(target) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

    val showBars = route in Route.tabs
    Scaffold(
        topBar = {
            if (showBars) {
                TopAppBar(
                    title = { Text("Meridian") },
                    navigationIcon = {
                        androidx.compose.material3.Icon(
                            painterResource(R.drawable.ic_stat_meridian), contentDescription = null,
                            modifier = Modifier.padding(start = 16.dp, end = 4.dp),
                        )
                    },
                    actions = {
                        IconButton(onClick = { goToTab(Route.PROFILE) }, modifier = Modifier.semantics { contentDescription = "Abrir perfil" }) {
                            com.meridian.app.presentation.components.Avatar(session?.initials.orEmpty(), size = 32)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }
        },
        bottomBar = {
            if (showBars) {
                NavigationBar {
                    tabItems.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = { goToTab(tab.route) },
                            icon = { androidx.compose.material3.Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        NavHost(navController, startDestination = startDestination, modifier = Modifier.padding(padding)) {
            composable(Route.LOGIN) {
                val vm: LoginViewModel = viewModel(factory = factory)
                LoginScreen(vm)
            }
            composable(Route.HOME) {
                val vm: HomeViewModel = viewModel(factory = factory)
                HomeScreen(vm, snackbarHostState, onGoToProfile = { goToTab(Route.PROFILE) })
            }
            composable(Route.TEAM) {
                val vm: TeamViewModel = viewModel(factory = factory)
                TeamScreen(vm)
            }
            composable(Route.OFFICE) {
                val vm: OfficeViewModel = viewModel(factory = factory)
                OfficeScreen(vm)
            }
            composable(Route.PROFILE) {
                val vm: ProfileViewModel = viewModel(factory = factory)
                ProfileScreen(vm)
            }
        }
    }
}
