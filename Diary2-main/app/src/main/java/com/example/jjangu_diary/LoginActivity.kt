package com.example.jjangu_diary

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.jjangu_diary.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    /// 객체 초기화
    // 뷰 바인딩 객체 초기화(xml 레이아웃 UI 접근)
    private lateinit var binding: ActivityLoginBinding

    // Google Sign-In 클라이언트 초기화(로그인 요청 관리)
    private lateinit var googleSignInClient: GoogleSignInClient

    // Firebase 인증 객체 초기화(사용자의 인증 상태 관리)
    private lateinit var firebaseAuth: FirebaseAuth

    // (Google)요청 코드 상수 정의
    companion object {
        private const val RC_SIGN_IN = 9001 // Google Sign-In 요청 코드
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // 레이아웃(상태,네비 바 없는 화면) 활성화

        // 액티비티 뷰 바인딩 설정
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 시스템 바 인셋을 설정(기기마다 다른 상태 바, 내비게이션 바의 크기만큼 여백을 자동으로 조절)하여 레이아웃에 적용
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // FirebaseAuth 인스턴스 초기화
        firebaseAuth = FirebaseAuth.getInstance()

        // Google Sign-In 옵션 설정
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // 웹 클라이언트 ID 요청
            .requestEmail() // 이메일 요청
            .build()

        // Google Sign-In 클라이언트 초기화
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Google 로그인 버튼 클릭 시
        binding.imageButton2.setOnClickListener {
            signIn() // signIn 메서드 호출
        }
    }

    // Google 로그인 화면 요청
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent // Google Sign-In 인텐트 생성
        startActivityForResult(signInIntent, RC_SIGN_IN) // 인텐트를 사용하여 로그인 화면 시작
    }

    // 로그인 요청 결과 처리(로그인 일치 여부)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) { // 요청 코드가 로그인 요청 코드(RC_SIGN_IN)와 일치하면
            val task = GoogleSignIn.getSignedInAccountFromIntent(data) // 로그인 결과 가져오기
            handleSignInResult(task) // 결과 처리 메서드 호출
        }
    }

    // 로그인 결과를 처리하는 메서드
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            // 로그인 성공 시 GoogleSignInAccount 객체를 가져옴
            val account = completedTask.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account) // Firebase 인증 호출(로그인)
        } catch (e: ApiException) {
            // 로그인 실패 처리 (예외 발생 시)
            Toast.makeText(this, "로그인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Firebase에 Google 계정으로 로그인하는 메서드
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        // Google 계정에서 인증 자격 증명 생성
        val credential: AuthCredential = GoogleAuthProvider.getCredential(acct.idToken, null)
        // Firebase에 자격 증명으로 로그인
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // 로그인 성공 후 처리
                    val user = firebaseAuth.currentUser // 현재 로그인된 사용자 정보 가져오기

                    // MainActivity로 intent 이동
                    val intent = Intent(this, MainActivity::class.java)
                    // 새 액티비티가 백 스택에 쌓이지 않도록 플래그 설정
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent) // MainActivity 시작
                    finish() // 현재 액티비티 종료
                } else {
                    // 로그인 실패 처리
                    Toast.makeText(this, "로그인 실패: ${task.exception?.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }
    //  1. Google 로그인 버튼을 클릭했을 때 Google 계정으로 인증을 수행
    //  2. 로그인 인증이 성공하면 MainActivity로 이동
}
