package com.dev.cuckooxa.nndraw.neural

class Layer(var size: Int, var nextSize: Int) {
    var neurons: DoubleArray = DoubleArray(size)
    var biases: DoubleArray = DoubleArray(size)
    var weights: Array<DoubleArray> = Array(size) { DoubleArray(nextSize) }

}