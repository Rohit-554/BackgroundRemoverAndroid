package com.example.myapplication

import android.content.Intent
import android.graphics.*
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc


class MainActivity : AppCompatActivity() {
    private var imageview: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        OpenCVLoader.initDebug();
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Toast.makeText(this, "ActivityCreated", Toast.LENGTH_SHORT).show()
        val navigate = findViewById<TextView>(R.id.secondActivity)
        navigate.setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
        }
        imageview = findViewById(R.id.test)

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.rohit)
        removeMultipleColors(bitmap)
//        removeBackground2(bitmap)
//        opencvRemovebg(bitmap)
//        removebackground()
//        getreducedcolor(bitmap,.5f)
//        reduceAlpha(bitmap, 0.5f)
//        reduceAlphaForColor(bitmap,4201728,0.5f)
    }


    fun removebackground(){

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.tim)
        val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        val paint = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        }
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
        imageview?.setImageBitmap(resultBitmap)

    }
    fun getcolors(bitmap: Bitmap,factor:Float) {
        val colors = mutableListOf<Pair<Int, Pair<Int, Int>>>()
        var color = 0
        val reducedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(reducedBitmap)
        var hexColor: String
        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                val pixel = bitmap.getPixel(x, y)
                color = Color.red(pixel) shl 16 or (Color.green(pixel) shl 8) or Color.blue(pixel)
                val alpha = (Color.alpha(pixel) * factor).toInt()
                val colorx = Color.argb(alpha, Color.red(pixel), Color.green(pixel), Color.blue(pixel))
                hexColor = Integer.toHexString(color) // assign value to hexColor
                colors.add(Pair(color, x to y))
                reducedBitmap.setPixel(x, y, colorx)
            }
        }
        imageview?.setImageBitmap(reducedBitmap)

        //if the color hex is different for the same coordinates then group them
        val uniqueColors = colors.groupByTo(mutableMapOf()) { entry ->
            entry.first // use color hex value as key
        }.mapValues { entry ->
            entry.value.map { it.second } // extract only the coordinates from the group
        }

        Log.d("uniqueColors", uniqueColors.toString())


    }

    fun opencvRemovebg(bitmap: Bitmap){
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY)
        Utils.matToBitmap(mat, bitmap)
        imageview?.setImageBitmap(bitmap)

    }

    fun removeBackground(bitmap: Bitmap){
        // Convert the input bitmap to grayscale
        val grayscaleBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(grayscaleBitmap)
        val paint = Paint()
        val cm = ColorMatrix(floatArrayOf(
            0.299f, 0.587f, 0.114f, 0f, 0f,
            0.299f, 0.587f, 0.114f, 0f, 0f,
            0.299f, 0.587f, 0.114f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        // Apply thresholding to create a binary mask
        val threshold = 255 // Choose a threshold value between 0 and 255.
        val binaryBitmap = Bitmap.createBitmap(grayscaleBitmap.width, grayscaleBitmap.height, Bitmap.Config.ARGB_8888)
        for (x in 0 until grayscaleBitmap.width) {
            for (y in 0 until grayscaleBitmap.height) {
                val pixel = grayscaleBitmap.getPixel(x, y)
                val gray = Color.red(pixel)
                val binary = if (gray > threshold) Color.WHITE else Color.BLACK
                binaryBitmap.setPixel(x, y, binary)
            }
        }

        // Extract the foreground pixels using the binary mask
        val outputBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas2 = Canvas(outputBitmap)
        val paint2 = Paint()
        paint2.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        canvas2.drawBitmap(bitmap, 0f, 0f, null)
        canvas2.drawBitmap(binaryBitmap, 0f, 0f, paint2)

        // Return the processed image
        imageview?.setImageBitmap(outputBitmap)
    }

    fun getreducedcolor(bitmap: Bitmap,factor:Float) {
        val colors = mutableListOf<Pair<Int, Pair<Int, Int>>>()
        var color: Int
        var colorx= 0
        val reducedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(reducedBitmap)
        var hexColor: String
        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                val pixel = bitmap.getPixel(x, y)
                color = Color.red(pixel) shl 16 or (Color.green(pixel) shl 8) or Color.blue(pixel)
                reducedBitmap.setPixel(x,y,color)
                if(color == 4267520){
                    val alpha = (Color.alpha(pixel) * factor).toInt()
                    colorx = Color.argb(alpha, Color.red(pixel), Color.green(pixel), Color.blue(pixel))
                    hexColor = Integer.toHexString(color)// assign value to hexColor
                    colors.add(Pair(color, x to y))
                    reducedBitmap.setPixel(x, y, Color.WHITE)
                }
            }
        }
        imageview?.setImageBitmap(reducedBitmap)
    }


    fun reduceAlpha(bitmap: Bitmap, factor: Float) {
        val reducedBitmap =
            Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(reducedBitmap)
        val colors = mutableListOf<Pair<Int, Pair<Int, Int>>>()
        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                val pixel = bitmap.getPixel(x, y)
                val alpha = (Color.alpha(pixel) * factor).toInt()
                val color =
                    Color.argb(alpha, Color.red(pixel), Color.green(pixel), Color.blue(pixel))
                val hexColor = Integer.toHexString(color)
                colors.add(Pair(color, x to y))
                reducedBitmap.setPixel(x, y, color)
            }
        }
        // Do something with the reduced colors list here
