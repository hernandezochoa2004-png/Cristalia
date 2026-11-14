package me.mariahernandez.cristalia.densidad.radio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.cristalia.model.EstructuraCristalina
import com.cristalia.model.RelacionRadioParametro
import me.mariahernandez.cristalia.celdaunitaria.Paleta

/** Una fila de la tabla de conteo de iones (Vértices, Caras, Aristas, Centro, Huecos...). */
data class FilaConteo(
    val posicion: String,   // "Vértices", "Caras", "Aristas", "Centro", "Huecos tetraédricos"
    val cantidad: Int,      // cuántas posiciones hay en la celda: 8, 6, 12, 1...
    val fraccion: String,   // fracción de ion que aporta cada una: "⅛", "½", "¼", "1"
    val aporte: Int,        // iones que aporta en total (cantidad × fracción)
    val esAnion: Boolean    // true = anión (cian), false = catión (rosa)
)

data class DatosCalculoRadio(
    val estructura: EstructuraCristalina,
    val masaMolar: Double,
    val radioCatPm: Double,
    val radioAnPm: Double,
    val aPm: Double,
    val radioCatCmStr: String,
    val radioAnCmStr: String,
    val volumenCm3: Double,
    val volumenCm3Str: String,
    val densidadGcm3: Double,
    val densidadGcm3Str: String,
    val formulaAnionCationStr: String,
    val zStr: String,
    val paso1Lineas: List<String>,
    val conteoFilas: List<FilaConteo> = emptyList(),
    val totalAniones: Int = 0,
    val totalCationes: Int = 0
)

object FuncionesCalculoRadio {

    const val NUMERO_AVOGADRO = 6.02214076e23

    private const val SUPERINDICES = "⁰¹²³⁴⁵⁶⁷⁸⁹"

    private fun superindice(n: Int): String =
        n.toString().map { if (it == '-') '⁻' else SUPERINDICES[it - '0'] }.joinToString("")

    /** 5.83e-24 -> "5.83 × 10⁻²⁴" (con superíndices reales y punto decimal fijo). */
    fun formatoCientifico(valor: Double, decimales: Int = 2): String {
        if (valor == 0.0) return "0"
        var exp = Math.floor(Math.log10(Math.abs(valor))).toInt()
        var mant = valor / Math.pow(10.0, exp.toDouble())
        var texto = "%.${decimales}f".format(java.util.Locale.US, mant)
        if (texto.trimStart('-').startsWith("10")) {   // el redondeo dio 10.00
            exp += 1
            mant = valor / Math.pow(10.0, exp.toDouble())
            texto = "%.${decimales}f".format(java.util.Locale.US, mant)
        }
        return "$texto × 10${superindice(exp)}"
    }

