package me.mariahernandez.cristalia.celdaunitaria

import androidx.compose.ui.graphics.Color

object Paleta {
    val Fondo = Color(0xFF0B0E17)
    val Tarjeta = Color(0xFF121726)
    val Cian = Color(0xFF22E5EE)
    val Magenta = Color(0xFFE91E8C)
    val Violeta = Color(0xFFC9A0FF)
    val TextoSecundario = Color(0xFF9AA3B5)

    // NUEVOS
    val Rosado = Color(0xFFFF0EB3)     // el rosado que enviaste
    val GrisIcono = Color(0xFF9AA3B5)  // gris para el ícono
}

/** Una fila de la sección "Conteo de átomos". */
data class ItemConteo(
    val descripcion: String,
    val badge: String,
    val color: Color
)

/** Un término de la fórmula: cantidad × (numerador/denominador). Ej: 8 × 1/8 */
data class TerminoFormula(
    val cantidad: Int,
    val numerador: Int,
    val denominador: Int
)

/** Cada tipo de celda con TODO lo que cambia en la sección inferior. */
enum class TipoCelda(
    val sigla: String,
    val subtitulo: String,
    val titulo: String,
    val archivoGlb: String,          // ruta dentro de app/src/main/assets/
    val archivoGlbConteo: String,     // glb de conteo
    val items: List<ItemConteo>,
    val resumen: String,
    val terminos: List<TerminoFormula>,
    val total: Int
) {
    SC(
        sigla = "SC", subtitulo = "Cúbica Simple",
        titulo = "SC — Cúbica Simple (Simple Cubic)",
        archivoGlb = "models/celdaunitaria/SC_centrada_fix.glb",         // Ruta celda
        archivoGlbConteo = "models/celdaunitaria/1atom_centrado.glb",   // Ruta conteo
        items = listOf(
            ItemConteo("1 esfera hace parte de 8 estructuras cristalinas", "1/8 por celda", Paleta.Cian)
        ),
        resumen = "SC: 8 vértices (1/8 cada uno)",
        // SC:  8 × 1/8 = 1
        terminos = listOf(TerminoFormula(8, 1, 8)),
        total = 1
    ),
    BCC(
        sigla = "BCC", subtitulo = "Cent. Cuerpo",
        titulo = "BCC — Cúbica Centrada en el Cuerpo (Body-Centered)",
        archivoGlb = "models/celdaunitaria/bcc_centrado.glb",
        archivoGlbConteo = "models/celdaunitaria/2atoms_centrado.glb",   // <-- NUEVO
        items = listOf(
            ItemConteo("1 esfera hace parte de 8 estructuras cristalinas", "1/8 por celda", Paleta.Cian),
            ItemConteo("1 esfera en el centro pertenece solo a esta celda", "1 por celda", Paleta.Magenta)
        ),
        resumen = "BCC: 8 vértices (1/8 cada uno) + 1 átomo central",
        // BCC: 8 × 1/8 + 1 × 1 = 2
        terminos = listOf(TerminoFormula(8, 1, 8), TerminoFormula(1, 1, 1)),
        total = 2
    ),
    FCC(
        sigla = "FCC", subtitulo = "Cent. Caras",
        titulo = "FCC — Cúbica Centrada en las Caras (Face-Centered)",
        archivoGlb = "models/celdaunitaria/FCC.glb",
        archivoGlbConteo = "models/celdaunitaria/4atoms_centrado.glb",   // <-- NUEVO
        items = listOf(
            ItemConteo("1 esfera hace parte de 8 estructuras cristalinas", "1/8 por celda", Paleta.Cian),
            ItemConteo("6 esferas hacen parte de 2 estructuras cristalinas", "1/2 por celda", Paleta.Magenta)
        ),
        resumen = "FCC: 8 vértices (1/8) + 6 caras (1/2 cada una)",
        // FCC: 8 × 1/8 + 6 × 1/2 = 4
        terminos = listOf(TerminoFormula(8, 1, 8), TerminoFormula(6, 1, 2)),
        total = 4
    ),
    HCP(
        sigla = "HCP", subtitulo = "Hexagonal",
        titulo = "HCP — Hexagonal Compacta (Hexagonal Close-Packed)",
        archivoGlb = "models/celdaunitaria/hcp.glb",
        archivoGlbConteo = "models/celdaunitaria/6atoms_centrado.glb",   // <-- NUEVO
        items = listOf(
            ItemConteo("12 esferas en los vértices del prisma hexagonal", "1/6 por celda", Paleta.Cian),
            ItemConteo("2 esferas en el centro de las bases hexagonales", "1/2 por celda", Paleta.Magenta),
            ItemConteo("3 esferas dentro del prisma", "1 por celda", Paleta.Violeta)
        ),
        resumen = "HCP: 12 vértices (1/6) + 2 bases (1/2) + 3 internas (1)",
        // HCP: 12 × 1/6 + 2 × 1/2 + 3 × 1 = 6
        terminos = listOf(TerminoFormula(12, 1, 6), TerminoFormula(2, 1, 2), TerminoFormula(3, 1, 1)),
        total = 6
    );
}

/** Archivos GLB de la parte superior (estructura -> celda unitaria). */
const val GLB_INTRO = "models/celdaunitaria/celda_centrado.glb"

