package com.app.vntpokedex.ui.main

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter

class NoFilterArrayAdapter(
    context: Context,
    layout: Int,
    private val items: List<String>
) : ArrayAdapter<String>(context, layout, items) {

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?) =
                FilterResults().apply {
                    values = items
                    count = items.size
                }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                notifyDataSetChanged()
            }
        }
    }
}