package me.mariahernandez.cristalia.empaquetamiento

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.mariahernandez.cristalia.celdaunitaria.Paleta

/**
 * Mini lenguaje para escribir fórmulas matemáticas sin imágenes.
 * Se arma como un árbol: una fórmula es una lista de piezas (texto, fracción, raíz, etc.)
 * y [Matematica] sabe dibujar cada pieza.
 */
sealed interface Expr {
    data class Txt(val s: String, val color: Color? = null) : Expr          // texto normal: "x = "
    data class Sub(val base: Expr, val sub: String) : Expr                   // subíndice: V_esfera
    data class Sup(val base: Expr, val sup: String) : Expr                   // exponente: r³
    data class Frac(val num: Expr, val den: Expr) : Expr                     // fracción
    data class Raiz(val dentro: Expr) : Expr                                 // raíz cuadrada
    data class Seq(val items: List<Expr>) : Expr                             // varias piezas en fila
    data class Tachado(val dentro: Expr) : Expr                              // pieza con raya diagonal (se cancela)
}

// ------------------------------------------------------------------
// Funciones cortas para escribir fórmulas en Modelos.kt
// ------------------------------------------------------------------
fun t(s: String, color: Color? = null): Expr = Expr.Txt(s, color)
fun sub(base: String, s: String, color: Color? = null): Expr = Expr.Sub(Expr.Txt(base, color), s)
fun sup(base: String, s: String, color: Color? = null): Expr = Expr.Sup(Expr.Txt(base, color), s)
fun sup(base: Expr, s: String): Expr = Expr.Sup(base, s)
fun fr(num: Expr, den: Expr): Expr = Expr.Frac(num, den)
fun fr(num: String, den: String): Expr = Expr.Frac(Expr.Txt(num), Expr.Txt(den))
fun raiz(dentro: Expr): Expr = Expr.Raiz(dentro)
fun raiz(dentro: String): Expr = Expr.Raiz(Expr.Txt(dentro))
fun seq(vararg items: Expr): Expr = Expr.Seq(items.toList())
fun tach(dentro: Expr): Expr = Expr.Tachado(dentro)

// ------------------------------------------------------------------
// Dibujo de la fórmula
// ------------------------------------------------------------------
@Composable
fun Matematica(e: Expr, tam: TextUnit = 14.sp, color: Color = Color.White) {
    when (e) {
        is Expr.Txt -> Text(
            text = e.s,
            color = e.color ?: color,
            fontSize = tam,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            softWrap = false
        )

        is Expr.Sub -> {
            val c = (e.base as? Expr.Txt)?.color ?: color
            Row(verticalAlignment = Alignment.Bottom) {
                Matematica(e.base, tam, color)
                Text(
                    text = e.sub,
                    color = c,
                    fontSize = tam * 0.62f,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    softWrap = false,
                    modifier = Modifier.offset(y = 2.dp)
                )
            }
        }

        is Expr.Sup -> {
            val c = (e.base as? Expr.Txt)?.color ?: color
            Row(verticalAlignment = Alignment.Top) {
                Matematica(e.base, tam, color)
                Text(
                    text = e.sup,
                    color = c,
                    fontSize = tam * 0.62f,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    softWrap = false,
                    modifier = Modifier.offset(y = (-1).dp)
                )
            }
        }

        is Expr.Frac -> Column(
            modifier = Modifier.width(IntrinsicSize.Max),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.padding(horizontal = 2.dp)) { Matematica(e.num, tam, color) }
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp)
                    .height(1.5.dp)
                    .background(color.copy(alpha = 0.9f))
            )
            Box(Modifier.padding(horizontal = 2.dp)) { Matematica(e.den, tam, color) }
        }

        is Expr.Raiz -> Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "√",
                color = color,
                fontSize = tam,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                softWrap = false
            )
            // La rayita de arriba de la raíz se dibuja sobre el contenido
            Box(
                Modifier
                    .drawBehind {
                        drawLine(
                            color = color,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 2f
                        )
                    }
                    .padding(top = 2.dp, start = 1.dp)
            ) { Matematica(e.dentro, tam, color) }
        }

        is Expr.Seq -> Row(verticalAlignment = Alignment.CenterVertically) {
            e.items.forEach { Matematica(it, tam, color) }
        }

        is Expr.Tachado -> Box(
            Modifier.drawWithContent {
                drawContent()
                drawLine(
                    color = Paleta.Cian,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, 0f),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
            }
        ) { Matematica(e.dentro, tam, color) }
    }
}
