package me.mariahernandez.cristalia.empaquetamiento

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.mariahernandez.cristalia.R
import me.mariahernandez.cristalia.celdaunitaria.EventoUi
import me.mariahernandez.cristalia.celdaunitaria.Paleta
import me.mariahernandez.cristalia.celdaunitaria.Visor3D
import me.mariahernandez.cristalia.celdaunitaria.guardarGlbEnDescargas

@Composable
fun EmpaquetamientoScreen(
    onVolver: () -> Unit,
    onIrALogin: () -> Unit,
    viewModel: EmpaquetamientoViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paleta.Fondo)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ---------- PARTE FIJA (no cambia al tocar SC/BCC/FCC/HCP) ----------
        BarraSuperior(onVolver = onVolver, onIrALogin = onIrALogin)

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(Paleta.Cian))
                Text("CRISTALOGRAFÍA", color = Paleta.Cian, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
            Text("Factor de Empaquetamiento", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }

        TarjetaDefinicion()

        EtiquetaSeccion("TIPOS DE ESTRUCTURA")
        SelectorTipo(
            seleccionado = state.tipoSeleccionado,
            onSeleccion = viewModel::seleccionarTipo
        )

        // ---------- PARTE QUE CAMBIA (solo esta sección se actualiza) ----------
        AnimatedContent(
            targetState = state.tipoSeleccionado,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "seccionEstructura"
        ) { tipo ->
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TarjetaEstructura(
                    tipo = tipo,
                    onDescargar = { viewModel.onDescargarClick(tipo.archivoGlb) }
                )
                EtiquetaSeccion("CÁLCULOS PASO A PASO")
                PasosCalculo(tipo)
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ======================================================================
// PARTE FIJA
// ======================================================================

@Composable
private fun BarraSuperior(onVolver: () -> Unit, onIrALogin: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onVolver) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Paleta.Tarjeta)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                Icon(Icons.Default.AccountCircle, null, tint = Paleta.TextoSecundario, modifier = Modifier.size(18.dp))
                Text("Iniciar sesión", color = Paleta.TextoSecundario, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun EtiquetaSeccion(texto: String) {
    Text(
        texto,
        color = Paleta.TextoSecundario,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
}

@Composable
private fun TarjetaDefinicion() {
    val texto = buildAnnotatedString {
        append("Indica el ")
        withStyle(SpanStyle(color = Paleta.Cian)) { append("porcentaje del espacio ocupado") }
        append(" por las esferas (átomos) dentro de una celda unitaria.")
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Paleta.Violeta.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .background(Paleta.Tarjeta)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Info, null, tint = Paleta.Cian)
            Text(texto, color = Color.White, fontSize = 14.sp)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.35f))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("FÓRMULA GENERAL:", color = Paleta.Magenta, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Matematica(
                seq(t("F.E = ", Paleta.Cian), fr(seq(t("N · "), sub("V", "esferas")), sub("V", "celda"))),
                tam = 14.sp,
                color = Paleta.Cian
            )
        }
    }
}

