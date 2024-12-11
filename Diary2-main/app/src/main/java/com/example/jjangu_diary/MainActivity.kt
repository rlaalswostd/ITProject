package com.example.jjangu_diary

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.jjangu_diary.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth // Firebase 인증 객체(로그인 상태 관리)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /// 기능별 카드 및 버튼 리스너 설정
        //  1. 일기 쓰기 카드
        binding.writeCard.setOnClickListener {
            Toast.makeText(this, "일기 쓰기 이동", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, WriteActivity::class.java)
            startActivity(intent)
        }
        auth = FirebaseAuth.getInstance() // FirebaseAuth 초기화

        // 2. 날씨 카드
        binding.weatherCard.setOnClickListener {
            Toast.makeText(this, "날씨 페이지 이동", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, WeatherActivity::class.java)
            startActivity(intent)
        }
        // 3. 일기 목록 카드
        binding.listCard.setOnClickListener {
            Toast.makeText(this, "일기 목록 이동", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, DiaryDetailActivity::class.java)
            startActivity(intent)
        }

        // 4. 로그아웃 버튼 클릭 시
        binding.logoutButton.setOnClickListener {
            logout()
        }
    }

    // 로그아웃 처리
    private fun logout() {
        auth.signOut() // Firebase 인증 로그아웃

        // Google 인증 해제
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInClient.revokeAccess().addOnCompleteListener {
            Toast.makeText(this, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show()

            // 로그인 화면으로 이동
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
// 메인 화면에서 일기 쓰기, 날씨 확인, 일기 목록 조회, 그리고 로그아웃 기능
