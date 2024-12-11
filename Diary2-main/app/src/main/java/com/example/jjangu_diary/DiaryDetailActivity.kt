package com.example.jjangu_diary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jjangu_diary.databinding.ActivityDiaryDetailBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class DiaryDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDiaryDetailBinding // 데이터 바인딩 객체
    private lateinit var diaryList: MutableList<DiaryItem> // 일기 항목 리스트
    private lateinit var adapter: DiaryAdapter // RecyclerView 어댑터

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // 엣지 투 엣지 모드 활성화
        binding = ActivityDiaryDetailBinding.inflate(layoutInflater) // 바인딩 객체 초기화
        setContentView(binding.root) // 뷰 설정

        // 윈도우 인셋 적용 리스너 설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. 글 쓴 데이터 가져오기
        diaryList = mutableListOf() // 일기 리스트 초기화
        // Firestore에서 데이터 가져오기
        fetchDiaryEntries()


        // 글쓰기 버튼 클릭 시 이벤트 설정
        binding.btnWrite.setOnClickListener {
            val intent = Intent(this, WriteActivity::class.java) // WriteActivity로 이동
            startActivity(intent)
        }

        adapter = DiaryAdapter(diaryList,
            { position -> showDeleteDialog(position) }, // 롱클릭 시 삭제 다이얼로그 표시
            { position -> showEditDialog(position) } // 짧은 클릭 시 수정 다이얼로그 표시
        )

        // RecyclerView 설정
        binding.txtList.layoutManager = LinearLayoutManager(this)
        binding.txtList.adapter = adapter

        // < 기본 구분선 추가 >
        val dividerItemDecoration = DividerItemDecoration(binding.txtList.context, LinearLayoutManager.VERTICAL)
        binding.txtList.addItemDecoration(dividerItemDecoration)

        // 리스트 비었을 때 메시지 표시 함수 호출
        checkListEmpty()

        // btnHome 클릭 리스너 (홈화면으로 이동)
        binding.btnHome.setOnClickListener {
            Toast.makeText(this, "홈화면으로 이동", Toast.LENGTH_SHORT).show()
            finish() // 현재 액티비티 종료하여 MainActivity로 돌아가기
        }
    }

    // Firestore에서 일기 항목 가져오기
    private fun fetchDiaryEntries() {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("diaryEntries")
            .orderBy("regdate", Query.Direction.ASCENDING) // 작성 순서로 정렬
            .get()
            .addOnSuccessListener { documents ->
                diaryList.clear() // 기존 데이터 삭제
                for (document in documents) {
                    val diaryEntry = document.toObject(DiaryItem::class.java) // Firestore에서 일기 항목 객체로 변환
                    diaryEntry.id = document.id // Firestore 문서 ID 저장
                    diaryList.add(diaryEntry) // 리스트에 추가
                    Log.d("DiaryDetailActivity", "Image URL: ${diaryEntry.imageUrl}") // 이미지 URL 로그 출력
                }
                adapter.notifyDataSetChanged() // 어댑터 갱신
                checkListEmpty() // 리스트 비었을 때 메시지 확인
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "일기 정보를 가져오는 데 실패했습니다: ${exception.message}", Toast.LENGTH_SHORT).show() // 실패 메시지
            }
    }

    // < 삭제 확인 다이얼로그 표시 >
    private fun showDeleteDialog(position: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("메모를 삭제하시겠습니까?")
            .setPositiveButton("확인") { dialog, id ->
                deleteItem(position) // 확인 클릭 시 삭제
            }
            .setNegativeButton("취소") { dialog, id ->
                dialog.dismiss() // 취소 클릭 시 다이얼로그 닫기
            }
            .create()
            .show()
    }

    // < 수정 다이얼로그 표시>
    private fun showEditDialog(position: Int) {
        val diaryItem = diaryList[position] // 수정할 일기 항목 가져오기
        val builder = AlertDialog.Builder(this)

        // 제목과 내용을 수정할 EditText 추가
        val titleEditText = EditText(this).apply {
            setText(diaryItem.title) // 기존 제목 설정
            hint = "제목을 수정하세요" // 힌트 설정
        }

        val contentEditText = EditText(this).apply {
            setText(diaryItem.content) // 기존 내용 설정
            hint = "내용을 수정하세요" // 힌트 설정
        }

        // 수직 레이아웃에 EditText와 TextView 추가
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            // 제목 레이블
            val titleLabel = TextView(this@DiaryDetailActivity).apply {
                text = "제목"
                textSize = 16f // 글자 크기 설정
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            // 내용 레이블
            val contentLabel = TextView(this@DiaryDetailActivity).apply {
                text = "내용"
                textSize = 16f // 글자 크기 설정
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            // 레이아웃에 추가
            addView(titleLabel)
            addView(titleEditText)
            addView(contentLabel)
            addView(contentEditText)
        }

        builder.setTitle("상세 보기") // 다이얼로그 제목
            .setView(layout) // 커스텀 뷰 설정
            .setPositiveButton("확인") { dialog, id ->
                // 사용자가 입력한 제목과 내용을 가져와서 업데이트
                val newTitle = titleEditText.text.toString()
                val newContent = contentEditText.text.toString()
                if (newTitle.isNotEmpty() && newContent.isNotEmpty()) {
                    diaryItem.title = newTitle
                    diaryItem.content = newContent

                    // Firestore에서 업데이트
                    val firestore = FirebaseFirestore.getInstance()
                    val updates = mapOf(
                        "title" to newTitle,
                        "content" to newContent
                    )

                    firestore.collection("diaryEntries").document(diaryItem.id).update(updates)
                        .addOnSuccessListener {
                            adapter.notifyItemChanged(position) // 어댑터 갱신
                            Toast.makeText(this, "메모가 수정되었습니다.", Toast.LENGTH_SHORT).show() // 성공 메시지
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "수정 실패: ${exception.message}", Toast.LENGTH_SHORT).show() // 실패 메시지
                        }
                } else {
                    Toast.makeText(this, "제목과 내용을 입력하세요.", Toast.LENGTH_SHORT).show() // 입력 유효성 검사
                }
            }
            .setNegativeButton("취소") { dialog, id ->
                dialog.dismiss() // 다이얼로그 닫기
            }
            .create()
            .show()
    }

    // < 항목 삭제 메소드 >
    private fun deleteItem(position: Int) {
        val diaryItem = diaryList[position] // 삭제할 항목 가져오기

        // Firestore에서 데이터 삭제
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("diaryEntries").document(diaryItem.id).delete()
            .addOnSuccessListener {
                diaryList.removeAt(position) // 리스트에서 삭제
                adapter.notifyItemRemoved(position) // 어댑터 갱신

                // 리스트가 비었을 때 메시지 표시 함수 호출
                checkListEmpty()
                Toast.makeText(this, "메모가 삭제되었습니다.", Toast.LENGTH_SHORT).show() // 성공 메시지
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "삭제 실패: ${exception.message}", Toast.LENGTH_SHORT).show() // 실패 메시지
            }
    }

    //  < 리스트 비었을 때 메시지 표시 함수 >
    private fun checkListEmpty() {
        if (diaryList.isEmpty()) { // 리스트가 비어있는지 확인
            binding.txtList.visibility = View.GONE // 리스트 숨기기
            binding.emptyMessageTextView.visibility = View.VISIBLE // 비어있다는 메시지 표시
            binding.btnWrite.visibility = View.VISIBLE // 글쓰기 버튼 보이기
        } else {
            binding.txtList.visibility = View.VISIBLE // 리스트 표시
            binding.emptyMessageTextView.visibility = View.GONE // 비어있다는 메시지 숨기기
            binding.btnWrite.visibility = View.GONE // 글쓰기 버튼 숨기기
        }
    }
}
