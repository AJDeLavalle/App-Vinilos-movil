package co.edu.uniandes.vinilos.testutil

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.accessibility.enableAccessibilityChecks
import com.google.android.apps.common.testing.accessibility.framework.integrations.espresso.AccessibilityValidator
import org.junit.rules.TestRule

/** Activa los chequeos automáticos de ATF sobre la regla de Compose (solo en API 34+). */
fun <R : TestRule, A : ComponentActivity> AndroidComposeTestRule<R, A>.enableVinilosAccessibilityChecks(
    configure: AccessibilityValidator.() -> Unit = {},
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return
    val validator = AccessibilityValidator()
        .setRunChecksFromRootView(true)
        .apply(configure)
    enableAccessibilityChecks(validator)
}
