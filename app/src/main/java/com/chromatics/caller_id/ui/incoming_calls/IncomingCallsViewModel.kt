package com.chromatics.caller_id.ui.home

import android.app.Application
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class IncomingCallsViewModel @Inject constructor(val application: Application) : ViewModel() {



}
