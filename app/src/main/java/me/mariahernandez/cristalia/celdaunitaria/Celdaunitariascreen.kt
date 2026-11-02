package me.mariahernandez.cristalia.celdaunitaria

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download   // requiere material-icons-extended
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

//Nuevo
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

//Nuevo
import androidx.compose.ui.res.painterResource
import me.mariahernandez.cristalia.R

// Nuevo
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.TextUnit

@Composable
fun CeldaUnitariaScreen(
    onVolver: () -> Unit,                                            // parametro 1, lambda acción
    onIrALogin: () -> Unit,                                          // parametro 2, lambda accion
    viewModel: CeldaUnitariaViewModel = viewModel()                  //parametro 3, objeto de tipo CeldaUnitariaViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()     // Al cambiar de tipo (SC -> FCC) avisa a compose para recomponer la pantalla
    val context = LocalContext.current                               // Da el contexto de la pantalla, que se utilizará más abajo

    // Eventos de una sola vez: ir al login o descargar
    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->             // Obtiene los eventos del objeto Celdaunitariaviewmodel (login, descargar)
            when (evento) {
                EventoUi.IrALogin -> onIrALogin()     // Si el evento es login, ejecuta onIRALogin que es parametro1
                is EventoUi.Descargar -> {            // Si el evento es descargar, haz esto:
                    val ok = guardarGlbEnDescargas(context, evento.archivoGlb)  // Devuelve un boolean
                    Toast.makeText(
                        context,
                        if (ok) "Guardado en Descargas" else "Error al guardar",    // Si ok es true, se descargó
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
            .statusBarsPadding()                              // Deja espacio hacia arriba para que no quede pegado a la hora y bateria
            .verticalScroll(rememberScrollState())    // Permite deslizar hacia abajo el contenido mas largo
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)     // Deja 16 dp de cada elemento de la columna
    ) {
        // ---------- PARTE FIJA (no cambia al tocar SC/BCC/FCC/HCP) ----------
        BarraSuperior(onVolver = onVolver, onIrALogin = onIrALogin)

        Text("Celda Unitaria", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)

        TarjetaVisor(
            titulo = null,
            archivoGlb = GLB_INTRO,
            alturaVisor = 220.dp,
            onDescargar = { viewModel.onDescargarClick(GLB_INTRO) }
        )

        TarjetaInfo(
            icono = true,
            texto = "Estructura cristalina: Forma ordenada en que se acomodan los átomos, iones o moléculas dentro de un sólido, siguiendo un patrón que se repite en el espacio.",
            negrita = "Estructura cristalina"
        )
        TarjetaInfo(
            icono = false,
            texto = "Celda unitaria: Porción más pequeña de una estructura cristalina.",
            negrita = "Celda unitaria"
        )

        Text("Tipo", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        SelectorTipo(
            seleccionado = state.tipoSeleccionado,
            onSeleccion = viewModel::seleccionarTipo
        )

        // ---------- PARTE QUE CAMBIA (solo esta sección se actualiza) ----------
        AnimatedContent(
            targetState = state.tipoSeleccionado,                          // Detecta el cambio de un tipo a otro
            transitionSpec = { fadeIn() togetherWith fadeOut() },          // Contenido viejo desaparece al mismo tiempo que aparece el nuevo
            label = "seccionTipo"
        ) { tipo ->
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TarjetaVisor(
                    titulo = tipo.titulo,
                    archivoGlb = tipo.archivoGlb,
                    alturaVisor = 260.dp,
                    onDescargar = { viewModel.onDescargarClick(tipo.archivoGlb) }
                )
                SeccionConteo(
                    tipo = tipo,
                    onDescargarConteo = { viewModel.onDescargarClick(tipo.archivoGlbConteo) }
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// Barra superior
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
            // Logo + texto cristalia
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
                Icon(Icons.Default.AccountCircle, null, tint = Paleta.TextoSecundario, modifier = Modifier.size(18.dp))
                Text("Iniciar sesión", color = Paleta.TextoSecundario, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun TarjetaVisor(
    titulo: String?,
    archivoGlb: String,
    alturaVisor: androidx.compose.ui.unit.Dp,
    onDescargar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Paleta.Tarjeta)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                titulo ?: "",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDescargar) {
                Icon(Icons.Default.Download, contentDescription = "Descargar modelo", tint = Paleta.Cian)
            }
        }
        Visor3D(
            rutaGlb = archivoGlb,
            modifier = Modifier
                .fillMaxWidth()
                .height(alturaVisor)
        )
    }
}

// Nuevo
@Composable
private fun TarjetaInfo(
    icono: Boolean,
    texto: String,
    negrita: String? = null      // <-- NUEVO: la parte inicial que va en negrita
) {
    val contenido = buildAnnotatedString {
        if (negrita != null && texto.startsWith(negrita)) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(negrita)
            }
            append(texto.removePrefix(negrita))
        } else {
            append(texto)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(BorderStroke(1.dp, Paleta.Violeta.copy(alpha = 0.4f)), RoundedCornerShape(12.dp))
            .background(Paleta.Tarjeta)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Default.CheckCircle, null,
            tint = if (icono) Paleta.GrisIcono else Paleta.Rosado
        )
        Text(contenido, color = Color.White, fontSize = 14.sp)   // <-- ahora recibe "contenido"
    }
}

@Composable
private fun SelectorTipo(seleccionado: TipoCelda, onSeleccion: (TipoCelda) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Paleta.Tarjeta)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        TipoCelda.entries.forEach { tipo ->
            val activo = tipo == seleccionado
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .then(
                        if (activo) Modifier.border(1.dp, Paleta.Cian, RoundedCornerShape(10.dp)) else Modifier
                    )
                    .background(if (activo) Paleta.Cian.copy(alpha = 0.12f) else Color.Transparent)
                    .clickable { onSeleccion(tipo) }
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    tipo.sigla,
                    color = if (activo) Paleta.Cian else Paleta.TextoSecundario,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    tipo.subtitulo,
                    color = Paleta.TextoSecundario,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun SeccionConteo(
    tipo: TipoCelda,
    onDescargarConteo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Paleta.Tarjeta)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Conteo de átomos", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        tipo.items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, item.color.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(Modifier.size(20.dp).clip(RoundedCornerShape(50)).background(item.color))
                Text(item.descripcion, color = Color.White, modifier = Modifier.weight(1f), fontSize = 14.sp)
                Text(
                    item.badge,
                    color = item.color,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(item.color.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // GLB de la zona amarilla + botón de descarga
        TarjetaVisor(
            titulo = null,
            archivoGlb = tipo.archivoGlbConteo,
            alturaVisor = 160.dp,
            onDescargar = onDescargarConteo
        )

        TarjetaInfo(icono = false, texto = tipo.resumen)

        // Fórmula dibujada (fracción, tachado y resultado)
        FormulaVisual(tipo)
    }
}

@Composable
private fun FormulaVisual(tipo: TipoCelda) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Paleta.Cian.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text("entonces:", color = Paleta.TextoSecundario, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.35f))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            tipo.terminos.forEachIndexed { i, termino ->
                if (i > 0) SimboloFormula("+")
                TerminoVisual(termino)
            }
            SimboloFormula("=")
            ResultadoAtomos(tipo.total)
        }
    }
}

