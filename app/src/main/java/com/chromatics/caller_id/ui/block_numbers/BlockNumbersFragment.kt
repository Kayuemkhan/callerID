package com.chromatics.caller_id.ui.block_numbers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import com.chromatics.caller_id.R
import com.chromatics.caller_id.databinding.BlockNumbersFragmentBinding
import com.chromatics.caller_id.databinding.SplashFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BlockNumbersFragment : Fragment() {

  private val viewModel: BlockNumbersViewModel by hiltNavGraphViewModels(R.id.mobile_navigation)
  private lateinit var binding: BlockNumbersFragmentBinding

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = BlockNumbersFragmentBinding.inflate(inflater, container, false)


    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)




  }
}