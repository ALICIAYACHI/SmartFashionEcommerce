package com.ropa.smartfashionecommerce

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.ropa.smartfashionecommerce.carrito.Carrito
import com.ropa.smartfashionecommerce.miperfil.MiPerfilActivity
import com.ropa.smartfashionecommerce.ui.theme.SmartFashionEcommerceTheme
import androidx.compose.foundation.lazy.items
import com.ropa.smartfashionecommerce.catalog.CatalogActivity



// Modelo de producto
data class Product(val name: String, val price: String, val image: Int)

// Lista de productos de ejemplo
val productList = listOf(
    Product("Women's Sweater", "€ 300.00", R.drawable.fondo),
    Product("Casual Wear", "€ 280.00", R.drawable.fondo2),
    Product("Lady Pant", "€ 790.00", R.drawable.fondo),
    Product("Women Pant", "€ 790.00", R.drawable.fondo),
    Product("Jacket", "€ 450.00", R.drawable.fondo),
    Product("Dress", "€ 650.00", R.drawable.fondo2)
)

// Lista de categorías
val categories = listOf("ZARA", "VOGUE", "CHANEL", "RALPH")

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartFashionEcommerceTheme {
                FashionHomeScreen()
            }
        }
    }
}

@Composable
fun FashionHomeScreen() {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("Home") }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 4.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == "Home",
                    onClick = { selectedTab = "Home" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == "Cart",
                    onClick = {
                        selectedTab = "Cart"
                        context.startActivity(Intent(context, Carrito::class.java))
                    },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart") },
                    label = { Text("Cart") }
                )
                NavigationBarItem(
                    selected = selectedTab == "Favorites",
                    onClick = {
                        selectedTab = "Favorites"
                        // Usamos el context ya definido arriba
                        context.startActivity(Intent(context, FavActivity::class.java))
                    },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                    label = { Text("Favorites") }
                )

                NavigationBarItem(
                    selected = selectedTab == "Profile",
                    onClick = {
                        selectedTab = "Profile"
                        context.startActivity(Intent(context, MiPerfilActivity::class.java))
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // Banner principal
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.imagendehomeprincipal),
                    contentDescription = "Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = "VOGUE",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Categorías scroll horizontal
            Text(
                text = "Categorías",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )

            LazyRow(
                modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
            ) {
                val categories = listOf("ZARA", "VOGUE", "CHANEL", "RALPH")
                items(categories) { category ->
                    val context = LocalContext.current
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray.copy(alpha = 0.3f))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable {
                                // Solo abrimos CatalogActivity si la categoría es ZARA
                                if (category == "ZARA") {
                                    context.startActivity(Intent(context, CatalogActivity::class.java))
                                }
                            }
                    ) {
                        Text(category, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }



            // Productos grid
            Text(
                text = "Productos",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                items(productList) { product ->
                    ProductCard(product)
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { }
            .padding(8.dp)
    ) {
        Image(
            painter = painterResource(id = product.image),
            contentDescription = product.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(180.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(product.price, color = Color.Gray, fontSize = 13.sp)
    }
}

@Preview(showSystemUi = true)
@Composable
fun HomePreview() {
    SmartFashionEcommerceTheme {
        FashionHomeScreen()
    }
}
