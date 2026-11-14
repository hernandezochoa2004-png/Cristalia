package com.cristalia.ui.ingresardatos

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cristalia.model.EstructuraCristalina
import me.mariahernandez.cristalia.R
import me.mariahernandez.cristalia.celdaunitaria.EventoUi
import me.mariahernandez.cristalia.celdaunitaria.Paleta
import me.mariahernandez.cristalia.celdaunitaria.Visor3D
import me.mariahernandez.cristalia.celdaunitaria.guardarGlbEnDescargas

private val Fondo = Color(0xFF0A0E17)
private val Tarjeta = Color(0xFF141A2B)
private val BordeApagado = Color(0xFF1E2A3D)
private val LineaGrilla = Color(0xFF111826)

@Composable
fun IngresarDatosScreen(
    estructura: EstructuraCristalina,
    onBack: () -> Unit = {},
    onIrALogin: () -> Unit = {},
    onNavegarAResultado: (densidad: Double) -> Unit = {},
    viewModel: IngresarDatosViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(estructura.id) {
        viewModel.cargarEstructura(estructura)
    }

    // Eventos de una sola vez: ir al login o descargar
    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                EventoUi.IrALogin -> onIrALogin()
                is EventoUi.Descargar -> {
                    val ok = guardarGlbEnDescargas(context, evento.archivoGlb)
                    Toast.makeText(
                        context,
                        if (ok) "Guardado en Descargas" else "Error al guardar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Cuando la densidad se calcula exitosamente (sin errores), se ejecuta el callback para ir a la siguiente pantalla
    LaunchedEffect(uiState.densidadCalculada) {
        uiState.densidadCalculada?.let { densidad ->
            onNavegarAResultado(densidad)
            viewModel.resetearDensidadCalculada()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .drawBehind {
                val paso = 24.dp.toPx()
                var x = 0f
                while (x < size.width) {
                    drawLine(LineaGrilla, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                    x += paso
                }
                var y = 0f
                while (y < size.height) {
                    drawLine(LineaGrilla, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                    y += paso
                }
            }
            .statusBarsPadding()
    ) {
        BarraSuperior(onVolver = onBack, onIrALogin = onIrALogin)

        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BordeApagado.copy(alpha = 0.6f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val glbPath = estructura.modeloGlbAssetPath ?: "${estructura.id}.glb"

            TarjetaVisorModelo(
                estructura = estructura,
                onDescargar = { viewModel.onDescargarClick(glbPath) }
            )

            SelectorModoEntrada(
                modoSeleccionado = uiState.modoEntrada,
                onModoSeleccionado = viewModel::seleccionarModo
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Tarjeta,
                border = BorderStroke(1.dp, Paleta.Cian.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                            volumenMantisa = uiState.volumenMantisa,
                            onVolumenMantisaChange = viewModel::actualizarVolumenMantisa,
                            volumenExponente = uiState.volumenExponente,
                            onVolumenExponenteChange = viewModel::actualizarVolumenExponente,
                            onAlternarSignoExponente = viewModel::alternarSignoExponente
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

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = viewModel::calcularDensidad,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Paleta.Cian,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Calculate,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        "Calcular densidad",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                uiState.mensajeError?.let {
                    Text(
                        text = it,
                        color = Color(0xFFFF5252),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            SeccionCompuestos(
                estructura = estructura,
                expandido = uiState.seccionCompuestosExpandida,
                onAlternar = viewModel::alternarSeccionCompuestos
            )
        }
    }
}

@Composable
private fun BarraSuperior(onVolver: () -> Unit, onIrALogin: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onVolver) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White
            )
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Tarjeta)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo Cristalia",
                    modifier = Modifier.size(20.dp)
                )
                Text("Cristalia", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier.clickable { onIrALogin() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = null,
                    tint = Paleta.TextoSecundario,
                    modifier = Modifier.size(18.dp)
                )
                Text("Iniciar sesión", color = Paleta.TextoSecundario, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun TarjetaVisorModelo(
    estructura: EstructuraCristalina,
    onDescargar: () -> Unit = {}
) {
    val glbPath = estructura.modeloGlbAssetPath ?: "${estructura.id}.glb"
    val context = LocalContext.current
    val existeGlb = remember(glbPath) {
        try { context.assets.open(glbPath).close(); true } catch (e: Exception) { false }
    }

    val textoExplicativo = buildAnnotatedString {
        append("La densidad teórica se calcula a partir del ")
        withStyle(SpanStyle(color = Paleta.Cian, fontWeight = FontWeight.Bold)) {
            append("radio iónico")
        }
        append(", ")
        withStyle(SpanStyle(color = Paleta.Cian, fontWeight = FontWeight.Bold)) {
            append("volumen")
        }
        append(" o ")
        withStyle(SpanStyle(color = Paleta.Cian, fontWeight = FontWeight.Bold)) {
            append("parámetro a")
        }
        append(".")
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Tarjeta,
        border = BorderStroke(1.dp, Paleta.Cian.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (existeGlb) {
                    Visor3D(rutaGlb = glbPath, modifier = Modifier.fillMaxSize())
                    IconButton(
                        onClick = onDescargar,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Descargar modelo GLB",
                            tint = Paleta.Cian
                        )
                    }
                } else {
                    Text(
                        text = "Modelo 3D (.glb)\napp/src/main/assets/$glbPath",
                        color = Paleta.TextoSecundario,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .border(1.dp, Paleta.Cian.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Tipo ${estructura.nombre} · ${estructura.sistema.name}",
                        color = Paleta.Cian,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = textoExplicativo,
                color = Color.White,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Tarjeta)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        opciones.forEach { (modo, titulo) ->
            val activo = modo == modoSeleccionado
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .then(
                        if (activo) Modifier.border(1.dp, Paleta.Cian, RoundedCornerShape(10.dp))
                        else Modifier
                    )
                    .background(if (activo) Paleta.Cian else Color.Transparent)
                    .clickable { onModoSeleccionado(modo) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = titulo,
                    color = if (activo) Color.Black else Paleta.TextoSecundario,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun SeccionCompuestos(
    estructura: EstructuraCristalina,
    expandido: Boolean,
    onAlternar: () -> Unit
) {
    val forma = RoundedCornerShape(16.dp)

    Surface(
        shape = forma,
        color = Tarjeta,
        border = BorderStroke(1.dp, BordeApagado),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAlternar() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.GridView,
                        contentDescription = null,
                        tint = Paleta.Magenta,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Compuestos con esta estructura",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Icon(
                    imageVector = if (expandido) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Paleta.TextoSecundario
                )
            }

            AnimatedVisibility(visible = expandido) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (estructura.compuestosRelacionados.isEmpty()) {
                        Text(
                            text = "Sin compuestos cargados para esta estructura",
                            color = Paleta.TextoSecundario,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        estructura.compuestosRelacionados.forEach { compuesto ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.35f))
                                    .padding(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = compuesto.categoria,
                                        color = Paleta.Magenta,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = compuesto.formula,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}