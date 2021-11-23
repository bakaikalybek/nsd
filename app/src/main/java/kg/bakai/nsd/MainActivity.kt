package kg.bakai.nsd

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import kg.bakai.nsd.databinding.ActivityMainBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.net.ServerSocket
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private const val TAG = "SOCKET"
    }

    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModel<MainViewModel>()
    private val mServer = SocketServer(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindView()

        startServer()
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnSend -> {
                viewModel.sendMessage(binding.etMessage.text.toString())
            }
            binding.btnDiscover -> {
                viewModel.findDevices()
            }
        }
    }

    private fun bindView() {
        binding.apply {
            btnSend.setOnClickListener(this@MainActivity)
            btnDiscover.setOnClickListener(this@MainActivity)
            viewModel.loading.observe(this@MainActivity) { loading ->
                tvServer.text = "Loading ..."
                progressBar.isVisible = loading
                btnSend.isEnabled = !loading
                btnDiscover.isEnabled = !loading
            }
            viewModel.message.observe(this@MainActivity) {
                Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
            }
            viewModel.server.observe(this@MainActivity) {
                tvServer.text = "Connected to: ${it.hostAddress}"
            }
        }
    }

    private fun startServer() {
        val thread = Thread(mServer)
        thread.start()
    }

    override fun onResume() {
        super.onResume()
        viewModel.findDevices()
    }
}