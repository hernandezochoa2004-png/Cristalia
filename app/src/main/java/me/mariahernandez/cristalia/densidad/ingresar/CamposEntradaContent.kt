package com.cristalia.ui.ingresardatos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cristalia.model.EstructuraCristalina
import me.mariahernandez.cristalia.celdaunitaria.Paleta

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

/** 2) Interfaz cuando se da click en el tab "Volumen". */
@Composable
fun CampoVolumenContent(
    estructura: EstructuraCristalina,
    masaMolar: String,
    onMasaMolarChange: (String) -> Unit,
    volumenCelda: String,
    onVolumenCeldaChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CampoNumerico(
            label = "Masa molar (M)",
            unidad = "g/mol",
            value = masaMolar,
            onValueChange = onMasaMolarChange
        )

        CampoNumerico(
            label = "Volumen de la celda (V)",
            unidad = "cm³",
            value = volumenCelda,
            onValueChange = onVolumenCeldaChange
        )

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
