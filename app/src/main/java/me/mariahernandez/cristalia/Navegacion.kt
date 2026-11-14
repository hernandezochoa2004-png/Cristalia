package me.mariahernandez.cristalia


import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cristalia.model.EstructuraCristalina
import com.cristalia.ui.ingresardatos.IngresarDatosScreen
import me.mariahernandez.cristalia.celdaunitaria.CeldaUnitariaScreen
import me.mariahernandez.cristalia.inicio.CristaliaSplashScreen
import me.mariahernandez.cristalia.empaquetamiento.EmpaquetamientoScreen
import me.mariahernandez.cristalia.menu.MenuScreen
import me.mariahernandez.cristalia.densidad.DensidadScreen
import me.mariahernandez.cristalia.densidad.radio.ResultadoDensidadRadioScreen
import me.mariahernandez.cristalia.huecos.HuecosScreen

object Rutas {

    const val INICIO = "inicio"             //
    const val MENU = "menu"                 // pantalla anterior (Menu sebas)
    const val LOGIN = "login"               // pantalla de login (Login sebas)

                                            // Otras pantallas que haga sebas
    const val CELDA_UNITARIA = "celda_unitaria"        // Pantalla celda unitaria

    const val EMPAQUETAMIENTO = "factor-empaquetamiento"    // Pantalla factor de empaquetamiento

    const val DENSIDAD = "densidad"               // Pantalla menú densidad

    const val HUECOS = "huecos"                   // Pantalla menú huecos

    const val INGRESAR_DATOS = "ingresar_datos/{opcionId}" // Pantalla ingresar datos densidad

    const val RESULTADO_DENSIDAD_RADIO = "resultado_densidad_radio/{opcionId}/{masaMolar}/{radioCation}/{radioAnion}"
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
                    navController.navigate(Rutas.MENU)
                }
            )
        }

        composable(Rutas.MENU) {
            MenuScreen(
                onCeldaUnitariaClick = {
                    navController.navigate(Rutas.CELDA_UNITARIA)
                },

                onFactorEmpaquetamientoClick = {
                    navController.navigate(route=Rutas.EMPAQUETAMIENTO)
                },

                onDensidadClick = {
                    navController.navigate(route=Rutas.DENSIDAD)
                },

                onHuecosClick = {
                    navController.navigate(route= Rutas.HUECOS)
                },

                onIrALogin = {
                    navController.navigate(Rutas.LOGIN)
                }
            )

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

        composable(route=Rutas.EMPAQUETAMIENTO) {
            EmpaquetamientoScreen(
                onVolver = { navController.popBackStack() },
                onIrALogin = { navController.navigate(route = Rutas.LOGIN) }
            )
        }

        composable(route = Rutas.DENSIDAD) {
            DensidadScreen(
                onVolver = { navController.popBackStack() },
                onIrALogin = { navController.navigate(route = Rutas.LOGIN) },
                onOpcionClick = { _, opcionId ->
                    navController.navigate("ingresar_datos/$opcionId")
                }
            )
        }

        composable(route = Rutas.INGRESAR_DATOS) { backStackEntry ->
            val opcionId = backStackEntry.arguments?.getString("opcionId") ?: "nacl"
            val estructura = EstructuraCristalina.catalogoEjemplo()
                .firstOrNull { it.id == opcionId }
                ?: EstructuraCristalina.catalogoEjemplo().first()

            val viewModel: com.cristalia.ui.ingresardatos.IngresarDatosViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

            IngresarDatosScreen(
                estructura = estructura,
                onBack = { navController.popBackStack() },
                onIrALogin = { navController.navigate(route = Rutas.LOGIN) },
                onNavegarAResultado = { _ ->
                    val masaMolar = viewModel.uiState.value.masaMolar
                    val rCat = viewModel.uiState.value.radioCation
                    val rAn = viewModel.uiState.value.radioAnion
                    navController.navigate("resultado_densidad_radio/$opcionId/$masaMolar/$rCat/$rAn")
                },
                viewModel = viewModel
            )
        }

        composable(route = Rutas.RESULTADO_DENSIDAD_RADIO) { backStackEntry ->
            val opcionId = backStackEntry.arguments?.getString("opcionId") ?: "nacl"
            val masaMolar = backStackEntry.arguments?.getString("masaMolar")?.toDoubleOrNull() ?: 0.0
            val rCat = backStackEntry.arguments?.getString("radioCation")?.toDoubleOrNull() ?: 0.0
            val rAn = backStackEntry.arguments?.getString("radioAnion")?.toDoubleOrNull() ?: 0.0

            val estructura = EstructuraCristalina.catalogoEjemplo()
                .firstOrNull { it.id == opcionId }
                ?: EstructuraCristalina.catalogoEjemplo().first()

            ResultadoDensidadRadioScreen(
                estructura = estructura,
                masaMolar = masaMolar,
                radioCation = rCat,
                radioAnion = rAn,
                onBack = { navController.popBackStack() },
                onIrALogin = { navController.navigate(route = Rutas.LOGIN) }
            )
        }

        composable(route = Rutas.HUECOS) {
            HuecosScreen(
                onVolver = { navController.popBackStack() },
                onIrALogin = { navController.navigate(route = Rutas.LOGIN) }
            )
        }



    }
}