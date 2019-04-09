package com.manny.plotting.Controller

import org.jzy3d.maths.Coord3d
import org.jzy3d.maths.Range
import org.jzy3d.plot3d.builder.Mapper

class CustomMapper(
    var conMap: ArrayList<ArrayList<Double>>
) : Mapper() {

    override fun f(x: Double, y: Double): Double {
        return conMap[x.toInt()][y.toInt()]
    }
}


open class OrthonormalGridCustom : GridCustom {

    constructor(xyrange: Range, xysteps: Int) : super(xyrange, xysteps)

    constructor(xrange: Range, xsteps: Int, yrange: Range, ysteps: Int) : super(xrange, xsteps, yrange, ysteps)

    constructor(xrange: Range, xsteps: Int, yrange: Range, ysteps: Int, xrangeReal: Range, xstepsReal: Int) : super(
        xrange,
        xsteps,
        yrange,
        ysteps,
        xrangeReal,
        xstepsReal
    )

    override fun apply(mapper: Mapper): List<Coord3d> {
        val xstepR = xrangeReal!!.range / (xstepsReal - 1).toDouble()
        val xstep = xrange.range / (xsteps - 1).toDouble()
        val ystep = yrange.range / (ysteps - 1).toDouble()

        val output = java.util.ArrayList<Coord3d>(xsteps * ysteps)

        for (xi in 0 until xsteps) {
            for (yi in 0 until ysteps) {
                val xin = xrangeReal!!.min + xi * xstepR
                val x = xrange.min + xi * xstep
                val y = yrange.min + yi * ystep
                output.add(Coord3d(x, y, mapper.f(xin, y)))
            }
        }
        return output
    }
}


abstract class GridCustom {

    protected var xrange: Range
    protected var yrange: Range
    protected var xsteps: Int = 0
    protected var ysteps: Int = 0
    protected var xrangeReal: Range? = null
    protected var xstepsReal: Int = 0

    constructor(xyrange: Range, xysteps: Int) {
        this.xrange = xyrange
        this.yrange = xyrange
        this.xsteps = xysteps
        this.ysteps = xysteps
    }

    constructor(xrange: Range, xsteps: Int, yrange: Range, ysteps: Int) {
        this.xrange = xrange
        this.yrange = yrange
        this.xsteps = xsteps
        this.ysteps = ysteps
    }

    constructor(xrange: Range, xsteps: Int, yrange: Range, ysteps: Int, xrangeReal: Range, xstepsReal: Int) {
        this.xrange = xrange
        this.yrange = yrange
        this.xsteps = xsteps
        this.ysteps = ysteps
        this.xrangeReal = xrangeReal
        this.xstepsReal = xstepsReal
    }

    abstract fun apply(mapper: Mapper): List<Coord3d>
}