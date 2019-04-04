package com.manny.plotting.Controller

import com.manny.plotting.Main
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.TextField
import javafx.scene.layout.AnchorPane
import kotlin.math.absoluteValue

class MainLayoutController {

    lateinit var graphCaPane: AnchorPane
    lateinit var diameterField: TextField
    lateinit var lengthField: TextField
    lateinit var activ2Field: TextField
    lateinit var activ1Field: TextField
    lateinit var k2Field: TextField
    lateinit var k1Field: TextField
    lateinit var concBField: TextField
    lateinit var concAField: TextField
    lateinit var tempField: TextField
    lateinit var rateField: TextField


    private var main: Main? = null

    fun calculate(
        D: Double,
        L: Double,
        Q: Double,
        CAin: Double,
        CBin: Double,
        T: Double,
        k01: Double,
        Ea1: Double,
        k02: Double,
        Ea2: Double
    ): HashMap<String, Any> {
        var x0 = 2.0
        var Ku = 0.95
        var e_max = 1.3
        var q_max = 1

        var S = (Math.PI * (D * D)) / 4
        var u = Q / S * 0.001
        var tR = L / u
        var Sigma = 2 * tR
        var k1 = k01 * Math.pow(Math.E, -Ea1 / (8.31 * (T + 273)))
        var k2 = k02 * Math.pow(Math.E, -Ea2 / (8.31 * (T + 273)))
        var q = 0
        var e = 2 * e_max

        var deltaX = x0
        var deltaT = Ku * deltaX / u
        var M = Math.round(L / deltaX)
        var N = Math.round(Sigma / deltaT)

        var x = ArrayList<Double>()
        var t = ArrayList<Double>()
        var CA = ArrayList<ArrayList<Double>>()
        var CB = ArrayList<ArrayList<Double>>()
        var CC = ArrayList<ArrayList<Double>>()
        var CC1 = ArrayList<ArrayList<Double>>()

        var m1 = 0L
        var n1 = 0L
        var max = 0.0
        var ea = 0.0
        // строки N
        // столбцы M

        do {
            CA.clear()
            CB.clear()
            CC.clear()
            x.clear()
            t.clear()
            if (q != 0) {
                deltaX /= 2
                deltaT /= 2
                M *= 2
                N *= 2
            }

            var adder1 = ArrayList<Double>()
            var adder2 = ArrayList<Double>()
            var adder3 = ArrayList<Double>()
            for (i in 0..M) {
                x.add(i * deltaX)
                adder1.add(0.0)
                adder2.add(0.0)
                adder3.add(0.0)
            }
            CA.add(adder1)
            CB.add(adder2)
            CC.add(adder3)
            t.add(0.0)

            for (i in 1..N) {
                var adder11 = ArrayList<Double>()
                var adder22 = ArrayList<Double>()
                var adder33 = ArrayList<Double>()
                for (j in 0..M) {
                    adder11.add(0.0)
                    adder22.add(0.0)
                    adder33.add(0.0)
                }
                t.add(i * deltaT)
                CA.add(adder11)
                CA[i.toInt()][0] = CAin
                CB.add(adder22)
                CA[i.toInt()][0] = CBin
                CC.add(adder33)
                CC[i.toInt()][0] = 0.0
            }

            for (j in 0 until N) { //316
                println(j)
                for (i in 1 until M) { //150
                    CA[(j + 1).toInt()][i.toInt()] =
                        ((CA[j.toInt()][(i - 1).toInt()] + CA[j.toInt()][(i + 1).toInt()]) / 2) - (Ku / 2) * (CA[j.toInt()][(i + 1).toInt()] - CA[j.toInt()][(i - 1).toInt()]) - k1 * deltaT * ((CB[j.toInt()][(i - 1).toInt()] + CB[j.toInt()][(i + 1).toInt()]) / 2) * ((CA[j.toInt()][(i - 1).toInt()] + CA[j.toInt()][(i + 1).toInt()]) / 2)
                    CB[(j + 1).toInt()][i.toInt()] =
                        ((CB[j.toInt()][(i - 1).toInt()] + CB[j.toInt()][(i + 1).toInt()]) / 2) - (Ku / 2) * (CB[j.toInt()][(i + 1).toInt()] - CB[j.toInt()][(i - 1).toInt()]) - k1 * deltaT * ((CB[j.toInt()][(i - 1).toInt()] + CB[j.toInt()][(i + 1).toInt()]) / 2) * ((CA[j.toInt()][(i - 1).toInt()] + CA[j.toInt()][(i + 1).toInt()]) / 2)
                    CC[(j + 1).toInt()][i.toInt()] =
                        ((CC[j.toInt()][(i - 1).toInt()] + CC[j.toInt()][(i + 1).toInt()]) / 2) - (Ku / 2) * (CC[j.toInt()][(i + 1).toInt()] - CC[j.toInt()][(i - 1).toInt()]) + k1 * deltaT * ((CB[j.toInt()][(i - 1).toInt()] + CB[j.toInt()][(i + 1).toInt()]) / 2) * ((CA[j.toInt()][(i - 1).toInt()] + CA[j.toInt()][(i + 1).toInt()]) / 2) - k2 * deltaT * ((CC[j.toInt()][(i - 1).toInt()] + CC[j.toInt()][(i + 1).toInt()]) / 2)
                }
                CA[(j + 1).toInt()][M.toInt()] =
                    CA[j.toInt()][(M).toInt()] - Ku * (CA[j.toInt()][(M).toInt()] - CA[j.toInt()][(M - 1).toInt()]) - k1 * deltaT * CB[j.toInt()][(M).toInt()] * CA[j.toInt()][(M).toInt()]
                CB[(j + 1).toInt()][M.toInt()] =
                    CB[j.toInt()][(M).toInt()] - Ku * (CB[j.toInt()][(M).toInt()] - CB[j.toInt()][(M - 1).toInt()]) - k1 * deltaT * CB[j.toInt()][(M).toInt()] * CA[j.toInt()][(M).toInt()]
                CC[(j + 1).toInt()][M.toInt()] =
                    CC[j.toInt()][(M).toInt()] - Ku * (CC[j.toInt()][(M).toInt()] - CC[j.toInt()][(M - 1).toInt()]) + k1 * deltaT * CB[j.toInt()][(M).toInt()] * CA[j.toInt()][(M).toInt()] - k2 * deltaT * CC[j.toInt()][(M).toInt()]

            }

            if (q != 0) {
                var summ = 0.0
                for (j in 1..n1) {
                    for (i in 1..m1) {
                        summ += Math.pow(
                            CC[(2 * j).toInt()][(2 * i).toInt()] -
                                    CC1[j.toInt()][i.toInt()], 2.0
                        )
                    }
                }
                ea = Math.sqrt((1 / (m1 * n1)) * summ)
                max = CC.map { it.max() }.maxBy { it!!.absoluteValue }!!
                e = (ea / max) * 100

            }
            CC1.clear()
            CC1.addAll(CC)
            m1 = M
            n1 = N
            q++

        } while (e >= e_max && q <= q_max)

        return hashMapOf(
            "s" to S,
            "u" to u,
            "tR" to tR,
            "Sigma" to Sigma,
            "k1" to k1,
            "k2" to k2,
            "deltaX" to deltaX,
            "deltaT" to deltaT,
            "M" to M,
            "N" to N,
            "x" to x,
            "t" to t,
            "CA" to CA,
            "CB" to CB,
            "CC" to CC,
            "CCmax" to max,
            "ea" to ea,
            "e" to e,
            "q" to q
        )
    }

