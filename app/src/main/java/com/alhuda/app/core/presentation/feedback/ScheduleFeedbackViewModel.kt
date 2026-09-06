package com.alhuda.app.core.presentation.feedback

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

@HiltViewModel
class ScheduleFeedbackViewModel @Inject constructor(
    scheduleFeedback: ScheduleFeedback,
) : ViewModel() {
    val rescheduled: SharedFlow<ScheduleFeedbackInfo> = scheduleFeedback.events
}
