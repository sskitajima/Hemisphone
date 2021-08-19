package com.example.hemisphone.prototype

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.os.HandlerCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(){
    companion object {
        private const val TAG = "MAIN_ACTIVITY_DEBUG_TAG"

        // SPECTRE-X13
//        private const val TARGET_DEVICE_NAME = "SPECTRE-X13"      // 接続端末に応じて変える
//        private val MacAddress = "38:00:25:94:09:39"

        // RNBT-95E6
        private const val TARGET_DEVICE_NAME = "RNBT-95E6"      // 接続端末に応じて変える
        private val MacAddress = "68:27:19:F3:95:E6"

        private const val REQUEST_ENABLE_BLUETOOTH = 1
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        private const val LIGHT_TAG = "L"
        private const val VIBE_TAG = "V"
    }

    // bluetooth
    private var isRunning = false
    private var isConnecting = false
    private val mBluetoothAdapter: BluetoothAdapter? by lazy{ BluetoothAdapter.getDefaultAdapter() }
    private var mBTDevice: BluetoothDevice? = null
    private var mBTSocket: BluetoothSocket? = null
    private var mInputStream: InputStream? = null
    private var mOutputStream: OutputStream? = null

    // Acc
    private lateinit var mSensorManager: SensorManager
//    private var mAcc: Sensor? = null
    private lateinit var mAccAdapter: AccAdapter

    // 選択された値
    private var mLightValue: Int = 0
    private var mlightType: String = "Blue"
    private var mVibeValue: Int = 0
    private var mSpeedValue: Int = 50

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // bluetoothを端末がサポートしているかチェック
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "bluetooth is not supported.")
            Toast.makeText(this@MainActivity, R.string.bluetooth_is_not_supported, Toast.LENGTH_SHORT).show()
            return
        }
    }

    override fun onStart() {
        super.onStart()

        setup()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        super.onDestroy()

        if (mBTSocket != null) {
            try {
                mBTSocket!!.connect()
            }
                catch (connectException: IOException) { /*ignore*/
            }
            mBTSocket = null
        }
    }

    //////////////////////////////////////////////////////
    /// Option Menu

    // オプションメニューを作成する
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_options_settings, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        var returnVal = true
//
//        when(item.itemId)
//        {
//            R.id.menuListOptionConnection->
//            {
//                Log.d(TAG, "menuListOptionConnection selected")
//                val intent2DeviceSelect = Intent(this@MainActivity, DeviceSelectActivity::class.java)
//                intent2DeviceSelect.putExtra("isConnect", isConnecting)
//                startActivity(intent2DeviceSelect)
//            }
//            else->
//            {
//                returnVal = super.onOptionsItemSelected(item)
//            }
//        }
//
//        return returnVal
//    }

    //////////////////////////////////////////////////////


    //////////////////////////////////////////////////////
    /// Listener
    private inner class ButtonClickListener(): View.OnClickListener
    {
        override fun onClick(view: View)
        {
            var inputString: String = ""

            // 押されたボタンに応じて、送信する文字列を作成する
            when(view.id)
            {
                R.id.bt_light->{
                    inputString = createCommand(LIGHT_TAG)
                }
                R.id.bt_vibe->{
                    inputString = createCommand(VIBE_TAG)
                }
//                R.id.sendButton->{
//                    inputString = findViewById<EditText>(R.id.editText).text.toString()
//                }
                R.id.bt_connection->{
                    Log.d(TAG, "bt_connection")
                    if(!isConnecting)
                    {
                        val executorService = Executors.newSingleThreadScheduledExecutor()
                        executorService.submit(object: Runnable{
                            override fun run()
                            {
                                BTConnect()
                                updateConnectStatus(isConnecting)
                                if(!isConnecting)
                                {
                                    Toast.makeText(this@MainActivity, getString(R.string.connection_failed), Toast.LENGTH_SHORT).show()
                                }
                            }
                        })
                    }
                    else
                    {
                        Toast.makeText(this@MainActivity, getString(R.string.bluetooth_is_already_connecting), Toast.LENGTH_SHORT).show()
                    }

                }
            }

            if(mBTSocket!=null)
            {
                Log.d(TAG, "input_string: $inputString")

                val executeService = Executors.newSingleThreadExecutor()
                executeService.submit(object: Runnable{
                    override fun run() {
                        Log.d(TAG, "send() function service start")
                        Send(inputString)
                    }
                })
//                Send(inputString)

                Toast.makeText(this@MainActivity, "Send Command: $inputString", Toast.LENGTH_SHORT).show()
            }
            else{
                Log.d(TAG, "ButtonClickListener: !sendFlag or mBTSockt == null")
                Toast.makeText(this@MainActivity, R.string.bluetooth_is_not_connected, Toast.LENGTH_SHORT).show()
            }

//            // 目的のデバイスが見つかったら、別のスレッドで処理を実装する
//            val handler = HandlerCompat.createAsync(mainLooper)
//            val connectHandler = ConnectHandler(handler, mmTargetDevice, inputString)
//            val executorSurvice = Executors.newSingleThreadExecutor()
//            executorSurvice.submit(connectHandler)
        }

    }

    private inner class SeekBarChangedListener: SeekBar.OnSeekBarChangeListener
    {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean)
        {
            Log.d(TAG, "onProgressChanged")

            when(seekBar.id)
            {
                R.id.sb_light ->
                {
                    val lightTextView = findViewById<TextView>(R.id.tv_light_val)
                    lightTextView.text = progress.toString()
                    mLightValue = progress
                }

                R.id.sb_vibe ->
                {
                    val vibeTextView = findViewById<TextView>(R.id.tv_vibe_val)
                    vibeTextView.text = progress.toString()
                    mVibeValue = progress
                }
                R.id.sb_speed ->
                {
                    val speedTextView = findViewById<TextView>(R.id.tv_speed_val)
                    speedTextView.text = progress.toString()
                    mSpeedValue = progress
                }


            }

            println("progress: $progress")
        }

        override fun onStartTrackingTouch(seek : SeekBar)
        {
        }

        override fun onStopTrackingTouch(seek : SeekBar)
        {
        }
    }

    //////////////////////////////////////////////////////
    /// Bluetooth

    private fun BTConnect() {
        Log.d(TAG, "BTConnect")

        //BTアダプタのインスタンスを取得
//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        //相手先BTデバイスのインスタンスを取得
        mBTDevice = mBluetoothAdapter?.getRemoteDevice(MacAddress)
        //ソケットの設定
        mBTSocket = try {
            mBTDevice?.createRfcommSocketToServiceRecord(SPP_UUID)
        } catch (e: IOException) {
            isConnecting = false
            null
        }
        if (mBTSocket != null) {
            //接続開始
            mBluetoothAdapter?.cancelDiscovery()
            try {
                mBTSocket!!.connect()
                Log.d(TAG, "success to connect")
            } catch (connectException: IOException) {
                Log.d(TAG, "IOException in connect", connectException)
                isRunning = false
                mBTSocket = try {
                    mBTSocket!!.close()
                    isConnecting = false
                    null
                } catch (closeException: IOException){
                    isConnecting = false
                    null
                }
            }
        }

        //ソケットが取得出来たら、出力用ストリームを作成する
        if (mBTSocket != null) {
            try {
                mOutputStream = mBTSocket!!.outputStream
                isConnecting = true
            } catch (e: IOException) { /*ignore*/
                Log.d(TAG, "IOException in getting outputStream", e)
                isConnecting = false
            }
        }

        Log.d(TAG, "end of BTConnect(): isConnecting: $isConnecting")
    }

    private fun Send(str: String) {
        Log.d(TAG, "Send")

        //文字列を送信する
        isRunning = true
        var bytes_ = byteArrayOf()
        bytes_ = str.toByteArray()
        try {
            mOutputStream!!.write(bytes_)


            val executorService = Executors.newSingleThreadExecutor()
            executorService.submit {
                Log.d(TAG, "waiting message from remote device")

                mInputStream = mBTSocket!!.inputStream
                // InputStreamのバッファを格納
                val buffer = ByteArray(1024)

                // InputStreamの読み込み
                val bytes: Int? = mInputStream?.read(buffer)
                //            Log.i(TAG, "bytes=$bytes")
                // String型に変換
                val readMsg = bytes?.let { String(buffer, 0, it) }
                // null以外なら表示
                if (readMsg != null) {
                    Log.d(TAG, "readMsg: $readMsg")

                    //                val valueMsg = Message()
                    //                valueMsg.obj = readMsg
                    //                mHandler.sendMessage(valueMsg)
                }
            }

        }
        catch (e: IOException) {
            try {
                mBTSocket!!.close()
            } catch (e1: IOException) { /*ignore*/
            }
        }
    }

    //////////////////////////////////////////////////////
    // member function
    private fun setup()
    {
        // シークバー
        val seekBarLightView = findViewById<SeekBar>(R.id.sb_light)
        val seekBarLightSpeed = findViewById<SeekBar>(R.id.sb_speed)
        val seekBarVibeView = findViewById<SeekBar>(R.id.sb_vibe)

        seekBarLightSpeed.setProgress(mSpeedValue)
        findViewById<TextView>(R.id.tv_speed_val).text = mSpeedValue.toString()


        val onSeekBarChangedListener = SeekBarChangedListener()
        seekBarLightView?.setOnSeekBarChangeListener(onSeekBarChangedListener)
        seekBarLightSpeed?.setOnSeekBarChangeListener(onSeekBarChangedListener)
        seekBarVibeView?.setOnSeekBarChangeListener(onSeekBarChangedListener)

        /////////////////////////////

        // ラジオボタン
        val radioButtonLight = findViewById<RadioGroup>(R.id.radioGroup)
        radioButtonLight.check(R.id.rb_blue)
        radioButtonLight?.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener{
            override fun onCheckedChanged(group: RadioGroup, checkedId: Int)
            {
//                val selectedTextView = findViewById<TextView>(R.id.selectedLightType)

                if(checkedId != -1)
                {
                    val radioButton = findViewById<RadioButton>(checkedId)
                    val lightType = radioButton.getText().toString()
//                    selectedTextView.text = lightType
                    mlightType = lightType

                }
                else
                {
//                    selectedTextView.text = "nothing selected"
                }
            }
        })

        ////////////////////////////////
        // ボタン
        val sendButtonLight = findViewById<Button>(R.id.bt_light)
        val sendButtonVibe = findViewById<Button>(R.id.bt_vibe)
