package com.chromatics.callerID.ui.home

import android.app.Application
import androidx.lifecycle.ViewModel
import com.chromatics.callerID.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class IncomingCallsViewModel @Inject constructor(val application: Application) : ViewModel() {
  @Inject
  private var _navigatePage = SingleLiveEvent<State>()
  val navigatePage: SingleLiveEvent<State>
    get() = _navigatePage


}
