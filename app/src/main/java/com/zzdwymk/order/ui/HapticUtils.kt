package com.zzdwymk.order.ui

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

object HapticUtils {
    fun performClick(feedback: HapticFeedback) {
        feedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    fun performLightImpact(feedback: HapticFeedback) {
        feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    fun performMediumImpact(feedback: HapticFeedback) {
        feedback.performHapticFeedback(HapticFeedbackType.ContextClick)
    }
}
