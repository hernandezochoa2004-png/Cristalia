package me.mariahernandez.cristalia.celdaunitaria

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

/** Estado que sobrevive a la rotación de pantalla. */
data class CeldaUnitariaUiState(
    val tipoSeleccionado: TipoCelda = TipoCelda.SC             // Se busca que al entrar a la pantalla, al inicio sale SC. Abajo se hace el prceso (1)
)

/** Eventos de "una sola vez" (navegar, descargar). */
sealed interface EventoUi {
    data object IrALogin : EventoUi                              // Iniciar sesión
    data class Descargar(val archivoGlb: String) : EventoUi      // Descargar
}

// Filtro de inicio de sesión. Si inició sesión puede descargar, de lo contrario no

//                                       sesion          tipo           valor por defecto         clase padre
class CeldaUnitariaViewModel(private val session: SessionRepository = SessionRepositoryFalso) : ViewModel() {

    // Estado (Lo que la pantalla observa)
    private val _uiState = MutableStateFlow(CeldaUnitariaUiState() )         // (1) Crea el estado inicial (SC sale primero)
    val uiState: StateFlow<CeldaUnitariaUiState> = _uiState.asStateFlow()

    // Eventos que solo pasan una vez
    private val _eventos = Channel<EventoUi>(Channel.BUFFERED)           // Avisa cada vez que el estado de celda unitaria cambia (SC->FCC), como es privada, sólo viewModel lo cambia
    val eventos = _eventos.receiveAsFlow()                                         // Es pública y de sólo lectura. La pantalla la observa pero no la modifica.

    // Cambia selección. Ej: SC -> FCC
    fun seleccionarTipo(tipo: TipoCelda) {
        _uiState.update { it.copy(tipoSeleccionado = tipo) }
    }

    /** Si hay sesión descarga; si no, manda al login. */
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