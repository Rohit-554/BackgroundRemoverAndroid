package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        displayToast("SecondActivityOnStart",this)
    }



    override fun onStart() {
        super.onStart()
        displayToast("SecondOnStart",this)
    }

    override fun onResume() {
        super.onResume()
        displayToast("SecondOnResume",this)
    }
    override fun onPause() {
        super.onPause()
        displayToast("SecondOnPause",this)
    }

    override fun onStop() {
        super.onStop()
        displayToast("SecondOnStop",this)
    }

    override fun onDestroy() {
        super.onDestroy()
        displayToast("SecondOnDestroy",this)
    }


}

fun displayToast(text:String,context: Context){
    val toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
    toast.setGravity(Gravity.TOP, 0, 0)
    toast.show()
}