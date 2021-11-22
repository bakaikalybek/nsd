package kg.bakai.nsd

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kg.bakai.nsd.repository.MainRepository
import kotlinx.coroutines.*
import java.io.*
import java.net.InetAddress
import java.net.Socket
import kotlin.concurrent.thread

class MainViewModel(private val repository: MainRepository): ViewModel() {
    private val TAG = "SOCKET"

    val loading = MutableLiveData(false)
    val message = MutableLiveData<String>()
    val devices = MutableLiveData<MutableList<InetAddress>>()

    private fun connectToServer(ip: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val s = Socket(ip, 64666)
                val dos = DataOutputStream(s?.getOutputStream())
                val dis = DataInputStream(s?.getInputStream())
                dos.writeUTF("Connect")

                Log.i(TAG, "Connected to: $ip")
                message.postValue(dis.readUTF())

                dos.close()

                s.close()
            } catch (e: IOException) {
                Log.i(TAG, "Couldn't connect to server: $ip ${e.localizedMessage}")
            }
        }
    }

    fun sendMessage(input: String) {
        Log.i(TAG, "sendMessage from vm")
        CoroutineScope(Dispatchers.IO).launch {
            devices.value?.forEach {
                try {
                    val s = Socket(it.hostAddress, 64666)
                    val dos = DataOutputStream(s.getOutputStream())
                    dos.writeUTF(input)
                    Log.i(TAG, "Connected to: ${it.hostAddress}")
                    val dis = DataInputStream(s.getInputStream())
                    message.postValue(dis.readUTF())
                    dos.close()

                    s.close()
                } catch (e: IOException) {
                    Log.i(TAG, "Couldn't connect to Server: ${e.localizedMessage}")
                }
            }
        }
    }

    fun findDevices() {
        loading.postValue(true)
        CoroutineScope(Dispatchers.IO).launch {
            val localInetAddress = repository.getInetAddress()
            val prefix = localInetAddress.hostAddress!!.substring(0, localInetAddress.hostAddress!!.lastIndexOf(".") + 1)

            val devicesLocal = mutableListOf<InetAddress>()
            val findDevices = launch {
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
            }
            Log.i(TAG, "searching devices ...")
            findDevices.join()
            devicesLocal.forEach {
                Log.i(TAG, "Connecting to: ${it.hostAddress}")
                connectToServer(it.hostAddress!!)

            }
            devices.postValue(devicesLocal)
            loading.postValue(false)
        }
    }
}