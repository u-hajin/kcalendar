package com.kkwakjavacoding.kcalendar.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kkwakjavacoding.kcalendar.Dialog
import com.kkwakjavacoding.kcalendar.R
import com.kkwakjavacoding.kcalendar.adapter.RecordAdapter
import com.kkwakjavacoding.kcalendar.databinding.ActivityKcalendarBinding
import com.kkwakjavacoding.kcalendar.firebase.Database
import com.kkwakjavacoding.kcalendar.firebase.Nutrition
import com.kkwakjavacoding.kcalendar.fooddatabase.Food
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
    private val foods = arrayOf("바나나", "달걀프라이", "버거", "피자", "샌드위치")
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

    lateinit var recordAdapter: RecordAdapter
    private val db = Database()
    private val context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKcalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        interpreter = Interpreter(loadModel(), null)

        binding.breakfastBtn.setOnClickListener(ButtonListener())
        binding.lunchBtn.setOnClickListener(ButtonListener())
        binding.dinnerBtn.setOnClickListener(ButtonListener())

        calendarListener()

        val currentDate = Calendar.getInstance().time
        date = SimpleDateFormat("yyyy-MM-dd").format(currentDate)
        today=date

        val beforeDate = Calendar.getInstance()
        beforeDate.add(Calendar.DAY_OF_YEAR,-1)

        var timeToDate = beforeDate.time
        yesterday=SimpleDateFormat("yyyy-MM-dd").format(timeToDate)

        binding.apply {
            foodAddBtn.setOnClickListener {
                openGallery()
            }
        }

        getTotalRecord()
        getGoalRecord()
        setProgressBar()
        initRecyclerView()
        buttonListener()
        imgListener()
    }


    private fun buttonListener() {
        binding.weightBtn.setOnClickListener {
            val dialog = Dialog(this)
            dialog.addWeightDialog()
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

            getRecord()
            getGoalRecord()
            getTotalRecord()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
            super.onActivityResult(requestCode, resultCode, data)
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

        val output = Array(1) { FloatArray(5) }

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
                val foodInfoDialog = Dialog(context)
                foodInfoDialog.showFoodInfoDialog(data)
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
                goal = db.getGoal(date)
                total = db.getTotal(date)
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

    private fun imgListener() {
        binding.GraphImg.setOnClickListener {
            val intent = Intent(this, GraphActivity::class.java)
            startActivity(intent)
        }
        binding.GraphText.setOnClickListener {
            val intent = Intent(this, GraphActivity::class.java)
            startActivity(intent)
        }
    }


}