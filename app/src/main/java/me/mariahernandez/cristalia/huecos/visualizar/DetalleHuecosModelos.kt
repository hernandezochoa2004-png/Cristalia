package me.mariahernandez.cristalia.huecos.detalle

/**
 * Tipos de hueco que puede mostrar la pantalla.
 * [nc] es el número de coordinación del hueco (cuántos átomos lo rodean).
 */
enum class TipoHueco(val nombre: String, val nombreSingular: String, val nc: Int) {
    OCTAEDRICO("Huecos Octaédricos", "Hueco Octaédrico", 6),
    TETRAEDRICO("Huecos Tetraédricos", "Hueco Tetraédrico", 4),
    CUBICO("Huecos Cúbicos", "Hueco Cúbico", 8)
}

/**
 * Un tipo de hueco dentro de una estructura.
 *
 * @param total      huecos de ese tipo por celda ("Total = 4")
 * @param ubicacion  dónde están (primera barrita bajo la fila)
 * @param ocupacion  quién los ocupa o si están vacíos (segunda barrita bajo la fila)
 */
data class HuecoInfo(
    val tipo: TipoHueco,
    val total: Int,
    val ubicacion: String,
    val ocupacion: String
)

/**
 * Todo lo que cambia en la pantalla según la estructura elegida en el menú.
 *
 * @param nc         número de coordinación principal que se muestra en el recuadro ("NC=6")
 * @param nc2        segundo número de coordinación opcional para estructuras AB₂ ("NC = 8 : 4")
 * @param etiquetaGlb texto verde sobre el modelo 3D ("OCTAEDROS")
 * @param leyendaGlb  texto bajo el modelo 3D ("Simetría octaédrica")
 * @param rutaGlb    ruta dentro de app/src/main/assets/.
 * @param huecos     una fila por tipo de hueco (debajo del modelo 3D).
 * @param imagenHuecos drawable de la 1.ª tarjeta inferior: huecos octaédricos y tetraédricos.
 * @param imagenNc   drawable de la 2.ª tarjeta inferior: número de coordinación 1.
 * @param imagenNc2  drawable opcional para el número de coordinación 2 (AB₂).
 */
data class InfoHuecos(
    val id: String,
    val nc: Int,
    val nc2: Int? = null,
    val etiquetaGlb: String,
    val leyendaGlb: String,
    val rutaGlb: String?,
    val huecos: List<HuecoInfo>,
    val imagenHuecos: String = "huecos_$id",
    val imagenNc: String = "nc_$id",
    val imagenNc2: String? = null
) {
    /** Título de la primera tarjeta. */
    val tituloTarjeta: String
        get() = if (id == "sc") "Hueco Cúbico"
        else "Huecos Octaédricos y Tetraédricos"

    /** Texto para la insignia del encabezado ("NC = 8 : 4" o "NC = 6"). */
    val textoNc: String
        get() = if (nc2 != null) "NC = $nc : $nc2" else "NC = $nc"

    /** Hueco al que corresponde el NC del recuadro (6 = octaédrico, 4 = tetraédrico, 8 = cúbico). */
    val tipoNc: TipoHueco
        get() = when (nc) {
            4 -> TipoHueco.TETRAEDRICO
            6 -> TipoHueco.OCTAEDRICO
            else -> TipoHueco.CUBICO
        }
}

// Atajos para no repetir el tipo en cada línea
private fun oct(total: Int, ubicacion: String, ocupacion: String) =
    HuecoInfo(TipoHueco.OCTAEDRICO, total, ubicacion, ocupacion)

private fun tet(total: Int, ubicacion: String, ocupacion: String) =
    HuecoInfo(TipoHueco.TETRAEDRICO, total, ubicacion, ocupacion)

private fun cub(total: Int, ubicacion: String, ocupacion: String) =
    HuecoInfo(TipoHueco.CUBICO, total, ubicacion, ocupacion)

private const val UBIC_OCT_FCC = "Centro (1) + aristas (12 × ¼ = 3)"
private const val UBIC_TET_FCC = "8 dentro de la celda"
private const val VACIOS = "Vacíos"

/**
 * Catálogo: un registro por cada opción del menú de huecos (mismos ids que HuecosViewModel).
 */
