package me.mariahernandez.cristalia.empaquetamiento

import androidx.compose.ui.graphics.Color
import me.mariahernandez.cristalia.celdaunitaria.Paleta
import kotlin.math.PI
import kotlin.math.sqrt

// ------------------------------------------------------------------
// Bloques con los que se arma el contenido de cada paso
// ------------------------------------------------------------------

enum class EstiloFila { Normal, Cian, Magenta }

sealed interface Bloque {
    /** Una o dos imágenes normales (drawable) en fila. Se buscan por nombre en res/drawable. */
    data class Imagenes(val recursos: List<String>) : Bloque

    /** Fila: etiqueta a la izquierda, fórmula a la derecha. */
    data class Fila(
        val etiqueta: Expr,
        val valor: Expr,
        val subtitulo: String? = null,      // texto pequeño bajo la etiqueta
        val comentario: String? = null,     // texto gris al lado del valor: "(cancela r³)"
        val estilo: EstiloFila = EstiloFila.Normal
    ) : Bloque

    /** Línea de fórmula suelta, alineada a la izquierda. */
    data class Linea(val expr: Expr, val comentario: String? = null) : Bloque

    /** Fórmula centrada dentro de un recuadro, con título opcional ("ECUACIÓN A"). */
    data class Ecuacion(val titulo: String?, val expr: Expr, val color: Color) : Bloque

    /** Punto de color + texto en negrita. */
    data class Encabezado(val texto: String, val color: Color) : Bloque

    /** Tarjeta con punto de color + título + bloques adentro. */
    data class Caja(
        val titulo: String?,
        val color: Color,
        val bloques: List<Bloque>,
        val chip: String? = null            // etiqueta a la derecha del título: "x = arista"
    ) : Bloque

    /** Varias tarjetas lado a lado. */
    data class Columnas(val cajas: List<Caja>) : Bloque

    /** Sección que se puede plegar/desplegar (Ab, C — Altura de la celda). */
    data class Seccion(val titulo: String, val color: Color, val bloques: List<Bloque>) : Bloque

    data object Separador : Bloque
}

/** Un paso desplegable: V esfera / V celda. (El paso 3, F.E., se arma aparte porque trae el resultado.) */
data class Paso(
    val titulo: Expr,
    val subtitulo: String,
    val badge: Expr,
    val bloques: List<Bloque>
)

// ------------------------------------------------------------------
// Atajos para no repetir
// ------------------------------------------------------------------
private val Cian = Paleta.Cian
private val Magenta = Paleta.Magenta

private val r3 = sup("r", "3")
private val volEsfera = seq(fr("4", "3"), t(" π "), r3)                // 4/3 π r³
private val r3Tachado = tach(sup("r", "3", Magenta))                   // r³ que se cancela

private val etqVesfera = seq(sub("V", "esfera"), t(" ="))
private val etqVesferas = seq(sub("V", "esferas"), t(" ="))
private val etqVcelda = seq(sub("V", "celda"), t(" ="))
private val etqFE = t("F.E =")

private fun glb(nombre: String) = "models/empaquetamiento/$nombre.glb"

private fun atomos(n: Int, detalle: String) = Bloque.Fila(
    etiqueta = t("Número de átomos (N)"),
    valor = t("N = $n ${if (n == 1) "átomo" else "átomos"}", Cian),
    subtitulo = detalle
)

private val feFilaGeneral = Bloque.Fila(
    etiqueta = etqFE,
    valor = fr(sub("V", "esferas"), sub("V", "celda"))
)

