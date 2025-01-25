package com.chromatics.caller_id.ui.incoming_calls

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CallLog
import androidx.core.content.ContextCompat
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chromatics.caller_id.common.CallLogItemGroup

class CallLogPagingSource(
    private val application: Application
) : PagingSource<Int, CallLogItemGroup>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CallLogItemGroup> {
        return try {
            if (ContextCompat.checkSelfPermission(
                    application,
                    Manifest.permission.READ_CALL_LOG
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val callLogList = arrayListOf<com.chromatics.caller_id.common.CallLogItem>()

                val projection = arrayOf(
                    CallLog.Calls._ID,
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.DATE,
                    CallLog.Calls.DURATION,
                    CallLog.Calls.TYPE
                )

                val cursor: Cursor? = application.contentResolver.query(
                    CallLog.Calls.CONTENT_URI,
                    projection,
                    null,
                    null,
                    "${CallLog.Calls.DATE} DESC LIMIT ${params.loadSize} OFFSET ${(params.key ?: 0)}"
                )

                cursor?.use {
                    while (it.moveToNext()) {
                        val id = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls._ID))
                        val number = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                        val date = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DATE))
                        val duration = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DURATION))
                        val type = com.chromatics.caller_id.common.CallLogItem.Type.fromProviderType(
                            it.getInt(it.getColumnIndexOrThrow(CallLog.Calls.TYPE))
                        )

                        // Add the CallLogItem to the list
                        callLogList.add(
                            com.chromatics.caller_id.common.CallLogItem(
                                 id,
                                 type,
                                 number,
                                 date,
                                duration
                            )
                        )
                    }
                }

// Group the list by phone number
                val grouped = callLogList.groupBy { it.number }
                    .map { com.chromatics.caller_id.common.CallLogItemGroup(it.value) }


                LoadResult.Page(
                    data = grouped,
                    prevKey = if (params.key == 0) null else params.key?.minus(1),
                    nextKey = if (callLogList.isEmpty()) null else (params.key ?: 0) + 1
                )
            } else {
                LoadResult.Error(IllegalStateException("Permission not granted"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }

    }

    override fun getRefreshKey(state: PagingState<Int, CallLogItemGroup>): Int? {
        return state.anchorPosition
    }
}