fun infoHuecos(id: String): InfoHuecos? = when (id) {

    // ---------------- Estructuras A (monoatómicas) ----------------
    "sc" -> InfoHuecos(
        id = "sc", nc = 6,
        etiquetaGlb = "SC", leyendaGlb = "Sistema cúbico",
        rutaGlb = "models/huecos/scgordo_centrado.glb",
        huecos = listOf(cub(1, "Centro de la celda", VACIOS)),
        imagenHuecos = "huecocubicosc",
        imagenNc = "ncsc"
    )
    "bcc" -> InfoHuecos(
        id = "bcc", nc = 8,
        etiquetaGlb = "BCC", leyendaGlb = "Cúbica centrada en el cuerpo",
        rutaGlb = "models/huecos/bccgordo_centrado.glb",
        huecos = listOf(
            oct(6, "Caras (6 × ½) + aristas (12 × ¼)", VACIOS),
            tet(12, "4 por cada cara (24 × ½)", VACIOS)
        ),
        imagenHuecos = "huecosbcctotal",
        imagenNc = "ncbcc"
    )
    "fcc" -> InfoHuecos(
        id = "fcc", nc = 12,
        etiquetaGlb = "FCC", leyendaGlb = "Cúbica centrada en las caras",
        rutaGlb = "models/huecos/huecosfcc_centrado.glb",
        huecos = listOf(
            oct(4, UBIC_OCT_FCC, VACIOS),
            tet(8, UBIC_TET_FCC, VACIOS)
        ),
        imagenHuecos = "totalhuecos",
        imagenNc = "ncfcc"
    )
    "hcp" -> InfoHuecos(
        id = "hcp", nc = 12,
        etiquetaGlb = "HCP", leyendaGlb = "Hexagonal compacta",
        rutaGlb = "models/huecos/hcpgordo.glb",
        huecos = listOf(
            oct(6, "Entre capas A y B", VACIOS),
            tet(12, "Entre capas A y B", VACIOS)
        ),
        imagenHuecos = "hcphuecos",
        imagenNc = "nchcp"
    )

    // ---------------- Estructuras AB (1:1) ----------------
    "nacl" -> InfoHuecos(
        id = "nacl", nc = 6,
        etiquetaGlb = "NaCl", leyendaGlb = "FCC",
        rutaGlb = "models/huecos/naclgordo_centrado.glb",
        huecos = listOf(
            oct(4, UBIC_OCT_FCC, "Ocupados por Na⁺ (100 %)"),
            tet(8, UBIC_TET_FCC, VACIOS)
        ),
        imagenHuecos = "huecosnacl",
        imagenNc = "ncsc"
    )
    "blenda" -> InfoHuecos(
        id = "blenda", nc = 4,
        etiquetaGlb = "Blenda", leyendaGlb = "También conocida como esfalerita",
        rutaGlb = "models/huecos/blendagordo_centrado.glb",
        huecos = listOf(
            oct(4, UBIC_OCT_FCC, VACIOS),
            tet(8, UBIC_TET_FCC, "Zn²⁺ en 4 de 8 (50 %)")
        ),
        imagenHuecos = "blendahuecos",
        imagenNc = "ncblenda"
    )
    "wurtzita" -> InfoHuecos(
        id = "wurtzita", nc = 4,
        etiquetaGlb = "Wurtzita", leyendaGlb = "HCP",
        rutaGlb = "models/huecos/wurtzitagordo_centrado.glb",
        huecos = listOf(
            oct(6, "Entre capas A y B", VACIOS),
            tet(12, "Entre capas A y B", "Zn²⁺ en 6 de 12 (50 %)")
        ),
        imagenHuecos = "wurtzitahuecos",
        imagenNc = "ncwurtzita"
    )
    "cscl" -> InfoHuecos(
        id = "cscl", nc = 8,
        etiquetaGlb = "CsCl", leyendaGlb = "Cloruro de cesio",
        rutaGlb = "models/huecos/cscl_centrado.glb",
        huecos = listOf(
            oct(6, "Caras (6 × ½) + aristas (12 × ¼)", "Ocupado por Cs⁺ (100 %)"),
            tet(12, "4 por cada cara (24 × ½)", VACIOS)
        ),
        imagenHuecos = "huecosbcctotal",
        imagenNc = "ncbcc"
    )

    // ---------------- Estructuras AB₂ (1:2) ----------------
    "fluorita" -> InfoHuecos(
        id = "fluorita", nc = 8, nc2 = 4,
        etiquetaGlb = "Fluorita", leyendaGlb = "También conocida como fluorina",
        rutaGlb = "models/huecos/fluoritagordo_centrado.glb",
        huecos = listOf(
            oct(4, UBIC_OCT_FCC, VACIOS),
            tet(8, UBIC_TET_FCC, "Ocupados por F⁻ (100 %)")
        ),
        imagenHuecos = "huecosfluorita",
        imagenNc = "ncfluorita"
    )
    "antifluorita" -> InfoHuecos(
        id = "antifluorita", nc = 4, nc2 = 8,
        etiquetaGlb = "Antifluorita", leyendaGlb = "Invertida respecto a fluorita",
        rutaGlb = "models/huecos/antifluoritagordo_centrado.glb",
        huecos = listOf(
            oct(4, UBIC_OCT_FCC, VACIOS),
            tet(8, UBIC_TET_FCC, "Ocupados por Li⁺ (100 %)")
        ),
        imagenHuecos = "huecosantifluorita",
        imagenNc = "ncantifluorita"
    )
    "cdi2" -> InfoHuecos(
        id = "cdi2", nc = 6, nc2 = 3,
        etiquetaGlb = "Ypduro de cadmio", leyendaGlb = "HCP",
        rutaGlb = "models/densidad/yodurocadmio_centrado.glb",
        huecos = listOf(
            oct(1/2, "Entre capas de I⁻", "Cd²⁺ en 3 de 6 (50 %)"),
            tet(0, "Entre capas de I⁻", VACIOS)
        ),
        imagenHuecos = "huecosyodurocadmio",
        imagenNc = "ncyodurocadmio"
    )
    "rutilo" -> InfoHuecos(
        id = "rutilo", nc = 6, nc2 = 3,
        etiquetaGlb = "Rutilo", leyendaGlb = "Proviene de rutilus",
        rutaGlb = "models/densidad/rutilo_centrado.glb",
        huecos = listOf(
            oct(4, "Entre cadenas de O²⁻", "Ti⁴⁺ en 2 de 4 (50 %)"),
            tet(8, "Dentro de la celda", VACIOS)
        ),
        imagenHuecos = "huecosrutilo",
        imagenNc = "ncrutilo"
    )

    else -> null
}
