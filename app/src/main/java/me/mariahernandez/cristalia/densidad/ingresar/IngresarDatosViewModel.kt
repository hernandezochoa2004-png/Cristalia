package com.cristalia.ui.ingresardatos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.cristalia.model.EstructuraCristalina

/** Los 3 tabs del mockup: Radio, Volumen, Parámetro a. */
enum class ModoEntrada { RADIO, VOLUMEN, PARAMETRO_A }

/**
 * Estado completo de la pantalla IngresarDatosScreen.
 * Todo lo que las 3 interfaces de CamposEntradaContent.kt necesitan pintar.
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

    // Resultado
    val densidadCalculada: Double? = null,
    val mensajeError: String? = null
)

/**
 * b) ViewModel para IngresarDatosScreen.
 *
 * Recibe la estructura seleccionada en el menú (NaCl, Blenda, Wurtzita, CsCl,
 * Fluorita, Antifluorita, Yoduro de cadmio, Rutilo) y maneja el estado de los
 * 3 tabs de entrada, validaciones y el cálculo final de densidad:
 *
 *      ρ = (Z · M) / (Na · V)
 *
 * donde V se obtiene directo (tab Volumen) o se deriva de a (y c) ingresados
 * o calculados desde los radios iónicos.
 */
class IngresarDatosViewModel(
    savedStateHandle: SavedStateHandle? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(IngresarDatosUiState())
    val uiState: StateFlow<IngresarDatosUiState> = _uiState.asStateFlow()

    /** c) Punto de entrada: la pantalla llama esto con la estructura que llegó por navegación. */
    fun cargarEstructura(estructura: EstructuraCristalina) {
        _uiState.update {
            IngresarDatosUiState(estructura = estructura) // resetea los campos al cambiar de estructura
        }
    }

    fun seleccionarModo(modo: ModoEntrada) {
        _uiState.update { it.copy(modoEntrada = modo, densidadCalculada = null, mensajeError = null) }
    }

    fun actualizarMasaMolar(valor: String) = _uiState.update { it.copy(masaMolar = valor) }
    fun actualizarRadioCation(valor: String) = _uiState.update { it.copy(radioCation = valor) }
    fun actualizarRadioAnion(valor: String) = _uiState.update { it.copy(radioAnion = valor) }
    fun actualizarVolumenCelda(valor: String) = _uiState.update { it.copy(volumenCelda = valor) }
    fun actualizarParametroA(valor: String) = _uiState.update { it.copy(parametroA = valor) }
    fun actualizarParametroC(valor: String) = _uiState.update { it.copy(parametroC = valor) }

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
