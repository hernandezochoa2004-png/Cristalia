package me.mariahernandez.cristalia.densidad.parametroa

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.cristalia.model.EstructuraCristalina
import me.mariahernandez.cristalia.densidad.radio.DatosCalculoRadio
import me.mariahernandez.cristalia.densidad.radio.FuncionesCalculoRadio

data class ResultadoDensidadParametroAUiState(
    val estructura: EstructuraCristalina? = null,
    val masaMolar: Double = 0.0,
    val aPm: Double = 0.0,
    val cPm: Double? = null,
    val datos: DatosCalculoRadio? = null,
    val pasosExpandidos: Set<Int> = emptySet()
)

/**
 * Resultado de la densidad cuando el usuario ingresó masa molar + parámetro de red a
 * (y c en las estructuras hexagonales / tetragonales), ambos en pm.
 * El cálculo sale de FuncionesCalculoRadio, igual que en los modos Radio y Volumen.
 */
class ResultadoDensidadParametroAViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ResultadoDensidadParametroAUiState())
    val uiState: StateFlow<ResultadoDensidadParametroAUiState> = _uiState.asStateFlow()

    fun inicializar(
        estructura: EstructuraCristalina,
        masaMolar: Double,
        aPm: Double,
        cPm: Double?
    ) {
        val actual = _uiState.value
        val cambio = actual.estructura?.id != estructura.id ||
            actual.masaMolar != masaMolar ||
            actual.aPm != aPm ||
            actual.cPm != cPm

        if (cambio) {
            // Valores <= 0 no se calculan; si la estructura pide c y no llega, tampoco
            // (calcularVolumenCelda lanzaría una excepción).
            val datosValidos = masaMolar > 0.0 && aPm > 0.0 &&
                (!estructura.requiereParametroC || (cPm != null && cPm > 0.0))

            val datos = if (datosValidos) {
                FuncionesCalculoRadio.calcularDesdeParametroA(
                    estructura = estructura,
                    masaMolar = masaMolar,
                    aPm = aPm,
                    cPm = if (estructura.requiereParametroC) cPm else null
                )
            } else null

            _uiState.update {
                ResultadoDensidadParametroAUiState(
                    estructura = estructura,
                    masaMolar = masaMolar,
                    aPm = aPm,
                    cPm = cPm,
                    datos = datos,
                    pasosExpandidos = emptySet()
                )
            }
        }
    }

    /** Pliega o despliega un paso (1 = iones, 2 = unidad fórmula, 3 = volumen, 4 = densidad). */
    fun alternarPaso(pasoNumero: Int) {
        _uiState.update { estado ->
            estado.copy(
                pasosExpandidos = if (pasoNumero in estado.pasosExpandidos) {
                    estado.pasosExpandidos - pasoNumero
                } else {
                    estado.pasosExpandidos + pasoNumero
                }
            )
        }
    }
}
