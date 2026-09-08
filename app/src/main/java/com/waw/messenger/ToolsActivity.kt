package com.waw.messenger

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import com.waw.messenger.workspace.WorkspaceShell

/** WAW Workspace standalone shell: Remote, PDF, documents, attendance, and diagnostics. */
class ToolsActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WorkspaceShell() }
    }
}
