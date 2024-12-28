package cs10.apps.common.android.ui

import android.os.Bundle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import cs10.apps.travels.tracer.utils.Utils

open class LocalizableActivity : FormActivity() {
    private lateinit var client: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        client = LocationServices.getFusedLocationProviderClient(this)
    }

    @Throws(SecurityException::class)
    fun getLocation(onGetLocation: (Double, Double) -> Unit) {
        if (Utils.checkPermissions(this)) client.lastLocation.addOnSuccessListener {
            if (it != null){
                onGetLocation(it.latitude, it.longitude)
            }
        }
    }
}