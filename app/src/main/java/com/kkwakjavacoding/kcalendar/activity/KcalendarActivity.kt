package com.kkwakjavacoding.kcalendar.activity

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kkwakjavacoding.kcalendar.Dialog
import com.kkwakjavacoding.kcalendar.R
import com.kkwakjavacoding.kcalendar.adapter.RecordAdapter
import com.kkwakjavacoding.kcalendar.databinding.ActivityKcalendarBinding
import com.kkwakjavacoding.kcalendar.firebase.Database
import com.kkwakjavacoding.kcalendar.firebase.Nutrition
import com.kkwakjavacoding.kcalendar.fooddatabase.Food
import com.kkwakjavacoding.kcalendar.weightdatabase.Weight
import com.kkwakjavacoding.kcalendar.weightdatabase.WeightViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

const val REQUEST_GALLERY = 100
const val REQUEST_CAMERA = 200
const val IMAGE_MEAN = 127.5f
const val IMAGE_STD = 127.5f
const val BREAKFAST = "breakfast"
const val LUNCH = "lunch"
const val DINNER = "dinner"

class KcalendarActivity : AppCompatActivity() {
    private var inputBuffer: ByteBuffer? = null
    private var pixelArray = IntArray(224 * 224)
    private val foods = arrayOf(
        "갈비구이",
        "계란프라이",
        "김밥",
        "닭볶음탕",
        "두부김치",
        "떡갈비",
        "라면",
        "라볶이",
        "만두",
        "물냉면",
        "미역국",
        "바나나",
        "배추김치",
        "불고기",
        "비빔냉면",
        "비빔밥",
        "삼겹살",
        "샌드위치",
        "수제비",
        "순대",
        "순두부찌개",
        "알밥",
        "어묵볶음",
        "오이소박이",
        "오징어튀김",
        "유부초밥",
        "육개장",
        "잔치국수",
        "잡채",
        "전복죽",
        "제육볶음",
        "족발",
        "주꾸미볶음",
        "주먹밥",
        "짜장면",
        "짬뽕",
        "쫄면",
        "찜닭",
        "칼국수",
        "콩국수",
        "콩나물국",
        "피자",
        "햄버거",
        "호박전",
        "훈제오리",
    )
    private lateinit var interpreter: Interpreter
    private var predictResult: String? = null

    private lateinit var binding: ActivityKcalendarBinding
    private var bitmapImage: Bitmap? = null

    private var year = 0
    private var month = 0
    private var day = 0
    private var date = ""
    private var time = BREAKFAST
    private var yesterday = ""
    private var installDate = ""
    private var today = ""
    private var weightFlag: Boolean = false
    private var graphMonth = 0


    private lateinit var weightViewModel: WeightViewModel
    private lateinit var recordAdapter: RecordAdapter
    private val db = Database()
    private val context = this

    var message = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKcalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        weightViewModel = ViewModelProvider(
            this,
            WeightViewModel.Factory(application)
        )[WeightViewModel::class.java]

        getInstallDate()
        setMinMaxDate()

        interpreter = Interpreter(loadModel(), null)

        binding.breakfastBtn.setOnClickListener(ButtonListener())
        binding.lunchBtn.setOnClickListener(ButtonListener())
        binding.dinnerBtn.setOnClickListener(ButtonListener())

        calendarListener()
        imgListener()

        val currentDate = Calendar.getInstance().time

        date = SimpleDateFormat("yyyy-MM-dd").format(currentDate)
        today = date
        graphMonth = SimpleDateFormat("M").format(currentDate).toInt()

        val beforeDate = Calendar.getInstance()
        beforeDate.add(Calendar.DAY_OF_YEAR, -1)

        var timeToDate = beforeDate.time
        yesterday = SimpleDateFormat("yyyy-MM-dd").format(timeToDate)

        checkWeightExists()
        foodAddButtonListener()

