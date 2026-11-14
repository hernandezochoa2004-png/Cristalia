package me.mariahernandez.cristalia.densidad.volumen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cristalia.model.EstructuraCristalina
import me.mariahernandez.cristalia.densidad.radio.ContenidoDensidad
import me.mariahernandez.cristalia.densidad.radio.EsqueletoResultadoDensidad
import me.mariahernandez.cristalia.densidad.radio.PasoCard
import me.mariahernandez.cristalia.densidad.radio.Paso1ConteoIones
import me.mariahernandez.cristalia.densidad.radio.Paso2UnidadFormula

private val AcentoApagado = Color(0xFF9AA4B8)

/**
 * Pantalla que se abre al pulsar "Calcular densidad" en el tab VOLUMEN.
 * Como el volumen ya lo da el usuario, no hay paso de "Volumen celda unitaria":
 *   1) Número de iones y cationes
 *   2) Unidad fórmula
 *   3) Densidad teórica (ρ)
 *
 * Todo el diseño sale de ResultadoDensidadRadioScreen / FuncionesCalculoRadio.
 *
 * @param volumenCm3 volumen de la celda unitaria en cm³ (tal cual lo escribió el usuario)
 */
@Composable
fun ResultadoDensidadVolumenScreen(
    estructura: EstructuraCristalina,
    masaMolar: Double,
    volumenCm3: Double,
    onBack: () -> Unit = {},
    onIrALogin: () -> Unit = {},
    viewModel: ResultadoDensidadVolumenViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(estructura.id, masaMolar, volumenCm3) {
        viewModel.inicializar(estructura, masaMolar, volumenCm3)
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
                titulo = "Densidad teórica (ρ)",
                expandido = 3 in uiState.pasosExpandidos,
                onAlternar = { viewModel.alternarPaso(3) },
                destacado = true
            ) {
                ContenidoDensidad(datos)
            }
        }
    }
}
