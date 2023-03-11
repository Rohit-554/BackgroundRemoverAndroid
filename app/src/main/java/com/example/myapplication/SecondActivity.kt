package com.example.myapplication

import ConvolutionMatrix
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

import java.nio.IntBuffer


class SecondActivity : AppCompatActivity() {
    private var imageview: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.flowchart)
        imageview = findViewById(R.id.test)


        val converetdImage = getResizedBitmap(bitmap, 500)
        val colorMatrixNegative = floatArrayOf(
            -1.0f, 0f, 0f, 0f, 255f, //red
            0f, -1.0f, 0f, 0f, 255f, //green
            0f, 0f, -1.0f, 0f, 255f, //blue
            0f, 0f, 0f, 1.0f, 0f //alpha
        )
        val colorMatrix = ColorMatrix()
        colorMatrix.set(colorMatrixNegative)

        val colorFilterNegative = ColorMatrixColorFilter(colorMatrixNegative)

        val matrix = ColorMatrix()
        matrix.setSaturation(0F)
        val filter = ColorMatrixColorFilter(matrix)
        imageview?.colorFilter = filter


        imageview?.setImageBitmap(converetdImage)

        val bm: Bitmap = (imageview?.drawable as BitmapDrawable).bitmap

        val colorMatrixConcat = ColorMatrix()
        colorMatrixConcat.setConcat(matrix, colorMatrix)
        val filter1 = ColorMatrixColorFilter(colorMatrixConcat)
        imageview?.colorFilter = filter1
        imageview?.setImageBitmap(applyGaussianBlur(converetdImage));
        val cm = (imageview?.drawable as BitmapDrawable).bitmap

        val result = ColorDodgeBlend(cm, bm)
        imageview?.colorFilter = filter

        imageview?.setImageBitmap(result)

    }
    private fun colordodge(in1: Int, in2: Int): Int {
        val image = in2.toFloat()
        val mask = in1.toFloat()
        return (if (image == 255f) image else 255f.coerceAtMost((mask.toLong() shl 8) / (255 - image))).toInt()
    }

    fun ColorDodgeBlend(source: Bitmap, layer: Bitmap): Bitmap {
        val base = source.copy(Bitmap.Config.ARGB_8888, true)
        val blend = layer.copy(Bitmap.Config.ARGB_8888, false)
        val buffBase = IntBuffer.allocate(base.width * base.height)
        base.copyPixelsToBuffer(buffBase)
        buffBase.rewind()
        val buffBlend = IntBuffer.allocate(blend.width * blend.height)
        blend.copyPixelsToBuffer(buffBlend)
        buffBlend.rewind()
        val buffOut = IntBuffer.allocate(base.width * base.height)
        buffOut.rewind()
        while (buffOut.position() < buffOut.limit()) {
            val filterInt = buffBlend.get()
            val srcInt = buffBase.get()
            val redValueFilter = Color.red(filterInt)
            val greenValueFilter = Color.green(filterInt)
            val blueValueFilter = Color.blue(filterInt)
            val redValueSrc = Color.red(srcInt)
            val greenValueSrc = Color.green(srcInt)
            val blueValueSrc = Color.blue(srcInt)
            val redValueFinal = colordodge(redValueFilter, redValueSrc)
            val greenValueFinal = colordodge(greenValueFilter, greenValueSrc)
            val blueValueFinal = colordodge(blueValueFilter, blueValueSrc)
            val pixel = Color.argb(255, redValueFinal, greenValueFinal, blueValueFinal)
            buffOut.put(pixel)
        }
        buffOut.rewind()
        base.copyPixelsFromBuffer(buffOut)
        blend.recycle()
        return base
    }

    fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 0) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }
    fun applyGaussianBlur(src: Bitmap): Bitmap {
        val gaussianBlurConfig = arrayOf(
            doubleArrayOf(-1.0, 0.0, -1.0),
            doubleArrayOf(0.0, 4.0, 0.0),
            doubleArrayOf(-1.0, 0.0, -1.0)
        )


        val convMatrix = ConvolutionMatrix(3)

        convMatrix.applyConfig(gaussianBlurConfig)
        convMatrix.Factor = 1.0
        convMatrix.Offset = 150.0

        return convMatrix.computeConvolution3x3(src, convMatrix)
    }
}