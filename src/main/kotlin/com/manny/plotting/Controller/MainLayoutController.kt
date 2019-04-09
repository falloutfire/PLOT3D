package com.manny.plotting.Controller

import com.manny.plotting.Main
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.Alert
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.util.Callback
import org.jzy3d.chart.AWTChart
import org.jzy3d.colors.Color
import org.jzy3d.colors.ColorMapper
import org.jzy3d.colors.colormaps.ColorMapGrayscale
import org.jzy3d.colors.colormaps.ColorMapRBG
import org.jzy3d.colors.colormaps.ColorMapRainbow
import org.jzy3d.colors.colormaps.ColorMapRainbowNoBorder
import org.jzy3d.javafx.JavaFXChartFactory
import org.jzy3d.maths.Range
import org.jzy3d.plot3d.builder.concrete.OrthonormalTessellator
import org.jzy3d.plot3d.primitives.Shape
import org.jzy3d.plot3d.rendering.canvas.Quality
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.regex.Pattern
import kotlin.math.absoluteValue


class MainLayoutController {

    lateinit var graph3DCCPane: HBox
    lateinit var graph3DCBPane: HBox
    lateinit var graph3DCAPane: HBox
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
    lateinit var firstStepField: TextField
    lateinit var kurantField: TextField
    lateinit var maxSigmaField: TextField
    lateinit var caTableView: TableView<ObservableList<String>>
    lateinit var cbTableView: TableView<ObservableList<String>>
    lateinit var ccTableView: TableView<ObservableList<String>>

    lateinit var areaField: TextField
    lateinit var nField: TextField
    lateinit var sigmaField: TextField
    lateinit var absolutSigmaField: TextField
    lateinit var mField: TextField
    lateinit var dtField: TextField
    lateinit var dxField: TextField

    lateinit var maxConField: TextField
    lateinit var const1Field: TextField
    lateinit var modelTimeField: TextField
    lateinit var const2Field: TextField
    lateinit var speedField: TextField
    lateinit var timeField: TextField
    lateinit var qmaxField: TextField

    private var imageViewCA: ImageView? = null
    private var imageViewCB: ImageView? = null
    private var imageViewCC: ImageView? = null
    private var factory: JavaFXChartFactory? = null
    private var chartCA: AWTChart? = null
    private var chartCB: AWTChart? = null
    private var chartCC: AWTChart? = null


    private var main: Main? = null

    fun initialize() {
        val p = Pattern.compile("(\\d+\\.?\\d*)?")
        setPattern(diameterField, p)
        setPattern(lengthField, p)
        setPattern(activ2Field, p)
        setPattern(activ1Field, p)
        setPattern(k2Field, p)
        setPattern(k1Field, p)
        setPattern(concBField, p)
        setPattern(concAField, p)
        setPattern(tempField, p)
        setPattern(rateField, p)

        setPattern(firstStepField, p)
        setPattern(kurantField, p)
        setPattern(maxSigmaField, p)
    }

