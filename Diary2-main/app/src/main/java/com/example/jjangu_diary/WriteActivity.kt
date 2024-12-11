package com.example.jjangu_diary

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.jjangu_diary.databinding.ActivityWriteBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WriteActivity : AppCompatActivity() {
    private val firestore = FirebaseFirestore.getInstance() // Firestore 인스턴스
    private lateinit var binding: ActivityWriteBinding // 데이터 바인딩 변수
    private val PICK_IMAGE = 1 // 이미지 선택 요청 코드
    private var selectedImageBitmap: Bitmap? = null // 선택된 이미지 비트맵
//    private lateinit var cityName: String
    private var cityName: String? = null

    // 상태를 저장할 변수
    private var savedTitle: String? = null
    private var savedContent: String? = null
    private var savedWeather: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // 엣지 투 엣지 모드 활성화
        binding = ActivityWriteBinding.inflate(layoutInflater) // 바인딩 객체 초기화
        setContentView(binding.root) // 뷰 설정

        // 윈도우 인셋 적용 리스너 설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 이전 상태가 존재하는 경우 복원
        savedTitle = savedInstanceState?.getString("title")
        savedContent = savedInstanceState?.getString("content")
        savedWeather = savedInstanceState?.getString("weather")

        // 복원된 값이 있으면 EditText와 TextView에 설정
        binding.editTitle.setText(savedTitle)
        binding.editContent.setText(savedContent)
        binding.textViewWeather.text = savedWeather

        // 이미지 첨부 버튼 클릭 리스너
        binding.btnImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*" // 모든 이미지 타입 허용
            intent.action = Intent.ACTION_GET_CONTENT // 콘텐츠 선택 액션
            startActivityForResult(
                Intent.createChooser(intent, "이미지 선택"),
                PICK_IMAGE
            ) // 이미지 선택 창 띄우기
        }

        // 저장 버튼 클릭 리스너
        binding.btnSave.setOnClickListener {
            saveDiaryEntry() // 일기 저장 메소드 호출
        }

// btnHome 클릭 리스너 (홈화면으로 이동)
        binding.btnHome1.setOnClickListener {
            Toast.makeText(this, "홈화면으로 이동", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java) // MainActivity로 이동하는 Intent 생성
            startActivity(intent) // MainActivity 시작
            finish() // 현재 액티비티 종료
        }

        // 전달받은 도시 이름
        val receivedCityName = intent.getStringExtra("cityName")
        if (!receivedCityName.isNullOrEmpty()) {
            cityName = receivedCityName
            fetchWeather(cityName!!) // cityName이 null이 아님을 보장
        } else {
            binding.textViewWeather.text = "도시 이름이 유효하지 않습니다." // 기본 메시지
        }

