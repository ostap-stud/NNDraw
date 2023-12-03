package com.dev.cuckooxa.nndraw

import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Canvas
import android.graphics.Color
import com.dev.cuckooxa.nndraw.neural.NeuralNetwork
import kotlin.math.pow


typealias OnFieldChangedListener = (field: DrawingField) -> Unit

class DrawingField(
    val width: Int,
    val height: Int,
    val scaledPx: Int,
    val nn: NeuralNetwork?
    ) {

    val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val bitmapCanvas = Canvas(bitmap).also { it.drawColor(Color.BLACK) }

    val listeners = mutableSetOf<OnFieldChangedListener>()

    fun getPixel(x: Int, y: Int): Int{
        if ( x < 0 || y < 0 || x >= width || y >= height )
            throw ArrayIndexOutOfBoundsException("GET pixel ERROR -> OUT OF BOUNDS")
        return bitmap.getPixel(x, y)
    }

    fun paintPixel(x: Int, y: Int): DoubleArray {
        if (x < 0 || y < 0 || x >= width || y >= height)
            throw ArrayIndexOutOfBoundsException("SET pixel ERROR -> OUT OF BOUNDS")
        val inputs = DoubleArray(width * height){ 0.0 }
        /*
        *   Test with neural network to use restriction for brush size,
        *   like (i in x-3 until x+2 ) and the same for j-var
        *   Then there is have to be next condition
        *   (i in 0 until width && j in 0 until height).
        *   So, it may deliver better results for NN testing
        */
        for (i in 0 until width ){
            for (j in 0 until height){
                var color = (getPixel(i, j) and 0xff) / 255.0
                var distance = (i - x).toDouble().pow(2) + (j - y).toDouble().pow(2)
                if (distance < 1) distance = 1.0
                distance *= distance
                color += 0.1 / distance
                if (color > 1) color = 1.0
                val setColor = (color * 255).toInt()
                bitmap.setPixel(i, j, Color.rgb(setColor, setColor, setColor))
                inputs[i + j * width] = color
            }
        }
        listeners.forEach { it.invoke(this) }
        return inputs
    }

    fun clear(){
        bitmap.setPixels(IntArray(width * height){ Color.BLACK }, 0, width, 0, 0, width, height)
        listeners.forEach { it.invoke(this) }
    }

}