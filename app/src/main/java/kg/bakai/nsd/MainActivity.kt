package kg.bakai.nsd

import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.os.StrictMode
import android.util.Log
import android.view.View
import kg.bakai.nsd.databinding.ActivityMainBinding
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress


class MainActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        const val TAG = "Activity"
    }

    private lateinit var binding: ActivityMainBinding
    private val mServer = SocketServer(this)
    private val mBroadcastReceive = BroadcastReceive()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindView()

        startServer()
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnSend -> {
                val client = SocketClient()
                client.execute(binding.etIp.text.toString(), binding.etMessage.text.toString())
            }
            binding.btnStartSender -> {
                sendBroadcast()

            }
            binding.btnStartReceiver -> {
                binding.btnStartReceiver.isEnabled = false
                startBroadcastReceiver()
            }
            binding.btnDiscover -> {
                val finder = NetworkFinder(this)
                finder.execute()
            }
        }
    }

    private fun bindView() {
        binding.apply {
            btnSend.setOnClickListener(this@MainActivity)
            btnStartSender.setOnClickListener(this@MainActivity)
            btnStartReceiver.setOnClickListener(this@MainActivity)
            btnDiscover.setOnClickListener(this@MainActivity)
        }
    }

    private fun startServer() {
        val thread = Thread(mServer)
        thread.start()
    }

    private fun startBroadcastReceiver() {
        val thread = Thread(mBroadcastReceive)
        thread.start()
    }

    private fun sendBroadcast(message: String = "Hello Server I am broadcast") {
        Log.i(TAG, "sendBroadcast")
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        try {
            Log.i(TAG, "sendBroadcast try block")
            val socket = DatagramSocket()
            socket.broadcast = true
            val data = message.toByteArray()
            val sendPacket = DatagramPacket(data, data.size, getBroadcastAddress(), 8080)
            socket.send(sendPacket)
            Log.i(TAG, "Broadcast sent to: ${getBroadcastAddress().hostAddress}")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getBroadcastAddress(): InetAddress {
        val wifi = (getSystemService(WIFI_SERVICE) as WifiManager)
        val dhcp = wifi.dhcpInfo

        val broadcast = dhcp.ipAddress and dhcp.netmask or dhcp.netmask.inv()
        val quads = ByteArray(4)
        for (k in 0..3) quads[k] = (broadcast shr k * 8 and 0xFF).toByte()

//        return InetAddress.getByAddress(quads)
        return InetAddress.getByName("192.168.31.118")
    }

    private fun fetchAvailableIpAddresses() {

    }
}