    fun calcularTodo(
        estructura: EstructuraCristalina,
        masaMolar: Double,
        radioCatPm: Double,
        radioAnPm: Double
    ): DatosCalculoRadio {
        val aPm = estructura.calcularParametroA(radioCatPm, radioAnPm) ?: (2.0 * (radioCatPm + radioAnPm))
        val volumenCm3 = estructura.calcularVolumenCelda(aPm)
        val densidad = (estructura.z * masaMolar) / (NUMERO_AVOGADRO * volumenCm3)

        val radioCatCm = radioCatPm * 1e-10
        val radioAnCm = radioAnPm * 1e-10

        val (formulaAnionCation, zText) = when (estructura.id) {
            "nacl" -> Pair("A₄B₄  ➔  4 AB", "z = 4 UF")
            "blenda" -> Pair("Zn₄S₄  ➔  4 ZnS", "z = 4 UF")
            "cscl" -> Pair("Cs₁Cl₁  ➔  1 CsCl", "z = 1 UF")
            "fluorita" -> Pair("Ca₄F₈  ➔  4 CaF₂", "z = 4 UF")
            "antifluorita" -> Pair("Li₈O₄  ➔  4 Li₂O", "z = 4 UF")
            else -> Pair("${estructura.nombre}  ➔  ${estructura.z} UF", "z = ${estructura.z} UF")
        }

        val p1Lineas = when (estructura.id) {
            "nacl" -> listOf(
                "Aniones (Cl⁻) en red FCC: 8 esquinas × 1/8 + 6 caras × 1/2 = 4 aniones Cl⁻",
                "Cationes (Na⁺) en huecos octaédricos: 12 aristas × 1/4 + 1 centro = 4 cationes Na⁺",
                "Total: 4 aniones Cl⁻ y 4 cationes Na⁺ por celda unitaria"
            )
            "blenda" -> listOf(
                "Aniones (S²⁻) en red FCC: 8 esquinas × 1/8 + 6 caras × 1/2 = 4 aniones S²⁻",
                "Cationes (Zn²⁺) en 1/2 huecos tetraédricos = 4 cationes Zn²⁺",
                "Total: 4 aniones S²⁻ y 4 cationes Zn²⁺ por celda unitaria"
            )
            "cscl" -> listOf(
                "Aniones (Cl⁻) en esquinas Cúbica Simple: 8 × 1/8 = 1 anión Cl⁻",
                "Cationes (Cs⁺) en el centro de la celda = 1 catión Cs⁺",
                "Total: 1 anión Cl⁻ y 1 catión Cs⁺ por celda unitaria"
            )
            "fluorita" -> listOf(
                "Cationes (Ca²⁺) en red FCC: 8 esquinas × 1/8 + 6 caras × 1/2 = 4 cationes Ca²⁺",
                "Aniones (F⁻) ocupan todos los 8 huecos tetraédricos = 8 aniones F⁻",
                "Total: 4 cationes Ca²⁺ y 8 aniones F⁻ por celda unitaria"
            )
            "antifluorita" -> listOf(
                "Aniones (O²⁻) en red FCC: 8 esquinas × 1/8 + 6 caras × 1/2 = 4 aniones O²⁻",
                "Cationes (Li⁺) ocupan todos los 8 huecos tetraédricos = 8 cationes Li⁺",
                "Total: 4 aniones O²⁻ y 8 cationes Li⁺ por celda unitaria"
            )
            else -> listOf(
                "Conteo para ${estructura.nombre}: Z = ${estructura.z} unidades fórmula por celda"
            )
        }

        // Tabla de conteo para el paso 1 (esAnion = true -> cian, false -> rosa)
        val conteoFilas = when (estructura.id) {
            "nacl" -> listOf(
                FilaConteo("Vértices", 8, "⅛", 1, true),
                FilaConteo("Caras", 6, "½", 3, true),
                FilaConteo("Aristas", 12, "¼", 3, false),
                FilaConteo("Centro", 1, "1", 1, false)
            )
            "blenda" -> listOf(
                FilaConteo("Vértices", 8, "⅛", 1, true),
                FilaConteo("Caras", 6, "½", 3, true),
                FilaConteo("½ huecos tetraédricos", 4, "1", 4, false)
            )
            "cscl" -> listOf(
                FilaConteo("Vértices", 8, "⅛", 1, true),
                FilaConteo("Centro", 1, "1", 1, false)
            )
            "fluorita" -> listOf(
                FilaConteo("Vértices", 8, "⅛", 1, false),
                FilaConteo("Caras", 6, "½", 3, false),
                FilaConteo("Huecos tetraédricos", 8, "1", 8, true)
            )
            "antifluorita" -> listOf(
                FilaConteo("Vértices", 8, "⅛", 1, true),
                FilaConteo("Caras", 6, "½", 3, true),
                FilaConteo("Huecos tetraédricos", 8, "1", 8, false)
            )
            else -> emptyList()
        }
        val totalAniones = conteoFilas.filter { it.esAnion }.sumOf { it.aporte }
        val totalCationes = conteoFilas.filter { !it.esAnion }.sumOf { it.aporte }

        val catFormat = formatoCientifico(radioCatCm)
        val anFormat = formatoCientifico(radioAnCm)
        val volFormat = formatoCientifico(volumenCm3)

        return DatosCalculoRadio(
            estructura = estructura,
            masaMolar = masaMolar,
            radioCatPm = radioCatPm,
            radioAnPm = radioAnPm,
            aPm = aPm,
            radioCatCmStr = catFormat,
            radioAnCmStr = anFormat,
            volumenCm3 = volumenCm3,
            volumenCm3Str = volFormat,
            densidadGcm3 = densidad,
            densidadGcm3Str = "%.2f".format(densidad),
            formulaAnionCationStr = formulaAnionCation,
            zStr = zText,
            paso1Lineas = p1Lineas,
            conteoFilas = conteoFilas,
            totalAniones = totalAniones,
            totalCationes = totalCationes
        )
    }
}

