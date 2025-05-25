package com.example.raxar.collection

import com.example.raxar.TimberRule
import com.google.common.truth.Truth.assertThat
import org.junit.ClassRule
import org.junit.Test

class CircularMaskedListTest {

  companion object {
    @get:ClassRule
    @JvmStatic
    var timberRule = TimberRule()
  }

  @Test
  fun returnsMaskedValues() {
    val list = CircularMaskedList(listOf(1, 2, 3, 4, 5), 3)
    assertThat(list.getMaskedValues()).containsExactly(1, 2, 3).inOrder()
  }

  @Test
  fun returnsMaskedValues_maxSize0() {
    val list = CircularMaskedList(listOf(1, 2, 3, 4, 5), 0)
    assertThat(list.getMaskedValues()).isEmpty()
  }

  @Test
  fun returnsMaskedValues_withMaskBiggerThanSize_returnsAllValues() {
    val list = CircularMaskedList(listOf(1, 2, 3, 4, 5), 10)
    assertThat(list.getMaskedValues()).containsExactly(1, 2, 3, 4, 5).inOrder()
  }

  @Test
  fun negativeMaskSize_wrapsAround_wholeList() {
    val list = CircularMaskedList(listOf(1, 2, 3, 4, 5), -5)
    assertThat(list.getMaskedValues()).containsExactly(1, 2, 3, 4, 5).inOrder()
  }

  @Test
  fun negativeMaskSize_wrapsAround_partialList() {
    val list = CircularMaskedList(listOf(1, 2, 3, 4, 5), -3)
    assertThat(list.getMaskedValues()).containsExactly(3, 4, 5).inOrder()
  }

  @Test
  fun setStartIndex() {
    val list = CircularMaskedList(listOf(1, 2, 3, 4, 5), 3, 2)
    assertThat(list.getMaskedValues()).containsExactly(3, 4, 5).inOrder()
  }

  @Test
  fun startIndex_greaterThanSize_wrapsAround() {
    val list = CircularMaskedList(listOf(1, 2, 3, 4, 5), 3, 6)
    assertThat(list.getMaskedValues()).containsExactly(2, 3, 4).inOrder()
  }

  @Test
  fun startIndex_lessThanZero_wrapsAround() {
    val list = CircularMaskedList(listOf(1, 2, 3, 4, 5), 3, -3)
    assertThat(list.getMaskedValues()).containsExactly(3, 4, 5).inOrder()
  }

  @Test
  fun shiftRightMaskBound_right() {
    val list = CircularMaskedList(listOf(1, 2, 3, 4, 5), 3)
    val newValues = list.shiftRightMaskBound(1)
    assertThat(newValues).containsExactly(4).inOrder()
    assertThat(list.getMaskedValues()).containsExactly(1, 2, 3, 4).inOrder()
  }

  @Test
  fun shiftRightMaskBound_right_longerThanList_returnsWholeList() {
    val list = CircularMaskedList(listOf(1, 2, 3, 4, 5), 3)
    val newValues = list.shiftRightMaskBound(4)
    assertThat(newValues).containsExactly(4, 5).inOrder()
    assertThat(list.getMaskedValues()).containsExactly(1, 2, 3, 4, 5).inOrder()
  }

  @Test
  fun shiftRightMaskBound_right_wrapsAround() {
    val list = CircularMaskedList(listOf(1, 2, 3, 4, 5), 3, 1)
    val newValues = list.shiftRightMaskBound(2)
    assertThat(newValues).containsExactly(5, 1).inOrder()
    assertThat(list.getMaskedValues()).containsExactly(2, 3, 4, 5, 1).inOrder()
  }

  @Test
  fun shiftRightMaskBound_left() {
    val list = CircularMaskedList(listOf(1, 2, 3, 4, 5), 3, 2)
    val newValues = list.shiftRightMaskBound(-2)
    assertThat(newValues).containsExactly(5, 4).inOrder()
    assertThat(list.getMaskedValues()).containsExactly(3).inOrder()
  }

  @Test
  fun shiftRightMaskBound_leftPastStartIndex_returnsWrapAroundValues() {
    val list = CircularMaskedList(listOf(1, 2, 3, 4, 5), 3, 2)
    val newValues = list.shiftRightMaskBound(-4)
    assertThat(newValues).containsExactly(5, 4, 3, 2).inOrder()
    assertThat(list.getMaskedValues()).containsExactly(2).inOrder()
  }

  @Test
  fun shiftLeftMaskBound_left() {
    val list = CircularMaskedList(listOf(1, 2, 3, 4, 5), 3, 2)
    val newValues = list.shiftLeftMaskBound(-1)
    assertThat(newValues).containsExactly(2).inOrder()
    assertThat(list.getMaskedValues()).containsExactly(2, 3, 4, 5).inOrder()
  }

  @Test
  fun shiftLeftMaskBound_left_longerThanList_returnsWholeList() {
    val list = CircularMaskedList(listOf(1, 2, 3, 4, 5), 3, 2)
    val newValues = list.shiftLeftMaskBound(-4)
    assertThat(newValues).containsExactly(2, 1).inOrder()
    assertThat(list.getMaskedValues()).containsExactly(4, 5, 1, 2, 3).inOrder()
  }

  @Test
  fun shiftLeftMaskBound_left_wrapsAround() {
    val list = CircularMaskedList(listOf(1, 2, 3, 4, 5), 3, 1) // [2, 3, 4]
    val newValues = list.shiftLeftMaskBound(-2)
    assertThat(newValues).containsExactly(1, 5).inOrder()
    assertThat(list.getMaskedValues()).containsExactly(5, 1, 2, 3, 4).inOrder()
  }

  @Test
  fun shiftLeftMaskBound_right() {
    val list = CircularMaskedList(listOf(1, 2, 3, 4, 5), 3, 2)
    val newValues = list.shiftLeftMaskBound(2)
    assertThat(newValues).containsExactly(3, 4).inOrder()
    assertThat(list.getMaskedValues()).containsExactly(5).inOrder()
  }

  @Test
  fun shiftLeftMaskBound_rightPastEndIndex_returnsWrapAroundValues() {
    val list = CircularMaskedList(listOf(1, 2, 3, 4, 5), 3, 2)
    val newValues = list.shiftLeftMaskBound(4)
    assertThat(newValues).containsExactly(3, 4, 5, 1).inOrder()
    assertThat(list.getMaskedValues()).containsExactly(1).inOrder()
  }

  @Test
  fun shiftLeftMaskBound_0_returnsEmptyList() {
    val list = CircularMaskedList(listOf(1, 2, 3, 4, 5), 3, 2)
    val newValues = list.shiftLeftMaskBound(0)
    assertThat(newValues).isEmpty()
    assertThat(list.getMaskedValues()).containsExactly(3, 4, 5).inOrder()
  }

  @Test
  fun shiftRightMaskBound_0_returnsEmptyList() {
    val list = CircularMaskedList(listOf(1, 2, 3, 4, 5), 3, 2)
    val newValues = list.shiftRightMaskBound(0)
    assertThat(newValues).isEmpty()
    assertThat(list.getMaskedValues()).containsExactly(3, 4, 5).inOrder()
  }

  @Test
  fun init_emptyList() {
    val list = CircularMaskedList<Int>()
    assertThat(list.getMaskedValues()).isEmpty()
  }
}