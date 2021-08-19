package com.example.hemisphone.prototype

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.hardware.SensorEventListener
import android.util.Log


// https://tomoima525.hatenablog.com/entry/2014/01/13/152559
class AccAdapter(manager: SensorManager) : SensorEventListener {
    //端末が実際に取得した加速度値。重力加速度も含まれる。This values include gravity force.
    private val currentOrientationValues = floatArrayOf(0.0f, 0.0f, 0.0f)

    //ローパス、ハイパスフィルタ後の加速度値 Values after low pass and high pass filter
    private val currentAccelerationValues = floatArrayOf(0.0f, 0.0f, 0.0f)

    //diff 差分
    private var dx = 0.0f
    private var dy = 0.0f
    private var dz = 0.0f

    //previous data 1つ前の値
    private var old_x = 0.0f
    private var old_y = 0.0f
    private var old_z = 0.0f

    //ベクトル量
    private var vectorSize = 0.0

    //カウンタ
    var counter: Long = 0

    //一回目のゆれを省くカウントフラグ（一回の端末の揺れで2回データが取れてしまうのを防ぐため）
    //count flag to prevent aquiring data twice with one movement of a device
    var counted = false

    // X軸加速方向
    var vecx = true

    // Y軸加速方向
    var vecy = true

    // Z軸加速方向
    var vecz = true

    //ノイズ対策
    var noiseflg = true

    //ベクトル量(最大値)
    var vectorMax = 0.0
        private set

    fun getx(): Float {
        return dx
    }

    fun gety(): Float {
        return dy
    }

    fun getz(): Float {
        return dz
    }

    fun getcounter(): Long {
        return counter
    }

    fun stopSensor(manager: SensorManager?) {

        // センサー停止時のリスナ解除 Stopping Listener
        var manager = manager
        manager?.unregisterListener(this)
        manager = null
    }

    override fun onAccuracyChanged(arg0: Sensor?, arg1: Int) {
        // TODO 自動生成されたメソッド・スタブ
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // 取得 Acquiring data

            // ローパスフィルタで重力値を抽出　Isolate the force of gravity with the low-pass filter.
            currentOrientationValues[0] =
                event.values[0] * 0.1f + currentOrientationValues[0] * (1.0f - 0.1f)
            currentOrientationValues[1] =
                event.values[1] * 0.1f + currentOrientationValues[1] * (1.0f - 0.1f)
            currentOrientationValues[2] =
                event.values[2] * 0.1f + currentOrientationValues[2] * (1.0f - 0.1f)

            // 重力の値を省くRemove the gravity contribution with the high-pass filter.
            currentAccelerationValues[0] = event.values[0] - currentOrientationValues[0]
            currentAccelerationValues[1] = event.values[1] - currentOrientationValues[1]
            currentAccelerationValues[2] = event.values[2] - currentOrientationValues[2]

            // ベクトル値を求めるために差分を計算　diff for vector
            dx = currentAccelerationValues[0] - old_x
            dy = currentAccelerationValues[1] - old_y
            dz = currentAccelerationValues[2] - old_z
            vectorSize = Math.sqrt((dx * dx + dy * dy + dz * dz).toDouble())

            // 一回目はノイズになるから省く
            if (noiseflg == true) {
                noiseflg = false
            } else {
                if (vectorSize > THRESHOLD /* && dz <0.0f */) {
                    if (counted == true) {
//                        println("$dx,$dz,$vectorSize")
                        counter++
                        counted = false
                        // System.out.println("count is "+counter);
                        // 最大値なら格納
                        if (vectorSize > vectorMax) {
                            vectorMax = vectorSize
                        }
                    } else if (counted == false) {
                        counted = true
                    }
                }
            }

            // 状態更新
            //vectorSize_old = vectorSize;
            old_x = currentAccelerationValues[0]
            old_y = currentAccelerationValues[1]
            old_z = currentAccelerationValues[2]

            Log.d(TAG, "current Acc: %.2f, %.2f, %.2f".format(currentAccelerationValues[0], currentAccelerationValues[1], currentAccelerationValues[2]))
        }
    }

    // 指定ミリ秒実行を止めるメソッド
    @Synchronized
    fun sleep(msec: Long) {
        try {
            sleep(msec)
        } catch (e: InterruptedException) {
        }
    }

    fun getValues(): FloatArray
    {
        return currentAccelerationValues
    }

    companion object {
        //THRESHOLD ある値以上を検出するための閾値
        protected const val THRESHOLD = 4.0
        protected const val THRESHOLD_MIN = 1.0

        //low pass filter alpha ローパスフィルタのアルファ値
        protected const val alpha = 0.8f

        val TAG = "AccAdapter"
    }

    init {
        // construct sensor
        val sensors: List<Sensor> = manager.getSensorList(Sensor.TYPE_ACCELEROMETER)
        if (sensors.isNotEmpty()) {
            val s: Sensor = sensors[0]
            manager.registerListener(this, s, SensorManager.SENSOR_DELAY_UI)
        }
    }
}