package me.mariahernandez.cristalia


import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.mariahernandez.cristalia.celdaunitaria.CeldaUnitariaScreen
import me.mariahernandez.cristalia.inicio.CristaliaSplashScreen

object Rutas {

    const val INICIO = "inicio"             //
    const val MENU = "menu"                 // pantalla anterior (Menu sebas)
    const val LOGIN = "login"               // pantalla de login (Login sebas)

                                            // Otras pantallas que haga sebas
    const val CELDA_UNITARIA = "celda_unitaria"        // Pantalla celda unitaria
}

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Rutas.INICIO
    ) {

        // PROVISIONAL PASA DE INICIO A CELDA UNITARIA     (DEBE PASAR POR INICIO -> MENU -> CELDA UNITARIA)
        composable(Rutas.INICIO) {

            // Ejecuta la función que recibe como parámetro una lambda (Acción de navegar)
            CristaliaSplashScreen(
                onEnter = {                                                  // lambda que no devuelve nada y no recibe nada. Por ello entra como parametro en esa funcion en onEnter : () -> Unit
                    navController.navigate(Rutas.CELDA_UNITARIA)
                }
            )
        }

        composable(Rutas.MENU) {
            // TODO: aquí irá la pantalla anterior / menú

        }

        composable(Rutas.LOGIN) {
            // TODO: aquí irá la pantalla de inicio de sesión
        }

        composable(Rutas.CELDA_UNITARIA) {
            CeldaUnitariaScreen(
                // Volver a la pantalla anterior del back stack
                onVolver = { navController.popBackStack() },
                // Ir al login (sirve tanto para el botón "Iniciar sesión" como para la descarga sin sesión)
                onIrALogin = { navController.navigate(Rutas.LOGIN) }
            )
        }
    }
}