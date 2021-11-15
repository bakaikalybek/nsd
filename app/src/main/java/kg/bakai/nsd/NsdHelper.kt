package kg.bakai.nsd

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdManager.DiscoveryListener
import android.net.nsd.NsdManager.RegistrationListener
import android.net.nsd.NsdServiceInfo
import android.util.Log
import android.widget.Toast

class NsdHelper(private val context: Context) {
    private val TAG = "LOG"
    private var mServiceName = "nsd-test"
    private val SERVICE_TYPE = "_http._tcp."

    private val nsdManager = (context.getSystemService(Context.NSD_SERVICE) as NsdManager)
    private var mResolveListener: NsdManager.ResolveListener? = null
    private var mDiscoveryListener: DiscoveryListener? = null
    private var mRegistrationListener: RegistrationListener? = null

    private var mService: NsdServiceInfo? = null

    fun initializeNsd() {
        initializeResolveListener()
    }

    private fun initializeResolveListener() {
        mResolveListener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                Log.w(TAG, "Resolve failed: $errorCode")
            }

            override fun onServiceResolved(serviceInfo: NsdServiceInfo?) {
                Log.w(TAG, "Resolve success: $serviceInfo")

                if (serviceInfo?.serviceName.equals(mServiceName)) {
                    return
                }
                mService = serviceInfo
            }
        }
    }

    private fun initializeDiscoveryListener() {
        mDiscoveryListener = object : NsdManager.DiscoveryListener {
            override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
                Log.w(TAG, "Discovery start failed")
                nsdManager.stopServiceDiscovery(this)
            }

            override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
                Log.w(TAG, "Discovery stop failed")
                nsdManager.stopServiceDiscovery(this)
            }

            override fun onDiscoveryStarted(serviceType: String?) {
                Log.w(TAG, "Discovery started")
            }

            override fun onDiscoveryStopped(serviceType: String?) {
                Log.w(TAG, "Discovery stopped")
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
                Log.w(TAG, "Discovery found: $serviceInfo")
                when {
                    serviceInfo?.serviceType != SERVICE_TYPE -> {
                        Toast.makeText(context, "Unknown service type: ${serviceInfo?.serviceType}", Toast.LENGTH_SHORT).show()
                    }
                    serviceInfo.serviceName.equals(mServiceName) -> {
                        Log.w(TAG, "Same machine")

                        nsdManager.resolveService(serviceInfo, mResolveListener)
                    }
                    serviceInfo.serviceName.contains(mServiceName) -> {
                        Log.w(TAG, serviceInfo.serviceType)

                        nsdManager.resolveService(serviceInfo, mResolveListener)
                    }
                }
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
                Log.w(TAG, "Service lost: $serviceInfo")

                if (mService == serviceInfo) {
                    mService = null
                }
            }
        }
    }

    private fun initializeRegistrationListener() {
        mRegistrationListener = object : NsdManager.RegistrationListener {
            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                Log.w(TAG, "Registration failed: $serviceInfo")
            }

            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                Log.w(TAG, "Service unregister failed: $errorCode")
            }

            override fun onServiceRegistered(serviceInfo: NsdServiceInfo?) {
                Log.w(TAG, "Discovery started")
                mServiceName = serviceInfo?.serviceName!!
            }

            override fun onServiceUnregistered(serviceInfo: NsdServiceInfo?) {
                Log.w(TAG, "Service unregistered: ${serviceInfo?.serviceName}")
            }
        }
    }

    fun registerService(port: Int) {
        tearDown()
        initializeRegistrationListener()
        val serviceInfo = NsdServiceInfo().apply {
            setPort(port)
            serviceName = mServiceName
            serviceType = SERVICE_TYPE
        }

        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener)
    }

    fun discoverServices() {
        stopDiscovery()
        initializeDiscoveryListener()
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener)
    }

    fun stopDiscovery() {
        if (mDiscoveryListener != null) {
            try {
                nsdManager.stopServiceDiscovery(mDiscoveryListener)
            } finally {
                Log.w(TAG, "Discovery stop finally block")
            }
            mDiscoveryListener = null
        }
    }

    fun tearDown() {
        if (mRegistrationListener != null) {
            try {
                nsdManager.unregisterService(mRegistrationListener)
            } finally {
                Log.w(TAG, "Unregister finally block")
            }
            mRegistrationListener = null
        }
    }

    fun getChosenInfo(): NsdServiceInfo? {
        return mService
    }
}