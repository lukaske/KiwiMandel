package n.com.kiwimandel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.app.BundleCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity


class CardStackAdapter(
    private var spots: List<Spot> = emptyList(),
    private var context: Context
) : RecyclerView.Adapter<CardStackAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_spot, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val spot = spots[position]
        holder.name.text = "${spot.name}"
        holder.city.text = spot.city
        Glide.with(holder.image)
            .load(spot.url)
            .into(holder.image)
        holder.itemView.setOnClickListener { v ->
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse(spot.book)
            ContextCompat.startActivity(context, openURL,null)        }
    }

    override fun getItemCount(): Int {
        return spots.size
    }

    fun setSpots(spots: List<Spot>) {
        this.spots = spots
    }

    fun getSpots(): List<Spot> {
        return spots
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.item_name)
        var city: TextView = view.findViewById(R.id.item_city)
        var image: ImageView = view.findViewById(R.id.item_image)
    }

}