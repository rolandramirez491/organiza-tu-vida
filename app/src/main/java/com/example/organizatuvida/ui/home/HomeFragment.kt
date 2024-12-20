package com.example.organizatuvida.ui.home

import android.Manifest
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizatuvida.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var weatherApi: WeatherApiService

    private lateinit var recyclerView: RecyclerView
    private var appUsageList: MutableList<Any> = mutableListOf() // Cambiado a MutableList<Any> para datos mixtos
    private lateinit var adapter: AppUsageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Inicializa cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Inicializa Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherApi = retrofit.create(WeatherApiService::class.java)

        // Llama a la función para obtener la ubicación y el clima
        getLocationAndWeather()

        // Configura RecyclerView y verifica estadísticas de uso
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setupRecyclerView()
            checkAndLoadUsageStats()
        }

        return root
    }

    private fun setupRecyclerView() {
        recyclerView = binding.recyclerApps
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = AppUsageAdapter(appUsageList) // Ahora soporta datos mixtos
        recyclerView.adapter = adapter
    }

    private fun getLocationAndWeather() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude

                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                try {
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val addressLine = addresses[0].getAddressLine(0)
                        binding.gpsInfo.text = "Dirección: $addressLine"
                        getWeatherData(latitude, longitude)
                    } else {
                        binding.gpsInfo.text = "No se pudo obtener la dirección exacta"
                    }
                } catch (e: Exception) {
                    Log.e("Geocoder", "Error al obtener la dirección", e)
                    binding.gpsInfo.text = "Error al obtener la dirección"
                }
            } else {
                binding.gpsInfo.text = "No se pudo obtener la ubicación"
            }
        }.addOnFailureListener {
            binding.gpsInfo.text = "Error al obtener la ubicación"
        }
    }

    private fun getWeatherData(lat: Double, lon: Double) {
        val call = weatherApi.getCurrentWeather(lat, lon, "metric", "dc87f4093412ad3aaf6d863ddc3dacd7")

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val temp = response.body()?.main?.temp
                    binding.temperatureInfo.text = "Temperatura: $temp°C"
                } else {
                    binding.temperatureInfo.text = "Error al obtener la temperatura"
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                binding.temperatureInfo.text = "Error: ${t.message}"
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun checkAndLoadUsageStats() {
        if (hasUsageAccessPermission(requireContext())) {
            loadUsageStats()
        } else {
            binding.textHome.append("\nPor favor, otorga acceso a los datos de uso en la configuración.")
            requestUsageAccessPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun loadUsageStats() {
        lifecycleScope.launch(Dispatchers.IO) {
            val usageStatsManager =
                requireContext().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

            val currentTime = System.currentTimeMillis()

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = currentTime
            calendar.add(Calendar.DAY_OF_YEAR, -7) // Últimos 7 días
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val startTime = calendar.timeInMillis

            val usageStatsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                currentTime
            )

            if (!usageStatsList.isNullOrEmpty()) {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                val groupedData = usageStatsList
                    .mapNotNull {
                        val usageTimeInSeconds = it.totalTimeInForeground / 1000
                        if (usageTimeInSeconds > 60) { // Filtrar apps con uso mayor a 1 minuto
                            val lastUsedDate = dateFormat.format(Date(it.lastTimeUsed))
                            AppUsageInfo(
                                packageName = it.packageName,
                                usageTimeInSeconds = usageTimeInSeconds,
                                lastUsedTime = dateFormat.format(Date(it.lastTimeUsed)),
                                date = lastUsedDate
                            )
                        } else {
                            null
                        }
                    }
                    .groupBy { it.date }

                val mixedList = mutableListOf<Any>()
                for ((date, apps) in groupedData) {
                    mixedList.add(date) // Agrega el encabezado (fecha)
                    mixedList.addAll(apps.sortedByDescending { it.usageTimeInSeconds }) // Apps ordenadas
                }

                withContext(Dispatchers.Main) {
                    appUsageList.clear()
                    appUsageList.addAll(mixedList)
                    adapter.notifyDataSetChanged()
                }
            } else {
                withContext(Dispatchers.Main) {
                    binding.textHome.append("\nNo se encontraron estadísticas de uso.")
                }
            }
        }
    }

    private fun hasUsageAccessPermission(context: Context): Boolean {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun requestUsageAccessPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
        binding.textHome.text = "Habilita el acceso a estadísticas de uso en las configuraciones."
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface WeatherApiService {
        @GET("weather")
        fun getCurrentWeather(
            @Query("lat") lat: Double,
            @Query("lon") lon: Double,
            @Query("units") units: String,
            @Query("appid") appid: String
        ): Call<WeatherResponse>
    }

    data class WeatherResponse(val main: Main)
    data class Main(val temp: Double)

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}

data class AppUsageInfo(
    val packageName: String,
    val usageTimeInSeconds: Long,
    val lastUsedTime: String,
    val date: String // Fecha en formato "dd/MM/yyyy"
)
