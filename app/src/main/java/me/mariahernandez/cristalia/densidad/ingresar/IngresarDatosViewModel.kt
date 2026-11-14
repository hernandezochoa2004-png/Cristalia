package com.cristalia.ui.ingresardatos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.mariahernandez.cristalia.SessionRepository
import me.mariahernandez.cristalia.SessionRepositoryFalso
import me.mariahernandez.cristalia.celdaunitaria.EventoUi
import com.cristalia.model.EstructuraCristalina

/** Los 3 tabs del mockup: Radio, Volumen, Parámetro a. */
enum class ModoEntrada { RADIO, VOLUMEN, PARAMETRO_A }

/**
 * Estado completo de la pantalla IngresarDatosScreen.
 */
data class IngresarDatosUiState(
    val estructura: EstructuraCristalina? = null,
    val modoEntrada: ModoEntrada = ModoEntrada.RADIO,

    // Campos compartidos
    val masaMolar: String = "",

    // Tab Radio
    val radioCation: String = "",
    val radioAnion: String = "",

    // Tab Volumen
    val volumenCelda: String = "",

    // Tab Parámetro a
    val parametroA: String = "",
    val parametroC: String = "",

    // Sección expandible "Compuestos"
    val seccionCompuestosExpandida: Boolean = false,

    // Resultado
    val densidadCalculada: Double? = null,
    val mensajeError: String? = null
)

class IngresarDatosViewModel(
    savedStateHandle: SavedStateHandle? = null,
    private val session: SessionRepository = SessionRepositoryFalso
) : ViewModel() {

    private val _uiState = MutableStateFlow(IngresarDatosUiState())
    val uiState: StateFlow<IngresarDatosUiState> = _uiState.asStateFlow()

    private val _eventos = Channel<EventoUi>(Channel.BUFFERED)
    val eventos = _eventos.receiveAsFlow()

    /** Carga la estructura si no es la misma que ya está en el estado (evita reset al rotar). */
    fun cargarEstructura(estructura: EstructuraCristalina) {
        if (_uiState.value.estructura?.id != estructura.id) {
            _uiState.update {
                IngresarDatosUiState(estructura = estructura)
            }
        }
    }

    fun resetearDensidadCalculada() {
        _uiState.update { it.copy(densidadCalculada = null) }
    }

    fun seleccionarModo(modo: ModoEntrada) {
        _uiState.update { it.copy(modoEntrada = modo, densidadCalculada = null, mensajeError = null) }
    }

    fun alternarSeccionCompuestos() {
        _uiState.update { it.copy(seccionCompuestosExpandida = !it.seccionCompuestosExpandida) }
    }

    fun actualizarMasaMolar(valor: String) = _uiState.update { it.copy(masaMolar = valor) }
    fun actualizarRadioCation(valor: String) = _uiState.update { it.copy(radioCation = valor) }
    fun actualizarRadioAnion(valor: String) = _uiState.update { it.copy(radioAnion = valor) }
    fun actualizarVolumenCelda(valor: String) = _uiState.update { it.copy(volumenCelda = valor) }
    fun actualizarParametroA(valor: String) = _uiState.update { it.copy(parametroA = valor) }
    fun actualizarParametroC(valor: String) = _uiState.update { it.copy(parametroC = valor) }

    /** Si hay sesión descarga el GLB; si no, manda al login. */
    fun onDescargarClick(archivoGlb: String) {
        viewModelScope.launch {
            if (session.estaLogueado()) {
                _eventos.send(EventoUi.Descargar(archivoGlb))
            } else {
                _eventos.send(EventoUi.IrALogin)
            }
        }
    }

    /** Ejecuta el cálculo según el tab activo. Equivalente al botón "Calcular densidad". */
    fun calcularDensidad() {
        val estado = _uiState.value
        val estructura = estado.estructura ?: run {
            _uiState.update { it.copy(mensajeError = "No se ha seleccionado una estructura") }
            return
        }

        val masaMolar = estado.masaMolar.toDoubleOrNull()
        if (masaMolar == null || masaMolar <= 0.0) {
            _uiState.update { it.copy(mensajeError = "Ingresa una masa molar válida", densidadCalculada = null) }
            return
        }

        val volumenCm3: Double? = when (estado.modoEntrada) {
            ModoEntrada.RADIO -> calcularVolumenDesdeRadios(estructura, estado)
            ModoEntrada.VOLUMEN -> estado.volumenCelda.toDoubleOrNull()
            ModoEntrada.PARAMETRO_A -> calcularVolumenDesdeParametroA(estructura, estado)
        }

        if (volumenCm3 == null || volumenCm3 <= 0.0) {
            _uiState.update { it.copy(mensajeError = "Revisa los campos ingresados", densidadCalculada = null) }
            return
        }

        val densidad = (estructura.z * masaMolar) / (EstructuraCristalina.NUMERO_AVOGADRO * volumenCm3)
        _uiState.update { it.copy(densidadCalculada = densidad, mensajeError = null) }
    }

    private fun calcularVolumenDesdeRadios(
        estructura: EstructuraCristalina,
        estado: IngresarDatosUiState
    ): Double? {
        val rCat = estado.radioCation.toDoubleOrNull() ?: return null
        val rAn = estado.radioAnion.toDoubleOrNull() ?: return null
        val aPm = estructura.calcularParametroA(rCat, rAn) ?: return null
        return estructura.calcularVolumenCelda(aPm)
    }

    private fun calcularVolumenDesdeParametroA(
        estructura: EstructuraCristalina,
        estado: IngresarDatosUiState
    ): Double? {
        val aPm = estado.parametroA.toDoubleOrNull() ?: return null
        return if (estructura.requiereParametroC) {
            val cPm = estado.parametroC.toDoubleOrNull() ?: return null
            estructura.calcularVolumenCelda(aPm, cPm)
        } else {
            estructura.calcularVolumenCelda(aPm)
        }
    }
}
