package co.edu.uniandes.vinilos.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale
import kotlin.math.pow

class ColorContrastTest {

    @Test
    fun paletaClara_cumpleContrasteMinimo() {
        verifyScheme("clara", LightColorScheme)
    }

    @Test
    fun paletaOscura_cumpleContrasteMinimo() {
        verifyScheme("oscura", DarkColorScheme)
    }

    private fun verifyScheme(schemeName: String, scheme: ColorScheme) {
        val pairs = listOf(
            ContrastPair("onBackground/background", scheme.onBackground, scheme.background, 4.5),
            ContrastPair("onSurface/surface", scheme.onSurface, scheme.surface, 4.5),
            ContrastPair("primary/background", scheme.primary, scheme.background, 4.5),
            ContrastPair("primary/surfaceVariant", scheme.primary, scheme.surfaceVariant, 4.5),
            ContrastPair("onSurfaceVariant/background", scheme.onSurfaceVariant, scheme.background, 4.5),
            ContrastPair("onSurfaceVariant/surfaceVariant", scheme.onSurfaceVariant, scheme.surfaceVariant, 4.5),
            ContrastPair(
                "onSurfaceVariant/surfaceContainerHighest",
                scheme.onSurfaceVariant,
                scheme.surfaceContainerHighest,
                4.5,
            ),
            ContrastPair("onSurface/surfaceContainerHighest", scheme.onSurface, scheme.surfaceContainerHighest, 4.5),
            ContrastPair("onBackground/surfaceVariant", scheme.onBackground, scheme.surfaceVariant, 4.5),
            ContrastPair("error/background", scheme.error, scheme.background, 4.5),
            ContrastPair("onPrimary/primary", scheme.onPrimary, scheme.primary, 4.5),
            ContrastPair("onSurfaceVariant/surfaceContainer", scheme.onSurfaceVariant, scheme.surfaceContainer, 4.5),
            ContrastPair(
                "onPrimaryContainer/primaryContainer",
                scheme.onPrimaryContainer,
                scheme.primaryContainer,
                3.0,
            ),
            ContrastPair(
                "onSecondaryContainer/secondaryContainer",
                scheme.onSecondaryContainer,
                scheme.secondaryContainer,
                3.0,
            ),
            ContrastPair("outline/background", scheme.outline, scheme.background, 3.0),
            // Colores copiados de res/drawable/ic_*_placeholder.xml (#FF5F5F5F sobre #FFC8C8C8).
            ContrastPair("placeholderIcon/placeholderBg", Color(0xFF5F5F5F), Color(0xFFC8C8C8), 3.0),
        )
        for (pair in pairs) {
            val ratio = contrastRatio(pair.foreground, pair.background)
            val label = "$schemeName ${pair.name}"
            assertTrue(
                String.format(Locale.US, "%s: %.2f < %.1f", label, ratio, pair.minRatio),
                ratio >= pair.minRatio,
            )
        }
    }

    private fun relativeLuminance(color: Color): Double {
        fun linearize(channel: Float): Double {
            val c = channel.toDouble()
            return if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * linearize(color.red) +
            0.7152 * linearize(color.green) +
            0.0722 * linearize(color.blue)
    }

    private fun contrastRatio(foreground: Color, background: Color): Double {
        val l1 = relativeLuminance(foreground)
        val l2 = relativeLuminance(background)
        return (maxOf(l1, l2) + 0.05) / (minOf(l1, l2) + 0.05)
    }

    private data class ContrastPair(
        val name: String,
        val foreground: Color,
        val background: Color,
        val minRatio: Double,
    )
}
