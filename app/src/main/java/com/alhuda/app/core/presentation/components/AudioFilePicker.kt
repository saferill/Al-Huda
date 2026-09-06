package com.alhuda.app.core.presentation.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

data class PickedAudio(

    val filepath: String,

    val suggestedName: String,
)

@Composable
fun rememberAudioFilePicker(onPicked: (PickedAudio) -> Unit): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentOnPicked by rememberUpdatedState(onPicked)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val picked = withContext(Dispatchers.IO) { copyAudioToInternal(context, uri) }
            if (picked != null) currentOnPicked(picked)
        }
    }
    return remember(launcher) { { launcher.launch(arrayOf("audio/*")) } }
}

private fun copyAudioToInternal(
    context: Context,
    uri: Uri,
): PickedAudio? {
    val resolver = context.contentResolver
    val displayName = resolver
        .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { if (it.moveToFirst()) it.getString(0) else null }
    val extension = displayName?.substringAfterLast('.', "")?.takeIf { it.isNotEmpty() }
        ?: MimeTypeMap.getSingleton().getExtensionFromMimeType(resolver.getType(uri))
    val dir = File(context.filesDir, "audio").apply { mkdirs() }
    val target = File(dir, UUID.randomUUID().toString() + (extension?.let { ".$it" } ?: ""))
    return try {
        resolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        PickedAudio(
            filepath = Uri.fromFile(target).toString(),
            suggestedName = displayName?.substringBeforeLast('.')?.takeIf { it.isNotBlank() } ?: target.name,
        )
    } catch (_: Exception) {
        target.delete()
        null
    }
}

suspend fun deleteAudioFile(filepath: String?) {
    val path = filepath?.toUri()?.takeIf { it.scheme == "file" }?.path ?: return
    withContext(Dispatchers.IO) { runCatching { File(path).delete() } }
}
