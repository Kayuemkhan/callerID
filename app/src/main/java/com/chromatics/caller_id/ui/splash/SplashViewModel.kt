package com.chromatics.caller_id.ui.splash

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(val application: Application) : ViewModel() {

  // Directly initialize the SingleLiveEvent without using @Inject
  private val _navigatePage = SingleLiveEvent<State>()
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
