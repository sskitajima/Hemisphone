package com.example.hemisphone.prototype

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class DeviceSelectActivity : AppCompatActivity() {
    companion object
    {
        val TAG = "DeviceSelectActivity"
        private const val REQUEST_ENABLE_BLUETOOTH = 1
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    // device Map
    private var mDeviceNameAndMAC: MutableMap<String, String> = mutableMapOf()

    // bluetooth
    private val mBluetoothAdapter: BluetoothAdapter by lazy{ BluetoothAdapter.getDefaultAdapter()}
    private var isRunning = false
    private var isConnecting: Boolean = false
    private var mBTAdapter: BluetoothAdapter? = null
    private var mBTDevice: BluetoothDevice? = null
    private var mBTSocket: BluetoothSocket? = null
    private var mInputStream: InputStream? = null
    private var mOutputStream: OutputStream? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_select)

        // bluetoothが有効化されていない場合、有効化するように促す
        if (!mBluetoothAdapter!!.isEnabled) {
            Log.e(TAG, "bluetooth not enabled")
            Toast.makeText(this@DeviceSelectActivity, "bluetooth is not enabled", Toast.LENGTH_SHORT).show()

            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }

        /// ペアリング済のデバイスを取得して、listViewに表示
        val deviceList = mutableListOf<String>()
        val macList = mutableListOf<String>()

        val pairedDevices: Set<BluetoothDevice> = mBluetoothAdapter.bondedDevices
        if (pairedDevices.size > 1) {
            for (device in pairedDevices) {
                Log.d(TAG, "device: ${device.name}, MAC: ${device.address}")
                deviceList.add(device.name)
                macList.add(device.address)

                mDeviceNameAndMAC[device.name] = device.address
            }
        }

        val listView = findViewById<ListView>(R.id.lv_device)
        val adapter = ArrayAdapter(this@DeviceSelectActivity, android.R.layout.simple_list_item_1, deviceList)
        listView.adapter = adapter

        listView.onItemClickListener = ListItemClickListener()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        ///////////////////////////////////////////
        // メイン画面のデータを受け取る
        isConnecting = intent.getBooleanExtra("isConnect", false);

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected()")

        var returnVal = true

        // 「戻る」ボタンの場合、アクティビティを終了
        if(item.itemId == android.R.id.home)
        {
            finish()
        }
        else
        {
            returnVal = super.onOptionsItemSelected(item)
        }

        return returnVal
    }

    private inner class ListItemClickListener: AdapterView.OnItemClickListener
    {
        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            Log.d(TAG, "ListItemClickListener.onItemClick()")

            // タップされたデバイス名を取得
            val deviceName = parent.getItemAtPosition(position) as String
            val deviceMAC = mDeviceNameAndMAC[deviceName]
            Log.d(TAG, "selected device: $deviceName, MAC: $deviceMAC")

            Toast.makeText(this@DeviceSelectActivity,"item selected: $deviceName", Toast.LENGTH_SHORT).show()
        }
    }


    //////////////////////////////////////////////////////
    /// Bluetooth

    private fun BTConnect(macAddress: String) {
        Log.d(TAG, "BTConnect")

        //BTアダプタのインスタンスを取得
        mBTAdapter = BluetoothAdapter.getDefaultAdapter()

        //相手先BTデバイスのインスタンスを取得
        mBTDevice = mBTAdapter?.getRemoteDevice(macAddress)
        //ソケットの設定
        mBTSocket = try {
            mBTDevice?.createRfcommSocketToServiceRecord(SPP_UUID)
        } catch (e: IOException) {
            null
        }
        if (mBTSocket != null) {
            //接続開始
            mBTAdapter?.cancelDiscovery()
            try {
                mBTSocket!!.connect()
                Log.d(TAG, "success to connect")
            } catch (connectException: IOException) {
                Log.d(TAG, "IOException in connect", connectException)
                isRunning = false
                mBTSocket = try {
                    mBTSocket!!.close()
                    null
                } catch (closeException: IOException) {
                    return
                }
            }
        }

        //ソケットが取得出来たら、出力用ストリームを作成する
        if (mBTSocket != null) {
            try {
                mOutputStream = mBTSocket!!.outputStream
            } catch (e: IOException) { /*ignore*/
                Log.d(TAG, "IOException in getting outputStream", e)
            }
        }


//        val intent = Intent()
//        intent.putExtra("mBTSocket", mBTSocket)
//        setResult(Activity.RESULT_OK, intent)

    }

}