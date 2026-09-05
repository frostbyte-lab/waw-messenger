package com.waw.messenger.linked

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import com.waw.messenger.workspace.WorkspaceShell

/** Local WAW-owned tools; it never receives WhatsApp cookies or session data. */
class WorkspaceActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WorkspaceShell() }
    }
}
