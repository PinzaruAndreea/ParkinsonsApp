package com.example.audiorecorderparkinsonapp

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    private lateinit var ddkAnalysis: ArrayList<Button>
    private lateinit var words: ArrayList<Button>
    private lateinit var switchText: ImageView

    private lateinit var ddkAnalysisLabel: TextView
    private lateinit var wordsLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

//        ddkAnalysisLabel = findViewById(R.id.textView2)
//        wordsLabel = findViewById(R.id.textView3)
//
//        switchText = findViewById(R.id.arrowIcon)
//
//        ddkAnalysis = ArrayList<Button>().apply {
//            add(findViewById(R.id.button))
//            add(findViewById(R.id.button2))
//            add(findViewById(R.id.button3))
//            add(findViewById(R.id.button4))
//            add(findViewById(R.id.button5))
//        }
//
//        words = ArrayList()
//
//        val rootView = findViewById<ViewGroup>(android.R.id.content)
//        for (i in 0 until rootView.childCount) {
//            val childView = rootView.getChildAt(i)
//            if (childView is Button && !ddkAnalysis.contains(childView)) {
//                words.add(childView)
//            }
//        }
//
//        switchText.setOnClickListener {
//            switchHomePageViews()
//        }

    }

    private fun switchHomePageViews() {
        if (ddkAnalysis[0].visibility == View.VISIBLE) {
            for (button in ddkAnalysis) {
                button.visibility = View.GONE
            }
            ddkAnalysisLabel.visibility = View.GONE

            for (wordsBtn in words) {
                wordsBtn.visibility = View.VISIBLE
            }
            wordsLabel.visibility = View.VISIBLE
        } else {
            for (button in ddkAnalysis) {
                button.visibility = View.VISIBLE
            }
            ddkAnalysisLabel.visibility = View.VISIBLE

            for (wordsBtn in words) {
                wordsBtn.visibility = View.GONE
            }
            wordsLabel.visibility = View.GONE
        }
    }
}