// =====================================================================================
//  UI DEL PASO 3 (VOLUMEN DE LA CELDA) — SOLO PARA NaCl
// =====================================================================================
//  OJO: este composable está escrito a mano para NaCl (celda FCC, a = 2(r+ + r-), V = a³).
//  Las fórmulas, el texto de "a" y el desarrollo "V = [ 2 (rA⁺ + rB⁻) ]³" NO sirven para
//  las otras 8 estructuras (Blenda, Wurtzita, CsCl, Fluorita, Antifluorita, CdI₂, Rutilo...),
//  porque cada una tiene su propia relación radio–parámetro de red y su propio volumen
//  (cúbico, hexagonal o tetragonal).
//
//  TODO: crear una versión por estructura (ContenidoVolumenBlenda, ContenidoVolumenCsCl...)
//  y elegir cuál mostrar según datos.estructura.id, por ejemplo:
//
//      when (datos.estructura.id) {
//          "nacl" -> ContenidoVolumenCelda(datos)   // <- esta
//          // "blenda" -> ContenidoVolumenBlenda(datos)
//          // "cscl"   -> ContenidoVolumenCsCl(datos)
//          else     -> { /* pendiente */ }
//      }
// =====================================================================================

private val FondoSubTarjeta = Color(0xFF111827)
private val BordeSubTarjeta = Color(0xFF1E2A3D)
private val FondoCaja = Color.Black.copy(alpha = 0.35f)
private val TextoApagado = Color(0xFF9AA4B8)
private val ColorCation = Paleta.Cian
private val ColorAnion = Color(0xFFFF8A8A)

// ---------- Helpers de texto ----------

/** Escribe "r" con el subíndice "A⁺" / "B⁻" (rA⁺, rB⁻). */
private fun AnnotatedString.Builder.radio(letra: String, carga: String) {
    append("r")
    withStyle(SpanStyle(baselineShift = BaselineShift.Subscript, fontSize = 0.75.em)) {
        append(letra + carga)
    }
}

/** 102.0 -> "102", 30.5 -> "30.5" */
private fun numeroLimpio(v: Double): String =
    if (v == Math.floor(v)) "%.0f".format(java.util.Locale.US, v)
    else "%.2f".format(java.util.Locale.US, v).trimEnd('0').trimEnd('.')

// ---------- Contenedores ----------

