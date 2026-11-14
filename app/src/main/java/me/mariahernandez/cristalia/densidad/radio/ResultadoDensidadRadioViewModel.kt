package me.mariahernandez.cristalia.densidad.radio

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.cristalia.model.EstructuraCristalina

data class ResultadoDensidadRadioUiState(
    val estructura: EstructuraCristalina? = null,
    val masaMolar: Double = 0.0,
    val radioCatPm: Double = 0.0,
    val radioAnPm: Double = 0.0,
    val datos: DatosCalculoRadio? = null,
    val pasosExpandidos: Set<Int> = emptySet()
)

class ResultadoDensidadRadioViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ResultadoDensidadRadioUiState())
    val uiState: StateFlow<ResultadoDensidadRadioUiState> = _uiState.asStateFlow()

    fun inicializar(
        estructura: EstructuraCristalina,
        masaMolar: Double,
        radioCatPm: Double,
        radioAnPm: Double
    ) {
        if (_uiState.value.estructura?.id != estructura.id ||
            _uiState.value.masaMolar != masaMolar ||
            _uiState.value.radioCatPm != radioCatPm ||
            _uiState.value.radioAnPm != radioAnPm
        ) {
            val datos = FuncionesCalculoRadio.calcularTodo(estructura, masaMolar, radioCatPm, radioAnPm)

            _uiState.update {
                ResultadoDensidadRadioUiState(
                    estructura = estructura,
                    masaMolar = masaMolar,
                    radioCatPm = radioCatPm,
                    radioAnPm = radioAnPm,
                    datos = datos,
                    pasosExpandidos = emptySet()
                )
            }
        }
    }

    fun alternarPaso(pasoNumero: Int) {
        _uiState.update { estado ->
            val nuevosPaso = if (pasoNumero in estado.pasosExpandidos) {
                estado.pasosExpandidos - pasoNumero
            } else {
                estado.pasosExpandidos + pasoNumero
            }
            estado.copy(pasosExpandidos = nuevosPaso)
        }
    }
}
