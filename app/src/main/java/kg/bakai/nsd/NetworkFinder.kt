package kg.bakai.nsd

import android.content.Context
import android.net.ConnectivityManager
import android.net.InetAddresses
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.util.Log
import java.lang.ref.WeakReference
import java.math.BigInteger
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

class NetworkFinder(context: Context): AsyncTask<String, Unit, String>() {
    private val TAG = "FINDER"

    private var mContext: WeakReference<Context> = WeakReference<Context>(context)

    override fun doInBackground(vararg params: String?): String {
        Log.i(TAG, "Start ip finder")
        val context = mContext.get()
        try {
            val cm = (context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
            val activeNetwork = cm.activeNetwork
            val wm = (context.getSystemService(Context.WIFI_SERVICE) as WifiManager)

            val connectionInfo = wm.connectionInfo
            val ipString = InetAddress.getByAddress(
                ByteBuffer
                    .allocate(Integer.BYTES)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .putInt(connectionInfo.ipAddress)
                    .array()
            ).hostName

            Log.d(TAG, "activeNetwork: $activeNetwork")
            Log.d(TAG, "ipString: $ipString")

            val prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1)
            Log.d(TAG, "prefix: $prefix")

            for (i in 0..254) {
                val testIp = prefix + i.toString()
                val address = InetAddress.getByName(testIp)
                val reachable = address.isReachable(10000)
                val hostName = address.canonicalHostName
                if (reachable) Log.i(TAG, "Host: $hostName($testIp) is reachable!")
            }
        } catch (t: Throwable) {
            Log.i(TAG, "Error: ${t.localizedMessage}")
        }
        return ""
    }


}