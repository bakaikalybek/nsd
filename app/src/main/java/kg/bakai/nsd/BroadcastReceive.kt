package kg.bakai.nsd

import android.util.Log
import kg.bakai.nsd.MainActivity.Companion.TAG
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class BroadcastReceive: Runnable {
    override fun run() {
        Log.i(MainActivity.TAG,"startBroadcastReceiver");
        try {
            val socket = DatagramSocket(8080, InetAddress.getByName("192.168.31.118"))
            socket.broadcast = true

            while (true) {
                Log.i(MainActivity.TAG,"Ready to receive broadcast packets!")
                val recvBuf = ByteArray(2048)
                val packet = DatagramPacket(recvBuf, recvBuf.size)
                socket.receive(packet)
                Log.i(MainActivity.TAG,"Packet receiver: ${packet.address.hostAddress}");
                val data = String(packet.data).trim()
                Log.i(MainActivity.TAG,"Received data: $data");
            }
        } catch (e: IOException) {
            Log.i(TAG, e.toString())
        }
    }
}