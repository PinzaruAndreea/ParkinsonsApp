package com.example.audiorecorderparkinsonapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.audiorecorderparkinsonapp.adapter.Adapter
import com.example.audiorecorderparkinsonapp.database.AppDatabase
import com.example.audiorecorderparkinsonapp.database.AudioRecord
import com.example.audiorecorderparkinsonapp.interfaces.OnItemClickListener
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GalleryActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var records: ArrayList<AudioRecord>
    private lateinit var mAdapter: Adapter
    private lateinit var db: AppDatabase

    private lateinit var editBar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        records = ArrayList()

        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "audioRecords"
        ).build()

        mAdapter = Adapter(records, this)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)

        recyclerView.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(context)
        }

        fetchAll()
    }

    private fun fetchAll() {
        GlobalScope.launch {
            val queryResult = db.audioRecordDAO().getAll()
            records.clear()
            records.addAll(queryResult)

            runOnUiThread {
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onItemClickListener(position: Int) {
        var audioRecord = records[position]
        var intent = Intent(this, AudioPlayerActivity::class.java)

        intent.putExtra("filepath", audioRecord.filePath)
        intent.putExtra("filename", audioRecord.fileName)
        startActivity(intent)
    }

    override fun onItemLongClickListener(position: Int) {
        Toast.makeText(this, "Long click", Toast.LENGTH_LONG).show()
    }
}