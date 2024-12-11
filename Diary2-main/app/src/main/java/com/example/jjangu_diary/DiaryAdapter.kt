package com.example.jjangu_diary

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.load.DataSource

// DiaryAdapter 클래스는 RecyclerView의 어댑터로, 일기 항목을 표시하는 데 사용됩니다.
class DiaryAdapter(
    private val diaryList: List<DiaryItem>, // 일기 항목 리스트
    private val onItemLongClick: (Int) -> Unit, // 롱 클릭 이벤트 처리기
    private val onItemClick: (Int) -> Unit // 클릭 이벤트 처리기
) : RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder>() {

    // ViewHolder 클래스 정의
    class DiaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.txtDate) // 날짜 TextView
        val contentTextView: TextView = itemView.findViewById(R.id.txtContent) // 내용 TextView
        val weatherTextView: TextView = itemView.findViewById(R.id.txtWeather) // 날씨 TextView
        val titleTextView: TextView = itemView.findViewById(R.id.txtTitle) // 제목 TextView
        val imageView: ImageView = itemView.findViewById(R.id.txtImage) // 이미지 ImageView
    }

    // ViewHolder를 생성하는 메소드
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        // memo_list 레이아웃을 inflate하여 ViewHolder 생성
        val view = LayoutInflater.from(parent.context).inflate(R.layout.memo_list, parent, false)
        return DiaryViewHolder(view)
    }

    // ViewHolder에 데이터를 바인딩하는 메소드
    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        val diaryItem = diaryList[position] // 현재 항목 가져오기
        holder.dateTextView.text = diaryItem.regdate // 날짜 설정
        holder.contentTextView.text = diaryItem.content // 내용 설정
        holder.weatherTextView.text = diaryItem.weather // 날씨 설정
        holder.titleTextView.text = diaryItem.title // 제목 설정

        val imageUrl = diaryItem.imageUrl // 이미지 URL 가져오기
        Log.d("DiaryAdapter", "Image URL for item at position $position: $imageUrl")

        // 이미지 URL이 비어있지 않으면 Glide를 사용하여 이미지를 로드
        if (imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(imageUrl) // 이미지 로드
                .error(R.drawable.shinchang) // 로드 실패 시 대체 이미지
                .listener(object : RequestListener<Drawable> { // 이미지 로드 리스너
                    override fun onLoadFailed(
                        @Nullable e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e("DiaryAdapter", "Image load failed: ${e?.message}")
                        return false // 리소스 로드 실패 처리
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d("DiaryAdapter", "Image loaded successfully")
                        return false // 리소스 로드 성공 처리
                    }
                })
                .into(holder.imageView) // 이미지View에 로드
        } else {
            // 이미지 URL이 비어있을 경우 기본 이미지를 설정
            Log.d("DiaryAdapter", "Image URL is empty for item at position $position")
            holder.imageView.setImageResource(R.drawable.shinchang) // 기본 이미지
        }

        // 롱 클릭 리스너 설정
        holder.itemView.setOnLongClickListener {
            val position = holder.adapterPosition // 현재 항목의 위치 가져오기
            if (position != RecyclerView.NO_POSITION) { // 위치가 유효한 경우
                onItemLongClick(position) // 롱 클릭 이벤트 호출
            }
            true // 이벤트 처리 완료
        }

        // 클릭 리스너 설정
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition // 현재 항목의 위치 가져오기
            if (position != RecyclerView.NO_POSITION) { // 위치가 유효한 경우
                onItemClick(position) // 클릭 이벤트 호출
            }
        }
    }

    // RecyclerView의 항목 수를 반환하는 메소드
    override fun getItemCount(): Int = diaryList.size // 일기 항목 리스트의 크기 반환
}
