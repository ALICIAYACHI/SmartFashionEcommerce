package com.ropa.smartfashionecommerce

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ropa.smartfashionecommerce.R
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import com.ropa.smartfashionecommerce.ui.theme.SmartFashionEcommerceTheme


class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartFashionEcommerceTheme {
                FashionApp()
            }
        }
    }
}

@Composable
fun FashionApp() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        BrandCategories()

        BannerSection()

        ProductList()

        HomeScreen()

    }
}


@Composable
fun BrandCategories() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp , 30.dp, 12.dp, 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("ZARA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text("VOGUE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text("CHANEL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text("Ralph", fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BannerSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(8.dp)
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
}

data class Product(val name: String, val price: String, val image: Int)

val productList = listOf(
    Product("Women's Sweater", "€ 300.00", R.drawable.fondo),
    Product("Casual Wear", "€ 280.00", R.drawable.fondo2),
    Product("Lady Pant", "€ 790.00", R.drawable.fondo),
    Product("Women Pant", "€ 790.00", R.drawable.fondo)
)

@Composable
fun ProductList() {
    LazyRow(
        modifier = Modifier.padding(8.dp)
    ) {
        items(productList) { product ->
            ProductCard(product)
        }
    }
}

@Composable
fun ProductCard(product: Product) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .padding(8.dp)
            .clickable { }
    ) {
        Image(
            painter = painterResource(id = product.image),
            contentDescription = product.name,
            modifier = Modifier
                .height(180.dp)
                .fillMaxWidth()
                .background(Color.LightGray, RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(product.price, color = Color.Gray, fontSize = 13.sp)
    }
}


@Composable
fun HomeScreen() {
    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(50.dp)),
                contentAlignment = Alignment.Center
            ) {
                NavigationBar(
                    containerColor = Color.Black.copy(alpha = 0.1f),
                    modifier = Modifier
                        .width(350.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .zIndex(3f)
                        .height(80.dp)


                ) {
                    NavigationBarItem(
                        modifier = Modifier.padding(0.dp ,30.dp ,0.dp ,0.dp),
                        selected = true,
                        onClick = { },
                        label = { Text("Home") },
                        icon = { Icon(painterResource(id = R.drawable.home2), contentDescription = null) }
                    )
                    NavigationBarItem(
                        modifier = Modifier.padding(0.dp ,30.dp ,0.dp ,0.dp),
                        selected = false,
                        onClick = { },
                        label = { Text("Cart") },
                        icon = { Icon(painterResource(id = R.drawable.carritocompra), contentDescription = null) }
                    )
                    NavigationBarItem(
                        modifier = Modifier.padding(0.dp ,30.dp ,0.dp ,0.dp),
                        selected = false,
                        onClick = { },
                        label = { Text("Favorites") },
                        icon = { Icon(painterResource(id = R.drawable.heart), contentDescription = null) }
                    )
                    NavigationBarItem(
                        modifier = Modifier.padding(0.dp ,30.dp ,0.dp ,0.dp),
                        selected = false,
                        onClick = { },
                        label = { Text("Profile") },
                        icon = { Icon(painterResource(id = R.drawable.person), contentDescription = null) }
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            items(2) { index ->
                Text(
                    text = "Aca va a ir los demas productos "
                )
            }
        }
    }
}


@Preview(showSystemUi = true)
@Composable
fun HomePreview() {
    SmartFashionEcommerceTheme {
        FashionApp()
    }
}