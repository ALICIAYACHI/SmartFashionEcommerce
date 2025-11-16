package com.ropa.smartfashionecommerce.catalog

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ropa.smartfashionecommerce.R
import com.ropa.smartfashionecommerce.detalles.DetailsActivity // Asegura la importación

class ViewHolderAdapter(
    private val context: Context,
    private val productList: List<Product>
) : RecyclerView.Adapter<ViewHolderAdapter.ProductViewHolder>() {

    private val currentList: MutableList<Product> = productList.toMutableList()

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.image_producto)
        val name: TextView = itemView.findViewById(R.id.name_producto)
        val price: TextView = itemView.findViewById(R.id.product_price)
        val btnDetails: Button = itemView.findViewById(R.id.btn_details)
        val layoutContent: LinearLayout = itemView.findViewById(R.id.layout_content)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = currentList[position]

        holder.image.setImageResource(product.imageRes)
        holder.name.text = product.name
        holder.price.text = product.price

        // Lógica de navegación a Detalles
        val navigateToDetails = {
            val intent = Intent(context, DetailsActivity::class.java)
            // Claves que DetailsActivity espera: "name", "price", "imageRes"
            intent.putExtra("name", product.name)
            intent.putExtra("price", product.price)
            intent.putExtra("imageRes", product.imageRes)
            context.startActivity(intent)
        }

        // Desactivar el clic en el resto de la tarjeta.
        // La imagen y el contenido ya no navegan a detalles.
        holder.image.setOnClickListener(null)
        holder.layoutContent.setOnClickListener(null)

        // ASIGNAR LA NAVEGACIÓN ÚNICAMENTE AL BOTÓN "VER PRODUCTO"
        holder.btnDetails.setOnClickListener {
            navigateToDetails()
        }
    }

    override fun getItemCount(): Int = currentList.size

    fun updateList(newList: List<Product>) {
        currentList.clear()
        currentList.addAll(newList)
        notifyDataSetChanged()
    }
}