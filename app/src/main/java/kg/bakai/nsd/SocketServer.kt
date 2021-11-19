package kg.bakai.nsd

import android.content.Context
import android.os.Handler
import android.util.Log
import android.widget.Toast
import java.io.DataInputStream
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket

class SocketServer(private val context: Context): Runnable {
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