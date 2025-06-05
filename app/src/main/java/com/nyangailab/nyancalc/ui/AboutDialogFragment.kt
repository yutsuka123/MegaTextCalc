package com.nyangailab.nyancalc.ui

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nyangailab.nyancalc.billing.BillingClientHelper

class AboutDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("About MegaTextCalc")
            .setMessage("MegaTextCalc is a powerful text calculator app.")
            .setPositiveButton("OK") { _, _ -> }
            .create()
    }

    companion object {
        fun newInstance(billingHelper: BillingClientHelper): AboutDialogFragment {
            val fragment = AboutDialogFragment()
            // Pass any necessary arguments to the fragment here using Bundle if needed
            return fragment
        }
    }
}
