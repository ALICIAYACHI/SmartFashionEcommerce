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
import coil.load
import com.ropa.smartfashionecommerce.R
import com.ropa.smartfashionecommerce.detalles.ProductDetailActivity

class ViewHolderAdapter(
    private val context: Context,
    private val productList: List<Product>
) : RecyclerView.Adapter<ViewHolderAdapter.ProductViewHolder>() {

    private val currentList: MutableList<Product> = productList.toMutableList()

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.image_producto)
        val name: TextView = itemView.findViewById(R.id.name_producto)
        val price: TextView = itemView.findViewById(R.id.product_price)
        val layoutContent: LinearLayout = itemView.findViewById(R.id.layout_content)
        val cartIcon: ImageView = itemView.findViewById(R.id.icon_cart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = currentList[position]

        if (product.imageUrl != null) {
            holder.image.load(product.imageUrl) {
                crossfade(true)
                placeholder(product.imageRes)
                error(product.imageRes)
            }
        } else {
            holder.image.setImageResource(product.imageRes)
        }

        holder.name.text = product.name
        holder.price.text = product.price

        // Navegar a ProductDetailActivity con los datos reales del producto (mismo id que en Inicio)
        val navigateToDetails = {
            val intent = Intent(context, ProductDetailActivity::class.java).apply {
                putExtra("productId", product.id)
                putExtra("productName", product.name)

                // Precio en double: extraemos número del string "S/ xx.xx"
                val numericPrice = product.price
                    .replace("S/", "")
                    .replace("s/", "")
                    .replace(" ", "")
                    .replace(",", ".")
                    .toDoubleOrNull() ?: 0.0
                putExtra("productPrice", numericPrice)

                putExtra("productDescription", product.description ?: "")

                if (product.imageUrl != null) {
                    putExtra("imageType", "url")
                    putExtra("productImageUrl", product.imageUrl)
                } else {
                    putExtra("imageType", "local")
                    putExtra("productImageRes", product.imageRes)
                }
            }
            context.startActivity(intent)
        }

        // Clic en toda la tarjeta (contenido) para ir al detalle
        holder.layoutContent.setOnClickListener { navigateToDetails() }
        holder.image.setOnClickListener { navigateToDetails() }

        // Icono de carrito: por ahora también navega al detalle (podemos cambiar a "agregar al carrito" más adelante)
        holder.cartIcon.setOnClickListener { navigateToDetails() }
    }

    override fun getItemCount(): Int = currentList.size

    fun updateList(newList: List<Product>) {
        currentList.clear()
        currentList.addAll(newList)
        notifyDataSetChanged()
    }
}