//        val sendButton = findViewById<Button>(R.id.bt_)
        val connectButton = findViewById<Button>(R.id.bt_connection)

        val buttonClickListener = ButtonClickListener()
        sendButtonLight.setOnClickListener(buttonClickListener)
        sendButtonVibe.setOnClickListener(buttonClickListener)
//        sendButton.setOnClickListener(buttonClickListener)
        connectButton.setOnClickListener(buttonClickListener)

        ////////////////////////////////
        // Bluetooth

        // bluetoothが有効化されていない場合、有効化するように促す
        if (!mBluetoothAdapter!!.isEnabled) {
            Log.e(TAG, "bluetooth not enabled")
            Toast.makeText(this@MainActivity, "bluetooth is not enabled", Toast.LENGTH_SHORT).show()

            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }

//        BTConnect()
        findViewById<TextView>(R.id.tv_bt_status).text = getString(R.string.bluetooth_connecting)

        val executorService = Executors.newSingleThreadScheduledExecutor()
        executorService.submit(object: Runnable{
            override fun run()
            {
                BTConnect()
                updateConnectStatus(isConnecting)
            }
        })



        // 接続の処理を実装できていないため
//        findViewById<Button>(R.id.bt_connection).isEnabled = false


        // sensor
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
//        mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//        mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_UI)

        mAccAdapter = AccAdapter(mSensorManager)


        val handler = Handler()
        val r: Runnable = object : Runnable {
            override fun run() {
                // UIスレッド
                updateAcc() // 何かやる
                handler.postDelayed(this, 500) // 500ms
            }
        }
        handler.post(r)
    }


    private fun updateConnectStatus(isConnect: Boolean)
    {
        Log.d(TAG, "updateConnectStatus()")

        isConnecting =isConnect

        if(isConnect)
        {
            val textView = findViewById<TextView>(R.id.tv_bt_status)
            textView.text = "Connected: ${mBTDevice?.name}"
            textView.setTextColor(resources.getColor(R.color.black))

//            findViewById<Button>(R.id.sendButton).isEnabled = true
            findViewById<Button>(R.id.bt_light).isEnabled = true
            findViewById<Button>(R.id.bt_vibe).isEnabled = true
        }
        else
        {
            Log.d(TAG,  getString(R.string.bluetooth_is_not_connected))
            val textView = findViewById<TextView>(R.id.tv_bt_status)
            textView.text = getString(R.string.bluetooth_is_not_connected)
            textView.setTextColor(resources.getColor(R.color.red))

//            findViewById<Button>(R.id.sendButton).isEnabled = false
            findViewById<Button>(R.id.bt_light).isEnabled = false
            findViewById<Button>(R.id.bt_vibe).isEnabled = false
        }
    }

    private fun createCommand(type: String): String
    {
        var command = ""
        when(type)
        {
            LIGHT_TAG->
            {
//                command = "L,$mlightType,$mLightValue,$mSpeedValue"
                command = "L,$mlightType,$mLightValue,${mSpeedValue}x"
            }
            VIBE_TAG->
            {
//                command = "V,$mVibeValue\n"
                command = "V,${mVibeValue}x"
            }
        }

        return command
    }


    private fun updateAcc()
    {
//        Log.d(TAG, "updateAcc()")
        super.onResume()

        val accArray = mAccAdapter.getValues()
        val accTextView = findViewById<TextView>(R.id.tv_acc)
        val accStrength = abs(accArray[0]) + abs(accArray[1]) + abs(accArray[2])
        accTextView.text = "X: %.2f Y: %.2f Z: %.2f\nStrength: %.2f".format(accArray[0], accArray[1], accArray[2], accStrength)

        mLightValue = (accStrength.roundToInt()) * 5
        if(mLightValue < 0) mLightValue = 0
        if(mLightValue > 100) mLightValue = 100
    }

}