    fun onClickCalculate() {
        val diameter = diameterField.text.toDouble()
        val length = lengthField.text.toDouble()
        var activ2 = activ2Field.text.toDouble()
        var activ1 = activ1Field.text.toDouble()
        var k2 = k2Field.text.toDouble()
        var k1 = k1Field.text.toDouble()
        var concA = concAField.text.toDouble()
        var concB = concBField.text.toDouble()
        var temp = tempField.text.toDouble()
        var rate = rateField.text.toDouble()

        var map = calculate(diameter!!, length!!, rate!!, concA!!, concB!!, temp!!, k1!!, activ1!!, k2!!, activ2!!)

        val yAxis = NumberAxis()
        val xAxis = NumberAxis()
        var chart2dTemp = LineChart(xAxis, yAxis)

        chart2dTemp.setPrefSize(500.0, 500.0)

        xAxis.label = "Время "
        yAxis.label = "Концентрация СС"
        val seria1 = XYChart.Series<Number, Number>()

        var cc = map["CC"] as ArrayList<ArrayList<Double>>
        var time = map["t"] as ArrayList<Double>

        for (i in 0 until cc.size) {
            seria1.data.add(
                XYChart.Data(
                    time[i] as Number,
                    cc[i][cc[0].size - 1] as Number
                )
            )
        }

        chart2dTemp.data.add(seria1)
        graphCaPane.children.add(chart2dTemp)
    }


    fun setMainApp(main: Main) {
        this.main = main
    }
}