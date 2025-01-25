package com.chromatics.caller_id.ui.incoming_calls

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import com.chromatics.caller_id.R
import com.chromatics.caller_id.databinding.IncomingCallsBinding
import com.chromatics.caller_id.databinding.SplashFragmentBinding
import com.chromatics.caller_id.ui.home.IncomingCallsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IncomingCallsFragment : Fragment() {

  private val viewModel: IncomingCallsViewModel by hiltNavGraphViewModels(R.id.mobile_navigation)
  private lateinit var binding: IncomingCallsBinding

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = IncomingCallsBinding.inflate(inflater, container, false)


    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)


  }
}