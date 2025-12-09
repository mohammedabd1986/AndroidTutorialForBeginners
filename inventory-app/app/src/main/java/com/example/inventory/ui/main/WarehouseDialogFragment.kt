package com.example.inventory.ui.main

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.inventory.data.entity.WarehouseStock

class WarehouseDialogFragment(private val stocks: List<WarehouseStock>) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Warehouse Stock")

        val stockList = stocks.map { "${it.warehouseName}: ${it.quantity}" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, stockList)

        builder.setAdapter(adapter, null)
        builder.setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }

        return builder.create()
    }
}
