package com.mycelium.giftbox.cards

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mycelium.giftbox.cards.adapter.PurchasedAdapter
import com.mycelium.giftbox.model.Card
import com.mycelium.wallet.R
import kotlinx.android.synthetic.main.fragment_giftbox_purchased.*


class PurchasedFragment : Fragment(R.layout.fragment_giftbox_purchased) {
    private val adapter = PurchasedAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list.adapter = adapter

        adapter.itemClickListener = {
//            findNavController().navigate(PurchasedFragmentDirections.to)
//            startActivity(Intent(requireContext(), GiftBoxDetailsActivity::class.java))
        }
        adapter.submitList(listOf(
                Card("", "Amazon UK", "Food, books, electronics", 12),
                Card("", "Nike US", "Sport Clothes", 30),
                Card("", "Ikea IT", "Furniture", 1)))
    }
}