package com.kkwakjavacoding.kcalendar.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kkwakjavacoding.kcalendar.activity.FoodCustomActivity
import com.kkwakjavacoding.kcalendar.activity.IMAGE_MEAN
import com.kkwakjavacoding.kcalendar.activity.IMAGE_STD
import com.kkwakjavacoding.kcalendar.databinding.FragmentKcalendarBinding
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

const val REQUEST_GALLERY = 100
const val REQUEST_CAMERA = 200

class KcalendarFragment : Fragment() {
    private var inputBuffer: ByteBuffer? = null
    private var pixelArray = IntArray(224 * 224)
    private val foods = arrayOf<String>("바나나", "달걀프라이", "버거", "피자", "샌드위치")
    private lateinit var interpreter: Interpreter
    private var predictResult: String? = null

    private var binding: FragmentKcalendarBinding? = null
    private var bitmapImage: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        interpreter = Interpreter(loadModel(), null)
        binding = FragmentKcalendarBinding.inflate(layoutInflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding!!.apply {
            foodAddBtn.setOnClickListener {
                openGallery()
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null // 메모리 누수 방지
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_GALLERY) {
            bitmapImage =
                MediaStore.Images.Media.getBitmap(context?.contentResolver, data?.data)
            predict()
            val intent2 = Intent(activity, FoodCustomActivity::class.java)
            intent2.putExtra("result", predictResult)
            startActivity(intent2)
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CAMERA) {

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

}