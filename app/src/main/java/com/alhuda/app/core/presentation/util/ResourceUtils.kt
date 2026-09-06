package com.alhuda.app.core.presentation.util

import android.text.Spanned
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.core.text.HtmlCompat
import androidx.core.text.htmlEncode
import androidx.core.text.toHtml

@Composable
@ReadOnlyComposable
fun annotatedStringResource(@StringRes id: Int): AnnotatedString {
    val text = LocalResources.current.getText(id)
    val html = if (text is Spanned) {
        text.toHtmlWithoutParagraph()
    } else {
        text.toString()
    }
    return AnnotatedString.fromHtml(html)
}

@Composable
@ReadOnlyComposable
fun annotatedStringResource(
    @StringRes id: Int,
    vararg formatArgs: Any,
): AnnotatedString {
    val text = LocalResources.current.getText(id)
    val html = if (text is Spanned) {
        text.toHtmlWithoutParagraph()
    } else {
        text.toString()
    }
    val encodedArgs = formatArgs.map { if (it is String) it.htmlEncode() else it }.toTypedArray()
    return AnnotatedString.fromHtml(html.format(*encodedArgs))
}

private fun Spanned.toHtmlWithoutParagraph(): String {
    val htmlWithP = toHtml(HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
    return Regex("""<p dir="(?:ltr|rtl)">(.*?)</p>""")
        .find(htmlWithP)
        ?.groups?.get(1)?.value
        ?: htmlWithP
}
