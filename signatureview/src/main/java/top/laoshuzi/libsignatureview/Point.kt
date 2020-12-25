package top.laoshuzi.libsignatureview

import kotlin.math.pow
import kotlin.math.sqrt


/**
 * Created by laoshuzi on 2020/12/23.
 */
class Point(val x: Float, val y: Float, val time: Long) {

    fun velocityFrom(start: Point): Float {
        return distanceTo(start) / (time - start.time)
    }

    private fun distanceTo(start: Point): Float {
        return sqrt(
            (x - start.x).toDouble().pow(2.0) + (y - start.y).toDouble().pow(2.0)
        ).toFloat()
    }

}