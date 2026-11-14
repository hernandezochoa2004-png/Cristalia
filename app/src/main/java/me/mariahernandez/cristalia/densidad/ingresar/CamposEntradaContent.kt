package com.cristalia.ui.ingresardatos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cristalia.model.EstructuraCristalina
import me.mariahernandez.cristalia.celdaunitaria.Paleta
import me.mariahernandez.cristalia.densidad.radio.FuncionesCalculoRadio

private val FondoBox = Color(0xFF070A10)
private val BordeApagado = Color(0xFF1E2A3D)
private val TextoApagado = Color(0xFF9AA4B8)

/** 1) Interfaz cuando se da click en el tab "Radio". */
@Composable
fun CampoRadioContent(
    estructura: EstructuraCristalina,
    masaMolar: String,
    onMasaMolarChange: (String) -> Unit,
    radioCation: String,
    onRadioCationChange: (String) -> Unit,
    radioAnion: String,
    onRadioAnionChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CampoNumerico(
            label = "Masa molar (M)",
            unidad = "g/mol",
            value = masaMolar,
            onValueChange = onMasaMolarChange
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CampoNumerico(
                label = "Radio (A⁺)",
                unidad = "pm",
                value = radioCation,
                onValueChange = onRadioCationChange,
                modifier = Modifier.weight(1f),
                labelColor = Paleta.Magenta
            )
            CampoNumerico(
                label = "Radio (B⁻)",
                unidad = "pm",
                value = radioAnion,
                onValueChange = onRadioAnionChange,
                modifier = Modifier.weight(1f),
                labelColor = Paleta.Cian
            )
        }
    }
}

/**
 * 2) Interfaz cuando se da click en el tab "Volumen".
 * El volumen se escribe como  mantisa × 10^exponente  (ej. 1.79 × 10^-22 cm³)
 * para no tener que teclear "1.79e-22".
 */
@Composable
fun CampoVolumenContent(
    estructura: EstructuraCristalina,
    masaMolar: String,
    onMasaMolarChange: (String) -> Unit,
    volumenMantisa: String,
    onVolumenMantisaChange: (String) -> Unit,
    volumenExponente: String,
    onVolumenExponenteChange: (String) -> Unit,
    onAlternarSignoExponente: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CampoNumerico(
            label = "Masa molar (M)",
            unidad = "g/mol",
            value = masaMolar,
            onValueChange = onMasaMolarChange
        )

        // Volumen: [ mantisa ] × 10 [±] [ exponente ]
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Volumen de la celda (V)",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "cm³",
                    color = TextoApagado,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CajaTexto(
                    value = volumenMantisa,
                    onValueChange = onVolumenMantisaChange,
                    placeholder = "1.79",
                    modifier = Modifier.weight(1.3f)
                )

                Text(
                    text = "× 10",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                // Botón ± para cambiar el signo del exponente
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(FondoBox)
                        .border(1.dp, Paleta.Cian.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                        .clickable { onAlternarSignoExponente() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "±",
                        color = Paleta.Cian,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                CajaTexto(
                    value = volumenExponente,
                    onValueChange = onVolumenExponenteChange,
                    placeholder = "-22",
                    teclado = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            // Vista previa del número que se va a usar (ayuda a detectar errores de exponente)
            val mantisa = volumenMantisa.toDoubleOrNull()
            val exponente = volumenExponente.toIntOrNull()
            if (mantisa != null && exponente != null) {
                val volumen = mantisa * Math.pow(10.0, exponente.toDouble())
                Text(
                    text = "V = ${FuncionesCalculoRadio.formatoCientifico(volumen)} cm³",
                    color = Paleta.Cian,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Text(
            text = "Z = ${estructura.z} fórmulas unitarias por celda para ${estructura.nombre}",
            color = TextoApagado,
            fontSize = 11.sp,
            fontStyle = FontStyle.Italic,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/** 3) Interfaz cuando se da click en el tab "Parámetro a". */
@Composable
fun CampoParametroAContent(
    estructura: EstructuraCristalina,
    masaMolar: String,
    onMasaMolarChange: (String) -> Unit,
    parametroA: String,
    onParametroAChange: (String) -> Unit,
    parametroC: String,
    onParametroCChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CampoNumerico(
            label = "Masa molar (M)",
            unidad = "g/mol",
            value = masaMolar,
            onValueChange = onMasaMolarChange
        )

        if (estructura.requiereParametroC) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CampoNumerico(
                    label = "Parámetro a",
                    unidad = "pm",
                    value = parametroA,
                    onValueChange = onParametroAChange,
                    modifier = Modifier.weight(1f),
                    labelColor = Paleta.Cian
                )
                CampoNumerico(
                    label = "Parámetro c",
                    unidad = "pm",
                    value = parametroC,
                    onValueChange = onParametroCChange,
                    modifier = Modifier.weight(1f),
                    labelColor = Paleta.Magenta
                )
            }
        } else {
            CampoNumerico(
                label = "Parámetro de red (a)",
                unidad = "pm",
                value = parametroA,
                onValueChange = onParametroAChange
            )
        }

        Text(
            text = "Sistema ${estructura.sistema.name.lowercase()} · Z = ${estructura.z}",
            color = TextoApagado,
            fontSize = 11.sp,
            fontStyle = FontStyle.Italic,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/** Campo numérico reutilizable. */
@Composable
private fun CampoNumerico(
    label: String,
    unidad: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    labelColor: Color = Color.White
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = labelColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = unidad,
                color = TextoApagado,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = androidx.compose.ui.text.TextStyle(
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            ),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = FondoBox,
                unfocusedContainerColor = FondoBox,
                disabledContainerColor = FondoBox,
                focusedBorderColor = Paleta.Cian,
                unfocusedBorderColor = BordeApagado
            )
        )
    }
}

/** Caja de texto sola (sin etiqueta), con el mismo estilo que CampoNumerico. */
@Composable
private fun CajaTexto(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    teclado: KeyboardType = KeyboardType.Decimal
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = {
            Text(
                text = placeholder,
                color = TextoApagado.copy(alpha = 0.5f),
                fontSize = 15.sp,
                fontFamily = FontFamily.Monospace
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        textStyle = androidx.compose.ui.text.TextStyle(
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        ),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = FondoBox,
            unfocusedContainerColor = FondoBox,
            disabledContainerColor = FondoBox,
            focusedBorderColor = Paleta.Cian,
            unfocusedBorderColor = BordeApagado
        )
    )
}
