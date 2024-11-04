package com.example.eventbuspattern

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventbuspattern.adapters.OnClickListener
import com.example.eventbuspattern.adapters.ResultAdapter
import com.example.eventbuspattern.dataAccess.getAdEventsInRealtime
import com.example.eventbuspattern.dataAccess.getResultEventsInRealtime
import com.example.eventbuspattern.dataAccess.someTime
import com.example.eventbuspattern.databinding.ActivityMainBinding
import com.example.eventbuspattern.eventBus.EventBus
import com.example.eventbuspattern.eventBus.SportEvent
import com.example.eventbuspattern.services.SportsService
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpAdapter()
        setUpRecyclerView()
        setUpSwipeRefresh()
        setupClicks()
        setUpSubscribers()


    }

    private fun setupClicks() {
        binding.btnAd.run{
            setOnClickListener{
                lifecycleScope.launch {
                    binding.srlResults.isRefreshing = true
                    val events = getAdEventsInRealtime()
                    EventBus.instance().publish(events.first())
                }
            }
            setOnLongClickListener{ view ->
                lifecycleScope.launch {
                    binding.srlResults.isRefreshing = true
                    EventBus.instance().publish(SportEvent.CloseEent)

                }
                true
            }
        }
    }

    private fun setUpSwipeRefresh() {
        binding.srlResults.setOnRefreshListener {
            adapter.clear()
            getEvents()
            binding.btnAd.visibility = View.VISIBLE
        }
    }

    private fun setUpSubscribers() {
        lifecycleScope.launch {
            SportsService.instance().setUpSubscribers(this)
            Log.d("eventSubscribe", "suscribing")
            EventBus.instance().subscribe<SportEvent> { event ->

                Log.d("eventSubscribe", event.toString())

                binding.srlResults.isRefreshing = false

                when(event){
                    is SportEvent.ResultSuccess ->
                        adapter.add(event)
                    is SportEvent.SaveEvent ->
                        Toast.makeText(this@MainActivity, "Guardado", Toast.LENGTH_SHORT).show()
                    is SportEvent.ResultError ->
                        Snackbar.make(binding.root, "Code: ${event.code}, Message: ${event.msg}",
                            Snackbar.LENGTH_LONG).show()
                    is SportEvent.AdEvent ->
                        Toast.makeText(this@MainActivity, "Ad click, send data to server...", Toast.LENGTH_SHORT).show()
                    is SportEvent.CloseEent ->
                        binding.btnAd.visibility = View.GONE

                }
            }
        }
    }

    private fun getEvents(){
        lifecycleScope.launch {

            val events = getResultEventsInRealtime()
            events.forEach{ event ->
                delay(someTime())
                Log.d("event", event.toString())
                EventBus.instance().publish(event)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("onStart", "on start rn")
        binding.srlResults.isRefreshing = true
        getEvents()

    }

    private fun setUpAdapter() {
        adapter = ResultAdapter(this)
    }

    private fun setUpRecyclerView() {
        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    override fun onClick(result: SportEvent.ResultSuccess) {

        lifecycleScope.launch {

            binding.srlResults.isRefreshing = true
            SportsService.instance().saveResul(result)
        }

        //Toast.makeText(this@MainActivity, "Guardado", Toast.LENGTH_SHORT).show()

    }


}