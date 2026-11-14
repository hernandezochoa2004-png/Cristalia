package me.mariahernandez.cristalia.densidad.parametroa

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cristalia.model.EstructuraCristalina
import me.mariahernandez.cristalia.densidad.radio.ContenidoDensidad
import me.mariahernandez.cristalia.densidad.radio.ContenidoVolumenParametroA
import me.mariahernandez.cristalia.densidad.radio.EsqueletoResultadoDensidad
import me.mariahernandez.cristalia.densidad.radio.Paso1ConteoIones
import me.mariahernandez.cristalia.densidad.radio.Paso2UnidadFormula
import me.mariahernandez.cristalia.densidad.radio.PasoCard

private val AcentoApagado = Color(0xFF9AA4B8)

/**
 * Pantalla que se abre al pulsar "Calcular densidad" en el tab PARÁMETRO a.
 * Igual que la de Radio (iones, unidad fórmula, volumen, densidad), pero el paso 3
 * parte de a (y c) en pm en vez de los radios.
 *
 * @param parametroAPm parámetro de red a, en pm
 * @param parametroCPm parámetro c en pm; solo hace falta en hexagonal / tetragonal (null en cúbicas)
 */
@Composable
fun ResultadoDensidadParametroAScreen(
    estructura: EstructuraCristalina,
    masaMolar: Double,
    parametroAPm: Double,
    parametroCPm: Double? = null,
    onBack: () -> Unit = {},
    onIrALogin: () -> Unit = {},
    viewModel: ResultadoDensidadParametroAViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(estructura.id, masaMolar, parametroAPm, parametroCPm) {
        viewModel.inicializar(estructura, masaMolar, parametroAPm, parametroCPm)
    }

    EsqueletoResultadoDensidad(estructura, onBack, onIrALogin) {
        uiState.datos?.let { datos ->

            PasoCard(
                pasoNumero = 1,
                titulo = "Número de iones y cationes",
                expandido = 1 in uiState.pasosExpandidos,
                onAlternar = { viewModel.alternarPaso(1) }
            ) {
                Paso1ConteoIones(estructura, datos)
            }

            PasoCard(
                pasoNumero = 2,
                titulo = "Unidad fórmula",
                expandido = 2 in uiState.pasosExpandidos,
                onAlternar = { viewModel.alternarPaso(2) },
                colorAcento = AcentoApagado
            ) {
                Paso2UnidadFormula(datos)
            }

            PasoCard(
                pasoNumero = 3,
                titulo = "Volumen celda unitaria",
                expandido = 3 in uiState.pasosExpandidos,
                onAlternar = { viewModel.alternarPaso(3) }
            ) {
                ContenidoVolumenParametroA(datos)
            }

            PasoCard(
                pasoNumero = 4,
                titulo = "Densidad teórica (ρ)",
                expandido = 4 in uiState.pasosExpandidos,
                onAlternar = { viewModel.alternarPaso(4) },
                destacado = true
            ) {
                ContenidoDensidad(datos)
            }
        }
    }
}
