package cs10.apps.travels.tracer.pages.manage_zones

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import cs10.apps.common.android.ui.LocalizableActivity
import cs10.apps.travels.tracer.databinding.ActivityZoneViewBinding
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.Parada
import cs10.apps.travels.tracer.pages.stops.adapter.StopAdapter
import cs10.apps.travels.tracer.pages.stops.editor.StopEditor
import cs10.apps.travels.tracer.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ZoneViewActivity : LocalizableActivity() {
    private lateinit var binding: ActivityZoneViewBinding
    private var zoneId = -1L
    private val adapter = StopAdapter(mutableListOf()) { stop -> onStopClick(stop) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityZoneViewBinding.inflate(layoutInflater)
        setSupportActionBar(binding.toolbar)
        setContentView(binding.root)
        Utils.loadBusBanner(binding.appbarImage)

        zoneId = intent.getLongExtra("id", -1L)
        binding.toolbarLayout.title = intent.getStringExtra("name") ?: "Zona"
        binding.fab.setOnClickListener { openEditor() }

        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter
        populateRecycler()

        getLocation { latitude, longitude ->
            onSetDistances(latitude, longitude)
        }
    }

    private fun populateRecycler() {
        CoroutineScope(Dispatchers.IO).launch {
            val db = MiDB.getInstance(this@ZoneViewActivity)
            val currentZone = db.zonesDao().getZoneWithId(zoneId)
            val stops = db.stopsDao().getStopsInZone(zoneId)
            stops.forEach { it.zone = currentZone }

            CoroutineScope(Dispatchers.Main).launch {
                adapter.paradasList = stops
                adapter.notifyItemRangeInserted(0, stops.size)
            }
        }
    }

    private fun onSetDistances(latitude: Double, longitude: Double) {
        adapter.paradasList.forEach {
            it.updateDistance(latitude, longitude)
            it.angle = Utils.calculateAngle(it, latitude, longitude).toFloat()
        }
        adapter.notifyDataSetChanged()
    }

    private fun openEditor() {
        val intent = Intent(this, ZoneEditor::class.java)
        intent.putExtra("id", zoneId)
        startActivity(intent)
    }

    private fun onStopClick(stop: Parada) {
        val intent = Intent(this, StopEditor::class.java)
        intent.putExtra(StopEditor.IDENTIFIER_KEY, stop.nombre)
        startActivity(intent)
    }
}