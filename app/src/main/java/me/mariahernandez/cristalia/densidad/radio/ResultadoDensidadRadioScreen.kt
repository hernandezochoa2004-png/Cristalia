package me.mariahernandez.cristalia.densidad.radio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cristalia.model.EstructuraCristalina
import me.mariahernandez.cristalia.R
import me.mariahernandez.cristalia.celdaunitaria.Paleta
import me.mariahernandez.cristalia.celdaunitaria.Visor3D

private val Fondo = Color(0xFF0A0E17)
private val Tarjeta = Color(0xFF141A2B)
private val BordeApagado = Color(0xFF1E2A3D)
private val BotonFondo = Color(0xFF1C2335)
private val TextoApagado = Color(0xFF9AA4B8)
private val LineaGrilla = Color(0xFF111826)
private val Rosa = Color(0xFFF2708A)

@Composable
fun ResultadoDensidadRadioScreen(
    estructura: EstructuraCristalina,
    masaMolar: Double,
    radioCation: Double,
    radioAnion: Double,
    onBack: () -> Unit = {},
    onIrALogin: () -> Unit = {},
    viewModel: ResultadoDensidadRadioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(estructura.id, masaMolar, radioCation, radioAnion) {
        viewModel.inicializar(estructura, masaMolar, radioCation, radioAnion)
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
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TituloSeccionResultado()
            TarjetaEstructuraEncabezado(estructura = estructura)

            uiState.datos?.let { datos ->

                PasoCard(
                    pasoNumero = 1,
                    titulo = "Número de iones y cationes",
                    expandido = 1 in uiState.pasosExpandidos,
                    onAlternar = { viewModel.alternarPaso(1) }
                ) {
                    if (datos.conteoFilas.isNotEmpty()) {
                        ConteoIonesContent(estructura = estructura, datos = datos)
                    } else {
                        LineasConteoTexto(datos.paso1Lineas)
                    }
                }

                PasoCard(
                    pasoNumero = 2,
                    titulo = "Unidad fórmula",
                    expandido = 2 in uiState.pasosExpandidos,
                    onAlternar = { viewModel.alternarPaso(2) },
                    colorAcento = TextoApagado
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.35f))
                            .border(1.dp, BordeApagado, RoundedCornerShape(10.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = datos.formulaAnionCationStr,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = datos.zStr,
                                color = Paleta.Cian,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                PasoCard(
                    pasoNumero = 3,
                    titulo = "Volumen celda unitaria",
                    expandido = 3 in uiState.pasosExpandidos,
                    onAlternar = { viewModel.alternarPaso(3) }
                ) {
                    ContenidoVolumenCelda(datos)
                }

                PasoCard(
                    pasoNumero = 4,
                    titulo = "Densidad teórica (ρ)",
                    expandido = 4 in uiState.pasosExpandidos,
                    onAlternar = { viewModel.alternarPaso(4) },
                    destacado = true
                ) {
                    ContenidoDensidad(datos)
                }

            }
        }
    }
}

