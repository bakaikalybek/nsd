package kg.bakai.nsd

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket

class SocketClient: AsyncTask<String, Unit, String>() {
    private val TAG = "Activity"

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
            Log.i(TAG, "Couldn't connect to Server: ${e.localizedMessage}")
        }
        return ""
    }
}