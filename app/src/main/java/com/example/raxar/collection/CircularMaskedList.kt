package com.example.raxar.collection

import timber.log.Timber
import kotlin.math.abs

/**
 * A circular list with a mask. Good for viewing a moving subset of a collection.
 *
 * The subset can grow and shrink on either end.
 *
 * You can think of it internally like [1, 2, 3, {4, 5, 6, 7,} 8, 9, 10] where {4, 5, 6, 7} are the
 * visible values. In code this would be:
 *
 * CircularMaskedList<Int> list = new CircularMaskedList<>(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
 *       maskSize = 4,  startIndex = 3)
 * println(list.getMaskedValues()) // [4, 5, 6, 7]
 *
 * The mask can wraparound, but it cannot contain more elements than the list. For example, if the
 * startIndex is 3 in a list of length 4 and the mask size is 2, the mask will return the last item
 * in the list and then the first item. Example:
 *
 * CircularMaskedList<Int> list = new CircularMaskedList<>(listOf(1, 2, 3, 4), maskSize = 2,
 *      startIndex = 3)
 * println(list.getMaskedValues()) // [4, 1]
 *
 * However, the mask size is maintained regardless of the size of the list. In the future, perhaps
 * this could be extended to allow adding or removing elements from the list, but so far this has
 * not been necessary so it has not been implemented.
 *
 * @param values List of values to mask.
 * @param maskSize Length of items after startIndex in the mask. If maskSize is negative, the mask
 *      will be flipped so that startIndex is on the left and mask is positive. Mask sizes bigger
 *      than the list size will be stored in case the list increases in size.
 *      //TODO: Support updating internal values
 * @param startIndex Index of first element in the mask. `startIndex` should always be in the range
 *      [0, values.size). If it goes outside that range, values will be converted to be in the range
 *      of [0, values.size). Negative values will be wrapped around, for example
 */
class CircularMaskedList<T>(
  private val values: List<T> = listOf(),
  maskSize: Int = values.size,
  private var startIndex: Int = 0,
) {

  var maskSize = maskSize
    private set

  init {
    startIndex = fitIndexInValuesSize(startIndex)
    flipMaskIfNegative()
  }

  private fun flipMaskIfNegative() {
    if (maskSize < 0) {
      val tempMaskSize = if (values.isEmpty()) 0 else maskSize % values.size
      startIndex = fitIndexInValuesSize(startIndex + tempMaskSize)
      maskSize = -maskSize
    }
  }

  private fun endIndex() = startIndex + maskSize - 1

  fun getMaskedValues(): List<T> {
    if (maskSize == 0) {
      return listOf()
    }

    if (maskSize >= values.size) {
      return values.slice(startIndex until values.size) + values.slice(0 until startIndex)
    }

    val endIndex = fitIndexInValuesSize(endIndex())
    Timber.d(
      "startIndex=$startIndex,maskSize=$maskSize,valuesSize=${values.size}"
    )
    return if (startIndex <= endIndex) {
      values.slice(startIndex..endIndex)
    } else {
      values.slice(startIndex until values.size) + values.slice(0..endIndex)
    }
  }

  /**
   * Shifts the right mask bound.
   * Essentially, changes the mask size.
   * If indexCount is positive, the mask is extended to the right.
   * Added or removed values are returned.
   * If the mask is bigger than the list, the whole list is returned.
   */
  fun shiftRightMaskBound(indexCount: Int): List<T> {
    Timber.d(
      "shiftRightMaskBound(%s), startIndex=%s, maskSize=%s", indexCount, startIndex, maskSize
    )
    if (indexCount == 0) {
      return listOf()
    }
    val range = 1..abs(indexCount)
    val changeBy = if (indexCount > 0) 1 else -1
    val buildList = buildList {
      for (i in range) {
        val oldValues = getMaskedValues()
        maskSize += changeBy
        flipMaskIfNegative()
        val newValues = getMaskedValues()
        addAll(addedOrRemovedValues(oldValues, newValues))
      }
    }
    Timber.d(
      "shiftRightMaskBound(%s) = %s, startIndex=%s, maskSize=%s", indexCount, buildList, startIndex,
      maskSize
    )
    return buildList
  }

  /**
   * Shifts the left mask bound.
   * Essentially, shifts the startIndex and changes the mask size to keep the end index.
   * If indexCount is positive, the mask is extended to the left.
   * Added or removed values are returned.
   * If the mask is bigger than the list, the whole list is returned.
   */
  fun shiftLeftMaskBound(indexCount: Int): List<T> {
    Timber.d(
      "shiftLeftMaskBound(%s), startIndex=%s, maskSize=%s", indexCount, startIndex, maskSize
    )
    if (indexCount == 0) {
      return listOf()
    }
    val range = 1..abs(indexCount)
    val changeBy = if (indexCount > 0) 1 else -1
    val buildList = buildList {
      for (i in range) {
        val oldValues = getMaskedValues()
        startIndex += changeBy
        startIndex = fitIndexInValuesSize(startIndex)
        maskSize -= changeBy
        flipMaskIfNegative()
        val newValues = getMaskedValues()
        addAll(addedOrRemovedValues(oldValues, newValues))
      }
    }
    Timber.d(
      "shiftLeftMaskBound(%s) = %s, startIndex=%s, maskSize=%s", indexCount, buildList, startIndex,
      maskSize
    )
    return buildList
  }

  private fun fitIndexInValuesSize(index: Int): Int {
    if (values.isEmpty()) {
      return 0
    }
    return if (index < 0) values.size + (index % values.size) else (index % values.size)
  }

  private fun addedOrRemovedValues(
    oldValues: List<T>,
    newValues: List<T>,
  ): List<T> {
    return if (oldValues.size < newValues.size) {
      newValues.minus(oldValues.toSet())
    } else {
      oldValues.minus(newValues.toSet())
    }
  }
}