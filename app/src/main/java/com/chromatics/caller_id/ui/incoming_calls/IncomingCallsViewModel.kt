package com.chromatics.caller_id.ui.incoming_calls


import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.chromatics.caller_id.common.CallLogItemGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class IncomingCallsViewModel @Inject constructor(val application: Application) : ViewModel() {

    private val _callLogs = MutableLiveData<PagingData<CallLogItemGroup>>()
    val callLogs = Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { CallLogPagingSource(application) }
    ).flow.cachedIn(viewModelScope)


    fun fetchCallLogs() {
        val pager = Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { CallLogPagingSource(application) }
        )
        _callLogs.value = pager.flow.cachedIn(viewModelScope).asLiveData().value
    }

}