//        val uniqueColors = colors.groupByTo(mutableMapOf()) { entry ->
//            entry.first // use color hex value as key
//        }.mapValues { entry ->
//            entry.value.map { it.second } // extract only the coordinates from the group
//        }

//        Log.d("reducedalpha",uniqueColors.toString())
        imageview?.setImageBitmap(reducedBitmap)

    }

    //function to reduce particular pixel
    fun reduceAlphaForColor(bitmap: Bitmap, colorToReduce: Int, factor: Float) {
        val colors = mutableListOf<Pair<Int, Pair<Int, Int>>>()
        val reducedBitmap =
            Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(reducedBitmap)
        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                val pixel = bitmap.getPixel(x, y)
                val alpha = (Color.alpha(pixel) * factor).toInt()
                val color =
                    Color.argb(alpha, Color.red(pixel), Color.green(pixel), Color.blue(pixel))

                reducedBitmap.setPixel(x, y, color)

            }
        }
        // Do something with the modified bitmap here
    }

    fun removeBackground2(bitmap: Bitmap?): Bitmap? {
        //GrabCut part
        var bitmapmodified = bitmap
        val img = Mat()

        Utils.bitmapToMat(bitmapmodified, img)
        val r = img.rows()
        val c = img.cols()
        val p1 = Point(c / 100, r / 100)
        val p2 = Point(c - c / 100, r - r / 100)
        val rect = org.opencv.core.Rect()
        rect.x = p1.x
        rect.y = p1.y
        rect.width = (p2.x - p1.x)
        rect.height = (p2.y - p1.y)


        val mask = Mat()
        val fgdModel = Mat()
        val bgdModel = Mat()
        val imgC3 = Mat()
        Imgproc.cvtColor(img, imgC3, Imgproc.COLOR_RGBA2RGB)
        Imgproc.grabCut(imgC3, mask, rect, bgdModel, fgdModel, 4, Imgproc.GC_INIT_WITH_RECT)
        val source = Mat(1, 1, CvType.CV_8U, Scalar(3.0))
        Core.compare(mask, source /* GC_PR_FGD */, mask, Core.CMP_EQ)

        //This is important. You must use Scalar(255,255, 255,255), not Scalar(255,255,255)
        val foreground = Mat(img.size(), CvType.CV_8UC3, Scalar(255.0, 255.0, 255.0, 255.0))
        img.copyTo(foreground, mask)

        //  convert matrix to output bitmap
        bitmapmodified = Bitmap.createBitmap(
            foreground.size().width.toInt(), foreground.size().height.toInt(),
            Bitmap.Config.ARGB_8888
        )
        Utils.matToBitmap(foreground, bitmap)
        imageview?.setImageBitmap(bitmap)
        return bitmap
    }
    fun removeMultipleColors(bitmap: Bitmap){
        // Convert the bitmap to a 4-channel Mat with an alpha channel
        val image = Mat()
        Utils.bitmapToMat(bitmap, image)
        val rgba = Mat()
        Imgproc.cvtColor(image, rgba, Imgproc.COLOR_BGR2RGBA)

// Apply image processing to create a mask that selects the background
        val mask = Mat()
        Imgproc.cvtColor(rgba, mask, Imgproc.COLOR_RGBA2GRAY)
        Imgproc.threshold(mask, mask, 0.0, 255.0, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU)
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(3.0, 3.0))
        Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_CLOSE, kernel)
        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
        var largestContour = contours[0]
        for (i in 1 until contours.size) {
            if (Imgproc.contourArea(contours[i]) > Imgproc.contourArea(largestContour)) {
                largestContour = contours[i]
            }
        }
        Imgproc.drawContours(mask, listOf(largestContour), 0, Scalar(255.0), -1)

// Copy the processed image into a new Mat with an alpha channel
        val result = Mat(rgba.rows(), rgba.cols(), CvType.CV_8UC4, Scalar(255.0, 255.0, 255.0, 0.0))
        rgba.copyTo(result, mask)

// Convert the resulting Mat to a Bitmap with an alpha channel
        val resultBitmap = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(result, resultBitmap)

// Set the resulting Bitmap to the ImageView
        imageview?.setImageBitmap(resultBitmap)
    }


}