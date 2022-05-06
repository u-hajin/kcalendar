package com.kkwakjavacoding.kcalendar.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kkwakjavacoding.kcalendar.databinding.FragmentKcalendarBinding

const val REQUEST_GALLERY = 100
const val REQUEST_CAMERA = 200

class KcalendarFragment : Fragment() {

    var binding: FragmentKcalendarBinding? = null
    private var bitmapImage: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
            bitmapImage = MediaStore.Images.Media.getBitmap(context?.contentResolver, data?.data)
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CAMERA) {

        }
    }

}