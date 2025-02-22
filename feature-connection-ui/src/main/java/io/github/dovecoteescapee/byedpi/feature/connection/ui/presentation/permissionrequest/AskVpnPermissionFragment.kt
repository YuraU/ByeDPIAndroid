package io.github.dovecoteescapee.byedpi.feature.connection.ui.presentation.permissionrequest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import io.github.dovecoteescapee.byedpi.feature.connection.ui.R

class AskVpnPermissionFragment : BottomSheetFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_permission_request, container, false)
        val back: View = root.findViewById(R.id.back)
        back.setOnClickListener {
            dismiss()
        }

        val vpnContinue: View = root.findViewById(R.id.vpnperm_continue)
        vpnContinue.setOnClickListener {
            dismiss()
            parentFragmentManager.setFragmentResult("bottom_sheet_result", bundleOf("continue" to true))
        }

        return root
    }
}
