package kg.bakai.nsd

import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import kg.bakai.nsd.databinding.ActivityMainBinding
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket

class MainActivity : AppCompatActivity() {

    private val TAG = "SERVER"
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val thread = Thread(MyServer(this))
        thread.start()

        bindView()
    }


    private fun bindView() {
        binding.apply {
            btnSend.setOnClickListener {
                val b = BackgroundTask()
                b.execute(etIp.text.toString(), etMessage.text.toString())
            }
        }
    }
}

class MyServer(private val context: Context): Runnable {
    private val TAG = "SERVER"
    var ss: ServerSocket? = null
    var client: Socket? = null
    var dis: DataInputStream? = null
    var message = ""
    private val handler = Handler()

    override fun run() {
        try {
            ss = ServerSocket(8080)
            if (ss != null) {
                handler.post { Toast.makeText(context, "Waiting for client", Toast.LENGTH_SHORT).show() }
                Log.i(TAG, ss?.localSocketAddress.toString())
                while (true) {
                    client = ss?.accept()
                    Log.i(TAG, "New client: ${client?.inetAddress} ${client?.localPort}")
                    dis = DataInputStream(client?.getInputStream())
                    message = dis?.readUTF()!!
                    handler.post { Toast.makeText(context, "Message received: $message", Toast.LENGTH_SHORT).show() }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}


class BackgroundTask: AsyncTask<String, Unit, String>() {
    private val TAG = "LOG"

    private var s: Socket? = null
    private var dos: DataOutputStream? = null
    private var message = ""
    private var ip = ""

    override fun doInBackground(vararg params: String?): String {
        ip = params[0].toString()
        message = params[1].toString()

        try {
            s = Socket("192.168.31.$ip", 8080)
            dos = DataOutputStream(s?.getOutputStream())
            dos?.writeUTF(message)

            dos?.close()

            s?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }
}