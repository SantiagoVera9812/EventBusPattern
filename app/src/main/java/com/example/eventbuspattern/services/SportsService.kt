package com.example.eventbuspattern.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.eventbuspattern.eventBus.EventBus
import com.example.eventbuspattern.eventBus.SportEvent
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


class SportsService: Service() {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    fun saveResul(result: SportEvent.ResultSuccess){
        scope.launch {

            val response = if (result.isWarning)
                SportEvent.ResultError(30, "Error al guardar")
            else SportEvent.SaveEvent

            EventBus.instance().publish(response)
        }
    }


    fun setUpSubscribers(viewScope: CoroutineScope) {
        viewScope.launch {
            Log.d("eventSubscribe", "suscribing")
            EventBus.instance().subscribe<SportEvent> { event ->

                Log.d("eventSubscribe", event.toString())


                when(event){

                    is SportEvent.CloseEent ->
                        Log.i("CursosAnt", "Ad was cloee sent to data server")
                        //binding.btnAd.visibility = View.GONE
                    else -> {}

                }
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
    }

    //Singleton
    companion object{
        private val _service: SportsService by lazy {SportsService()}

        fun instance() = _service
    }
}