@Composable
private fun SelectorTipo(seleccionado: TipoEstructura, onSeleccion: (TipoEstructura) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Paleta.Tarjeta)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        TipoEstructura.entries.forEach { tipo ->
            val activo = tipo == seleccionado
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .then(if (activo) Modifier.border(1.dp, Paleta.Cian, RoundedCornerShape(10.dp)) else Modifier)
                    .background(if (activo) Paleta.Cian.copy(alpha = 0.12f) else Color.Transparent)
                    .clickable { onSeleccion(tipo) }
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(tipo.sigla, color = if (activo) Paleta.Cian else Paleta.TextoSecundario, fontWeight = FontWeight.Bold)
                Text(tipo.subtitulo, color = Paleta.TextoSecundario, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

// ======================================================================
// PARTE QUE CAMBIA: tarjeta con el GLB
// ======================================================================

@Composable
private fun TarjetaEstructura(tipo: TipoEstructura, onDescargar: () -> Unit) {
    val context = LocalContext.current
    val existe = remember(tipo.archivoGlb) { assetExiste(context, tipo.archivoGlb) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Paleta.Cian.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .background(Paleta.Tarjeta)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                tipo.titulo,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            if (existe) {
                IconButton(onClick = onDescargar) {
                    Icon(Icons.Default.Download, contentDescription = "Descargar modelo", tint = Paleta.Cian)
                }
            }
        }
        if (existe) {
            Visor3D(rutaGlb = tipo.archivoGlb, modifier = Modifier.fillMaxWidth().height(240.dp))
        } else {
            // Mientras no subas el .glb, se muestra este aviso en vez de cerrar la app
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Falta el modelo 3D:\napp/src/main/assets/${tipo.archivoGlb}",
                    color = Paleta.TextoSecundario,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun assetExiste(context: Context, ruta: String): Boolean =
    try { context.assets.open(ruta).close(); true } catch (e: Exception) { false }

// ======================================================================
// PARTE QUE CAMBIA: pasos de cálculo
// ======================================================================

@Composable
private fun PasosCalculo(tipo: TipoEstructura) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        PasoDesplegable(1, tipo.pasoEsfera.titulo, tipo.pasoEsfera.subtitulo, tipo.pasoEsfera.badge, Paleta.Cian) {
            Bloques(tipo.pasoEsfera.bloques)
        }
        PasoDesplegable(2, tipo.pasoCelda.titulo, tipo.pasoCelda.subtitulo, tipo.pasoCelda.badge, Paleta.Magenta) {
            Bloques(tipo.pasoCelda.bloques)
        }
        PasoDesplegable(
            3, t("F.E."), "Factor de Empaquetamiento final",
            t("%.1f %%".format(java.util.Locale.US, tipo.fe * 100), Paleta.Cian),
            Paleta.Cian
        ) {
            Bloques(tipo.filasFE)
            ResultadoFinal(tipo)
        }
    }
}

@Composable
private fun PasoDesplegable(
    numero: Int,
    titulo: Expr,
    subtitulo: String,
    badge: Expr,
    color: Color,
    contenido: @Composable ColumnScope.() -> Unit
) {
    var expandido by remember { mutableStateOf(false) }
    val forma = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .border(1.dp, color.copy(alpha = 0.4f), forma)
            .background(Paleta.Tarjeta)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expandido = !expandido }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(numero.toString(), color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Column {
                Matematica(titulo, tam = 15.sp)
                Text(subtitulo, color = Paleta.TextoSecundario, fontSize = 11.sp)
            }
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Matematica(badge, tam = 12.sp, color = color)
            }
            Icon(
                if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = color
            )
        }

        AnimatedVisibility(visible = expandido) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = contenido
            )
        }
    }
}

@Composable
private fun ResultadoFinal(tipo: TipoEstructura) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Resultado grande
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.5.dp, Paleta.Cian, RoundedCornerShape(12.dp))
                .background(Paleta.Cian.copy(alpha = 0.10f))
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("↑ F.E =", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "%.2f".format(java.util.Locale.US, tipo.fe),
                    color = Paleta.Cian, fontSize = 26.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace
                )
                Text(
                    "(${tipo.porcentajeOcupado}%)",
                    color = Paleta.TextoSecundario, fontSize = 12.sp, fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }

        // Banner extra (solo HCP)
        tipo.banner?.let { texto ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, Paleta.Magenta.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .background(Paleta.Magenta.copy(alpha = 0.10f))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(texto, color = Paleta.Magenta, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }

        // Ocupado / vacío
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TarjetaPorcentaje(
                modifier = Modifier.weight(1f),
                titulo = "OCUPADO", valor = "${tipo.porcentajeOcupado}%",
                detalle = "Volumen neto de esferas", color = Paleta.Cian
            )
            TarjetaPorcentaje(
                modifier = Modifier.weight(1f),
                titulo = "ESPACIO VACÍO", valor = "${tipo.porcentajeVacio}%",
                detalle = "Huecos intersticiales", color = Paleta.Magenta
            )
        }
    }
}

@Composable
private fun TarjetaPorcentaje(modifier: Modifier, titulo: String, valor: String, detalle: String, color: Color) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(titulo, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text(valor, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
        Text(detalle, color = Paleta.TextoSecundario, fontSize = 9.sp)
    }
}

// ======================================================================
// Dibujo de los bloques de contenido
// ======================================================================

