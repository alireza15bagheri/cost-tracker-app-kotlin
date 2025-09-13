package fr.alirezabagheri.simplecosttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import fr.alirezabagheri.simplecosttracker.ui.auth.LoginScreen
import fr.alirezabagheri.simplecosttracker.ui.auth.SignUpScreen
import fr.alirezabagheri.simplecosttracker.ui.dashboard.DashboardScreen
import fr.alirezabagheri.simplecosttracker.ui.income.IncomesScreen
import fr.alirezabagheri.simplecosttracker.ui.period.PeriodsScreen
import fr.alirezabagheri.simplecosttracker.ui.theme.SimpleCostTrackerTheme

class MainActivity : ComponentActivity() {

    private val auth: FirebaseAuth by lazy { Firebase.auth }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SimpleCostTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(auth)
                }
            }
        }
    }
}

sealed class Screen(val route: String) {
    object LoginScreen : Screen("login")
    object SignUpScreen : Screen("signup")
    object DashboardScreen : Screen("dashboard")
    object PeriodsScreen : Screen("periods")
    object IncomesScreen : Screen("incomes/{periodId}") {
        fun createRoute(periodId: String) = "incomes/$periodId"
    }
}

@Composable
fun AppNavigation(auth: FirebaseAuth) {
    val navController = rememberNavController()
    val startDestination = if (auth.currentUser != null) {
        Screen.DashboardScreen.route
    } else {
        Screen.LoginScreen.route
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.LoginScreen.route) {
            LoginScreen(navController = navController, auth = auth)
        }
        composable(Screen.SignUpScreen.route) {
            SignUpScreen(navController = navController, auth = auth)
        }
        composable(Screen.DashboardScreen.route) {
            DashboardScreen(navController = navController, auth = auth)
        }
        composable(Screen.PeriodsScreen.route) {
            PeriodsScreen(navController = navController)
        }
        composable(
            route = Screen.IncomesScreen.route,
            arguments = listOf(navArgument("periodId") { type = NavType.StringType })
        ) { backStackEntry ->
            val periodId = backStackEntry.arguments?.getString("periodId")
            if (periodId != null) {
                IncomesScreen(navController = navController, periodId = periodId)
            }
        }
    }
}