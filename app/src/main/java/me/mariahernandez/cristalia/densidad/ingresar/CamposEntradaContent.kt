package com.cristalia.ui.ingresardatos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cristalia.model.EstructuraCristalina

/**
 * a) Tres funciones que dibujan la interfaz de cada tab de IngresarDatosScreen
 * (Radio / Volumen / Parámetro a), siguiendo el mockup que enviaste.
 *
 * Cada una es "tonta": recibe el texto actual de cada campo y una lambda
 * de cambio, el estado real vive en IngresarDatosViewModel.
 */

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
    Column(modifier = modifier.fillMaxWidth()) {
        CampoNumerico(
            label = "Masa molar (M)",
            unidad = "g/mol",
            value = masaMolar,
            onValueChange = onMasaMolarChange
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CampoNumerico(
                label = "Radio (A⁺)",
                unidad = "pm",
                value = radioCation,
                onValueChange = onRadioCationChange,
                modifier = Modifier.weight(1f)
            )
            CampoNumerico(
                label = "Radio (B⁻)",
                unidad = "pm",
                value = radioAnion,
                onValueChange = onRadioAnionChange,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = estructura.formulaRelacionTexto,
            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
            modifier = Modifier.padding(top = 8.dp)
        )
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
    Column(modifier = modifier.fillMaxWidth()) {
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
            onValueChange = onVolumenCeldaChange,
            modifier = Modifier.padding(top = 12.dp)
        )

        Text(
            text = "Z = ${estructura.z} fórmulas unitarias por celda para ${estructura.nombre}",
            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
            modifier = Modifier.padding(top = 8.dp)
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
    Column(modifier = modifier.fillMaxWidth()) {
        CampoNumerico(
            label = "Masa molar (M)",
            unidad = "g/mol",
            value = masaMolar,
            onValueChange = onMasaMolarChange
        )

        if (estructura.requiereParametroC) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CampoNumerico(
                    label = "Parámetro a",
                    unidad = "pm",
                    value = parametroA,
                    onValueChange = onParametroAChange,
                    modifier = Modifier.weight(1f)
                )
                CampoNumerico(
                    label = "Parámetro c",
                    unidad = "pm",
                    value = parametroC,
                    onValueChange = onParametroCChange,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            CampoNumerico(
                label = "Parámetro de red (a)",
                unidad = "pm",
                value = parametroA,
                onValueChange = onParametroAChange,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Text(
            text = "Sistema ${estructura.sistema.name.lowercase()} · Z = ${estructura.z}",
            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

/** Campo numérico reutilizable (igual al de tus mockups: label arriba, unidad a la derecha). */
@Composable
private fun CampoNumerico(
    label: String,
    unidad: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            trailingIcon = {
                Text(
                    text = unidad,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
        )
    }
}