@Composable
private fun Bloques(bloques: List<Bloque>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        bloques.forEach { RenderBloque(it) }
    }
}

@Composable
private fun RenderBloque(b: Bloque) {
    when (b) {
        is Bloque.Imagenes -> FilaImagenes(b.recursos)
        is Bloque.Fila -> FilaFormula(b)
        is Bloque.Linea -> LineaFormula(b)
        is Bloque.Ecuacion -> EcuacionCentrada(b)
        is Bloque.Encabezado -> Encabezado(b.texto, b.color)
        is Bloque.Caja -> CajaContenido(b)
        is Bloque.Columnas -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
            b.cajas.forEach { caja -> Box(Modifier.weight(1f)) { CajaContenido(caja) } }
        }
        is Bloque.Seccion -> SeccionPlegable(b)
        Bloque.Separador -> Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)))
    }
}

@Composable
private fun Encabezado(texto: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(texto, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CajaContenido(c: Bloque.Caja) {
    val forma = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .border(1.dp, c.color.copy(alpha = 0.35f), forma)
            .background(Color.Black.copy(alpha = 0.30f))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (c.titulo != null) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(c.color))
                    Text(c.titulo, color = if (c.color == Paleta.Magenta) c.color else Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                c.chip?.let {
                    Text(
                        it, color = c.color, fontSize = 10.sp, fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(c.color.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        } else {
            Box(Modifier.size(8.dp).clip(CircleShape).background(c.color))
        }
        c.bloques.forEach { RenderBloque(it) }
    }
}

@Composable
private fun SeccionPlegable(s: Bloque.Seccion) {
    var abierta by remember { mutableStateOf(false) }
    val forma = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .border(1.dp, s.color.copy(alpha = 0.35f), forma)
            .background(Color.Black.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { abierta = !abierta }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(s.color))
            Text(
                s.titulo,
                color = if (s.color == Paleta.Magenta) s.color else Color.White,
                fontWeight = FontWeight.Bold, fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(if (abierta) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = s.color)
        }
        AnimatedVisibility(visible = abierta) {
            Column(Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                s.bloques.forEach { RenderBloque(it) }
            }
        }
    }
}

@Composable
private fun FilaFormula(f: Bloque.Fila) {
    val acento = when (f.estilo) {
        EstiloFila.Normal -> null
        EstiloFila.Cian -> Paleta.Cian
        EstiloFila.Magenta -> Paleta.Magenta
    }
    val forma = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(acento?.copy(alpha = 0.12f) ?: Color.Black.copy(alpha = 0.35f))
            .border(1.dp, (acento ?: Color.White).copy(alpha = if (acento == null) 0.08f else 0.5f), forma)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Matematica(f.etiqueta, tam = 12.sp, color = acento ?: Color.White)
            f.subtitulo?.let { Text(it, color = Paleta.TextoSecundario, fontSize = 9.sp) }
        }
        Spacer(Modifier.width(12.dp))
        // Si la fórmula es muy larga se puede deslizar de lado
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                f.comentario?.let { Text(it, color = Paleta.TextoSecundario, fontSize = 10.sp) }
                Matematica(f.valor, tam = 13.sp, color = acento ?: Color.White)
            }
        }
    }
}

@Composable
private fun LineaFormula(l: Bloque.Linea) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Matematica(l.expr, tam = 12.sp)
        l.comentario?.let { Text(it, color = Paleta.TextoSecundario, fontSize = 10.sp) }
    }
}

@Composable
private fun EcuacionCentrada(e: Bloque.Ecuacion) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        e.titulo?.let {
            Text("● $it", color = e.color, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, e.color.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.35f))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(Modifier.horizontalScroll(rememberScrollState())) { Matematica(e.expr, tam = 13.sp) }
        }
    }
}

// ======================================================================
// Imágenes normales (drawable). Se buscan por NOMBRE: si todavía no las
// subes, se ve un recuadro gris con el nombre que debe tener el archivo.
// ======================================================================

@Composable
private fun FilaImagenes(recursos: List<String>) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        recursos.forEach { nombre ->
            ImagenDrawable(nombre, Modifier.weight(1f).aspectRatio(1f))
        }
    }
}

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
