package com.kidverse.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView

class PremiumBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.layout_premium_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val btnMonthly = view.findViewById<MaterialCardView>(R.id.btnMonthly)
        val btnYearly = view.findViewById<MaterialCardView>(R.id.btnYearly)
        val btnClose = view.findViewById<TextView>(R.id.btnClose)

        btnMonthly.setOnClickListener {
            // TODO: Launch monthly purchase
        }

        btnYearly.setOnClickListener {
            // TODO: Launch yearly purchase
        }

        btnClose.setOnClickListener {
            dismiss()
        }
    }
}
