package me.mariahernandez.cristalia.densidad.volumen

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.cristalia.model.EstructuraCristalina
import me.mariahernandez.cristalia.densidad.radio.DatosCalculoRadio
import me.mariahernandez.cristalia.densidad.radio.FuncionesCalculoRadio

data class ResultadoDensidadVolumenUiState(
    val estructura: EstructuraCristalina? = null,
    val masaMolar: Double = 0.0,
    val volumenCm3: Double = 0.0,
    val datos: DatosCalculoRadio? = null,
    val pasosExpandidos: Set<Int> = emptySet()
)

/**
 * Resultado de la densidad cuando el usuario ingresó masa molar + volumen de celda (cm³).
 * El cálculo y los textos de los pasos salen de FuncionesCalculoRadio (mismo código del modo Radio).
 */
class ResultadoDensidadVolumenViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ResultadoDensidadVolumenUiState())
    val uiState: StateFlow<ResultadoDensidadVolumenUiState> = _uiState.asStateFlow()

    fun inicializar(
        estructura: EstructuraCristalina,
        masaMolar: Double,
        volumenCm3: Double
    ) {
        val actual = _uiState.value
        val cambio = actual.estructura?.id != estructura.id ||
            actual.masaMolar != masaMolar ||
            actual.volumenCm3 != volumenCm3

        if (cambio) {
            // Con masa o volumen <= 0 no se calcula (evita dividir entre cero).
            val datos = if (masaMolar > 0.0 && volumenCm3 > 0.0) {
                FuncionesCalculoRadio.calcularDesdeVolumen(estructura, masaMolar, volumenCm3)
            } else null

            _uiState.update {
                ResultadoDensidadVolumenUiState(
                    estructura = estructura,
                    masaMolar = masaMolar,
                    volumenCm3 = volumenCm3,
                    datos = datos,
                    pasosExpandidos = emptySet()
                )
            }
        }
    }

    /** Pliega o despliega un paso (1 = iones, 2 = unidad fórmula, 3 = densidad). */
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
