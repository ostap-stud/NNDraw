package com.dev.cuckooxa.nndraw.neural

import java.util.function.UnaryOperator

class NeuralNetwork(
    private val learningRate: Double,
    private val activation: UnaryOperator<Double>,
    private val derivative: UnaryOperator<Double>,
    private val sizes: IntArray
) {

    var layers: Array<Layer?> = arrayOfNulls<Layer>(sizes.size)

    init {
        for (i in sizes.indices) {
            var nextSize = 0
            if (i < sizes.size - 1) nextSize = sizes[i + 1]
            layers[i] = Layer(sizes[i], nextSize)
            for (j in 0 until sizes[i]) {
                layers[i]?.biases?.set(j, Math.random() * 2.0 - 1.0)
                for (k in 0 until nextSize) {
                    layers[i]?.weights?.get(j)?.set(k, Math.random() * 2.0 - 1.0)
                }
            }
        }
    }

    fun feedForward(inputs: DoubleArray): DoubleArray {
        System.arraycopy(inputs, 0, layers[0]?.neurons!!, 0, inputs.size)
        for (i in 1 until layers.size) {
            val l: Layer? = layers[i - 1]
            val l1: Layer? = layers[i]
            for (j in 0 until l1?.size!!) {
                l1.neurons[j] = 0.0
                for (k in 0 until l?.size!!) {
                    l1.neurons[j] += l.neurons[k] * l.weights[k][j]
                }
                l1.neurons[j] += l1.biases[j]
                l1.neurons[j] = activation.apply(l1.neurons[j])
            }
        }
        return layers[layers.size - 1]?.neurons!!
    }

    fun backpropagation(targets: DoubleArray) {
        var errors = DoubleArray(layers[layers.size - 1]?.size!!)
        for (i in 0 until layers[layers.size - 1]?.size!!) {
            errors[i] = targets[i] - (layers[layers.size - 1]?.neurons?.get(i) ?: 0.0)
        }
        for (k in layers.size - 2 downTo 0) {
            val l: Layer? = layers[k]
            val l1: Layer? = layers[k + 1]
            val errorsNext = DoubleArray(l?.size!!)
            val gradients = DoubleArray(l1?.size!!)
            for (i in 0 until l1.size) {
                gradients[i] = errors[i] * derivative.apply(layers[k + 1]?.neurons?.get(i) ?: 0.0)
                gradients[i] *= learningRate
            }
            val deltas = Array(l1.size) { DoubleArray(l.size) }
            for (i in 0 until l1.size) {
                for (j in 0 until l.size) {
                    deltas[i][j] = gradients[i] * l.neurons[j]
                }
            }
            for (i in 0 until l.size) {
                errorsNext[i] = 0.0
                for (j in 0 until l1.size) {
                    errorsNext[i] += l.weights[i][j] * errors[j]
                }
            }
            errors = DoubleArray(l.size)
            System.arraycopy(errorsNext, 0, errors, 0, l.size)
            val weightsNew = Array(l.weights.size) { DoubleArray(l.weights[0].size) }
            for (i in 0 until l1.size) {
                for (j in 0 until l.size) {
                    weightsNew[j][i] = l.weights[j][i] + deltas[i][j]
                }
            }
            l.weights = weightsNew
            for (i in 0 until l1.size) {
                l1.biases[i] += gradients[i]
            }
        }
    }
}