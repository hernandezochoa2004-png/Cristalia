package me.mariahernandez.cristalia.celdaunitaria

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import io.github.sceneview.Scene
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import java.io.File

//Nuevo
import io.github.sceneview.math.Position
import io.github.sceneview.rememberCameraNode

/**
 * Muestra un GLB de la carpeta assets. El usuario puede girarlo con el dedo.
 * Al cambiar [rutaGlb] se reemplaza el modelo sin recrear todo el visor.
 */
@Composable
fun Visor3D(rutaGlb: String, modifier: Modifier = Modifier) {

    val engine = rememberEngine()                         // Dibuja los gráficos 3D
    val modelLoader = rememberModelLoader(engine)         // Lee las imagenes .glb
    val nodos = rememberNodes()                           // Nodos que al inicio están vacío y son ocupados por el modelo

    // Cámara alejada del modelo (z mayor = más lejos)
    val cameraNode = rememberCameraNode(engine) {
        position = Position(x = 0f, y = 0f, z = 3f)
    }

    // Sirve para borrar un modelo que estaba en escena y se quiere pasar a uno nuevo. Ej: SCC -> FCC
    LaunchedEffect(rutaGlb) {
        nodos.clear()
        nodos.add(
            ModelNode(
                modelInstance = modelLoader.createModelInstance(rutaGlb),
                scaleToUnits = 1.0f,                         // el modelo cabe en un cubo de 1 unidad
                centerOrigin = Position(x = 0f, y = 0f, z = 0f)  // lo centra en pantalla
            )
        )
    }

    // Visor que se ve en pantalla
    Scene(
        modifier = modifier,
        engine = engine,
        modelLoader = modelLoader,
        childNodes = nodos,
        cameraNode = cameraNode,
        cameraManipulator = rememberCameraManipulator(             // Permite girar el modelo con el dedo
            orbitHomePosition = Position(x = 0f, y = 0f, z = 3f),
            targetPosition = Position(x = 0f, y = 0f, z = 0f)
        )
    )
}

/** Copia un GLB de assets a la carpeta Descargas (sin permisos en Android 10+). */

// Para descargar archivos, sólo si la persona inició sesión
fun guardarGlbEnDescargas(context: Context, rutaAsset: String): Boolean {
    return try {
        val nombre = File(rutaAsset).name          // De "modelos/celda.glb" saca solo "celda.glb".
        val valores = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, nombre)                             // nombre:celda.glb
            put(MediaStore.Downloads.MIME_TYPE, "model/gltf-binary")                   // tipo: modelo glb
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)   // carpeta:descargas
        }

        // Crea el archivo vacío.
        val uri = context.contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI, valores
        ) ?: return false

        // Copia el contenido
        context.assets.open(rutaAsset).use { entrada ->
            context.contentResolver.openOutputStream(uri)?.use { salida ->
                entrada.copyTo(salida)
            }
        }
        true
    } catch (e: Exception) {
        false
    }
}