// ------------------------------------------------------------------
// Cada tipo de estructura con TODO lo que cambia en la pantalla
// ------------------------------------------------------------------
enum class TipoEstructura(
    val sigla: String,
    val subtitulo: String,
    val titulo: String,
    val archivoGlb: String,          // ruta dentro de app/src/main/assets/
    val pasoEsfera: Paso,
    val pasoCelda: Paso,
    val filasFE: List<Bloque>,       // filas del paso 3
    val fe: Double,                  // factor de empaquetamiento real (0.0 - 1.0)
    val banner: String? = null       // aviso extra al final (solo HCP)
) {
    SC(
        sigla = "SC", subtitulo = "Cúb. Simple",
        titulo = "SC — Cúbica Simple (Simple Cubic)",
        archivoGlb = glb("sc"),
        pasoEsfera = Paso(
            titulo = sub("V", "esfera"),
            subtitulo = "Volumen neto ocupado por las esferas",
            badge = volEsfera,
            bloques = listOf(
                Bloque.Fila(t("Número de átomos"), t("= 1", Cian)),
                Bloque.Fila(seq(sub("V", "esfera"), t(" =")), volEsfera),
                Bloque.Fila(etqVesferas, seq(t("1 × "), volEsfera), estilo = EstiloFila.Cian)
            )
        ),
        pasoCelda = Paso(
            titulo = sub("V", "celda"),
            subtitulo = "Volumen total de la celda unitaria cúbica",
            badge = seq(sup("a", "3"), t(" = 8 "), r3),
            bloques = listOf(
                Bloque.Fila(seq(sub("V", "celda"), t(" = "), sup("x", "3")), t("x = 2r", Magenta)),
                Bloque.Fila(etqVcelda, sup(t("(2r)"), "3")),
                Bloque.Fila(etqVcelda, seq(t("8 "), r3), estilo = EstiloFila.Magenta)
            )
        ),
        filasFE = listOf(
            feFilaGeneral,
            Bloque.Fila(
                etqFE,
                fr(seq(fr("4", "3"), t(" π "), r3Tachado), seq(t("8 "), r3Tachado)),
                comentario = "(cancela r³)"
            ),
            Bloque.Fila(etqFE, fr(t("4 π"), t("3 × 8")), comentario = "= π / 6")
        ),
        fe = PI / 6
    ),

    BCC(
        sigla = "BCC", subtitulo = "Cent. Cuerpo",
        titulo = "BCC — Cúbica Centrada en el Cuerpo (Body-Centered Cubic)",
        archivoGlb = glb("bcc"),
        pasoEsfera = Paso(
            titulo = sub("V", "esfera"),
            subtitulo = "Volumen neto ocupado por los átomos",
            badge = seq(fr("8", "3"), t(" π "), r3),
            bloques = listOf(
                atomos(2, "8 vértices × 1/8 + 1 centro"),
                Bloque.Fila(etqVesfera, volEsfera),
                Bloque.Fila(
                    etqVesferas,
                    seq(t("2 × "), volEsfera, t(" = "), fr("8", "3"), t(" π "), r3),
                    estilo = EstiloFila.Cian
                )
            )
        ),
        pasoCelda = Paso(
            titulo = sub("V", "celda"),
            subtitulo = "Volumen total de la celda unitaria",
            badge = seq(sup("x", "3"), t(" = "), fr(t("64"), seq(t("3"), raiz("3"))), t(" "), r3),
            bloques = listOf(
                Bloque.Imagenes(listOf("emp_bcc_celda", "emp_bcc_diagonal")),
                Bloque.Columnas(
                    listOf(
                        Bloque.Caja(
                            titulo = null, color = Cian,
                            bloques = listOf(
                                Bloque.Linea(seq(sup("l", "2"), t(" = "), sup("x", "2"), t(" + "), sup("x", "2")))
                            )
                        ),
                        Bloque.Caja(
                            titulo = null, color = Magenta,
                            bloques = listOf(
                                Bloque.Linea(seq(sup("d", "2"), t(" = "), sup("l", "2"), t(" + "), sup("x", "2"))),
                                Bloque.Linea(seq(sup("d", "2"), t(" = "), sup("x", "2"), t(" + "), sup("x", "2"), t(" + "), sup("x", "2"))),
                                Bloque.Linea(seq(raiz(sup("d", "2")), t(" = "), raiz(seq(t("3"), sup("x", "2"))))),
                                Bloque.Linea(seq(t("d = "), raiz("3"), t(" x")))
                            )
                        )
                    )
                ),
                Bloque.Caja(
                    titulo = null, color = Cian,
                    bloques = listOf(
                        Bloque.Fila(t("Y d = 4r entonces:", Magenta), seq(t("4r = "), raiz("3"), t(" x")), estilo = EstiloFila.Normal),
                        Bloque.Fila(t("Arista:"), seq(t("x = "), fr(t("4r"), raiz("3")))),
                        Bloque.Fila(
                            t("Vcelda:"),
                            seq(sup("x", "3"), t(" = "), sup(seq(t("("), fr(t("4"), raiz("3")), t(" r)")), "3"))
                        ),
                        Bloque.Fila(
                            t("Vcelda:", Magenta),
                            seq(sup(seq(t("("), fr(t("4"), raiz("3")), t(")")), "3"), t(" "), r3),
                            estilo = EstiloFila.Magenta
                        )
                    )
                )
            )
        ),
        filasFE = listOf(
            feFilaGeneral,
            Bloque.Fila(
                etqFE,
                fr(
                    seq(fr("8", "3"), t(" π "), r3Tachado),
                    seq(sup(seq(t("("), fr(t("4"), raiz("3")), t(")")), "3"), t(" "), r3Tachado)
                ),
                comentario = "(cancela r³)"
            ),
            Bloque.Fila(etqFE, fr(seq(raiz("3"), t(" · π")), t("8")), comentario = "≈ 0.68017")
        ),
        fe = sqrt(3.0) * PI / 8
    ),

    FCC(
        sigla = "FCC", subtitulo = "Cent. Caras",
        titulo = "FCC — Cúbica Centrada en las Caras (Face-Centered Cubic)",
        archivoGlb = glb("fcc"),
        pasoEsfera = Paso(
            titulo = sub("V", "esfera"),
            subtitulo = "Volumen neto ocupado por los átomos",
            badge = seq(fr("16", "3"), t(" π "), r3),
            bloques = listOf(
                atomos(4, "8 vértices × 1/8 + 6 caras × 1/2"),
                Bloque.Fila(etqVesfera, volEsfera),
                Bloque.Fila(
                    etqVesferas,
                    seq(t("4 × "), volEsfera, t(" = "), fr("16", "3"), t(" π "), r3),
                    estilo = EstiloFila.Cian
                )
            )
        ),
        pasoCelda = Paso(
            titulo = sub("V", "celda"),
            subtitulo = "Volumen total de la celda unitaria",
            badge = seq(sup("x", "3"), t(" = 16"), raiz("2"), t(" "), r3),
            bloques = listOf(
                Bloque.Caja(
                    titulo = "Geometría Celda Unitaria FCC", color = Magenta, chip = "x = arista",
                    bloques = listOf(Bloque.Imagenes(listOf("emp_fcc_geometria")))
                ),
                Bloque.Columnas(
                    listOf(
                        Bloque.Caja(
                            titulo = null, color = Cian,
                            bloques = listOf(
                                Bloque.Linea(seq(sup("d", "2"), t(" = "), sup("x", "2"), t(" + "), sup("x", "2"))),
                                Bloque.Linea(seq(sup("d", "2"), t(" = 2 "), sup("x", "2")))
                            )
                        ),
                        Bloque.Caja(
                            titulo = null, color = Magenta,
                            bloques = listOf(
                                Bloque.Linea(seq(raiz(sup("d", "2")), t(" = "), raiz(seq(t("2 "), sup("x", "2"))))),
                                Bloque.Linea(seq(t("d = "), raiz("2"), t(" x")))
                            )
                        )
                    )
                ),
                Bloque.Caja(
                    titulo = null, color = Cian,
                    bloques = listOf(
                        Bloque.Fila(t("4r = ", Magenta), seq(raiz("2"), t(" x"))),
                        Bloque.Fila(t("Despeje de arista (x):"), seq(t("x = "), fr(t("4r"), raiz("2")))),
                        Bloque.Fila(
                            t("Racionalización:"),
                            seq(t("x = "), fr(t("4r"), raiz("2")), t(" · "), fr(raiz("2"), raiz("2")), t(" = "), fr(seq(t("4"), raiz("2")), t("2")), t(" r"))
                        ),
                        Bloque.Fila(t("Resultado de arista:"), seq(t("x = 2"), raiz("2"), t(" r")), estilo = EstiloFila.Cian),
                        Bloque.Fila(
                            seq(t("Vcelda = "), sup("x", "3"), t(":")),
                            seq(sup("x", "3"), t(" = "), sup(seq(t("(2"), raiz("2"), t(" r)")), "3"))
                        ),
                        Bloque.Fila(
                            t("Vcelda final:", Magenta),
                            seq(t("16"), raiz("2"), t(" "), r3),
                            estilo = EstiloFila.Magenta
                        )
                    )
                )
            )
        ),
        filasFE = listOf(
            feFilaGeneral,
            Bloque.Fila(
                etqFE,
                fr(
                    seq(fr("16", "3"), t(" π "), r3Tachado),
                    seq(t("16"), raiz("2"), t(" "), r3Tachado)
                ),
                comentario = "(cancela 16 r³)"
            ),
            Bloque.Fila(etqFE, fr(t("π"), seq(t("3"), raiz("2"))), comentario = "≈ 0.74048")
        ),
        fe = PI / (3 * sqrt(2.0))
    ),

    HCP(
        sigla = "HCP", subtitulo = "Hexagonal",
        titulo = "HCP — Hexagonal Compacta (Hexagonal Close-Packed)",
        archivoGlb = glb("hcp"),
        pasoEsfera = Paso(
            titulo = sub("V", "esfera"),
            subtitulo = "Volumen neto ocupado por los átomos",
            badge = seq(t("8 π "), r3),
            bloques = listOf(
                atomos(6, "12 vért × 1/6 + 2 caras × 1/2 + 3 × 1"),
                Bloque.Fila(etqVesfera, volEsfera),
                Bloque.Fila(
                    etqVesferas,
                    seq(t("6 × "), volEsfera, t(" = 8 π "), r3),
                    estilo = EstiloFila.Cian
                )
            )
        ),
        pasoCelda = Paso(
            titulo = sub("V", "celda"),
            subtitulo = "Volumen total de la celda unitaria",
            badge = seq(t("3"), raiz("2"), t(" "), sup("a", "3")),
            bloques = listOf(
                // ----- Área de la base (Ab) -----
                Bloque.Seccion(
                    titulo = "Ab", color = Cian,
                    bloques = listOf(
                        Bloque.Imagenes(listOf("emp_hcp_hexagono", "emp_hcp_triangulo")),
                        Bloque.Columnas(
                            listOf(
                                Bloque.Caja(
                                    titulo = null, color = Cian,
                                    bloques = listOf(
                                        Bloque.Linea(seq(sub("A", "t"), t(" = "), fr(t("base × h"), t("2"))), comentario = "(base = a)"),
                                        Bloque.Linea(seq(sub("A", "t"), t(" = "), fr(t("a × h"), t("2"))), comentario = "h = a√3/2"),
                                        Bloque.Linea(
                                            seq(
                                                sub("A", "t"), t(" = "), fr("a", "2"), t(" · "),
                                                fr(seq(t("a"), raiz("3")), t("2")), t(" = "),
                                                fr(seq(sup("a", "2"), raiz("3")), t("4"))
                                            )
                                        ),
                                        Bloque.Separador,
                                        Bloque.Encabezado("Un hexágono tiene 6 triángulos equiláteros.", Magenta),
                                        Bloque.Linea(seq(t("Ab = "), fr(seq(sup("a", "2"), raiz("3")), t("4")), t(" · 6"))),
                                        Bloque.Fila(
                                            t("Ab final:"),
                                            seq(fr("3", "2"), t(" "), sup("a", "2"), raiz("3")),
                                            estilo = EstiloFila.Cian
                                        )
                                    )
                                ),
                                Bloque.Caja(
                                    titulo = "Altura del triángulo (h)", color = Cian,
                                    bloques = listOf(
                                        Bloque.Linea(seq(t("sen(60°) = "), fr("h", "a"))),
                                        Bloque.Linea(t("h = a · sen(60°)")),
                                        Bloque.Linea(seq(t("h = a · "), fr(raiz("3"), t("2"))))
                                    )
                                )
                            )
                        )
                    )
                ),
                // ----- Altura de la celda (c) -----
                Bloque.Seccion(
                    titulo = "C — Altura de la Celda", color = Magenta,
                    bloques = listOf(
                        Bloque.Imagenes(listOf("emp_hcp_altura_1", "emp_hcp_altura_2")),
                        Bloque.Caja(
                            titulo = null, color = Magenta,
                            bloques = listOf(
                                Bloque.Ecuacion(
                                    "ECUACIÓN A",
                                    seq(sup("a", "2"), t(" = "), sup("b", "2"), t(" + "), sup("h", "2"), t(" ; h = "), fr("c", "2")),
                                    Magenta
                                ),
                                Bloque.Ecuacion(
                                    "ECUACIÓN B",
                                    seq(sup("a", "2"), t(" = "), sup("b", "2"), t(" + "), sup(seq(t("("), fr("c", "2"), t(")")), "2"), t(" ; b = ?")),
                                    Magenta
                                )
                            )
                        ),
                        Bloque.Imagenes(listOf("emp_hcp_angulo_60", "emp_hcp_corte_30")),

                        // ----- Despeje de b -----
                        Bloque.Caja(
                            titulo = "Despeje de b", color = Cian,
                            bloques = listOf(
                                Bloque.Fila(t("cos(30°) ="), fr(t("a / 2"), t("b"))),
                                Bloque.Fila(t("b ="), fr(t("a"), t("2 · cos(30°)"))),
                                Bloque.Fila(
                                    t("b ="),
                                    fr(t("a"), seq(t("2 · ("), fr(raiz("3"), t("2")), t(")"))),
                                    comentario = "(cancela 2)"
                                ),
                                Bloque.Fila(t("Resultado b:"), seq(t("b = "), fr(t("a"), raiz("3"))), estilo = EstiloFila.Cian)
                            )
                        ),
                        // ----- Metiendo b en la ecuación B -----
                        Bloque.Caja(
                            titulo = "Metiendo b en Ecuación B", color = Magenta,
                            bloques = listOf(
                                Bloque.Fila(
                                    t("Ecuación B:"),
                                    seq(sup("a", "2"), t(" = "), sup("b", "2"), t(" + "), sup(seq(t("(c / 2)")), "2"))
                                ),
                                Bloque.Fila(
                                    seq(sup("a", "2"), t(" =")),
                                    seq(
                                        sup(seq(t("("), fr(t("a"), raiz("3")), t(")")), "2"), t(" + "),
                                        sup(seq(t("("), fr("c", "2"), t(")")), "2")
                                    )
                                ),
                                Bloque.Fila(
                                    seq(sup("a", "2"), t(" =")),
                                    seq(fr(sup("a", "2"), t("3")), t(" + "), fr(sup("c", "2"), t("4"))),
                                    estilo = EstiloFila.Cian
                                )
                            )
                        ),
                        // ----- Despejando c -----
                        Bloque.Caja(
                            titulo = "Despejando para c", color = Cian,
                            bloques = listOf(
                                Bloque.Fila(
                                    seq(sup("c", "2"), t(" =")),
                                    seq(t("4 · ("), sup("a", "2"), t(" − "), fr(sup("a", "2"), t("3")), t(")"))
                                ),
                                Bloque.Fila(
                                    seq(sup("c", "2"), t(" =")),
                                    seq(t("4 · "), fr(seq(t("2 "), sup("a", "2")), t("3")))
                                ),
                                Bloque.Fila(
                                    t("Altura c final:", Magenta),
                                    seq(t("c = "), fr(seq(t("2"), raiz("2")), raiz("3")), t(" a")),
                                    estilo = EstiloFila.Magenta
                                )
                            )
                        )
                    )
                ),

                // ----- Volumen final -----
                Bloque.Seccion(
                    titulo = "Cálculo de Vcelda (Ab · c)", color = Cian,
                    bloques = listOf(
                        Bloque.Fila(t("Fórmula:"), seq(sub("V", "celda"), t(" = Ab · c"))),
                        Bloque.Fila(
                            t("Sustituyendo Ab y c:"),
                            seq(
                                sub("V", "celda"), t(" = "),
                                fr(seq(t("3"), raiz("3")), t("2")), t(" "), sup("a", "2"), t(" · "),
                                fr(seq(t("2"), raiz("2")), raiz("3")), t(" a")
                            )
                        ),
                        Bloque.Fila(
                            etqVcelda,
                            seq(t("3"), raiz("2"), t(" "), sup("a", "3")),
                            estilo = EstiloFila.Cian
                        ),
                        Bloque.Fila(
                            t("Con a = 2r:"),
                            seq(t("3"), raiz("2"), t(" (2r)³ = 24"), raiz("2"), t(" "), r3)
                        )
                    )
                )
            )
        ),
        filasFE = listOf(
            feFilaGeneral,
            Bloque.Fila(
                etqFE,
                fr(
                    seq(t("8 π "), r3Tachado),
                    seq(t("24"), raiz("2"), t(" "), r3Tachado)
                ),
                comentario = "(cancela r³)"
            ),
            Bloque.Fila(etqFE, fr(t("π"), seq(t("3"), raiz("2"))), comentario = "= 0.74048")
        ),
        fe = PI / (3 * sqrt(2.0)),
        banner = "Empaquetamiento máximo (Compacto)"
    );

    /** Porcentaje del espacio ocupado, redondeado: 52, 68, 74... */
    val porcentajeOcupado: Int get() = Math.round(fe * 100).toInt()
    val porcentajeVacio: Int get() = 100 - porcentajeOcupado
}
