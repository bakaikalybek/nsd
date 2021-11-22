package kg.bakai.nsd.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainRepository(private val context: Context) {
    private val TAG = "SOCKET"

    fun getInetAddress(): InetAddress {
        val cm = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
        val activeNetwork = cm.activeNetwork
        val wm = (context.getSystemService(Context.WIFI_SERVICE) as WifiManager)

        val connectionInfo = wm.connectionInfo
        return InetAddress.getByAddress(
            ByteBuffer
                .allocate(Integer.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(connectionInfo.ipAddress)
                .array()
        )
    }
}