@Composable
private fun SubTarjeta(
    titulo: String? = null,
    bordeColor: Color = BordeSubTarjeta,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FondoSubTarjeta)
            .border(1.dp, bordeColor, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (titulo != null) {
            Text(
                text = titulo,
                color = Paleta.TextoSecundario,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        content()
    }
}

@Composable
private fun Caja(
    bordeColor: Color = BordeSubTarjeta,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(FondoCaja)
            .border(1.dp, bordeColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) { content() }
}

/** Fracción de unidades:  1 cm / 10¹⁰ pm  con "pm" tachado. */
@Composable
private fun FraccionConversion() {
    Column(
        modifier = Modifier.width(IntrinsicSize.Max),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("1 cm", color = TextoApagado, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(TextoApagado)
        )
        Text(
            text = buildAnnotatedString {
                append("10¹⁰ ")
                withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) { append("pm") }
            },
            color = TextoApagado,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun FilaRadio(
    letra: String,
    carga: String,
    valorPm: Double,
    resultadoCm: String,
    colorResultado: Color
) {
    Caja(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    radio(letra, carga)
                    append(" = ${numeroLimpio(valorPm)} ")
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough, color = TextoApagado)) {
                        append("pm")
                    }
                    append(" ×")
                },
                color = Color(0xFFD5DAE5),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            FraccionConversion()
            Text(
                text = "= $resultadoCm cm",
                color = colorResultado,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ---------- Contenido del paso 3 (SOLO NaCl) ----------

/** Paso 3 para NaCl únicamente: V = [ 2 (rA⁺ + rB⁻) ]³. No usar con otras estructuras. */
@Composable
fun ContenidoVolumenCelda(datos: DatosCalculoRadio) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // 1) FÓRMULAS BASE
        SubTarjeta("FÓRMULAS BASE") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Caja(modifier = Modifier.weight(1f)) {
                    Text(
                        "V = a³", color = Color.White, fontSize = 13.sp,
                        fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace
                    )
                }
                Caja(modifier = Modifier.weight(1f)) {
                    Text(
                        text = buildAnnotatedString {
                            append("a = 2 "); radio("A", "⁺")
                            append(" + 2 "); radio("B", "⁻")
                        },
                        color = Paleta.Cian, fontSize = 12.sp,
                        fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace
                    )
                }
            }

            Box(Modifier.fillMaxWidth().height(1.dp).background(BordeSubTarjeta))

            Text(
                text = buildAnnotatedString {
                    append("V = (2 "); radio("A", "⁺")
                    append(" + 2 "); radio("B", "⁻"); append(")³")
                },
                color = Color(0xFFD5DAE5),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                "Fórmula del volumen factorizada:",
                color = TextoApagado, fontSize = 11.sp, fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
            )

            Caja(
                bordeColor = Paleta.Cian.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("V = [ 2 ("); radio("A", "⁺")
                        append(" + "); radio("B", "⁻"); append(") ]³")
                    },
                    color = Paleta.Cian, fontSize = 16.sp,
                    fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace
                )
            }
        }

        // 2) RADIOS IÓNICOS Y CONVERSIÓN
        SubTarjeta("RADIOS IÓNICOS Y CONVERSIÓN") {
            FilaRadio("A", "⁺", datos.radioCatPm, datos.radioCatCmStr, ColorCation)
            FilaRadio("B", "⁻", datos.radioAnPm, datos.radioAnCmStr, ColorAnion)
        }

        // 3) CÁLCULO DEL VOLUMEN
        SubTarjeta("CÁLCULO DEL VOLUMEN") {
            Caja(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "V = [ 2 (${datos.radioCatCmStr} + ${datos.radioAnCmStr}) ]³",
                    color = Color(0xFFD5DAE5),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Paleta.Cian.copy(alpha = 0.10f))
                    .border(1.dp, Paleta.Cian.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Volumen total:", color = Color.White, fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "V = ${datos.volumenCm3Str} cm³",
                    color = Paleta.Cian, fontSize = 14.sp,
                    fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// =====================================================================================
//  UI DEL PASO 4 (DENSIDAD TEÓRICA) — VÁLIDO PARA TODAS LAS ESTRUCTURAS
// =====================================================================================
//  A diferencia del paso 3, este NO depende de la estructura: usa ρ = Z·M / (Nₐ·V) con
//  los valores que ya vienen calculados en DatosCalculoRadio (z, masa molar, volumen).
//  Se puede reutilizar tal cual para las 8 estructuras.
// =====================================================================================

private val ColorVolumen = Color(0xFFFF8A8A)
private val TextoClaro = Color(0xFFD5DAE5)

/** Fracción apilada genérica: numerador / línea / denominador. */
@Composable
private fun Fraccion(
    numerador: AnnotatedString,
    denominador: AnnotatedString,
    color: Color = TextoClaro,
    fontSize: TextUnit = 11.sp
) {
    Column(
        modifier = Modifier.width(IntrinsicSize.Max),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(numerador, color = color, fontSize = fontSize, fontFamily = FontFamily.Monospace)
        Box(Modifier.fillMaxWidth().height(1.dp).background(TextoApagado))
        Text(denominador, color = color, fontSize = fontSize, fontFamily = FontFamily.Monospace)
    }
}

/** "V" con subíndice "celda". */
private fun AnnotatedString.Builder.vCelda() {
    append("V")
    withStyle(SpanStyle(baselineShift = BaselineShift.Subscript, fontSize = 0.75.em)) { append("celda") }
}

@Composable
private fun FilaDato(
    etiqueta: AnnotatedString,
    valor: String,
    colorValor: Color = Color.White
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(etiqueta, color = TextoApagado, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        Text(
            text = valor,
            color = colorValor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SeparadorFino() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(BordeSubTarjeta.copy(alpha = 0.7f)))
}

/** Paso 4: densidad teórica. Sirve para cualquier estructura. */
@Composable
fun ContenidoDensidad(datos: DatosCalculoRadio) {
    val masaTexto = "%.2f".format(java.util.Locale.US, datos.masaMolar)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // 1) ρ = m / V_celda   [ g / cm³ ]
        SubTarjeta(bordeColor = Paleta.Cian.copy(alpha = 0.3f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("ρ", color = Paleta.Cian, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text("=", color = TextoClaro, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                    Fraccion(
                        numerador = AnnotatedString("m"),
                        denominador = buildAnnotatedString { vCelda() },
                        fontSize = 13.sp
                    )
                }
                Caja {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("[", color = TextoApagado, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        Fraccion(AnnotatedString("g"), AnnotatedString("cm³"), color = TextoApagado, fontSize = 11.sp)
                        Text("]", color = TextoApagado, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // 2) Datos
        SubTarjeta {
            FilaDato(AnnotatedString("Masa molar:"), "$masaTexto g/mol")
            SeparadorFino()
            FilaDato(AnnotatedString("z ="), "${datos.estructura.z} UF", Paleta.Cian)
            SeparadorFino()
            FilaDato(AnnotatedString("Avogadro ="), "6.022 × 10²³ mol⁻¹")
            SeparadorFino()
            FilaDato(
                etiqueta = buildAnnotatedString { vCelda(); append(" =") },
                valor = "${datos.volumenCm3Str} cm³",
                colorValor = ColorVolumen
            )
        }

        // 3) Metiéndola en la fórmula
        SubTarjeta("METIÉNDOLA EN LA FORMULA:") {
            Caja(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("ρ =", color = Paleta.Cian, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("${datos.estructura.z} UF ×", color = TextoClaro, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Fraccion(AnnotatedString("$masaTexto g"), AnnotatedString("mol"), fontSize = 10.sp)
                            Text("×", color = TextoApagado, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Fraccion(AnnotatedString("1 mol"), AnnotatedString("6.022 × 10²³ UF"), fontSize = 10.sp)
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(TextoApagado))
                        Text(
                            text = "${datos.volumenCm3Str} cm³",
                            color = ColorVolumen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // 4) Resultado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Paleta.Cian.copy(alpha = 0.10f))
                .border(1.dp, Paleta.Cian.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Resultado:", color = Color.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Paleta.Cian, fontSize = 18.sp, fontWeight = FontWeight.Bold)) { append("ρ") }
                    withStyle(SpanStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)) { append(" = ") }
                    withStyle(SpanStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)) { append(datos.densidadGcm3Str) }
                    withStyle(SpanStyle(color = Paleta.Cian, fontSize = 12.sp)) { append(" g/cm³") }
                },
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
