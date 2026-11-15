package me.mariahernandez.cristalia.huecos.detalle

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download     // requiere material-icons-extended
import androidx.compose.material.icons.filled.Hub          // requiere material-icons-extended
import androidx.compose.material.icons.filled.ViewInAr     // requiere material-icons-extended
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import me.mariahernandez.cristalia.celdaunitaria.EventoUi
import me.mariahernandez.cristalia.celdaunitaria.Paleta
import me.mariahernandez.cristalia.celdaunitaria.Visor3D
import me.mariahernandez.cristalia.celdaunitaria.guardarGlbEnDescargas
import me.mariahernandez.cristalia.densidad.radio.BarraSuperior

// ------------------------------------------------------------------
// Colores de esta pantalla (Cian y Violeta vienen de tu Paleta)
// ------------------------------------------------------------------
private val Fondo = Color(0xFF0A0E17)
private val Tarjeta = Color(0xFF141A2B)
private val Interior = Color(0xFF0E1422)      // fondo de las filas de huecos
private val FondoVisor = Color(0xFF070B14)    // fondo del modelo 3D y de las imágenes
private val BordeApagado = Color(0xFF1E2A3D)
private val TextoApagado = Color(0xFF9AA4B8)
private val LineaGrilla = Color(0xFF111826)
private val Rosa = Color(0xFFF2708A)
private val Verde = Color(0xFF4ADE80)

private fun TipoHueco.color(): Color = when (this) {
    TipoHueco.OCTAEDRICO -> Paleta.Cian
    TipoHueco.TETRAEDRICO -> Rosa
    TipoHueco.CUBICO -> Paleta.Violeta
}

/**
 * Pantalla "Huecos y Coordinación". Es UNA sola pantalla: cambia su contenido según la
 * opción (sc, bcc, fcc, nacl...) que el usuario tocó en el menú de huecos.
 */
@Composable
fun DetalleHuecosScreen(
    opcionId: String,
    onVolver: () -> Unit = {},
    onIrALogin: () -> Unit = {},
    viewModel: DetalleHuecosViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(opcionId) { viewModel.cargar(opcionId) }

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

    val info = estado.info

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
        BarraSuperior(onVolver = onVolver, onIrALogin = onIrALogin)

        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BordeApagado.copy(alpha = 0.6f))
        )

        if (info != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EncabezadoTitulo(info)

                // Tarjeta 1: modelo 3D + filas de huecos
                TarjetaHuecos(
                    info = info,
                    onDescargar = { info.rutaGlb?.let { viewModel.onDescargarClick(it) } }
                )

                // Tarjeta 2: imagen de huecos octaédricos y tetraédricos
                TarjetaImagen(
                    icono = Icons.Filled.ViewInAr,
                    color = Paleta.Cian,
                    titulo = info.tituloTarjeta,
                    imagen = info.imagenHuecos
                )

                // Tarjeta 3: imagen del número de coordinación
                TarjetaImagen(
                    icono = Icons.Filled.Hub,
                    color = Rosa,
                    titulo = "Número de Coordinación",
                    badge = info.textoNc,
                    imagen = info.imagenNc
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ------------------------------------------------------------------
// Título + recuadro "NC = 8 : 4"
// ------------------------------------------------------------------
@Composable
private fun EncabezadoTitulo(info: InfoHuecos) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Huecos y Coordinación",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Interior)
                .border(1.dp, BordeApagado, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = info.textoNc,
                color = TextoApagado,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

// ------------------------------------------------------------------
// Tarjeta 1: encabezado, modelo GLB (zona naranja de tu captura) y filas de huecos
// ------------------------------------------------------------------
@Composable
private fun TarjetaHuecos(info: InfoHuecos, onDescargar: () -> Unit) {
    ContenedorTarjeta {
        EncabezadoTarjeta(
            icono = Icons.Filled.ViewInAr,
            color = Paleta.Cian,
            titulo = info.tituloTarjeta
        )
        VisorGlb(info, onDescargar)
        info.huecos.forEach { hueco -> FilaHueco(hueco) }
    }
}

@Composable
private fun VisorGlb(info: InfoHuecos, onDescargar: () -> Unit) {
    val context = LocalContext.current
    val ruta = info.rutaGlb
    // Solo muestra el visor si el archivo existe en assets (evita cerrar la app si falta)
    val existeGlb = remember(ruta) {
        ruta != null && try { context.assets.open(ruta).close(); true } catch (e: Exception) { false }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FondoVisor)
            .border(1.dp, BordeApagado, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = info.etiquetaGlb,
            color = Verde,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.25f),
            contentAlignment = Alignment.Center
        ) {
            if (existeGlb && ruta != null) {
                Visor3D(rutaGlb = ruta, modifier = Modifier.fillMaxSize())
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
                    text = "Aquí va tu modelo 3D",
                    color = TextoApagado,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
            }
        }

        Text(
            text = info.leyendaGlb,
            color = TextoApagado,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FilaHueco(hueco: HuecoInfo) {
    val color = hueco.tipo.color()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Interior)
            .border(1.dp, BordeApagado, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    text = hueco.tipo.nombre,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Insignia(texto = "Total = ${hueco.total}", color = color)
        }

        // Las dos barritas de la captura: dónde están y quién los ocupa
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BarritaInfo(hueco.ubicacion, Modifier.weight(1f).fillMaxHeight())
            BarritaInfo(hueco.ocupacion, Modifier.weight(1f).fillMaxHeight())
        }
    }
}

@Composable
private fun BarritaInfo(texto: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, BordeApagado, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = texto,
            color = TextoApagado,
            fontSize = 10.sp,
            lineHeight = 13.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ------------------------------------------------------------------
// Tarjetas inferiores: encabezado (+ insignia) y una imagen drawable
// ------------------------------------------------------------------
@Composable
private fun TarjetaImagen(
    icono: ImageVector,
    color: Color,
    titulo: String,
    imagen: String,
    badge: String? = null
) {
    ContenedorTarjeta {
        EncabezadoTarjeta(icono = icono, color = color, titulo = titulo, badge = badge)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(FondoVisor)
                .border(1.dp, BordeApagado, RoundedCornerShape(12.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            ImagenDrawable(
                nombre = imagen,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f)
            )
        }
    }
}

// ------------------------------------------------------------------
// Piezas compartidas
// ------------------------------------------------------------------
@Composable
private fun ContenedorTarjeta(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Tarjeta)
            .border(1.dp, BordeApagado, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        content()
    }
}

/** Ícono en recuadro + título (+ insignia opcional a la derecha). */
@Composable
private fun EncabezadoTarjeta(
    icono: ImageVector,
    color: Color,
    titulo: String,
    badge: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.12f))
                .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        }

        Text(
            text = titulo,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        if (badge != null) Insignia(texto = badge, color = color)
    }
}

/** Recuadro pequeño tipo "Total = 4" / "NC = 6". */
@Composable
private fun Insignia(texto: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = texto,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            softWrap = false
        )
    }
}

/** Busca el drawable por nombre en res/drawable; si falta, muestra un recuadro gris con el nombre. */
@Composable
private fun ImagenDrawable(nombre: String, modifier: Modifier) {
    val context = LocalContext.current
    val id = remember(nombre) { context.resources.getIdentifier(nombre, "drawable", context.packageName) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (id != 0) Color.Transparent else Color(0xFFCCCCCC)),
        contentAlignment = Alignment.Center
    ) {
        if (id != 0) {
            Image(
                painter = painterResource(id = id),
                contentDescription = nombre,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = nombre,
                color = Color.DarkGray,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
        }
    }
}
