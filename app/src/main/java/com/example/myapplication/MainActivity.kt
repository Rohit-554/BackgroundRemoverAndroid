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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.GrayscaleTransformation
import jp.wasabeef.glide.transformations.gpu.*
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
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.img_2)

        //opencv
//        removeBackground2(bitmap)

        //Threshold
//        separateForegroundFromBackground(bitmap)


        //Glide
        convertToSketch(bitmap)


        /*
        Another approach
        convrtBitmap(bitmap)
        opencvRemovebg(bitmap)
        removebackground()
        getreducedcolor(bitmap,.5f)
        reduceAlpha(bitmap, 0.5f)
        reduceAlphaForColor(bitmap,4201728,0.5f)
        */

    }



    fun separateForegroundFromBackground(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val grayBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(grayBitmap)
        val paint = Paint()

        // Create a color matrix that converts color to grayscale
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)

        // Apply the color matrix to the paint
        val colorFilter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = colorFilter

        // Draw the bitmap on the canvas using the paint
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        // Apply a Gaussian blur filter to the grayscale image
        val blurredBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val blurRadius = 5f
        val blurFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
        val blurPaint = Paint()
        blurPaint.maskFilter = blurFilter
        val blurCanvas = Canvas(blurredBitmap)
        blurCanvas.drawBitmap(grayBitmap, 0f, 0f, blurPaint)

        //otsu method


        // Compute the histogram of the blurred grayscale image
        val histogram = IntArray(256)
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = blurredBitmap.getPixel(x, y)
                val gray = Color.red(pixel)
                histogram[gray]++
            }
        }

        // Compute the total number of pixels
        val total = width * height

        // Compute the sum of grayscale levels
        var sum = 0
        for (i in 0..255) {
            sum += i * histogram[i]
        }

        // Compute the sum of variances for all possible thresholds
        var w = 0
        var sumB = 0
        var varMax = 0.0
        var threshold = 0
        for (i in 0..255) {
            w += histogram[i]
            if (w == 0) continue
            val w1 = w.toDouble() / total
            val w2 = 1.0 - w1
            sumB += i * histogram[i]
            val m1 = sumB.toDouble() / w
            val m2 = (sum - sumB).toDouble() / (total - w)
            val varBetween = w1 * w2 * (m1 - m2) * (m1 - m2)
            if (varBetween > varMax) {
                varMax = varBetween
                threshold = i
            }
        }

        // Create a binary bitmap with the foreground in white and the background in black
        val binaryBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = grayBitmap.getPixel(x, y)
                val gray = Color.red(pixel)
                val color = if (gray < threshold) Color.BLACK else Color.WHITE
                binaryBitmap.setPixel(x, y, color)
            }
        }

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = binaryBitmap.getPixel(x, y)
                val invertedColor = if (pixel == Color.BLACK) Color.WHITE else Color.BLACK
                binaryBitmap.setPixel(x, y, invertedColor)
            }
        }

        imageview?.setImageBitmap(binaryBitmap)

        return binaryBitmap
    }


    fun convertToSketch(bitmap: Bitmap) {

        //set stroke width
        val multi = MultiTransformation(
            SketchFilterTransformation(),
            GrayscaleTransformation(),
//                InvertFilterTransformation(),
//                BlurTransformation(10, 1) ,
//                ToonFilterTransformation()
//                PixelationFilterTransformation(),
        )
        // pass this transform to the removeBackground2 method
        imageview?.let {
            Glide.with(this).load(bitmap)
                .apply(RequestOptions.bitmapTransform(multi)).into(it)
        }
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

    private fun convrtBitmap(bitmap: Bitmap): Bitmap {
        // Convert bitmap to Mat
        val mat = Mat(bitmap.width, bitmap.height, CvType.CV_8UC4)
        val bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        org.opencv.android.Utils.bitmapToMat(bmp32, mat)

        // Convert to grayscale
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY)

        // Invert image
        Core.bitwise_not(mat, mat)

        // Apply Gaussian blur
        Imgproc.GaussianBlur(mat, mat, Size(3.0, 3.0), 0.0)

        // Apply dodge blend
        val sketch = Mat(mat.size(), CvType.CV_8UC1)
        val temp = Mat(mat.size(), CvType.CV_8UC1)
        val maxValue = 255.0
        val scalar = Scalar(maxValue)
        Core.subtract(mat, scalar, temp)
        Core.divide(maxValue, temp, temp)
        Core.divide(maxValue, mat, sketch)
        Core.add(sketch, temp, sketch)
        Core.multiply(sketch, scalar, sketch)

        // Convert back to bitmap
        val sketchBitmap =
            Bitmap.createBitmap(sketch.cols(), sketch.rows(), Bitmap.Config.ARGB_8888)
        org.opencv.android.Utils.matToBitmap(sketch, sketchBitmap)

        imageview?.setImageBitmap(sketchBitmap)

        return sketchBitmap
    }
}