@Composable
private fun TerminoVisual(t: TerminoFormula) {
    // Se tachan los números que se cancelan: 8 × 1/8, 2 × 1/2...
    val cancelado = t.denominador != 1 && t.cantidad == t.denominador

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NumeroFormula(t.cantidad, tachado = cancelado)
        SimboloFormula("×")
        if (t.denominador == 1) {
            NumeroFormula(t.numerador, tachado = false)
        } else {
            Fraccion(t.numerador, t.denominador, denominadorTachado = cancelado)
        }
    }
}

@Composable
private fun Fraccion(numerador: Int, denominador: Int, denominadorTachado: Boolean) {
    Column(
        modifier = Modifier.width(IntrinsicSize.Max),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NumeroFormula(numerador, tachado = false, tamano = 18.sp)
        Box(
            Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color.White)
        )
        NumeroFormula(denominador, tachado = denominadorTachado, tamano = 18.sp)
    }
}

@Composable
private fun NumeroFormula(valor: Int, tachado: Boolean, tamano: TextUnit = 22.sp) {
    Text(
        text = valor.toString(),
        color = Color.White,
        fontSize = tamano,
        fontWeight = FontWeight.Bold,
        modifier = if (tachado) {
            Modifier.drawWithContent {
                drawContent()
                // Raya diagonal cian sobre el número
                drawLine(
                    color = Paleta.Cian,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, 0f),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
            }
        } else Modifier
    )
}

@Composable
private fun SimboloFormula(simbolo: String) {
    Text(simbolo, color = Paleta.TextoSecundario, fontSize = 20.sp)
}

@Composable
private fun ResultadoAtomos(total: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Paleta.Cian.copy(alpha = 0.12f))
            .border(1.dp, Paleta.Cian.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(total.toString(), color = Paleta.Cian, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(if (total == 1) "átomo" else "átomos", color = Color.White, fontSize = 18.sp)
    }
}