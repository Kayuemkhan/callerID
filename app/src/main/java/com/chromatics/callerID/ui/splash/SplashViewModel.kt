package com.chromatics.callerID.ui.splash

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chromatics.callerID.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(val application: Application) : ViewModel() {
  @Inject
  private var _navigatePage = SingleLiveEvent<State>()
  val navigatePage: SingleLiveEvent<State>
    get() = _navigatePage

  fun homeRouteAfterThreeSecs() {
    viewModelScope.launch {
      delay(3000)
      _navigatePage.postValue(State.HOME)

    }
  }
}

enum class State(val state: Int) {
  HOME(0)
}