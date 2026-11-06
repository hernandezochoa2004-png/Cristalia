package me.mariahernandez.cristalia.menu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.mariahernandez.cristalia.R
import me.mariahernandez.cristalia.celdaunitaria.Paleta

@Composable
fun MenuScreen(
    onCeldaUnitariaClick: () -> Unit,
    onFactorEmpaquetamientoClick: () -> Unit = {},
    onDensidadClick: () -> Unit = {},
    onHuecosClick: () -> Unit = {},
    onIrALogin: () -> Unit = {},
    viewModel: MenuViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                MenuEventoUi.NavegarACeldaUnitaria -> onCeldaUnitariaClick()
                MenuEventoUi.NavegarAFactorEmpaquetamiento -> onFactorEmpaquetamientoClick()
                MenuEventoUi.NavegarADensidad -> onDensidadClick()
                MenuEventoUi.NavegarAHuecos -> onHuecosClick()
                MenuEventoUi.NavegarALogin -> onIrALogin()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paleta.Fondo)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Barra superior con logo y botón de Iniciar sesión
        HeaderBar(onIrALogin = viewModel::onIniciarSesionClick)

        Spacer(modifier = Modifier.height(4.dp))

        // Tarjetas dinámicas del menú
        state.opciones.forEach { opcion ->
            when (opcion.id) {
                "celda_unitaria" -> {
                    MenuCard(
                        icon = { IconIsometricCube(modifier = Modifier.size(28.dp)) },
                        title = opcion.titulo,
                        description = opcion.descripcion,
                        extraContent = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                opcion.badges.forEach { badge ->
                                    TagBadge(text = badge)
                                }
                            }
                        },
                        onClick = { viewModel.onOpcionClick(opcion.id) }
                    )
                }

                "factor_empaquetamiento" -> {
                    MenuCard(
                        icon = { IconFactorEmpaquetamiento(modifier = Modifier.size(28.dp)) },
                        title = opcion.titulo,
                        description = opcion.descripcion,
                        extraContent = {
                            opcion.formula?.let { formula ->
                                Text(
                                    text = formula,
                                    color = Paleta.Cian,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        },
                        onClick = { viewModel.onOpcionClick(opcion.id) }
                    )
                }

                "densidad" -> {
                    MenuCard(
                        icon = { IconDensidad(modifier = Modifier.size(28.dp)) },
                        title = opcion.titulo,
                        description = opcion.descripcion,
                        extraContent = {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "UNIDADES:",
                                    color = Color(0xFF6B7A90),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                opcion.unidades?.let { unidades ->
                                    Text(
                                        text = unidades,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        },
                        onClick = { viewModel.onOpcionClick(opcion.id) }
                    )
                }

                "huecos" -> {
                    MenuCard(
                        icon = { IconHuecos(modifier = Modifier.size(28.dp)) },
                        title = opcion.titulo,
                        description = opcion.descripcion,
                        extraContent = null,
                        onClick = { viewModel.onOpcionClick(opcion.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderBar(onIrALogin: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo y Nombre de la app
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo Cristalia",
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Cristalia",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Botón Iniciar sesión
        Surface(
            onClick = onIrALogin,
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF131825),
            border = BorderStroke(1.dp, Color(0xFF263044))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text(
                        text = "Iniciar",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 12.sp
                    )
                    Text(
                        text = "sesión",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuCard(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    extraContent: (@Composable () -> Unit)?,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Paleta.Tarjeta,
        border = BorderStroke(1.dp, Color(0xFF1E2638)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Contenedor del ícono a la izquierda
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF171E2E)),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Contenido central: Título, Descripción y contenido extra
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = Paleta.TextoSecundario,
                    fontSize = 13.sp,
                    lineHeight = 17.sp
                )
                if (extraContent != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    extraContent()
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Flecha a la derecha
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF5E6A82),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun TagBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF1A2232))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF8E9BAF),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun IconIsometricCube(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = w * 0.38f
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val strokeColor = Color.White.copy(alpha = 0.9f)

        val top = Offset(cx, cy - r)
        val topRight = Offset(cx + r * 0.866f, cy - r * 0.5f)
        val bottomRight = Offset(cx + r * 0.866f, cy + r * 0.5f)
        val bottom = Offset(cx, cy + r)
        val bottomLeft = Offset(cx - r * 0.866f, cy + r * 0.5f)
        val topLeft = Offset(cx - r * 0.866f, cy - r * 0.5f)
        val center = Offset(cx, cy)

        val pathHex = Path().apply {
            moveTo(top.x, top.y)
            lineTo(topRight.x, topRight.y)
            lineTo(bottomRight.x, bottomRight.y)
            lineTo(bottom.x, bottom.y)
            lineTo(bottomLeft.x, bottomLeft.y)
            lineTo(topLeft.x, topLeft.y)
            close()
        }
        drawPath(pathHex, color = strokeColor, style = stroke)

        drawLine(strokeColor, center, top, strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(strokeColor, center, bottomRight, strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(strokeColor, center, bottomLeft, strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
private fun IconFactorEmpaquetamiento(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cyanColor = Color(0xFF00F0FF)
        val radius = w * 0.38f
        val center = Offset(w / 2f, h / 2f)
        val topLeft = Offset(center.x - radius, center.y - radius)
        val diameter = Size(radius * 2, radius * 2)

        drawArc(cyanColor, startAngle = -80f, sweepAngle = 150f, useCenter = true, topLeft = topLeft, size = diameter)
        drawArc(cyanColor, startAngle = 80f, sweepAngle = 75f, useCenter = true, topLeft = topLeft, size = diameter)
        drawArc(cyanColor, startAngle = 165f, sweepAngle = 85f, useCenter = true, topLeft = topLeft, size = diameter)
    }
}

@Composable
private fun IconDensidad(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val dotRadius = 2.4.dp.toPx()
        val spacing = 6.5.dp.toPx()
        val magentaColor = Color(0xFFE91E8C)
        val violetColor = Color(0xFFC9A0FF)

        val offsets = listOf(
            Offset(cx - spacing, cy - spacing),
            Offset(cx, cy - spacing),
            Offset(cx + spacing, cy - spacing),
            Offset(cx - spacing, cy),
            Offset(cx, cy),
            Offset(cx + spacing, cy),
            Offset(cx - spacing, cy + spacing),
            Offset(cx, cy + spacing),
            Offset(cx + spacing, cy + spacing)
        )

        offsets.forEachIndexed { index, offset ->
            val color = if (index % 2 == 0) magentaColor else violetColor
            drawCircle(color = color, radius = dotRadius, center = offset)
        }
    }
}

@Composable
private fun IconHuecos(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val rRed = 5.5.dp.toPx()
        val rBlue = 3.2.dp.toPx()
        val redColor = Color(0xFFEE3B3B)
        val blueColor = Color(0xFF00F0FF)

        val p1 = Offset(cx, cy - 5.5.dp.toPx())
        val p2 = Offset(cx - 5.dp.toPx(), cy + 3.5.dp.toPx())
        val p3 = Offset(cx + 5.dp.toPx(), cy + 3.5.dp.toPx())

        drawCircle(color = redColor, radius = rRed, center = p1)
        drawCircle(color = redColor, radius = rRed, center = p2)
        drawCircle(color = redColor, radius = rRed, center = p3)

        drawCircle(color = blueColor, radius = rBlue, center = Offset(cx, cy + 0.5.dp.toPx()))
    }
}
