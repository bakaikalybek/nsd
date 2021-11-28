package kg.bakai.nsd

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kg.bakai.nsd.repository.MainRepository
import kotlinx.coroutines.*
import java.io.*
import java.net.InetAddress
import java.net.Socket

class MainViewModel(private val repository: MainRepository): ViewModel() {
    private val TAG = "SOCKET"

    val loading = repository.isLoading()
    val message = repository.receivedMessage()
    val server = repository.connectedServer()

    fun findDevices() {
        repository.findDevices()
    }

    fun sendMessage(input: String) {
        repository.sendMessage(input)
    }

    fun removeOne() {
        repository.removeOne()
    }

}