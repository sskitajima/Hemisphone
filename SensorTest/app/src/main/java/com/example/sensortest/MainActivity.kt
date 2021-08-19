package com.example.sensortest

/*
Copyright 2018 Philipp Jahoda

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed
under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied. See the License for the specific language governing permissions
and limitations under the License.
 */

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import android.graphics.Color

import androidx.appcompat.app.AppCompatActivity


import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet


class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var mAcc: Sensor? = null
    private var textView: TextView? = null

    private var lineChart:LineChart? = null

//    private var entryListX = mutableListOf<Entry>()
//    private var entryListY = mutableListOf<Entry>()
//    private var entryListZ = mutableListOf<Entry>()
    private var count = 0f
    private val numDrawData = 100f

    private val labels = arrayOf(
        "linear_accelerationX",
        "linear_accelerationY",
        "linear_accelerationZ"
    )
    private val colors = intArrayOf(
        Color.BLUE,
        Color.GRAY,
        Color.MAGENTA
    )

    public override fun onCreate(savedInstanceState: Bundle?) {
        println("!!!!!!!onCreate!!!!!!!")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.text_view)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mAcc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // 折れ線グラフ
        lineChart = findViewById(R.id.line_chart)

        // イベントリスナーを登録する
        sensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_UI)


        // インスタンス生成
        lineChart?.setData(LineData())
        // no description text
        lineChart?.getDescription()?.setEnabled(false)
        // Grid背景色
        lineChart?.setDrawGridBackground(true)
        // 右側の目盛り
        lineChart?.getAxisRight()?.setEnabled(false)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do something here if sensor accuracy changes.
        println("onAccuracyChanged")
    }

    override fun onSensorChanged(event: SensorEvent) {
        println("onSensorChanged")


        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Many sensors return 3 values, one for each axis.
            val accX = event.values[0]
            val accY = event.values[1]
            val accZ = event.values[2]

            // Do something with this sensor value
            val strListbuf = StringBuffer("Acceleration val:\n\n")

            val str: String = "X: %s\nY: %s\nZ: %s\n".format(accX.toString(), accY.toString(), accZ.toString())
            strListbuf.append(str)

            textView!!.text = strListbuf
//            textView!!.text = str

            val accArray = arrayOf(accX, accY, accZ)
            updateChart(accArray)
        }
    }

    private fun updateChart(accArray:Array<Float>)
    {
        count += 1
        val data: LineData? = lineChart?.getLineData()

        for(i in 0..2)
        {
            var lineData:ILineDataSet? = data?.getDataSetByIndex(i)

            if(lineData == null) {
                val set = LineDataSet(null, labels[i])
                set.lineWidth = 2.0f
                set.color = colors[i]
                // liner line
                set.setDrawCircles(false)
                // no values on the chart
                set.setDrawValues(false)
                lineData = set
                data!!.addDataSet(lineData)
            }

            println("addEntry")
            data!!.addEntry(Entry(lineData.getEntryCount().toFloat(), accArray.get(i)), i)

        }

        data?.notifyDataChanged()

//        lineChart?.legend?.textSize = 20f
        lineChart?.notifyDataSetChanged() // 表示の更新のために変更を通知する
        lineChart?.setVisibleXRangeMaximum(numDrawData) // 表示の幅を決定する
        lineChart?.moveViewToX(data?.getEntryCount()!!.toFloat()) // 最新のデータまで表示を移動させる
    }

    override fun onResume() {
        println("onResume")
        super.onResume()
        sensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        println("onPause")
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}