    private fun setPattern(textField: TextField, pattern: Pattern) {
        textField.textProperty().addListener { observable, oldValue, newValue ->
            if (!pattern.matcher(newValue).matches()) textField.text = oldValue
        }
    }

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
        Ea2: Double,
        x0: Double,
        Ku: Double,
        e_max: Double,
        q_max: Double
    ): HashMap<String, Any> {
        /*val x0 = 2.0
        val Ku = 0.95
        val e_max = 1.3*/
        //val q_max = 10

        val S = (Math.PI * Math.pow(D, 2.0)) / 4.0
        val u = Q / S * 0.001
        val tR = L / u
        val Sigma = 2 * tR
        val k1 = k01 * Math.pow(Math.E, -Ea1 / (8.31 * (T + 273.0)))
        val k2 = k02 * Math.pow(Math.E, -Ea2 / (8.31 * (T + 273.0)))
        var q = 0.0
        var e = 2 * e_max

        var deltaX = x0
        var deltaT = Ku * deltaX / u
        var M = Math.round(L / deltaX).toInt()
        var N = Math.round(Sigma / deltaT).toInt()

        val x = ArrayList<Double>()
        val t = ArrayList<Double>()
        val CA = ArrayList<ArrayList<Double>>()
        val CB = ArrayList<ArrayList<Double>>()
        val CC = ArrayList<ArrayList<Double>>()
        val CC1 = ArrayList<ArrayList<Double>>()

        var m1 = 0
        var n1 = 0
        var max = 0.0
        var ea = 0.0


        do {
            CA.clear()
            CB.clear()
            CC.clear()
            x.clear()
            t.clear()
            if (q != 0.0) {
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
                val adder11 = ArrayList<Double>()
                val adder22 = ArrayList<Double>()
                val adder33 = ArrayList<Double>()
                for (j in 0..M) {
                    adder11.add(0.0)
                    adder22.add(0.0)
                    adder33.add(0.0)
                }
                t.add(i * deltaT)
                CA.add(adder11)
                CA[i][0] = CAin
                CB.add(adder22)
                CB[i][0] = CBin
                CC.add(adder33)
                CC[i][0] = 0.0
            }

            for (j in 0 until N) { //316
                println(j)
                for (i in 1 until M) { //150
                    CA[(j + 1)][i] =
                        ((CA[j][(i - 1)] + CA[j][(i + 1)]) / 2.0) - (Ku * (CA[j][(i + 1)] - CA[j][(i - 1)]) / 2.0) - k1 * deltaT * ((CB[j][(i - 1)] + CB[j][(i + 1)]) / 2.0) * ((CA[j][(i - 1)] + CA[j][(i + 1)]) / 2.0)
                    CB[(j + 1)][i] =
                        ((CB[j][(i - 1)] + CB[j][(i + 1)]) / 2.0) - (Ku * (CB[j][(i + 1)] - CB[j][(i - 1)]) / 2.0) - k1 * deltaT * ((CB[j][(i - 1)] + CB[j][(i + 1)]) / 2.0) * ((CA[j][(i - 1)] + CA[j][(i + 1)]) / 2.0)
                    CC[(j + 1)][i] =
                        ((CC[j][(i - 1)] + CC[j][(i + 1)]) / 2.0) - (Ku * (CC[j][(i + 1)] - CC[j][(i - 1)]) / 2.0) + k1 * deltaT * ((CB[j][(i - 1)] + CB[j][(i + 1)]) / 2.0) * ((CA[j][(i - 1)] + CA[j][(i + 1)]) / 2.0) - k2 * deltaT * ((CC[j][(i - 1)] + CC[j][(i + 1)]) / 2.0)
                }
                CA[(j + 1)][M] =
                    CA[j][(M)] - Ku * (CA[j][(M)] - CA[j][(M - 1)]) - k1 * deltaT * CB[j][(M)] * CA[j][(M)]
                CB[(j + 1)][M] =
                    CB[j][(M)] - Ku * (CB[j][(M)] - CB[j][(M - 1)]) - k1 * deltaT * CB[j][(M)] * CA[j][(M)]
                CC[(j + 1)][M] =
                    CC[j][(M)] - Ku * (CC[j][(M)] - CC[j][(M - 1)]) + k1 * deltaT * CB[j][(M)] * CA[j][(M)] - k2 * deltaT * CC[j][(M)]
            }

            if (q != 0.0) {
                var summ = 0.0
                for (j in 1..n1) {
                    for (i in 1..m1) {
                        summ += Math.pow(
                            CC[(2 * j)][(2 * i)] - CC1[j][i], 2.0
                        )
                    }
                }
                ea = Math.sqrt((1 / (m1.toDouble() * n1.toDouble())) * summ.toDouble())
                max = CC.map { it.max() }.maxBy { it!!.absoluteValue }!!
                e = (ea / max) * 100.0

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

        if (kurantField.text.toDouble() <= 1 && kurantField.text.toDouble() > 0) {
            val timeRas = System.currentTimeMillis()
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

            var x0 = firstStepField.text.toDouble()
            var Ku = kurantField.text.toDouble()
            var e_max = maxSigmaField.text.toDouble()
            val q_max = qmaxField.text.toDouble()

            var map = calculate(diameter, length, rate, concA, concB, temp, k1, activ1, k2, activ2, x0, Ku, e_max, q_max)

            /*val yAxis = NumberAxis()
            val xAxis = NumberAxis()
            var chart2dTemp = LineChart(xAxis, yAxis)

            chart2dTemp.setPrefSize(500.0, 500.0)

            xAxis.label = "Время "
            yAxis.label = "Концентрация СС"
            val seria1 = XYChart.Series<Number, Number>()*/

            val cA = map["CA"] as ArrayList<ArrayList<Double>>
            val cB = map["CB"] as ArrayList<ArrayList<Double>>
            val cC = map["CC"] as ArrayList<ArrayList<Double>>
            val time = map["t"] as ArrayList<Double>
            val coord = map["x"] as ArrayList<Double>

            val df = DecimalFormat("#.##")
            df.roundingMode = RoundingMode.CEILING
            val dfout = DecimalFormat("#.###")
            dfout.roundingMode = RoundingMode.CEILING
            val coordTableArray = ArrayList<String>()
            coordTableArray.add("Координата, м")

            coord.map { coordTableArray.add(df.format(it)) }

            setCellFactoryTable(coordTableArray, caTableView.columns)
            val aArrayTable = adderValues(time, cA)
            caTableView.items = aArrayTable

            setCellFactoryTable(coordTableArray, cbTableView.columns)
            val bArrayTable = adderValues(time, cB)
            cbTableView.items = bArrayTable

            setCellFactoryTable(coordTableArray, ccTableView.columns)
            val cArrayTable = adderValues(time, cC)
            ccTableView.items = cArrayTable

            /* for (i in 0 until cC.size) {
                 seria1.data.add(
                     XYChart.Data(
                         time[i] as Number,
                         cC[i][cC[0].size - 1] as Number
                     )
                 )
             }

             chart2dTemp.data.clear()
             chart2dTemp.data.add(seria1)
             graphCaPane.children.clear()
             graphCaPane.children.add(chart2dTemp)*/


            factory = JavaFXChartFactory()
            chartCA = getChart(factory!!, cA, time, coord)
            chartCB = getChart(factory!!, cB, time, coord)
            chartCC = getChart(factory!!, cC, time, coord)

            imageViewCA = factory!!.bindImageView(chartCA)
            imageViewCB = factory!!.bindImageView(chartCB)
            imageViewCC = factory!!.bindImageView(chartCC)

            graph3DCAPane.children.clear()
            graph3DCBPane.children.clear()
            graph3DCCPane.children.clear()

            graph3DCAPane.children.add(imageViewCA)
            graph3DCBPane.children.add(imageViewCB)
            graph3DCCPane.children.add(imageViewCC)

            areaField.text = dfout.format(map["s"])
            nField.text = dfout.format(map["N"])
            sigmaField.text = dfout.format(map["e"])
            absolutSigmaField.text = dfout.format(map["ea"])
            mField.text = dfout.format(map["M"])
            dtField.text = dfout.format(map["deltaT"])
            dxField.text = dfout.format(map["deltaX"])
            maxConField.text = dfout.format(map["CCmax"])
            const1Field.text = dfout.format(map["k1"])
            modelTimeField.text = dfout.format(map["Sigma"])
            const2Field.text = dfout.format(map["k2"])
            speedField.text = dfout.format(map["u"])
            timeField.text = ((System.currentTimeMillis() - timeRas)/1000).toString()
        } else {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.headerText = "Нарушено условие устойчивости."
            alert.contentText = "Сеточное число Куранта должно быть больше 0 и меньше 1"
            alert.showAndWait()
        }


    }

    private fun adderValues(
        time: ArrayList<Double>,
        conc: ArrayList<ArrayList<Double>>
    ): ObservableList<ObservableList<String>> {
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.CEILING
        var array: ObservableList<ObservableList<String>> = FXCollections.observableArrayList()
        for (i in 0 until time.size step 10) {
            var line: ObservableList<String> = FXCollections.observableArrayList()
            line.add(df.format(time[i]))
            conc[i].map { line.add(df.format(it)) }
            array.add(line)
        }
        return array
    }

    private fun getChart(
        factory: JavaFXChartFactory,
        conMap: ArrayList<ArrayList<Double>>,
        time: ArrayList<Double>,
        coord: ArrayList<Double>
    ): AWTChart {
        // -------------------------------
        // Define a function to plot
        val rangeY = Range(0F, coord.size.toFloat() - 1)
        val rangeX = Range(0F, time[time.size - 1].toFloat() - 1)
        val stepsY = (coord.size - 1) / 10
        val stepsX = (time.size - 1) / 10

        val mapper = CustomMapper(conMap)

        // Create the object to represent the function over the given range.

        val surface = /*Builder.*/buildOrthonormalTest(
            OrthonormalGridCustom(
                rangeX,
                stepsX,
                rangeY,
                stepsY,
                Range(0F, time.size.toFloat() - 1),
                (time.size - 1) / 10
            ), mapper
        )
        var myColorMapper =
            ColorMapper(
                ColorMapRainbowNoBorder(),
                surface.bounds.zmax.toDouble(),
                surface.bounds.zmax.toDouble(),
                Color(1f, 1f, 1f, 1f)
            )
        surface.colorMapper = myColorMapper
        surface.faceDisplayed = true
        surface.wireframeDisplayed = true
        surface.wireframeColor = Color.BLACK

        /*var counter = 0
        for (j in 0 until conMap.size) {
            for (i in 0 until conMap[0].size) {
                val x = coord[i]
                val y = time[j]
                val z = conMap[j][i]
                points[counter] = Coord3d(x, y, z)
                val a = 0.25f
                //colors[counter] = Color(x.toFloat(), y.toFloat(), z.toFloat())
                colors[counter] = Color(0F, 0F, 170.toFloat(), a)
                counter++
            }

        }*/
        // Create a chart
        val quality = Quality.Advanced
        quality.isSmoothPolygon = true
        quality.isAnimated = true

        // let factory bind mouse and keyboard controllers to JavaFX node
        //val scatter = Scatter(points, colors, 1f)

        val chart = factory.newChart(quality, "offscreen") as AWTChart

        //chart.scene.graph.add(scatter)
        chart.scene.graph.add(surface)
        chart.axeLayout.xAxeLabel = "Time, min"
        chart.axeLayout.yAxeLabel = "Length, m"
        chart.axeLayout.zAxeLabel = "Concentration, Mol/L"
        return chart
    }

    private fun buildOrthonormalTest(grid: OrthonormalGridCustom, mapper: CustomMapper): Shape {
        val tesselator = OrthonormalTessellator()
        return tesselator.build(grid.apply(mapper)) as Shape
    }


    fun setMainApp(main: Main) {
        this.main = main
    }


    private fun setCellFactoryTable(
        columnsList: List<String>,
        columns: ObservableList<TableColumn<ObservableList<String>, *>>
    ) {

        val col0 = TableColumn<ObservableList<String>, String>(columnsList[0])
        col0.cellValueFactory =
            Callback<TableColumn.CellDataFeatures<ObservableList<String>, String>, ObservableValue<String>> { param ->
                SimpleStringProperty(
                    param.value[0].toString()
                )
            }
        columns.addAll(col0)

        for (i in 1 until columnsList.size step 10) {
            val col = TableColumn<ObservableList<String>, String>(columnsList[i])
            col.cellValueFactory =
                Callback<TableColumn.CellDataFeatures<ObservableList<String>, String>, ObservableValue<String>> { param ->
                    SimpleStringProperty(
                        param.value[i].toString()
                    )
                }
            columns.addAll(col)
        }
    }
}