//        fetchWeather(cityName) // 도시 이름으로 날씨 정보 호출
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 현재 상태를 저장
        outState.putString("title", binding.editTitle.text.toString())
        outState.putString("content", binding.editContent.text.toString())
        outState.putString("weather", binding.textViewWeather.text.toString())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data // 선택된 이미지 URI 가져오기
            try {
                val inputStream: InputStream? =
                    selectedImageUri?.let { contentResolver.openInputStream(it) } // URI로부터 InputStream 생성
                selectedImageBitmap = BitmapFactory.decodeStream(inputStream) // 비트맵으로 변환
                binding.imageView.setImageBitmap(selectedImageBitmap) // 이미지 뷰에 비트맵 설정
                binding.imageView.visibility = View.VISIBLE // 이미지 뷰 표시
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "이미지 선택 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show() // 오류 메시지
            }
        }
    }

    //// fetchWeather 함수를 수정하여 특정 지역 이름을 받아 API 호출
    private fun fetchWeather(city: String) {
        val apiKey = "976ed3e000d4b8edd015eca6706f8984"
        val url =
            "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=metric"

        val queue: RequestQueue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            Response.Listener<JSONObject> { response ->
                try {
                    val weatherDescription = response.getJSONArray("weather").getJSONObject(0)
                        .getString("description")
                    val temperature = response.getJSONObject("main").getDouble("temp")
                    val cityName = response.getString("name")
                    val countryCode = response.getJSONObject("sys").getString("country")

                    // 텍스트뷰에 날씨 정보 설정
                    binding.textViewWeather.text =
                        "도시: $cityName, 국가: $countryCode\n날씨: $weatherDescription\n온도: $temperature °C"
                } catch (e: JSONException) {
                    e.printStackTrace()
                    binding.textViewWeather.text = "날씨 정보를 처리하는 데 오류가 발생했습니다."
                }
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
                binding.textViewWeather.text = "날씨 정보를 가져오는 데 실패했습니다: ${error.message}"

                if (error.networkResponse != null) {
                    val statusCode = error.networkResponse.statusCode
                    binding.textViewWeather.append("\nHTTP 상태 코드: $statusCode")
                }
            }
        )

        queue.add(jsonObjectRequest)
    }

    private fun saveDiaryEntry() {
        val title = binding.editTitle.text.toString() // 제목
        val content = binding.editContent.text.toString() // 내용
        val weatherInfo = binding.textViewWeather.text.toString() // 날씨 정보

        // 현재 날짜와 시간을 "yyyy-MM-dd HH:mm:ss" 형식으로 가져오기
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = java.util.TimeZone.getTimeZone("Asia/Seoul") // 시간대 설정
        val date = dateFormat.format(Date()) // 현재 날짜 포맷팅

        // Firestore에 데이터 저장
        val diaryEntry = hashMapOf(
            "regdate" to date,
            "title" to title,
            "content" to content,
            "weather" to weatherInfo
        )

        // 이미지가 선택되었을 때만 Firebase Storage에 업로드
        if (selectedImageBitmap != null) {
            val storageRef =
                FirebaseStorage.getInstance().reference.child("images/${System.currentTimeMillis()}.jpg") // 이미지 저장 경로
            val baos = ByteArrayOutputStream()
            selectedImageBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos) // 비트맵을 JPEG로 압축
            val data = baos.toByteArray() // 바이트 배열로 변환

            // Firebase Storage에 이미지 업로드
            storageRef.putBytes(data)
                .addOnSuccessListener {
                    // 업로드 성공 시, 다운로드 URL을 가져온다
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        Log.d("WriteActivity", "Image URL: ${uri.toString()}")
                        diaryEntry["imageUrl"] = uri.toString() // 이미지 URL 추가

                        // Firestore에 데이터 저장
                        firestore.collection("diaryEntries")
                            .add(diaryEntry)
                            .addOnSuccessListener {
                                Toast.makeText(this, "일기가 저장되었습니다.", Toast.LENGTH_SHORT)
                                    .show() // 성공 메시지
                                resetFields() // 필드 초기화
                                navigateToListActivity() // 메인 액티비티로 이동
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "저장 실패: ${e.message}", Toast.LENGTH_SHORT)
                                    .show() // 실패 메시지
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "이미지 업로드 실패: ${e.message}", Toast.LENGTH_SHORT)
                        .show() // 실패 메시지
                }
        } else {
            // 이미지가 선택되지 않았을 때 Firestore에 저장
            firestore.collection("diaryEntries")
                .add(diaryEntry)
                .addOnSuccessListener {
                    Toast.makeText(this, "일기가 저장되었습니다.", Toast.LENGTH_SHORT).show() // 성공 메시지
                    resetFields() // 필드 초기화
                    navigateToListActivity() // 메인 액티비티로 이동
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "저장 실패: ${e.message}", Toast.LENGTH_SHORT).show() // 실패 메시지
                }
        }
    }

    private fun resetFields() {
        // 제목, 내용, 날씨, 이미지 초기화
        binding.editTitle.text.clear() // 제목 필드 초기화
        binding.editContent.text.clear() // 내용 필드 초기화
        binding.textViewWeather.text = "" // 날씨 필드 초기화
        binding.imageView.visibility = View.GONE // 이미지 뷰 숨기기
        selectedImageBitmap = null // 선택된 이미지 초기화
    }

    private fun navigateToListActivity() {
        val intent = Intent(this, MainActivity::class.java) // 메인 액티비티로 이동
        startActivity(intent)
    }
}
