package com.chromatics.caller_id.ui.incoming_calls

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.chromatics.caller_id.databinding.IncomingCallsBinding
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class IncomingCallsFragment : Fragment() {

  private lateinit var binding: IncomingCallsBinding
  private val viewModel: IncomingCallsViewModel by viewModels()

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = IncomingCallsBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val adapter = CallLogItemRecyclerViewAdapter()
    binding.callLogList.apply {
      layoutManager = LinearLayoutManager(context)
      this.adapter = adapter
    }

    // Observe PagingData and submit to adapter
    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {

        viewModel.callLogs.collectLatest { pagingData ->
          println("pagingData")
          println(Gson().toJson(pagingData))
          println(viewModel.callLogs)
          adapter.submitData(lifecycle, pagingData)
        }
      }
    }

    // Manage UI visibility
    binding.callLogPermissionMessage.visibility = View.GONE
    binding.callLogList.visibility = View.VISIBLE
  }
}
