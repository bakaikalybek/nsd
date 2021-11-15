package kg.bakai.nsd.connection

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import java.io.*
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.UnknownHostException
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

class ChatConnection(private val mUpdateHandler: Handler) {
    private val mChatServer = ChatServer(mUpdateHandler)
    private var mChatClient: ChatClient? = null

    private val TAG = "LOG"

    private var mSocket: Socket? = null
    private var mPort = -1

    fun teardown() {
        mChatServer.tearDown()
        mChatClient?.tearDown()
    }
    fun connectToServer(address: InetAddress, port: Int) {
        mChatClient = ChatClient(address, port)
    }

    fun sendMessage(msg: String) {
        mChatClient?.sendMessage(msg)
    }

    fun getLocalPort(): Int {
        return mPort
    }

    fun setLocalPort(port: Int) {
        mPort = port
    }

    @Synchronized fun updateMessages(msg: String, local: Boolean) {
        Log.w(TAG, "Updating: $msg")
        var newMsg = if (local) {
            "me: $msg"
        } else {
            "them: $msg"
        }

        val messageBundle = Bundle()
        messageBundle.putString("msg", newMsg)

        val message = Message()
        message.data = messageBundle
        mUpdateHandler.sendMessage(message)
    }

    @Synchronized fun setSocket(socket: Socket?) {
        Log.w(TAG, "setSocket being called")

        if (socket == null) {
            Log.w(TAG, "setting a null socket")
        }
        if (mSocket != null) {
            if (mSocket?.isConnected!!) {
                try {
                    mSocket?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        Log.w(TAG, "$socket")
        mSocket = socket
    }

    fun getSocket(): Socket? {
        return mSocket
    }

    inner class ChatServer(private val mUpdateHandler: Handler) {
        private var mServerSocket: ServerSocket? = null
        private var mThread: Thread = Thread(ServerThread())

        init {
            mThread.start()
        }

        fun tearDown() {
            mThread.interrupt()
            try {
                mServerSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error when closing server socket.")
            }
        }

        inner class ServerThread: Runnable {
            override fun run() {
                try {
                    mServerSocket = ServerSocket(0)
                    setLocalPort(mServerSocket?.localPort!!)

                    while (!Thread.currentThread().isInterrupted) {
                        Log.d(TAG, "ServerSocket Created, awaiting connection");
                        setSocket(mServerSocket?.accept())
                        Log.d(TAG, "Connected.");
                        if (mChatClient == null) {
                            val port = mSocket?.port
                            val address = mSocket?.inetAddress
                            connectToServer(address!!, port!!)
                        }
                    }
                } catch (e: IOException) {
                    Log.d(TAG, "Error creatin connection: $e");
                    e.printStackTrace()

                }
            }

        }

    }

    inner class ChatClient(private val address: InetAddress, private val port: Int) {
        private val CLIENT_TAG = "LOG"

        private var mSendThread: Thread? = Thread(SendingThread())
        private var mRecThread: Thread? = null

        init {
            mSendThread?.start()
        }

        inner class SendingThread: Runnable {

            var mMessageQueue: BlockingQueue<String> = ArrayBlockingQueue(10)

            override fun run() {
                try {
                    if (getSocket() == null) {
                        setSocket(Socket(address, port))
                        Log.d(CLIENT_TAG, "Client-side socket initialized.");
                    } else {
                        Log.d(CLIENT_TAG, "Socket already initialized. skipping");
                    }
                    mRecThread = Thread(ReceivingThread())
                    mRecThread?.start()
                } catch (e: UnknownHostException) {
                    Log.d(CLIENT_TAG, "Initializing socket failed, UHE", e);
                } catch (e: IOException) {
                    Log.d(CLIENT_TAG, "Initializing socket failed, UHE", e);
                }

                while (true) {
                    try {
                        val msg = mMessageQueue.take()
                        sendMessage(msg)
                    } catch (ie: InterruptedException) {
                        Log.d(CLIENT_TAG, "Message sending loop interrupted, exiting")
                    }
                }
            }
        }

        inner class ReceivingThread: Runnable {
            override fun run() {
                var input: BufferedReader
                try {
                    input = BufferedReader(InputStreamReader(mSocket?.getInputStream()))
                    while (!Thread.currentThread().isInterrupted) {
                        var messageStr: String? = null
                        messageStr = input.readLine()
                        if (messageStr != null) {
                            Log.d(CLIENT_TAG, "Read from the stream: $messageStr")
                            updateMessages(messageStr, false)
                        } else {
                            Log.d(CLIENT_TAG, "The nulls! The nulls!")
                            break
                        }
                    }
                    input.close()
                } catch (e: IOException) {
                    Log.d(CLIENT_TAG, "The nulls! The nulls!")
                }

            }
        }

        fun tearDown() {
            try {
                getSocket()?.close()
            } catch (ioe: IOException) {
                Log.e(CLIENT_TAG, "Error when closing server socket.")
            }
        }

        fun sendMessage(msg: String) {
            try {
                if (getSocket() == null) {
                    Log.d(CLIENT_TAG, "Socket is null")
                } else if (getSocket()?.getOutputStream() == null) {
                    Log.d(CLIENT_TAG, "Socket output stream is null")
                }

                val out = PrintWriter(
                    BufferedWriter(OutputStreamWriter(getSocket()?.getOutputStream())),
                    true
                )
                out.println(msg)
                out.flush()
                updateMessages(msg, true)
            } catch (e: UnknownHostException) {
                Log.d(CLIENT_TAG, "Unknown Host", e);
            } catch (e: IOException) {
                Log.d(CLIENT_TAG, "I/O Exception", e);
            } catch (e: Exception) {
                Log.d(CLIENT_TAG, "Error3", e);
            }
            Log.d(CLIENT_TAG, "Client sent message: $msg");
        }
    }


}