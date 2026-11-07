package me.mariahernandez.cristalia.huecos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ViewInAr // requiere material-icons-extended
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
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
import me.mariahernandez.cristalia.celdaunitaria.Paleta

// ------------------------------------------------------------------
// Colores de esta pantalla (Cian y Magenta vienen de tu Paleta)
// ------------------------------------------------------------------
private val Fondo = Color(0xFF0A0E17)
private val Tarjeta = Color(0xFF141A2B)
private val BordeApagado = Color(0xFF1E2A3D)
private val BotonFondo = Color(0xFF1C2335)
private val TextoApagado = Color(0xFF9AA4B8)
private val LineaGrilla = Color(0xFF111826)

/**
 * Pantalla "Huecos octaédricos y tetraédricos".
 *
 * @param onOpcionClick se llama al tocar una opción (SC, NaCl, Rutilo...). La pantalla NO navega:
 *                      el NavHost decide a qué destino ir con (grupoId, opcionId).
 */
@Composable
fun HuecosScreen(
    onVolver: () -> Unit,
    onIrALogin: () -> Unit = {},
    onOpcionClick: (grupoId: String, opcionId: String) -> Unit = { _, _ -> },
    viewModel: HuecosViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

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
        BarraSuperior(onVolver = onVolver, onIniciarSesion = onIrALogin)
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BordeApagado.copy(alpha = 0.6f))
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { TituloPantalla("Huecos octaédricos y tetraédricos") }
            items(estado.grupos, key = { it.id }) { grupo ->
                TarjetaGrupo(
                    grupo = grupo,
                    expandido = grupo.id in estado.expandidos,
                    onClick = { viewModel.alternar(grupo.id) },
                    onOpcionClick = { opcion -> onOpcionClick(grupo.id, opcion.id) }
                )
            }
        }
    }
}

// ------------------------------------------------------------------
// Barra superior: botón atrás + chips "Cristalia" / "Iniciar sesión"
// ------------------------------------------------------------------
@Composable
private fun BarraSuperior(onVolver: () -> Unit, onIniciarSesion: () -> Unit) {
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
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChipSuperior(
                texto = "Cristalia",
                icono = Icons.Filled.ViewInAr,
                colorIcono = Paleta.Magenta,
                colorTexto = Color.White,
                fondo = BotonFondo,
                onClick = {}
            )
            ChipSuperior(
                texto = "Iniciar sesión",
                icono = Icons.Filled.Person,
                colorIcono = TextoApagado,
                colorTexto = TextoApagado,
                fondo = BotonFondo,
                onClick = onIniciarSesion
            )
        }
    }
}

@Composable
private fun ChipSuperior(
    texto: String,
    icono: ImageVector,
    colorIcono: Color,
    colorTexto: Color,
    fondo: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(fondo)
            .border(1.dp, BordeApagado, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(14.dp))
        Text(texto, color = colorTexto, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

// ------------------------------------------------------------------
// Título con punto cian y la barra vacía que aparece debajo
// ------------------------------------------------------------------
@Composable
private fun TituloPantalla(texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Paleta.Cian)
        )
        Spacer(Modifier.width(12.dp))
        Text(texto, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 30.sp)
    }
}

// ------------------------------------------------------------------
// Tarjeta desplegable: título + badge + chevron, y debajo las opciones
// ------------------------------------------------------------------
@Composable
private fun TarjetaGrupo(
    grupo: GrupoHuecos,
    expandido: Boolean,
    onClick: () -> Unit,
    onOpcionClick: (OpcionHuecos) -> Unit
) {
    val forma = RoundedCornerShape(16.dp)
    val borde by animateColorAsState(
        if (expandido) Paleta.Cian.copy(alpha = 0.55f) else BordeApagado,
        label = "borde"
    )
    val giro by animateFloatAsState(if (expandido) 180f else 0f, label = "giro")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (expandido) 14.dp else 0.dp,
                shape = forma,
                ambientColor = Paleta.Cian,
                spotColor = Paleta.Cian
            )
            .clip(forma)
            .background(Tarjeta)
            .border(BorderStroke(1.dp, borde), forma)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = grupo.titulo,
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                maxLines = 1
            )
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (expandido) Paleta.Cian.copy(alpha = 0.12f) else BotonFondo)
                    .border(
                        1.dp,
                        if (expandido) Paleta.Cian.copy(alpha = 0.6f) else BordeApagado,
                        RoundedCornerShape(50)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = grupo.badge,
                    color = Paleta.Cian,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false
                )
            }
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BotonFondo),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expandido) "Plegar" else "Desplegar",
                    tint = if (expandido) Paleta.Cian else TextoApagado,
                    modifier = Modifier.rotate(giro)
                )
            }
        }

        AnimatedVisibility(visible = expandido) {
            Column {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BordeApagado.copy(alpha = 0.7f))
                )
                CuadriculaOpciones(
                    opciones = grupo.opciones,
                    onOpcionClick = onOpcionClick,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

// ------------------------------------------------------------------
// Opciones de cada grupo: cuadrícula de 2 columnas
// ------------------------------------------------------------------
@Composable
private fun CuadriculaOpciones(
    opciones: List<OpcionHuecos>,
    onOpcionClick: (OpcionHuecos) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        opciones.chunked(2).forEach { fila ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                fila.forEach { opcion ->
                    TarjetaOpcion(opcion, { onOpcionClick(opcion) }, Modifier.weight(1f))
                }
                // si la fila queda con un solo elemento, se mantiene el ancho de media columna
                if (fila.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TarjetaOpcion(opcion: OpcionHuecos, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val forma = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier
            .clip(forma)
            .background(Fondo.copy(alpha = 0.55f))
            .border(1.dp, BordeApagado, forma)
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ImagenDrawable(opcion.imagen, Modifier.fillMaxWidth().aspectRatio(1f))
        Spacer(Modifier.height(10.dp))
        Text(
            text = opcion.titulo,
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = opcion.subtitulo,
            color = TextoApagado,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            maxLines = 2
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
            .background(if (id != 0) Color.Black else Color(0xFFCCCCCC)),
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
            Text(nombre, color = Color.DarkGray, fontSize = 9.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
        }
    }
}
