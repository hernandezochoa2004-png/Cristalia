package me.mariahernandez.cristalia.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import me.mariahernandez.cristalia.SessionRepository
import me.mariahernandez.cristalia.SessionRepositoryFalso

data class MenuItemData(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val badges: List<String> = emptyList(),
    val formula: String? = null,
    val unidades: String? = null
)

data class MenuUiState(
    val estaLogueado: Boolean = false,
    val opciones: List<MenuItemData> = listOf(
        MenuItemData(
            id = "celda_unitaria",
            titulo = "Celda unitaria",
            descripcion = "Explora la geometría de cada estructura.",
            badges = listOf("SC", "BCC", "FCC", "HCP")
        ),
        MenuItemData(
            id = "factor_empaquetamiento",
            titulo = "Factor Empaquetamiento",
            descripcion = "Descubre cuánto espacio ocupan los átomos",
            formula = "APF = (N·V_átomo) / V_celda"
        ),
        MenuItemData(
            id = "densidad",
            titulo = "Densidad",
            descripcion = "Calcula la densidad de una estructura cirstalina",
            unidades = "g/cm³"
        ),
        MenuItemData(
            id = "huecos",
            titulo = "Huecos octaédricos y tetraédricos",
            descripcion = "Visualiza los huecos y números de coordinación"
        )
    )
)

sealed interface MenuEventoUi {
    data object NavegarACeldaUnitaria : MenuEventoUi
    data object NavegarAFactorEmpaquetamiento : MenuEventoUi
    data object NavegarADensidad : MenuEventoUi
    data object NavegarAHuecos : MenuEventoUi
    data object NavegarALogin : MenuEventoUi
}

class MenuViewModel(
    private val session: SessionRepository = SessionRepositoryFalso
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    private val _eventos = Channel<MenuEventoUi>(Channel.BUFFERED)
    val eventos = _eventos.receiveAsFlow()

    init {
        verificarSesion()
    }

    fun verificarSesion() {
        viewModelScope.launch {
            val logueado = session.estaLogueado()
            _uiState.value = _uiState.value.copy(estaLogueado = logueado)
        }
    }

    fun onOpcionClick(id: String) {
        viewModelScope.launch {
            when (id) {
                "celda_unitaria" -> _eventos.send(MenuEventoUi.NavegarACeldaUnitaria)
                "factor_empaquetamiento" -> _eventos.send(MenuEventoUi.NavegarAFactorEmpaquetamiento)
                "densidad" -> _eventos.send(MenuEventoUi.NavegarADensidad)
                "huecos" -> _eventos.send(MenuEventoUi.NavegarAHuecos)
            }
        }
    }

    fun onIniciarSesionClick() {
        viewModelScope.launch {
            _eventos.send(MenuEventoUi.NavegarALogin)
        }
    }
}
