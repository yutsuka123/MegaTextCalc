package com.nyangailab.nyancalc

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class PrivacyPolicyDialogFragment : DialogFragment() {

    // プライバシーポリシーのURLを指定されたURLに更新
    private val url = "https://github.com/nyangailab/application/blob/13ce2b01907bc6ca20eed08c9c99862b29f700a8/privacypolicy_nyancalc.md"

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity())
            .setTitle("プライバシーポリシー")
            .setMessage("プライバシーポリシーは以下のリンク先から確認できます")
            .setPositiveButton("確認する") { _, _ ->
                // 最もシンプルな形でブラウザを起動（余計なフラグなし）
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)

                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "ブラウザでページを開けませんでした: " + e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                dismiss()
            }
            .setNegativeButton("キャンセル") { _, _ -> dismiss() }
            .create()
    }
}
