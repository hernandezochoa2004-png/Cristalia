package com.cristalia.model

/**
 * Modelo de soporte para que ViewModel y las pantallas puedan compilar.
 * Ajusta el paquete (com.cristalia.model) al de tu proyecto real.
 *
 * Representa una estructura cristalina del menú "Densidad" (AB / AB2):
 * NaCl, Blenda, Wurtzita, CsCl, Fluorita, Antifluorita, Yoduro de cadmio, Rutilo.
 */

/** Sistema cristalino, determina cómo se calcula el volumen de la celda. */
enum class SistemaCristalino {
    CUBICO,       // V = a^3  (NaCl, Blenda, CsCl, Fluorita, Antifluorita)
    HEXAGONAL,    // V = (√3/2) * a^2 * c  (Wurtzita, CdI2)
    TETRAGONAL    // V = a^2 * c  (Rutilo)
}

/**
 * Cómo se relaciona el parámetro de red "a" con los radios iónicos r+ y r-.
 * Si tu estructura no tiene una fórmula cerrada todavía, usa NINGUNA y
 * deja que el usuario ingrese "a" directamente en el tab correspondiente.
 */
enum class RelacionRadioParametro {
    FCC_ARISTA,        // a = 2 (r+ + r-)                 -> NaCl
    FCC_DIAGONAL,      // a = 4 (r+ + r-) / sqrt(3)        -> Blenda, Fluorita, Antifluorita
    BCC_DIAGONAL,      // a = 2 (r+ + r-) / sqrt(3)        -> CsCl
    NINGUNA            // no hay fórmula simple cargada aún -> Wurtzita, CdI2, Rutilo
}

/** Compuesto de ejemplo que cristaliza en esta estructura (para la sección expandible). */
data class CompuestoEjemplo(
    val formula: String,
    val categoria: String
)

/**
 * Descripción completa de una estructura cristalina.
 *
 * @param z número de fórmulas unitarias por celda (usado en ρ = Z·M / (Na·V))
 * @param requiereDosRadios true si el tab "Radio" debe pedir r+ y r- (AB, AB2);
 *        false si solo aplica un radio (déjalo en true para tus 8 estructuras actuales).
 * @param requiereParametroC true si el sistema no es cúbico y el tab "Parámetro a"
 *        debe pedir también "c" (Wurtzita, CdI2, Rutilo).
 * @param formulaRelacionTexto texto que se muestra como ayuda bajo los campos de radio,
 *        igual al de tu mockup ("Relación r+/r- para celda FCC: parámetro a = 2(r+ + r-)").
 */
