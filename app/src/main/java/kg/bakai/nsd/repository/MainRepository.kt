package kg.bakai.nsd.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kg.bakai.nsd.Converter
import kg.bakai.nsd.data.model.Addon
import kg.bakai.nsd.data.model.Product
import kotlinx.coroutines.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainRepository(private val context: Context) {
    private val TAG = "SOCKET"

    private val loading = MutableLiveData<Boolean>()
    private val server = MutableLiveData<InetAddress>()
    private val message = MutableLiveData<String>()

    private val products = mutableListOf<Product>(
        Product(
            name = "Latte",
            count = 1,
            price = 5.4,
            addons = listOf(
                Addon(
                    name = "Caramel",
                    count = 1,
                    price = 2.3
                )
            )
        )
    )

    fun findDevices() {
        loading.postValue(true)
        CoroutineScope(Dispatchers.IO).launch {
            val localInetAddress = getInetAddress()
            val prefix = localInetAddress.hostAddress!!.substring(0, localInetAddress.hostAddress!!.lastIndexOf(".") + 1)

            val devicesLocal = mutableListOf<InetAddress>()
            Log.i(TAG, "searching devices ...")
            launch {
                for (num in 0..254) {
                    launch {
                        val testIp = prefix + num
                        val address = InetAddress.getByName(testIp)
                        val reachable = address.isReachable(1000)
                        if (reachable) {
                            if (localInetAddress != address) {
                                devicesLocal.add(address)
                            } else {
                                Log.i(TAG, "$address is same machine")
                            }
                        }
                    }
                }
            }.join()
            launch {
                for (device in devicesLocal) {
                    launch {
                        Log.i(TAG, "Connecting to: ${device.hostAddress}")
                        connectToServer(device.hostAddress!!)
                    }
                }
            }.join()
            loading.postValue(false)
        }
    }

    fun sendMessage(input: String) {
        products.add(Product(
            name = input,
            count = 2,
            price = 5.4,
            addons = listOf(
                Addon(
                    name = "Caramel",
                    count = 1,
                    price = 2.3
                )
            )
        )
        )
        Log.i(TAG, "sendMessage from vm")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val s = Socket(server.value?.hostAddress, 64666)
//                val oos = ObjectOutputStream(s.getOutputStream())
                val dos = DataOutputStream(s.getOutputStream())
                val dis = DataInputStream(s.getInputStream())
                val mes = Converter.listToJson(products)
                dos.writeUTF(mes)
//                oos.writeObject(products)
                Log.i(TAG, "Message sent to: ${server.value?.hostAddress}")

                Log.i(TAG, "Message sent is: $mes")
                message.postValue(dis.readUTF())
//                oos.close()

                dos.close()
                s.close()
            } catch (e: IOException) {
                Log.i(TAG, "Couldn't connect to Server: ${e.localizedMessage}")
            }
        }
    }

    fun removeOne() {
        products.clear()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val s = Socket(server.value?.hostAddress, 64666)
//                val oos = ObjectOutputStream(s.getOutputStream())
                val dos = DataOutputStream(s.getOutputStream())
                val dis = DataInputStream(s.getInputStream())
                val mes = Converter.listToJson(products)
                dos.writeUTF(mes)
//                oos.writeObject(products)
                Log.i(TAG, "Message sent to: ${server.value?.hostAddress}")

                Log.i(TAG, "Message sent is: $mes")
                message.postValue(dis.readUTF())
//                oos.close()

                dos.close()
                s.close()
            } catch (e: IOException) {
                Log.i(TAG, "Couldn't connect to Server: ${e.localizedMessage}")
            }
        }

    }


    private fun connectToServer(ip: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val s = Socket(ip, 64666)
                if (s.isConnected) {
                    server.postValue(InetAddress.getByName(ip))
                    val dos = DataOutputStream(s?.getOutputStream())
                    dos.writeUTF(Converter.listToJson(products))
                    message.postValue(DataInputStream(s?.getInputStream()).readUTF())

                    Log.i(TAG, "Connected to: $ip")

                    dos.close()
                    s.close()
                }
            } catch (e: IOException) {
                Log.i(TAG, "Couldn't connect to server: $ip")
            }
        }
    }

    private fun getInetAddress(): InetAddress = runBlocking {
        val cm = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
        val activeNetwork = cm.activeNetwork
        val wm = (context.getSystemService(Context.WIFI_SERVICE) as WifiManager)

        val connectionInfo = wm.connectionInfo
        InetAddress.getByAddress(
            ByteBuffer
                .allocate(Integer.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(connectionInfo.ipAddress)
                .array()
        )
    }

    fun isLoading(): LiveData<Boolean> = loading

    fun receivedMessage(): LiveData<String> = message

    fun connectedServer(): LiveData<InetAddress> = server

}