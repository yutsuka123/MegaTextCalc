package com.nyangailab.nyancalc

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class DisclaimerDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity())
            .setTitle(R.string.disclaimer_title)
            .setMessage(R.string.disclaimer_body)
            .setPositiveButton("OK") { _, _ -> dismiss() }
            .create()
    }
}
