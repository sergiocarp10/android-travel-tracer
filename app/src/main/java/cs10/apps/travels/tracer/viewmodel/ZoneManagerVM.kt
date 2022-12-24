package cs10.apps.travels.tracer.viewmodel

import android.app.Application
import androidx.lifecycle.*
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.db.ZonesDao
import cs10.apps.travels.tracer.domain.GetAllZonesUseCase
import cs10.apps.travels.tracer.model.Zone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ZoneManagerVM(application: Application) : AndroidViewModel(application) {

    private val zonesDao: ZonesDao
    private var getAllZonesUseCase: GetAllZonesUseCase

    init {
        val database: MiDB = MiDB.getInstance(getApplication<Application>().applicationContext)
        zonesDao = database.zonesDao()
        getAllZonesUseCase = GetAllZonesUseCase(zonesDao)
    }

    // mis live data
    private var myZones = MutableLiveData<MutableList<Zone>>()
    private lateinit var editingZone: Zone


    fun load(rootVM: RootVM){
        rootVM.enableLoading()

        viewModelScope.launch(Dispatchers.IO){

            // load zones
            val zones = getAllZonesUseCase()

            // process zones
            zones.forEach { z ->
                val stats = zonesDao.countTravelsIn(z.x0, z.x1, z.y0, z.y1)
                z.zoneStats = stats
            }

            // post zones
            zones.sort()
            myZones.postValue(zones)

            rootVM.disableLoading()
        }
    }

    fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<MutableList<Zone>>) {
        myZones.observe(lifecycleOwner, observer)
    }

    fun selectEditing(item: Zone) {
        editingZone = item
    }
}