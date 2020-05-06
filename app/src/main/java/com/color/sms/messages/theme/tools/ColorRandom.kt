package com.color.sms.messages.theme.tools

import android.graphics.Color
import kotlin.random.Random

class ColorRandom {
    companion object {
        val color = arrayOf(
                "eb7070",
                "fec771",
                "e6e56c",
                "64e291",
                "51dacf",
                "6d0c74",
                "6b591d",
                "7f78d2",
                "42b883",
                "ff7e67",
                "fda77f",
                "dd6892",
                "43ab92",
                "ffbbcc",
                "c886e5",
                "52de97",
                "1089ff",
                "64b2cd",
                "c70d3a",
                "47e4bb"
        )

        @JvmStatic
        fun getColor(): Int {
            val pos = Random.nextInt(0, color.size-1)

            return Color.parseColor("#" + color[pos])
        }
    }
}
