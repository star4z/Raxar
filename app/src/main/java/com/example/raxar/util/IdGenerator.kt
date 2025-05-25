package com.example.raxar.util

import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdGenerator @Inject constructor() {

  private val random = SecureRandom()

  fun genId(): Long {
    var id = random.nextLong()
    while (id == 0L) {
      id = random.nextLong()
    }
    return id
  }
}