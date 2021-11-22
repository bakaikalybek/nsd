package kg.bakai.nsd

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket

class SocketClient: AsyncTask<String, Unit, String>() {
    private val TAG = "SOCKET"

    private var s: Socket? = null
    private var dos: DataOutputStream? = null
    private var message = ""
    private var ip = ""
    private var port = 0

    override fun doInBackground(vararg params: String?): String {
        ip = params[0].toString()
        port = params[1]?.toInt()!!
        message = params[2].toString()

        try {
            s = Socket(ip, port)
            dos = DataOutputStream(s?.getOutputStream())
            dos?.writeUTF(message)

            dos?.close()

            s?.close()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.i(TAG, "Couldn't connect to Server: ${e.localizedMessage}")
        }
        return ""
    }
}