        MainScope().launch {
            withContext(Dispatchers.Default) {
                db.initGoal(yesterday, today)
            }
            getTotalRecord()
            getGoalRecord()
            setProgressBar()
        }
        initRecyclerView()
        buttonListener()
    }

    private fun firstWeightInput() {
        if (installDate == today && !weightFlag) {
            val dialog = Dialog(this, today, owner = this, application)
            dialog.showWeightInputDialog(true)
        }
    }

    private fun foodAddButtonListener() {

        val beforeDate = Calendar.getInstance()
        beforeDate.add(Calendar.DAY_OF_YEAR, -1)

        var timeToDate = beforeDate.time
        yesterday = SimpleDateFormat("yyyy-MM-dd").format(timeToDate)

        binding.apply {
            foodAddBtn.setOnClickListener {
                val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_layout, null)
                val bottomSheetDialog = BottomSheetDialog(this@KcalendarActivity)
                bottomSheetDialog.setContentView(bottomSheetView)
                bottomSheetDialog.show()

                bottomSheetDialog.findViewById<LinearLayout>(R.id.camera_section)
                    ?.setOnClickListener {
                        openCamera()
                    }
                bottomSheetDialog.findViewById<LinearLayout>(R.id.gallery_section)
                    ?.setOnClickListener {
                        openGallery()
                    }
            }
        }
    }

    private fun setMinMaxDate() {
        binding.calendarView.minDate = getInstallDate()
        binding.calendarView.maxDate = Calendar.getInstance().time.time
    }

    private fun getInstallDate(): Long {
        var date = Date(this.packageManager.getPackageInfo(this.packageName, 0).firstInstallTime)
        installDate = SimpleDateFormat("yyyy-MM-dd").format(date)

        return date.time
    }

    private fun initWeight() {
        weightViewModel.searchDate(yesterday).observe(this) {
            if (it.isNotEmpty()) {
                setWeight(it[0].weight.toString())
            }
        }
    }

    private fun checkWeightExists() {
        weightViewModel.searchDate(today).observe(this) {
            weightFlag = it.isNotEmpty()
            firstWeightInput()
            if (it.isEmpty() && installDate != today) {
                setMessage()
                initWeight()
            }
        }
    }

    private fun setWeight(weight: String) {
        weightViewModel.addWeight(Weight(today, weight.toDouble()))
    }

    private fun buttonListener() {

        binding.weightBtn.setOnClickListener {
            val dialog = Dialog(this, today, owner = this, application)
            dialog.showWeightInputDialog()
        }
    }

    private fun imgListener() {
        binding.GraphImg.setOnClickListener {
            val intent = Intent(this, GraphActivity::class.java)
            intent.putExtra("date", today)
            intent.putExtra("month", graphMonth)
            startActivity(intent)
        }
        binding.GraphText.setOnClickListener {
            val intent = Intent(this, GraphActivity::class.java)
            intent.putExtra("date", today)
            intent.putExtra("month", graphMonth)
            startActivity(intent)
        }
    }

    inner class ButtonListener : View.OnClickListener {
        override fun onClick(v: View?) {
            when (v?.id) {
                binding.breakfastBtn.id -> {
                    time = BREAKFAST
                    binding.breakfastBtn.setBackgroundResource(R.drawable.left_round_selected)
                    binding.lunchBtn.setBackgroundResource(R.color.pastel_green)
                    binding.dinnerBtn.setBackgroundResource(R.drawable.right_round)
                    getRecord()
                }
                binding.lunchBtn.id -> {
                    time = LUNCH
                    binding.breakfastBtn.setBackgroundResource(R.drawable.left_round)
                    binding.lunchBtn.setBackgroundResource(R.color.deep_green)
                    binding.dinnerBtn.setBackgroundResource(R.drawable.right_round)
                    getRecord()
                }
                binding.dinnerBtn.id -> {
                    time = DINNER
                    binding.breakfastBtn.setBackgroundResource(R.drawable.left_round)
                    binding.lunchBtn.setBackgroundResource(R.color.pastel_green)
                    binding.dinnerBtn.setBackgroundResource(R.drawable.right_round_selected)
                    getRecord()
                }
            }
        }
    }

    private fun calendarListener() {
        binding.calendarView.setOnDateChangeListener { _, i, i2, i3 ->
            year = i
            month = i2 + 1
            day = i3
            convertDateFormat()

            time = BREAKFAST
            binding.breakfastBtn.setBackgroundResource(R.drawable.left_round_selected)
            binding.lunchBtn.setBackgroundResource(R.color.pastel_green)
            binding.dinnerBtn.setBackgroundResource(R.drawable.right_round)

            getGoalRecord()
            getTotalRecord()
            getRecord()
            setProgressBar()
        }
    }

    private fun convertDateFormat() {
        date = ""
        date += year.toString() + "-" + month.toString().padStart(2, '0') + "-" + day.toString()
            .padStart(2, '0')
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        when {
            (ActivityCompat.checkSelfPermission(
                this@KcalendarActivity,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED) -> {
                startActivityForResult(intent, REQUEST_CAMERA)
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.CAMERA
            ) -> {
                cameraAlertDlg()
            }
            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.CAMERA),
                    REQUEST_CAMERA
                )
            }
        }
    }

    private fun cameraAlertDlg() {
        val builder = AlertDialog.Builder(this@KcalendarActivity)
        builder.setMessage("반드시 카메라 권한이 허용 되어야 합니다")
            .setTitle("권한 체크")
            .setPositiveButton("확인") { _, _ ->
                ActivityCompat.requestPermissions(
                    this@KcalendarActivity,
                    arrayOf(android.Manifest.permission.CAMERA),
                    REQUEST_CAMERA
                )
            }
            .setNegativeButton("취소") { dlg, _ ->
                dlg.dismiss()
            }
        val dlg = builder.create()
        dlg.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_GALLERY) {
            bitmapImage =
                MediaStore.Images.Media.getBitmap(contentResolver, data?.data)
            predict()
            val intent2 = Intent(this, FoodCustomActivity::class.java)
            intent2.putExtra("result", predictResult)
            intent2.putExtra("date", date)
            intent2.putExtra("time", time)
            startActivity(intent2)
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CAMERA) {
            bitmapImage =
                data?.extras?.get("data") as Bitmap
            predict()
            val intent2 = Intent(this, FoodCustomActivity::class.java)
            intent2.putExtra("result", predictResult)
            intent2.putExtra("date", date)
            intent2.putExtra("time", time)
            startActivity(intent2)
        }

    }

    private fun loadModel(): ByteBuffer {
        val assetManager = resources.assets
        val assetFileDescriptor = assetManager.openFd("mobilenet/MyMobile.tflite")
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val length = assetFileDescriptor.length

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, length)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap) {
        if (inputBuffer == null) {
            return;
        }
        inputBuffer!!.rewind()

        bitmap.getPixels(pixelArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        var index = 0
        for (i in 0 until 224) {
            for (j in 0 until 224) {
                val value = pixelArray[index++]
                inputBuffer!!.putFloat(((value shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                inputBuffer!!.putFloat(((value shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                inputBuffer!!.putFloat(((value and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
            }
        }
    }

    private fun predict() {
        val inputShape = interpreter.getInputTensor(0).shape()
        val inputWidth = inputShape[1] //224
        val inputHeight = inputShape[2] //224

        val resizedBitmap = Bitmap.createScaledBitmap(bitmapImage!!, inputWidth, inputHeight, true)

        inputBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
        inputBuffer!!.order(ByteOrder.nativeOrder())
        convertBitmapToByteBuffer(resizedBitmap)

        val output = Array(1) { FloatArray(45) }

        interpreter.run(inputBuffer, output)

        var maxIndex = 0
        var i = 0
        for (item in output[0]) {
            if (output[0][maxIndex] < item) {
                maxIndex = i
            }
            i += 1
        }

        predictResult = foods[maxIndex]
    }

    private fun initRecyclerView() {
        binding.recordList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        recordAdapter = RecordAdapter(ArrayList<Food>())

        recordAdapter.itemClickListener = object : RecordAdapter.OnItemClickListener {
            override fun OnItemClick(data: Food) {
                val foodInfoDialog = Dialog(context, today, this@KcalendarActivity, application)
                var flag = foodInfoDialog.showFoodInfoDialog(date, time, data)
            }

            override fun deleteClick(data: Food, position: Int) {
                recordAdapter.removeItem(position)
                db.deleteFood(date, time, data.name)
                MainScope().launch {
                    var total: Nutrition
                    withContext(Dispatchers.Default) {
                        total = db.getTotal(date)
                    }
                    var newTotal = Nutrition(
                        total.kcal - data.kcal,
                        total.carbs - data.carbs!!,
                        total.protein - data.protein!!,
                        total.fat - data.fat!!,
                        total.sugars - data.sugars!!,
                        total.sodium - data.sodium!!
                    )
                    db.insertTotal(date, newTotal)
                    getTotalRecord()
                    setProgressBar()
                }
            }
        }

        binding.recordList.adapter = recordAdapter
        getRecord()
    }

    private fun getRecord() {
        recordAdapter.items.clear()

        MainScope().launch {
            var foodList: ArrayList<Food>

            withContext(Dispatchers.Default) {
                foodList = db.getFood(date, time)!!
            }

            if (!foodList.isEmpty()) {
                for (i in foodList) {
                    recordAdapter.items.add(i.copy())
                }
            }

            recordAdapter.notifyDataSetChanged()
        }
    }

    private fun getGoalRecord() {
        MainScope().launch {
            var goal: Nutrition

            withContext(Dispatchers.Default) {
                goal = db.getGoal(date)
            }

            binding.apply {
                goalKcalInfo.text = goal.kcal.roundToInt().toString()
                goalCarbsInfo.text = goal.carbs.roundToInt().toString()
                goalProteinInfo.text = goal.protein.roundToInt().toString()
                goalFatInfo.text = goal.fat.roundToInt().toString()
                goalSugarsInfo.text = goal.sugars.roundToInt().toString()
                goalSodiumInfo.text = goal.sodium.roundToInt().toString()
            }
        }
    }

    private fun getTotalRecord() {
        MainScope().launch {
            var total: Nutrition
            withContext(Dispatchers.Default) {
                total = db.getTotal(date)
            }

            binding.apply {
                kcalInfo.text = total.kcal.roundToInt().toString()
                carbsInfo.text = total.carbs.roundToInt().toString()
                proteinInfo.text = total.protein.roundToInt().toString()
                fatInfo.text = total.fat.roundToInt().toString()
                sugarsInfo.text = total.sugars.roundToInt().toString()
                binding.sodiumInfo.text = total.sodium.roundToInt().toString()
            }
        }
    }

    private fun setProgressBar() {
        MainScope().launch {
            var goal: Nutrition
            var total: Nutrition
            withContext(Dispatchers.Default) {
                total = db.getTotal(date)
                goal = db.getGoal(date)!!
            }
            binding.apply {
                kcalBar.progress =
                    (total.kcal / goal.kcal).times(100).toInt()
                carbsBar.progress =
                    (total.carbs / goal.carbs).times(100).toInt()
                proteinBar.progress =
                    (total.protein / goal.protein).times(100).toInt()
                fatBar.progress =
                    (total.fat / goal.fat).times(100).toInt()
                sugarsBar.progress =
                    (total.sugars / goal.sugars).times(100).toInt()
                sodiumBar.progress =
                    (total.sodium / goal.sodium).times(100).toInt()

            }

        }
    }

    private fun setMessage() {

        MainScope().launch {
            var total: Nutrition
            var goal: Nutrition
            withContext(Dispatchers.Default) {
                total = db.getTotal(yesterday)
                goal = db.getGoal(yesterday)
            }

            var lackList: ArrayList<String> = arrayListOf()
            var fullList: ArrayList<String> = arrayListOf()

            if (total.kcal < goal.kcal) {
                lackList.add("칼로리")
            } else if (total.kcal > goal.kcal) {
                fullList.add("칼로리")
            }
            if (total.carbs < goal.carbs) {
                lackList.add("탄수화물")
            } else if (total.carbs > goal.carbs) {
                fullList.add("탄수화물")
            }
            if (total.protein < goal.protein) {
                lackList.add("단백질")
            } else if (total.protein > goal.protein) {
                fullList.add("단백질")
            }
            if (total.fat < goal.fat) {
                lackList.add("지방")
            } else if (total.fat > goal.fat) {
                fullList.add("지방")
            }
            if (total.sugars < goal.sugars) {
                lackList.add("당류")
            } else if (total.sugars > goal.sugars) {
                fullList.add("당류")
            }
            if (total.sodium < goal.sodium) {
                lackList.add("나트륨")
            } else if (total.sodium > goal.sodium) {
                fullList.add("나트륨")
            }

            if (lackList.isNotEmpty()) {
                message += "\n부족: "
                message += lackList.joinToString(separator = ", ")
            }

            if (fullList.isNotEmpty()) {
                message += "\n과잉: "
                message += fullList.joinToString(separator = ", ")
            }

            setNotification()
        }
    }

    private fun setNotification() {

        val id = "MyChannel"
        val name = "TimeCheckChannel"
        val notificationChannel =
            NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT)
        notificationChannel.enableVibration(true)
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.BLUE
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        val builder = NotificationCompat.Builder(this, id)
            .setSmallIcon(R.drawable.ic_outline_event_note_24)
            .setContentTitle("어제의 기록!")
            .setContentText(message)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        val intent = Intent(this, KcalendarActivity::class.java)
        intent.putExtra("time", message)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

        val pendingIntent =
            PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        builder.setContentIntent(pendingIntent)

        val notification = builder.build()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.createNotificationChannel(notificationChannel)
        manager.notify(10, notification)

    }
}