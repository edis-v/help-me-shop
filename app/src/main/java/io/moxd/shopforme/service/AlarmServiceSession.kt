package io.moxd.shopforme.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.moxd.shopforme.requireAuthManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

class AlarmServiceSession : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
         //refresh session
        try {


            GlobalScope.launch {
                requireAuthManager().auth2()
            }
        }catch (ex:Exception){

        }
    }
}