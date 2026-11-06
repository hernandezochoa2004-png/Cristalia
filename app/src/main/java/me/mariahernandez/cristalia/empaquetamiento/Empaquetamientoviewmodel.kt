package me.mariahernandez.cristalia.empaquetamiento

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

/** Estado que sobrevive a la rotación de pantalla. */
data class EmpaquetamientoUiState(
    val tipoSeleccionado: TipoEstructura = TipoEstructura.SC      // al entrar sale SC primero
)

//                                       sesion          tipo           valor por defecto
class EmpaquetamientoViewModel(
    private val session: SessionRepository = SessionRepositoryFalso
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmpaquetamientoUiState())
    val uiState: StateFlow<EmpaquetamientoUiState> = _uiState.asStateFlow()

    // Eventos de una sola vez (login / descargar). Se reutiliza EventoUi de celdaunitaria.
    private val _eventos = Channel<EventoUi>(Channel.BUFFERED)
    val eventos = _eventos.receiveAsFlow()

    fun seleccionarTipo(tipo: TipoEstructura) {
        _uiState.update { it.copy(tipoSeleccionado = tipo) }
    }

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
}