@Composable
private fun BarraSuperior(onVolver: () -> Unit, onIrALogin: () -> Unit) {
    val context = LocalContext.current
    // painterResource lanza excepción (y cierra la app) si el drawable no es
    // vector ni raster (p. ej. adaptive-icon / layer-list). Esto lo carga de forma segura.
    val logo = remember {
        runCatching {
            ContextCompat.getDrawable(context, R.drawable.logo)?.toBitmap(96, 96)?.asImageBitmap()
        }.getOrNull()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(BotonFondo)
                .border(1.dp, BordeApagado, RoundedCornerShape(12.dp))
                .clickable { onVolver() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Volver",
                tint = Color.White
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PildoraBarra {
                if (logo != null) {
                    Image(
                        bitmap = logo,
                        contentDescription = "Logo Cristalia",
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    "Cristalia",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }

            PildoraBarra(onClick = onIrALogin) {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Iniciar sesión",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun PildoraBarra(
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val forma = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .clip(forma)
            .background(Tarjeta)
            .border(1.dp, BordeApagado, forma)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }
}

@Composable
private fun TituloSeccionResultado() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Paleta.Cian)
        )
        Text(
            text = "Densidad",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            softWrap = false
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Paleta.Cian.copy(alpha = 0.12f))
                .border(1.dp, Paleta.Cian.copy(alpha = 0.6f), RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text(
                text = "Paso a paso",
                color = Paleta.Cian,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

private data class InfoRed(val descripcion: String, val sigla: String, val sistema: String)

private fun infoRed(id: String): InfoRed? = when (id) {
    "nacl", "blenda", "fluorita", "antifluorita" ->
        InfoRed("Red cúbica centrada en las caras", "FCC", "Cúbica FCC")
    "cscl" ->
        InfoRed("Red cúbica simple", "SC", "Cúbica simple")
    else -> null
}

@Composable
private fun TarjetaEstructuraEncabezado(estructura: EstructuraCristalina) {
    val glbPath = estructura.modeloGlbAssetPath ?: "${estructura.id}.glb"
    val context = LocalContext.current
    val existeGlb = remember(glbPath) {
        try { context.assets.open(glbPath).close(); true } catch (e: Exception) { false }
    }
    val info = infoRed(estructura.id)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Tarjeta,
        border = BorderStroke(1.dp, Paleta.Cian.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF070B14))
                    .border(1.dp, Paleta.Cian.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (existeGlb) {
                    Visor3D(rutaGlb = glbPath, modifier = Modifier.fillMaxSize())
                } else {
                    Text(
                        text = "Modelo 3D (.glb)\napp/src/main/assets/$glbPath",
                        color = Paleta.TextoSecundario,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Estructura Tipo ${estructura.nombre}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = info?.descripcion ?: "Red ${estructura.formulaQuimica}",
                        color = Paleta.TextoSecundario,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Paleta.Cian.copy(alpha = 0.15f))
                            .border(1.dp, Paleta.Cian.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = info?.sigla ?: estructura.sistema.name,
                            color = Paleta.Cian,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(BotonFondo)
                            .border(1.dp, BordeApagado, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = info?.sistema ?: "Z = ${estructura.z}",
                            color = Paleta.Cian,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PasoCard(
    pasoNumero: Int,
    titulo: String,
    expandido: Boolean,
    onAlternar: () -> Unit,
    colorAcento: Color = Paleta.Cian,
    destacado: Boolean = false,
    content: @Composable () -> Unit
) {
    val activo = expandido || destacado
    val forma = RoundedCornerShape(16.dp)

    Surface(
        shape = forma,
        color = Tarjeta,
        border = BorderStroke(1.dp, if (activo) colorAcento.copy(alpha = 0.6f) else BordeApagado),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAlternar() }
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(if (activo) colorAcento else Color.Transparent)
                        .border(2.dp, colorAcento, CircleShape)
                )

                Text(
                    text = titulo,
                    color = if (destacado) colorAcento else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = if (expandido) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = colorAcento
                )
            }

            AnimatedVisibility(visible = expandido) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(BordeApagado.copy(alpha = 0.7f))
                    )
                    content()
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  PASO 1: conteo de iones (imagen + tarjetas + totales)
// ─────────────────────────────────────────────────────────────

/**
 * AQUÍ PONES TUS MODELOS GLB DEL PASO 1.
 * 1) Copia el .glb a app/src/main/assets/ (puede ir en una subcarpeta, ej. models/densidad/).
 * 2) Escribe su ruta aquí, relativa a assets, sin barra al inicio.
 * Mientras valga null se muestra el recuadro con el texto "Aquí va tu modelo 3D".
 */
private fun glbConteoIones(id: String): String? = when (id) {
    "nacl" -> "models/densidad/naclatoms_centrado.glb"
    "blenda" -> null          // "models/densidad/blenda_atoms.glb"
    "cscl" -> null            // "models/densidad/cscl_atoms.glb"
    "fluorita" -> null        // "models/densidad/fluorita_atoms.glb"
    "antifluorita" -> null    // "models/densidad/antifluorita_atoms.glb"
    else -> null
}

@Composable
private fun ConteoIonesContent(estructura: EstructuraCristalina, datos: DatosCalculoRadio) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ImagenConteoIones(estructura.id)

        // Tarjetas de 2 en 2
        datos.conteoFilas.chunked(2).forEach { fila ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                fila.forEach { item ->
                    TarjetaConteo(
                        fila = item,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }

        ResumenIones(totalAniones = datos.totalAniones, totalCationes = datos.totalCationes)
    }
}

@Composable
private fun ImagenConteoIones(estructuraId: String) {
    val context = LocalContext.current
    val rutaGlb = glbConteoIones(estructuraId)
    // Solo muestra el visor si el archivo existe en assets (evita cerrar la app si falta)
    val existeGlb = remember(rutaGlb) {
        rutaGlb != null && try { context.assets.open(rutaGlb).close(); true } catch (e: Exception) { false }
    }

    // Marco exterior (como en el diseño)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0E1424))
            .border(1.dp, BordeApagado, RoundedCornerShape(16.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // ESPACIO DEL MODELO 3D. Cambia fillMaxWidth(0.7f) si lo quieres más grande o pequeño.
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .aspectRatio(1.1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
                .border(1.dp, Paleta.Cian.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (existeGlb && rutaGlb != null) {
                Visor3D(rutaGlb = rutaGlb, modifier = Modifier.fillMaxSize())
            } else {
                Text(
                    text = "Aquí va tu modelo 3D",
                    color = Color(0xFF9AA4B8),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun TarjetaConteo(fila: FilaConteo, modifier: Modifier = Modifier) {
    val color = if (fila.esAnion) Paleta.Cian else Rosa
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0E1424))
            .border(1.dp, BordeApagado, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${fila.posicion} (${fila.cantidad}):",
            color = TextoApagado,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "${fila.cantidad} × ${fila.fraccion} = ${fila.aporte}",
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun ResumenIones(totalAniones: Int, totalCationes: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Paleta.Cian.copy(alpha = 0.06f))
            .border(1.dp, Paleta.Cian.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ItemResumenIon(color = Paleta.Cian, texto = "Aniones B⁻:", valor = totalAniones)
        ItemResumenIon(color = Rosa, texto = "Cationes A⁺:", valor = totalCationes)
    }
}

@Composable
private fun ItemResumenIon(color: Color, texto: String, valor: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = texto, color = Color.White, fontSize = 14.sp, maxLines = 1, softWrap = false)
        Text(
            text = "$valor",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

/** Diseño anterior (texto), se usa solo si la estructura no tiene tabla de conteo. */
@Composable
private fun LineasConteoTexto(lineas: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        lineas.forEach { linea ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .border(1.dp, BordeApagado.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(linea, color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}