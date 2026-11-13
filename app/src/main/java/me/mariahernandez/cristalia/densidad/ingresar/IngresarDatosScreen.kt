package com.cristalia.ui.ingresardatos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cristalia.model.EstructuraCristalina
import androidx.compose.material.icons.filled.ArrowBack

/**
 * c) Pantalla que recibe la estructura seleccionada en el menú de Densidad
 * (NaCl, Blenda, Wurtzita, CsCl, Fluorita, Antifluorita, Yoduro de cadmio,
 * Rutilo) y adapta los campos de entrada según el tab activo.
 *
 * @param estructura la estructura que llegó por navegación desde el menú.
 * @param onBack callback del botón "<" de la barra superior.
 * @param viewModel se crea con Factory en tu NavHost real; aquí se obtiene
 *        con viewModel() para que el preview/uso directo compile.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngresarDatosScreen(
    estructura: EstructuraCristalina,
    onBack: () -> Unit = {},
    viewModel: IngresarDatosViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Carga la estructura seleccionada una sola vez (o cuando cambia el id).
    androidx.compose.runtime.LaunchedEffect(estructura.id) {
        viewModel.cargarEstructura(estructura)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cristalia") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
        ) {
            item {
                VisorModelo3D(estructura = estructura)
                Text(
                    text = "La densidad teórica se calcula a partir del radio iónico, volumen o parámetro a.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 20.dp)
                )
            }

            item {
                SelectorModoEntrada(
                    modoSeleccionado = uiState.modoEntrada,
                    onModoSeleccionado = viewModel::seleccionarModo
                )
                Row(modifier = Modifier.padding(bottom = 16.dp)) {} // espaciador
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // ---- Aquí se adapta la interfaz según el tab activo ----
                        when (uiState.modoEntrada) {
                            ModoEntrada.RADIO -> CampoRadioContent(
                                estructura = estructura,
                                masaMolar = uiState.masaMolar,
                                onMasaMolarChange = viewModel::actualizarMasaMolar,
                                radioCation = uiState.radioCation,
                                onRadioCationChange = viewModel::actualizarRadioCation,
                                radioAnion = uiState.radioAnion,
                                onRadioAnionChange = viewModel::actualizarRadioAnion
                            )
                            ModoEntrada.VOLUMEN -> CampoVolumenContent(
                                estructura = estructura,
                                masaMolar = uiState.masaMolar,
                                onMasaMolarChange = viewModel::actualizarMasaMolar,
                                volumenCelda = uiState.volumenCelda,
                                onVolumenCeldaChange = viewModel::actualizarVolumenCelda
                            )
                            ModoEntrada.PARAMETRO_A -> CampoParametroAContent(
                                estructura = estructura,
                                masaMolar = uiState.masaMolar,
                                onMasaMolarChange = viewModel::actualizarMasaMolar,
                                parametroA = uiState.parametroA,
                                onParametroAChange = viewModel::actualizarParametroA,
                                parametroC = uiState.parametroC,
                                onParametroCChange = viewModel::actualizarParametroC
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = viewModel::calcularDensidad,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 12.dp)
                ) {
                    Text("Calcular densidad")
                }

                uiState.mensajeError?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
                uiState.densidadCalculada?.let { densidad ->
                    Text(
                        text = "Densidad teórica: %.3f g/cm³".format(densidad),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }

            item {
                SeccionCompuestos(estructura = estructura)
            }
        }
    }
}

/** Tabs Radio / Volumen / Parámetro a, iguales a los del mockup. */
@Composable
private fun SelectorModoEntrada(
    modoSeleccionado: ModoEntrada,
    onModoSeleccionado: (ModoEntrada) -> Unit
) {
    val opciones = listOf(
        ModoEntrada.RADIO to "Radio",
        ModoEntrada.VOLUMEN to "Volumen",
        ModoEntrada.PARAMETRO_A to "Parámetro a"
    )
    val indiceSeleccionado = opciones.indexOfFirst { it.first == modoSeleccionado }

    TabRow(selectedTabIndex = indiceSeleccionado) {
        opciones.forEach { (modo, titulo) ->
            Tab(
                selected = modo == modoSeleccionado,
                onClick = { onModoSeleccionado(modo) },
                text = { Text(titulo) }
            )
        }
    }
}

/**
 * Espacio reservado para el visor del modelo 3D (.glb) de la estructura.
 * Reemplaza el contenido de este Box por tu componente de render GLB
 * (por ejemplo un AndroidView con Filament / SceneView / model-viewer),
 * usando `estructura.modeloGlbAssetPath` como fuente del modelo.
 */
@Composable
private fun VisorModelo3D(estructura: EstructuraCristalina) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.1f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // TODO: reemplazar por el visor GLB real.
            // estructura.modeloGlbAssetPath -> ruta del .glb a cargar.
            Text(
                text = "Modelo 3D (.glb)\n${estructura.nombre}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/** Sección expandible "Compuestos con esta estructura", igual al mockup. */
@Composable
private fun SeccionCompuestos(estructura: EstructuraCristalina) {
    var expandido by remember(estructura.id) { mutableStateOf(true) }

    Column(modifier = Modifier.padding(top = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expandido = !expandido },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Compuestos con esta estructura",
                style = MaterialTheme.typography.titleSmall
            )
            Icon(
                imageVector = if (expandido) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null
            )
        }

        if (expandido) {
            estructura.compuestosRelacionados.forEach { compuesto ->
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(text = compuesto.categoria, style = MaterialTheme.typography.labelMedium)
                    Text(text = compuesto.formula, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
