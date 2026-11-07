package me.mariahernandez.cristalia.huecos

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Una opción dentro de un grupo (SC, NaCl, Rutilo...). */
data class OpcionHuecos(
    val id: String,
    val titulo: String,
    val subtitulo: String,
    val imagen: String            // nombre del drawable en res/drawable (sin extensión)
)

/** Una tarjeta desplegable: "Estructuras A", "Estructura AB", "Estructura AB₂". */
data class GrupoHuecos(
    val id: String,
    val titulo: String,
    val badge: String,            // texto completo del recuadro: "Monoatómicas", "Relación 1:1"...
    val opciones: List<OpcionHuecos>
)

data class HuecosUiState(
    val grupos: List<GrupoHuecos> = listOf(
        GrupoHuecos(
            id = "A", titulo = "Estructuras A", badge = "Monoatómicas",
            opciones = listOf(
                OpcionHuecos("sc", "SC", "Cúbica Simple", "sc"),
                OpcionHuecos("bcc", "BCC", "Cúbica Centrada Cuerpo", "bcc"),
                OpcionHuecos("fcc", "FCC", "Cúbica Centrada Caras", "fcc"),
                OpcionHuecos("hcp", "HCP", "Hexagonal Compacta", "hcp")
            )
        ),
        GrupoHuecos(
            id = "AB", titulo = "Estructura AB", badge = "Relación 1:1",
            opciones = listOf(
                OpcionHuecos("nacl", "NaCl", "Cloruro de sodio", "nacl"),
                OpcionHuecos("blenda", "Blenda", "Blenda de zinc", "blenda"),
                OpcionHuecos("wurtzita", "Wurtzita", "Estructura wurtzita", "wurtzita"),
                OpcionHuecos("cscl", "CsCl", "Cloruro de cesio", "cscl")
            )
        ),
        GrupoHuecos(
            id = "AB2", titulo = "Estructura AB₂", badge = "Relación 1:2",
            opciones = listOf(
                OpcionHuecos("fluorita", "Fluorita", "CaF₂ / Estructura fluorita", "fluorita"),
                OpcionHuecos("antifluorita", "Antifluorita", "Óxido de litio / Antifluorita", "antifluorita"),
                OpcionHuecos("cdi2", "Yoduro de cadmio", "CdI₂ / Capas coordinadas", "yodurocadmio"),
                OpcionHuecos("rutilo", "Rutilo", "TiO₂ / Dióxido de titanio", "rutilo")
            )
        )
    ),
    /** Ids de los grupos desplegados; ninguno al empezar, pueden abrirse varios a la vez. */
    val expandidos: Set<String> = emptySet()
)

class HuecosViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HuecosUiState())
    val uiState: StateFlow<HuecosUiState> = _uiState.asStateFlow()

    /** Despliega el grupo; si ya estaba abierto lo pliega. */
    fun alternar(id: String) {
        _uiState.update { estado ->
            estado.copy(
                expandidos = if (id in estado.expandidos) estado.expandidos - id
                else estado.expandidos + id
            )
        }
    }
}
