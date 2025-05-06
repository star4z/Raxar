package com.example.raxar.collection

import timber.log.Timber

class CircularMaskedList<T>(
    private val values: List<T>,
    public var maskSize: Int,
    private var startIndex: Int = 0
) {

    init {
        startIndex = normalize(startIndex)
        // Flip mask around if it's negative
        if (maskSize < 0) {
            val tempMaskSize = maskSize % values.size
            startIndex = normalize(startIndex + tempMaskSize)
            maskSize = -maskSize
        }
    }

    private fun endIndex() = startIndex + maskSize - 1

    fun getMaskedValues(): List<T> {
        if (maskSize > values.size) {
            return values.slice(startIndex until values.size) + values.slice(0 until startIndex)
        }

        val endIndex = normalize(endIndex())
        if (startIndex <= endIndex) {
            return values.slice(startIndex..endIndex)
        } else {
            return values.slice(startIndex until values.size) + values.slice(0..endIndex)
        }
    }

    /**
     * Shifts the right mask bound.
     * If indexCount is positive, the mask is shifted to the right.
     * If indexCount is positive, added values are returned.
     * If indexCount is negative, removed values are returned.
     */
    fun shiftRightMaskBound(indexCount: Int): List<T> {
        val oldValues = getMaskedValues()
        Timber.d("startIndex = %s, maskSize = %s, oldValues = %s", startIndex, maskSize, oldValues)
        maskSize += indexCount
        val newValues = getMaskedValues()
        Timber.d("startIndex = %s, maskSize = %s, newValues = %s", startIndex, maskSize, newValues)
        return addedOrRemovedValues(oldValues, newValues)
    }

    fun shiftLeftMaskBound(indexCount: Int): List<T> {
        val oldValues = getMaskedValues()
        Timber.d("startIndex = %s, maskSize = %s, oldValues = %s", startIndex, maskSize, oldValues)
        startIndex += indexCount
        maskSize -= indexCount
        val newValues = getMaskedValues()
        Timber.d("startIndex = %s, maskSize = %s, newValues = %s", startIndex, maskSize, newValues)
        return addedOrRemovedValues(oldValues, newValues)
    }

    private fun normalize(index: Int): Int {
        return if (index < 0) values.size + (index % values.size) else (index % values.size)
    }

    private fun addedOrRemovedValues(oldValues: List<T>, newValues: List<T>): List<T> {
        return if (oldValues.size < newValues.size) {
            newValues.minus(oldValues.toSet())
        } else {
            oldValues.minus(newValues.toSet())
        }
    }
}