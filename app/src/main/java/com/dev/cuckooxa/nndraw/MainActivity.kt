package com.dev.cuckooxa.nndraw

import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dev.cuckooxa.nndraw.databinding.ActivityMainBinding
import com.dev.cuckooxa.nndraw.neural.Layer
import com.dev.cuckooxa.nndraw.neural.NeuralNetwork
import com.dev.cuckooxa.nndraw.views.Drawer28View
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.function.UnaryOperator


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var nn: NeuralNetwork? = null
    private val sigmoid = UnaryOperator { x: Double? -> 1 / (1 + Math.exp(-x!!)) }
    private val dsigmoid = UnaryOperator { y: Double -> y * (1 - y) }
    private var maxDigit = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also { setContentView(it.root) }

        lifecycleScope.launch {
            setTrainedJsonModel("trainedNeuralNetwork.json")
            binding.apply {
                loadingTv.visibility = View.GONE
                progressBar.visibility = View.GONE
                drawingCard.visibility = View.VISIBLE
                clearBtn.visibility = View.VISIBLE
                digitTv.visibility = View.VISIBLE
                digitProgress.visibility = View.VISIBLE
            }
        }

        val scaledPx = this.toPx(Drawer28View.DESIRED_PIXEL_SCALE_DP)
        binding.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                drawingView.isForceDarkAllowed = false
            }
            drawingView.drawingField = DrawingField(28, 28, scaledPx, nn)
            drawingView.actionListener = {x, y, field ->
                val inputs = field.paintPixel(x, y)
                val outputs = nn?.feedForward(inputs)
                var maxDigitWeight = -1.0
                for (i in 0..9){
                    if (outputs?.get(i)!! > maxDigitWeight){
                        maxDigitWeight = outputs[i]
                        maxDigit = i
                    }
                }
                digitTv.text = "$maxDigit"
                digitProgress.progress = maxDigit
            }
            clearBtn.setOnClickListener {
                drawingView.drawingField?.clear()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) = with(binding) {
        super.onWindowFocusChanged(hasFocus)
        // Dynamically sets the top margin for Drawing Card
        val marginParams = drawingCard.layoutParams as ViewGroup.MarginLayoutParams
        val topMargin = (Resources.getSystem().displayMetrics.widthPixels - drawingCard.width) / 2
        marginParams.setMargins(0, topMargin, 0, 0)
        drawingCard.layoutParams = marginParams
    }

    private suspend fun setTrainedJsonModel(filename: String) = withContext(Dispatchers.IO){
        nn = NeuralNetwork(0.001, sigmoid, dsigmoid, intArrayOf(784, 512, 128, 32, 10))
        val inStream = assets.open(filename)
        val decoded = String(inStream.readBytes())
        val layers = Gson().fromJson(decoded, Array<Layer?>::class.java)
//        val layers = Json.decodeFromString<Array<Layer?>>(decoded)
        nn?.layers = layers
    }

}