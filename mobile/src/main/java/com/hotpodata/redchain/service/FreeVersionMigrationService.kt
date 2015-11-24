package com.hotpodata.redchain.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import com.hotpodata.redchain.ChainMaster
import timber.log.Timber

/**
 * Created by jdrotos on 11/19/15.
 */
class FreeVersionMigrationService : Service() {
    //Target we publish for clients to send messages to IncomingHandler.
    var mMessenger: Messenger

    init {
        mMessenger = Messenger(DataMigrationRequestHandler());

    }

    public object Constants {
        public val MSG_REQUEST_FREE_VERSION_DATA = 1;
        public val MSG_FREE_VERSION_DATA = 2
    }

    /**
     * Handler of incoming messages from clients.
     */
    public class DataMigrationRequestHandler : Handler() {
        override public fun handleMessage(msg: Message) {
            Timber.d("handleMessage:" + msg.what)
            if (msg.what == Constants.MSG_REQUEST_FREE_VERSION_DATA) {
                if (msg.replyTo != null) {
                    var response = Message.obtain(null, Constants.MSG_FREE_VERSION_DATA)
                    var data = ChainMaster.chainToBundle(ChainMaster.getSelectedChain())
                    response.data = data
                    Timber.d("handleMessage - responding")
                    msg.replyTo.send(response)
                }
            } else {
                super.handleMessage(msg)
            }
        }
    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    public override fun onBind(intent: Intent): IBinder {
        Timber.d("onBind")
        return mMessenger.binder;
    }
}