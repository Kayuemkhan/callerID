package com.chromatics.caller_id.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.chromatics.caller_id.R
import com.chromatics.caller_id.databinding.SplashFragmentBinding
import com.chromatics.caller_id.ui.splash.State.HOME
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashFragment : Fragment() {

  private val viewModel: SplashViewModel by hiltNavGraphViewModels(R.id.mobile_navigation)
  private lateinit var binding: SplashFragmentBinding

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = SplashFragmentBinding.inflate(inflater, container, false)

    viewModel.homeRouteAfterThreeSecs()

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)


  }
}