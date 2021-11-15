package kg.bakai.nsd

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import kg.bakai.nsd.connection.ChatConnection
import kg.bakai.nsd.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val TAG = "LOG"
    private lateinit var binding: ActivityMainBinding

    private lateinit var nsdHelper: NsdHelper
    private lateinit var mConnection: ChatConnection

    private var mUpdateHandler: Handler? = null

    @SuppressLint("HandlerLeak")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mUpdateHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                val chatline = msg.data.getString("msg")
                addChatline(chatline)
            }
        }

        bindView()
    }

    private fun clickAdvertise() {
        if (mConnection.getLocalPort() > -1) {
            nsdHelper.registerService(mConnection.getLocalPort())
        } else {
            Log.d(TAG, "ServerSocket isn't bound.")
        }
    }

    private fun clickDiscover() {
        nsdHelper.discoverServices()
    }

    private fun clickConnect() {
        val service = nsdHelper.getChosenInfo()
        Log.d(TAG, "$service")
        if (service != null) {
            mConnection.connectToServer(service.host, service.port)
        } else {
            Log.d(TAG, "No service to connect to!")
        }
    }

    private fun clickSend() {
        val messageString = binding.editText.text.toString()
        if (messageString.isNotEmpty()) {
            mConnection.sendMessage(messageString)
        }
        binding.editText.setText("")
    }

    fun addChatline(line: String?) {
        binding.textView.append("\n" + line)
    }

    private fun bindView() {
        binding.apply {
            register.setOnClickListener {
                clickAdvertise()
            }
            discover.setOnClickListener {
                clickDiscover()
            }
            resolve.setOnClickListener {
                clickConnect()
            }
            btnSend.setOnClickListener {
                clickSend()
            }
        }
    }

    override fun onStart() {
        mConnection = ChatConnection(mUpdateHandler!!)
        nsdHelper = NsdHelper(this)
        nsdHelper.initializeNsd()
        super.onStart()
    }

    override fun onPause() {
        super.onPause()
        nsdHelper.stopDiscovery()
    }

    override fun onResume() {
        super.onResume()
        nsdHelper.discoverServices()
    }

    override fun onStop() {
        nsdHelper.tearDown()
        mConnection.teardown()
        super.onStop()
    }

}