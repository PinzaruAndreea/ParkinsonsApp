package com.example.audiorecorderparkinsonapp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.*
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.room.Room
import com.example.audiorecorderparkinsonapp.database.AppDatabase
import com.example.audiorecorderparkinsonapp.database.AudioRecord
import com.example.audiorecorderparkinsonapp.databinding.ActivityHomeBinding
import com.example.audiorecorderparkinsonapp.databinding.ActivityMainBinding
import com.example.audiorecorderparkinsonapp.timer.Timer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
import java.text.SimpleDateFormat
import java.util.*

const val REQUEST_CODE = 200

class MainActivity : AppCompatActivity(), Timer.OnTimerTickListener {

    private lateinit var amplitudes: ArrayList<Float>
    private lateinit var binding: ActivityMainBinding
    private lateinit var homeBinding: ActivityHomeBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private var permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO)
    private var permissionGranted = false

    private var isMainContentVisible = false

    private lateinit var recorder: MediaRecorder
    private var dirPath = ""
    private var fileName = ""
    private var isRecording = false
    private var isPaused = false

    private var duration = ""

    private lateinit var vibrator: Vibrator

    private lateinit var timer: Timer

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomSheet.bottomSheet.visibility = View.GONE
        binding.bottomSheetBG.visibility = View.GONE

        permissionGranted = ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED
        if (!permissionGranted)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)

        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "audioRecords"
        ).build()

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.bottomSheet)
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED


        timer = Timer(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        binding.btnRecord.setOnClickListener {
            when{
                isPaused -> resumeRecording()
                isRecording -> pauseRecorder()
                else -> startRecording()
            }
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }

        binding.btnList.setOnClickListener {
            startActivity(Intent(this, GalleryActivity::class.java))
        }

        binding.btnDone.setOnClickListener {
            stopRecording()
            Toast.makeText(this, "Record saved", Toast.LENGTH_LONG).show()

            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            binding.bottomSheetBG.visibility = View.VISIBLE
            binding.bottomSheet.fileNameInput.setText(fileName)
        }

        binding.bottomSheet.btnCancel.setOnClickListener {
            File("$dirPath$fileName.mp3").delete()
            dismiss()
        }

        binding.bottomSheet.btnOk.setOnClickListener {
            dismiss()
            save()
        }

        binding.bottomSheetBG.setOnClickListener {
            File("$dirPath$fileName.mp3").delete()
            dismiss()
        }

        binding.btnDelete.setOnClickListener {
            stopRecording()
            File("$dirPath$fileName.mp3").delete()
            Toast.makeText(this, "Record deleted", Toast.LENGTH_LONG).show()
        }

        binding.btnDelete.isClickable = false
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun dismiss() {
        binding.bottomSheetBG.visibility = View.GONE
        hideKeyboard(binding.bottomSheet.fileNameInput)

        Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }, 100)
    }

    private fun save() {
        val newFileName = binding.bottomSheet.fileNameInput.text.toString()
        if (newFileName != fileName) {
            var newFile = File("$dirPath$newFileName.mp3")
            File("$dirPath$fileName.mp3").renameTo(newFile)
        }

        var filePath = "$dirPath$newFileName.mp3"
        var timeStamp = Date().time
        var ampsPath = "$dirPath$newFileName"

        try {
            var fos = FileOutputStream(ampsPath)
            var out = ObjectOutputStream(fos)
            out.writeObject(amplitudes)
            fos.close()
            out.close()
        } catch (_: IOException) {}

        var record = AudioRecord(newFileName, filePath, timeStamp, duration, ampsPath)

        GlobalScope.launch {
            db.audioRecordDAO().insert(record)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE)
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    private fun pauseRecorder() {
        recorder.pause()
        isPaused = true
        binding.btnRecord.setImageResource(R.drawable.ic_record)
        timer.pause()
    }

    private fun resumeRecording() {
        recorder.resume()
        isPaused = false
        binding.btnRecord.setImageResource(R.drawable.ic_pause)
        timer.start()
    }

    private fun stopRecording() {
        timer.stop()

        recorder.apply {
            stop()
            release()
        }

        isPaused = false
        isRecording = false

        binding.btnList.visibility = View.VISIBLE
        binding.btnDone.visibility = View.GONE

        binding.btnDelete.isClickable = false
        binding.btnDelete.setImageResource(R.drawable.ic_delete_disabled)

        binding.btnRecord.setImageResource(R.drawable.ic_record)
        binding.timer.text = getString(R.string.start_timer)
        amplitudes = binding.waveformView.clear()
    }

    private fun startRecording() {
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
            return
        }
        // start recording
        recorder = MediaRecorder()
        dirPath = "${externalCacheDir?.absolutePath}/"

        val simpleDateFormat = SimpleDateFormat("yyyy.MM.DD_hh.mm.ss")
        val date = simpleDateFormat.format(Date())
        fileName = "audio_record_$date"

        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile("$dirPath$fileName.mp3")

            try {
                prepare()
            } catch (_: IOException) {}
            start()
        }

        binding.btnRecord.setImageResource(R.drawable.ic_pause)
        isRecording = true
        isPaused = false
        timer.start()

        binding.btnDelete.isClickable = true
        binding.btnDelete.setImageResource(R.drawable.ic_delete)

        binding.btnList.visibility = View.GONE
        binding.btnDone.visibility = View.VISIBLE
    }

    override fun onTimerTick(duration: String) {
        binding.timer.text = duration
        this.duration = duration.dropLast(3)
        binding.waveformView.addAmplitude(recorder.maxAmplitude.toFloat())
    }
}