package com.ropa.smartfashionecommerce

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ropa.smartfashionecommerce.ui.theme.SmartFashionEcommerceTheme

data class FavoriteItem(
    val id: Int,
    val name: String,
    val price: String,
    val sizes: List<String>,
    val imageRes: Int,
    var isFavorite: Boolean = true
)

class FavActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartFashionEcommerceTheme {
                FavApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavApp() {
    var favoriteItems by remember { mutableStateOf(getFavoriteItems()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "SmartFashion",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            navigationIcon = {
                val context = LocalContext.current
                val activity = context as? Activity
                IconButton(onClick = { activity?.finish() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                }
            }
            ,
            actions = {
                IconButton(onClick = { /* Perfil */ }) {
                    Icon(Icons.Default.Person, contentDescription = "Perfil")
                }
                IconButton(onClick = { /* Carrito */ }) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Mis Favoritos",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "${favoriteItems.size} productos guardados",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(favoriteItems) { item ->
                    FavoriteProductCard(
                        item = item,
                        onFavoriteClick = { product ->
                            favoriteItems = favoriteItems.map {
                                if (it.id == product.id) it.copy(isFavorite = false) else it
                            }.filter { it.isFavorite }
                        },
                        onAddToCart = { /* Agregar al carrito */ }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteProductCard(
    item: FavoriteItem,
    onFavoriteClick: (FavoriteItem) -> Unit,
    onAddToCart: (FavoriteItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Image(
                    painter = painterResource(id = item.imageRes),
                    contentDescription = item.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))

                )

                IconButton(
                    onClick = { onFavoriteClick(item) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Favorito",
                        tint = Color.Red
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = item.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = item.price,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00BCD4),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        item.sizes.take(4).forEach { size ->
                            Text(
                                text = size,
                                fontSize = 10.sp,
                                color = Color.Gray,
                                modifier = Modifier
                                    .background(
                                        Color(0xFFF0F0F0),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Button(
                        onClick = { onAddToCart(item) },
                        modifier = Modifier.size(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00BCD4)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "+",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun getFavoriteItems(): List<FavoriteItem> {
    return listOf(
        FavoriteItem(
            id = 1,
            name = "Blusa Elegante Negra",
            price = "S/ 89.90",
            sizes = listOf("S", "M", "L", "XL"),
            imageRes = R.drawable.blusa_negra
        ),
        FavoriteItem(
            id = 2,
            name = "Vestido Dorado Noche",
            price = "S/ 159.90",
            sizes = listOf("S", "M", "L"),
            imageRes = R.drawable.vestido_dorado
        )
    )
}

@Preview(showBackground = true)
@Composable
fun FavAppPreview() {
    SmartFashionEcommerceTheme {
        FavApp()
    }
}