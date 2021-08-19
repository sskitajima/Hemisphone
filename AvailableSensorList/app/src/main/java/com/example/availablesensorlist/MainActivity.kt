package com.example.availablesensorlist

import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private var sensorManager: SensorManager? = null
    private var textView: TextView? = null
    private val sensorList = intArrayOf(
        Sensor.TYPE_ACCELEROMETER,
        Sensor.TYPE_ACCELEROMETER_UNCALIBRATED,   //!
        Sensor.TYPE_AMBIENT_TEMPERATURE,          //!
        Sensor.TYPE_DEVICE_PRIVATE_BASE,          //!
        Sensor.TYPE_GAME_ROTATION_VECTOR,         //!
        Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR,  //!
        Sensor.TYPE_GRAVITY,                      //!
        Sensor.TYPE_GYROSCOPE,                    //!
        Sensor.TYPE_GYROSCOPE_UNCALIBRATED,       //!
        Sensor.TYPE_HEART_BEAT,  //
        Sensor.TYPE_HEART_RATE,
        Sensor.TYPE_LIGHT,
        Sensor.TYPE_LINEAR_ACCELERATION,
        Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT,
        Sensor.TYPE_MAGNETIC_FIELD,  //
        Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED,
        Sensor.TYPE_MOTION_DETECT,
        Sensor.TYPE_POSE_6DOF,
        Sensor.TYPE_PRESSURE,
        Sensor.TYPE_PROXIMITY,  //
        Sensor.TYPE_RELATIVE_HUMIDITY,
        Sensor.TYPE_ROTATION_VECTOR,
        Sensor.TYPE_SIGNIFICANT_MOTION,
        Sensor.TYPE_STATIONARY_DETECT,
        Sensor.TYPE_STEP_COUNTER,  //
        Sensor.TYPE_STEP_DETECTOR
    )
    private val sensorNameList = arrayOf(
        "ACCELEROMETER",
        "ACCELEROMETER_UNCALIBRATED",
        "AMBIENT_TEMPERATURE",
        "DEVICE_PRIVATE_BASE",
        "GAME_ROTATION_VECTOR",  //
        "GEOMAGNETIC_ROTATION_VECTOR",
        "GRAVITY",
        "GYROSCOPE",
        "GYROSCOPE_UNCALIBRATED",
        "HEART_BEAT",  //
        "HEART_RATE",
        "LIGHT",
        "LINEAR_ACCELERATION",
        "LOW_LATENCY_OFFBODY_DETECT",
        "MAGNETIC_FIELD",  //
        "MAGNETIC_FIELD_UNCALIBRATED",
        "MOTION_DETECT",
        "POSE_6DOF",
        "PRESSURE",
        "PROXIMITY",  //
        "RELATIVE_HUMIDITY",
        "ROTATION_VECTOR",
        "SIGNIFICANT_MOTION",
        "STATIONARY_DETECT",
        "STEP_COUNTER",  //
        "STEP_DETECTOR"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.text_view)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
    }

    override fun onResume() {
        super.onResume()

        // 端末で使用できるセンサーを表示
//        checkSensors()

        // リストから利用可能かどうかを表示
        checkSensorsEach()

    }

    private fun checkSensors() {
        val sensors = sensorManager!!.getSensorList(Sensor.TYPE_ALL)
        val strListbuf = StringBuffer("Sensor List:\n\n")
        var count = 0
        for (sensor in sensors) {
            count++
            val str = String.format(
                "%s: %s\n", (count + 1).toString(), sensor.name
            )
            strListbuf.append(str)
        }
        textView!!.text = strListbuf
    }

    private fun checkSensorsEach() {
        val strbuf = StringBuffer("Sensor List:\n\n")
        for (i in 0 until sensorList.size) {
            val sensor = sensorManager!!.getDefaultSensor(sensorList[i])
            if (sensor != null) {
                val strTmp = java.lang.String.format(
                    "%s: %s: 使用可能\n", (i + 1).toString(),
                    sensorNameList[i]
                )
                strbuf.append(strTmp)
            } else {
                val strTmp = java.lang.String.format(
                    "%s: %s: XXX 不可\n", (i + 1).toString(),
                    sensorNameList[i]
                )
                strbuf.append(strTmp)
            }
        }
        textView!!.text = strbuf
    }

}