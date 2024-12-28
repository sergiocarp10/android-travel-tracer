package cs10.apps.travels.tracer.pages.stops.db

import androidx.room.Dao
import androidx.room.Query
import cs10.apps.travels.tracer.model.Parada

@Dao
interface StopsDao {

    @Query("SELECT * FROM Parada P INNER JOIN Zone Z ON (P.latitud BETWEEN Z.x0 AND Z.x1) " +
            "AND (P.longitud BETWEEN Z.y0 AND Z.y1) WHERE Z.id = :zoneId ORDER BY P.nombre ASC")
    suspend fun getStopsInZone(zoneId: Long): MutableList<Parada>
}