data class EstructuraCristalina(
    val id: String,
    val nombre: String,
    val formulaQuimica: String,
    val grupoRelacion: String,            // "AB" o "AB2", tal como en el menú
    val sistema: SistemaCristalino,
    val relacionRadioParametro: RelacionRadioParametro,
    val z: Int,

    // AQUI (1)

    val requiereParametroC: Boolean = false,
    val prismaHexagonal: Boolean = false,   // true = celda = prisma hexagonal completo: V = (3√3/2)·a²·c
    val formulaRelacionTexto: String,
    val compuestosRelacionados: List<CompuestoEjemplo> = emptyList(),
    val modeloGlbAssetPath: String? = null, // modelo .glb de la pantalla "Ingresar datos"; null = aún sin asignar
) {

    /**
     * Calcula el parámetro de red "a" (en pm) a partir de los radios iónicos (en pm).
     * Devuelve null si la relación no está definida todavía (RelacionRadioParametro.NINGUNA),
     * en cuyo caso la UI debe pedir "a" directamente.
     */
    fun calcularParametroA(radioCatPm: Double, radioAnPm: Double): Double? {
        return when (relacionRadioParametro) {
            RelacionRadioParametro.FCC_ARISTA -> 2.0 * (radioCatPm + radioAnPm)
            RelacionRadioParametro.FCC_DIAGONAL -> 4.0 * (radioCatPm + radioAnPm) / Math.sqrt(3.0)
            RelacionRadioParametro.BCC_DIAGONAL -> 2.0 * (radioCatPm + radioAnPm) / Math.sqrt(3.0)
            RelacionRadioParametro.NINGUNA -> null
        }
    }

    /**
     * Calcula el volumen de la celda (en cm^3) a partir del/los parámetro(s) de red.
     * Los parámetros de red "a" (y "c") deben venir en pm; se convierten a cm.
     * 1 pm = 1e-10 cm.
     */
    fun calcularVolumenCelda(aPm: Double, cPm: Double? = null): Double {
        val aCm = aPm * 1e-10
        val cCm = cPm?.times(1e-10)
        return when (sistema) {
            SistemaCristalino.CUBICO -> aCm.pow3()
            SistemaCristalino.HEXAGONAL -> {
                requireNotNull(cCm) { "Esta estructura hexagonal requiere el parámetro c" }
                val factor = if (prismaHexagonal) 3.0 * Math.sqrt(3.0) / 2.0 else Math.sqrt(3.0) / 2.0
                factor * aCm * aCm * cCm                                                                                //(Math.sqrt(3.0) / 2.0) * aCm * aCm * cCm
            }
            SistemaCristalino.TETRAGONAL -> {
                requireNotNull(cCm) { "Esta estructura tetragonal requiere el parámetro c" }
                aCm * aCm * cCm
            }
        }
    }

    private fun Double.pow3(): Double = this * this * this

    companion object {
        const val NUMERO_AVOGADRO = 6.02214076e23

        /** Catálogo de ejemplo con las 8 estructuras de tu menú. Ajusta libremente. */
        fun catalogoEjemplo(): List<EstructuraCristalina> = listOf(
            EstructuraCristalina(
                id = "nacl",
                nombre = "NaCl",
                formulaQuimica = "Cloruro de sodio",
                grupoRelacion = "AB",
                sistema = SistemaCristalino.CUBICO,
                relacionRadioParametro = RelacionRadioParametro.FCC_ARISTA,
                z = 4,
                formulaRelacionTexto = "Relación r+/r- para celda FCC: parámetro a = 2(r+ + r-)",
                modeloGlbAssetPath = "models/densidad/nacl_centrado.glb",

                compuestosRelacionados = listOf(
                    CompuestoEjemplo("LiF, LiCl, LiBr, LiI", "Haluros alcalinos e hidruros"),
                    CompuestoEjemplo("NaCl, NaBr, NaI", "Haluros alcalinos e hidruros"),
                    CompuestoEjemplo("KCl, KBr, KI", "Haluros alcalinos e hidruros"),
                    CompuestoEjemplo("LiH, NaH, KH", "Haluros alcalinos e hidruros"),
                    CompuestoEjemplo("AgCl, AgBr", "Haluros de plata"),
                    CompuestoEjemplo("MgO, CaO, SrO, BaO, MnO, FeO, CoO", "Monóxidos bivalentes")
                )
            ),
            EstructuraCristalina(
                id = "blenda",
                nombre = "Blenda",
                formulaQuimica = "Blenda de zinc",
                grupoRelacion = "AB",
                sistema = SistemaCristalino.CUBICO,
                relacionRadioParametro = RelacionRadioParametro.FCC_DIAGONAL,
                z = 4,
                formulaRelacionTexto = "Relación r+/r- para celda FCC: parámetro a = 4(r+ + r-)/√3",
                modeloGlbAssetPath = "models/densidad/blenda_centrado.glb",

                compuestosRelacionados = listOf(
                    CompuestoEjemplo("BeX, ZnX, CdX, HgX (X = S, Se, Te)", "Calcogenuros de Be, Zn, Cd y Hg"),
                    CompuestoEjemplo("Diamante, silicio, germanio, estaño gris", "Elementos del grupo 14 (tipo diamante)"),
                    CompuestoEjemplo("SiO₂ (cristobalita)", "Sílice"),
                    CompuestoEjemplo("BN, BP, SiC, CuX (X = F, Cl, Br, I)", "Binarios con B, Si y haluros de cobre"),
                    CompuestoEjemplo("XY (X = Al, Ga, In; Y = P, As, Sb)", "Compuestos III-V")
                )

            ),
            EstructuraCristalina(
                id = "wurtzita",
                nombre = "Wurtzita",
                formulaQuimica = "Estructura wurtzita",
                grupoRelacion = "AB",
                sistema = SistemaCristalino.HEXAGONAL,
                relacionRadioParametro = RelacionRadioParametro.NINGUNA,
                z = 6,

                requiereParametroC = true,
                prismaHexagonal = true,

                formulaRelacionTexto = "Estructura hexagonal: ingresa a y c directamente (sin fórmula r+/r- aún)",
                modeloGlbAssetPath = "models/densidad/wurtzita_centrado.glb",

                compuestosRelacionados = listOf(
                        CompuestoEjemplo("ZnX (X = O, S, Te)", "Compuestos de zinc"),
                    CompuestoEjemplo("CdX (X = S, Se)", "Compuestos de cadmio"),
                    CompuestoEjemplo("BeO, MgTe, NH₄F", "Otros compuestos AB"),
                    CompuestoEjemplo("MN (M = Al, Ga, In)", "Nitruros III-V"),
                    CompuestoEjemplo("AgI, NH₄F", "Otros compuestos AB")
            )
            ),
            EstructuraCristalina(
                id = "cscl",
                nombre = "CsCl",
                formulaQuimica = "Cloruro de cesio",
                grupoRelacion = "AB",
                sistema = SistemaCristalino.CUBICO,
                relacionRadioParametro = RelacionRadioParametro.BCC_DIAGONAL,
                z = 1,
                formulaRelacionTexto = "Relación r+/r- para celda BCC: parámetro a = 2(r+ + r-)/√3",
                modeloGlbAssetPath = "models/densidad/cscl_centrado.glb",

                compuestosRelacionados = listOf(
                    CompuestoEjemplo("CsX (X = Cl, Br, I, SH, CN)", "Compuestos de cesio"),
                    CompuestoEjemplo("TlX (X = Cl, Br, I, CN)", "Compuestos de talio"),
                    CompuestoEjemplo("NH₄X (X = Cl, Br, CN)", "Compuestos de amonio")
                )
            ),
            EstructuraCristalina(
                id = "fluorita",
                nombre = "Fluorita",
                formulaQuimica = "CaF2 / Estructura fluorita",
                grupoRelacion = "AB2",
                sistema = SistemaCristalino.CUBICO,
                relacionRadioParametro = RelacionRadioParametro.FCC_DIAGONAL,
                z = 4,
                formulaRelacionTexto = "Relación r+/r- para celda FCC: parámetro a = 4(r+ + r-)/√3",
                modeloGlbAssetPath = "models/densidad/fluorita_centrado.glb",

                compuestosRelacionados = listOf(
                    CompuestoEjemplo("MF₂ (M = Ca, Cd, Hg, Sn)", "Fluoruros metálicos"),
                    CompuestoEjemplo("MCl₂ (M = Sr y Ba)", "Cloruros metálicos"),
                    CompuestoEjemplo("MO₂ (M = Po, Zr, Hf, Ce, Pr, y varios actínidos)", "Dióxidos metálicos"),
                    CompuestoEjemplo("Dihidruros de metales de transición", "Hidruros metálicos")
                )
            ),
            EstructuraCristalina(
                id = "antifluorita",
                nombre = "Antifluorita",
                formulaQuimica = "Óxido de litio / Antifluorita",
                grupoRelacion = "AB2",
                sistema = SistemaCristalino.CUBICO,
                relacionRadioParametro = RelacionRadioParametro.FCC_DIAGONAL,
                z = 4,
                formulaRelacionTexto = "Relación r+/r- para celda FCC: parámetro a = 4(r+ + r-)/√3",
                modeloGlbAssetPath = "models/densidad/antifluorita_centrado.glb",

                compuestosRelacionados = listOf(
                    CompuestoEjemplo("M₂X (M = Li, Na, K; X = O, S, Se, Te)", "Óxidos y calcogenuros alcalinos"),
                    CompuestoEjemplo("Rb₂X (X = O, S)", "Compuestos de rubidio")
                )
            ),
            EstructuraCristalina(
                id = "yodurocadmio",
                nombre = "Yoduro de cadmio",
                formulaQuimica = "CdI2 / Capas coordinadas",
                grupoRelacion = "AB2",
                sistema = SistemaCristalino.HEXAGONAL,
                relacionRadioParametro = RelacionRadioParametro.NINGUNA,
                z = 3,
                requiereParametroC = true,
                prismaHexagonal = true,       // <- NUEVO: V = (3√3/2)·a²·c, igual que Wurtzita
                formulaRelacionTexto = "Estructura hexagonal en capas: ingresa a y c directamente",
                modeloGlbAssetPath = "models/densidad/yodurocadmio_centrado.glb",

                compuestosRelacionados = listOf(
                    CompuestoEjemplo("MBr₂ (M = Mg, Fe, Co, Ni, Cd)", "Bromuros metálicos"),
                    CompuestoEjemplo("MCl₂ (M = Ti, V)", "Cloruros metálicos"),
                    CompuestoEjemplo("MI₂ (M = Mg, Ca, Ti, Mn, Fe, Co, Ni, Zn, Cd, Pb)", "Yoduros metálicos"),
                    CompuestoEjemplo("M(OH)₂ (M = Mg, Ca, Mn, Fe, Co, Ni, Cd)", "Hidróxidos metálicos"),
                    CompuestoEjemplo("MS₂ (M = Ti, Zr, Pt, Sn)", "Sulfuros metálicos"),
                    CompuestoEjemplo("MSe₂ (M = Ti, V)", "Seleniuros metálicos"),
                    CompuestoEjemplo("MTe₂ (M = Ti, V, Co, Ni)", "Telururos metálicos")
                )


            ),
            EstructuraCristalina(
                id = "rutilo",
                nombre = "Rutilo",
                formulaQuimica = "TiO2 / Dióxido de titanio",
                grupoRelacion = "AB2",
                sistema = SistemaCristalino.TETRAGONAL,
                relacionRadioParametro = RelacionRadioParametro.NINGUNA,
                z = 2,
                requiereParametroC = true,
                formulaRelacionTexto = "Estructura tetragonal: ingresa a y c directamente",
                modeloGlbAssetPath = "models/densidad/rutilo_centrado.glb",

                compuestosRelacionados = listOf(
                    CompuestoEjemplo("MF₂ (M = Mg, Cr, Mn, Fe, Co, Ni, Cu, Zn)", "Fluoruros metálicos"),
                    CompuestoEjemplo("MO₂ (M = Si, Ge, Sn, Pb, Te, Ti, V, Cr, Mn, Zr, Mo, Os, Ir, Ce)", "Dióxidos metálicos")
                )

            )
        )
    }
}
