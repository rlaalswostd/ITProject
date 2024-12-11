package com.example.jjangu_diary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.jjangu_diary.Weather.RetrofitInstance
import com.example.jjangu_diary.Weather.WeatherResponse
import com.example.jjangu_diary.databinding.ActivityWeatherBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeatherActivity : AppCompatActivity() {
    private lateinit var writeButton: Button
    private lateinit var database: DatabaseReference // Firebase Database Reference

    // OpenWeatherMap API 키
    private val apiKey = "278cfcf17c8946df995faddc5b1c5688"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Firebase 초기화
        database = FirebaseDatabase.getInstance().reference

        // UI 요소 참조
        val locationEditText: EditText = binding.locationEditText
        val searchButton: Button = binding.searchButton
        val weatherTextView: TextView = binding.weatherTextView
        val imageViewBack: ImageView = binding.imageViewBack
        writeButton = binding.writeButton // binding을 통해 writeButton 초기화

        // 뒤로가기 버튼 클릭 시 처리
        imageViewBack.setOnClickListener {
            // 현재 Activity 종료로 뒤로 가기
            finish()
        }

        // 검색 버튼 클릭 시 API 호출
        searchButton.setOnClickListener {
            val city = locationEditText.text.toString() // 입력된 지역 이름
            if (city.isNotEmpty()) {
                fetchWeather(city, weatherTextView) // 날씨 API 호출
            } else {
                // 지역 입력이 없을 경우
                Toast.makeText(this, "지역을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // WriteActivity로 넘어갈 버튼 클릭 리스너 (이 버튼은 검색 후에만 보이게 설정)
        writeButton.setOnClickListener {
            val city = locationEditText.text.toString()
            if (city.isNotEmpty()) {
                val intent = Intent(this, WriteActivity::class.java)
                intent.putExtra("cityName", city)
                startActivity(intent) // WriteActivity로 이동
            } else {
                Toast.makeText(this, "지역을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 날씨 정보를 API로부터 가져오기
    private fun fetchWeather(city: String, weatherTextView: TextView) {
        RetrofitInstance.api.getCurrentWeather(city, apiKey)
            .enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                    if (response.isSuccessful) {
                        val weather = response.body()
                        val temp = weather?.main?.temp
                        val description = weather?.weather?.get(0)?.description

                        // TextView에 날씨 정보 표시
                        weatherTextView.text = "지역: $city\n기온: $temp°C\n날씨: $description"

                        // WriteActivity로 넘어가는 버튼을 활성화
                        writeButton.visibility = View.VISIBLE

                        // Firebase에 지역 저장
                        saveCityToFirebase(city)
                    } else {
                        val errorMessage = "올바른 도시를 입력해주세요"
                        Toast.makeText(this@WeatherActivity, errorMessage, Toast.LENGTH_SHORT).show()
                        Log.e("API_ERROR", errorMessage)
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    Toast.makeText(this@WeatherActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("API_ERROR", "onFailure: ${t.message}")
                }
            })
    }

    // 지역 이름을 Firebase에 저장하는 함수
    private fun saveCityToFirebase(city: String) {
        val cityId = database.child("cities").push().key // 새로운 고유 키 생성
        cityId?.let {
            database.child("cities").child(it).setValue(city)
                .addOnSuccessListener {
                    Log.d("FIREBASE", "지역이 성공적으로 저장되었습니다: $city")
                }
                .addOnFailureListener { e ->
                    Log.e("FIREBASE", "지역 저장 실패: ${e.message}")
                }
        }
    }
}
