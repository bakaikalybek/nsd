package kg.bakai.nsd

import android.content.Context
import android.os.Handler
import android.util.Log
import android.widget.Toast
import kg.bakai.nsd.data.model.Test
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.net.ServerSocket
import java.net.Socket

class SocketServer(private val context: Context): Runnable {
    private val TAG = "SOCKET"
    var ss: ServerSocket? = null
    var client: Socket? = null
    var dis: DataInputStream? = null
    var dos: DataOutputStream? = null
    var ois: ObjectInputStream? = null
    var message = ""
    private val handler = Handler()

    override fun run() {
        try {
            ss = ServerSocket(64666)
            if (ss != null) {
                handler.post { Toast.makeText(context, "Waiting for client", Toast.LENGTH_SHORT).show() }
                Log.i(TAG, "$ss")
                while (true) {
                    client = ss?.accept()
                    Log.i(TAG, "New client: ${client?.inetAddress} ${client?.localPort}")
//                    dis = DataInputStream(client?.getInputStream())
                    dos = DataOutputStream(client?.getOutputStream())
                    ois = ObjectInputStream(client?.getInputStream())
                    dos?.writeUTF("Delivery: succeed")
                    handler.post { Toast.makeText(context, "Message received: $message", Toast.LENGTH_SHORT).show() }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}