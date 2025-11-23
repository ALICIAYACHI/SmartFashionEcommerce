package com.ropa.smartfashionecommerce.maps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.GoogleMap
import com.ropa.smartfashionecommerce.R
class MapsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync { googleMap: GoogleMap ->

            // ⚠️ Usa aquí las coordenadas reales de tu tienda
            val tienda = LatLng(-8.110812, -79.028944)

            googleMap.addMarker(MarkerOptions().position(tienda).title("Tienda SmartFashion"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tienda, 17f))
        }
    }
}