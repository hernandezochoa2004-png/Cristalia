package me.mariahernandez.cristalia.densidad

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Una opción dentro de una estructura (NaCl, Blenda, Fluorita...). */
data class OpcionDensidad(
    val id: String,
    val titulo: String,
    val subtitulo: String,
    val imagen: String            // nombre del drawable en res/drawable (sin extensión)
)

/** Una tarjeta de la pantalla: "Estructura AB" con su badge "Relación 1:1". */
data class EstructuraDensidad(
    val id: String,
    val titulo: String,
    val relacion: String,
    val opciones: List<OpcionDensidad>
)

data class DensidadUiState(
    val estructuras: List<EstructuraDensidad> = listOf(
        EstructuraDensidad(
            id = "AB", titulo = "Estructura AB", relacion = "1:1",
            opciones = listOf(
                OpcionDensidad("nacl", "NaCl", "Cloruro de sodio", "nacl"),
                OpcionDensidad("blenda", "Blenda", "Blenda de zinc", "blenda"),
                OpcionDensidad("wurtzita", "Wurtzita", "Estructura wurtzita", "wurtzita"),
                OpcionDensidad("cscl", "CsCl", "Cloruro de cesio", "cscl")
            )
        ),
        EstructuraDensidad(
            id = "AB2", titulo = "Estructura AB₂", relacion = "1:2",
            opciones = listOf(
                OpcionDensidad("fluorita", "Fluorita", "CaF₂ / Estructura fluorita", "fluorita"),
                OpcionDensidad("antifluorita", "Antifluorita", "Óxido de litio / Antifluorita", "antifluorita"),
                OpcionDensidad("cdi2", "Yoduro de cadmio", "CdI₂ / Capas coordinadas", "yodurocadmio"),
                OpcionDensidad("rutilo", "Rutilo", "TiO₂ / Dióxido de titanio", "rutilo")
            )
        )
    ),
    /** Ids de las tarjetas desplegadas; pueden estar varias abiertas a la vez. */
    val expandidas: Set<String> = emptySet()
)

class DensidadViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DensidadUiState())
    val uiState: StateFlow<DensidadUiState> = _uiState.asStateFlow()

    /** Despliega la tarjeta; si ya estaba abierta la pliega. */
    fun alternar(id: String) {
        _uiState.update { estado ->
            estado.copy(
                expandidas = if (id in estado.expandidas) estado.expandidas - id
                else estado.expandidas + id
            )
        }
    }
}
