package com.example.note.util

import android.os.Build
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.example.note.R
import com.permissionx.guolindev.PermissionX

/**
 * 通用请求权限
 * @param permissions 本次请求的权限，
 * @param api 请求最大限制的api，在大于该api时，不请求权限
 * @param callBack 请求成功或不需要请求权限时回调
 */
inline fun FragmentActivity.requestPermission(
    permissions: List<String>,
    api: Int = Build.VERSION_CODES.Q,
    crossinline callBack: () -> Unit = {},
) {
    if (Build.VERSION.SDK_INT >= api) {
        callBack.invoke()
        return
    }
    PermissionX.init(this)
        .permissions(permissions)
        .onExplainRequestReason { scope, deniedList ->
            scope.showRequestReasonDialog(
                deniedList,
                getString(R.string.permission_first_reject),
                getString(R.string.confirm),
                getString(R.string.cancel)
            )
        }
        .onForwardToSettings { scope, deniedList ->
            scope.showForwardToSettingsDialog(
                deniedList,
                getString(R.string.permission_reject_dont_ask_again),
                getString(R.string.confirm),
                getString(R.string.cancel)
            )
        }
        .request { allGranted, _, _ ->
            if (allGranted) {
                callBack.invoke()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.permission_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
}