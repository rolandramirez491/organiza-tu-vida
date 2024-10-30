package com.example.organizatuvida.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.organizatuvida.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var weatherApi: WeatherApiService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        textView.text = "Obteniendo ubicación..."

        // Inicializa el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Inicializa Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherApi = retrofit.create(WeatherApiService::class.java)

        // Llama a la función para obtener la ubicación y el clima
        getLocationAndWeather(textView)

        return root
    }

    private fun getLocationAndWeather(textView: TextView) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Solicita permisos si no están otorgados
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            return
        }

        // Obtiene la última ubicación conocida
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude

                // Usa Geocoder para convertir coordenadas a dirección
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                try {
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    if (addresses != null && addresses.isNotEmpty()) {
                        val address = addresses[0]
                        val addressLine = address.getAddressLine(0)
                        textView.text = "Dirección: $addressLine"

                        // Llama a la API del clima
                        getWeatherData(latitude, longitude, textView)

                    } else {
                        textView.text = "No se pudo obtener la dirección exacta"
                    }
                } catch (e: Exception) {
                    Log.e("Geocoder", "Error al obtener la dirección", e)
                    textView.text = "Error al obtener la dirección"
                }
            } else {
                textView.text = "No se pudo obtener la ubicación"
            }
        }.addOnFailureListener {
            textView.text = "Error al obtener la ubicación"
        }
    }

    private fun getWeatherData(lat: Double, lon: Double, textView: TextView) {
        // Llama a la API usando Retrofit para obtener el clima
        val call = weatherApi.getCurrentWeather(lat, lon, "metric", "dc87f4093412ad3aaf6d863ddc3dacd7") // Reemplaza con tu clave API

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    val temp = weatherResponse?.main?.temp
                    textView.append("\nTemperatura de la direccion actual: $temp°C")
                } else {
                    textView.text = "Error al obtener la temperatura"
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                textView.text = "Error: ${t.message}"
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndWeather(binding.textHome)
            } else {
                binding.textHome.text = "Permiso de ubicación denegado"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    // Definición de WeatherApiService dentro del fragmento
    interface WeatherApiService {
        @GET("weather")
        fun getCurrentWeather(
            @Query("lat") lat: Double,
            @Query("lon") lon: Double,
            @Query("units") units: String,
            @Query("appid") apiKey: String // Tu clave API aquí
        ): Call<WeatherResponse>
    }

    // Definición de WeatherResponse dentro del fragmento
    data class WeatherResponse(
        val main: Main
    ) {
        data class Main(
            val temp: Double // Temperatura en grados Celsius
        )
    }
}
