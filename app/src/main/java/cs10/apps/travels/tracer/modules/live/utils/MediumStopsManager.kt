package cs10.apps.travels.tracer.modules.live.utils

import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.enums.TransportType
import cs10.apps.travels.tracer.model.Viaje
import cs10.apps.travels.tracer.modules.live.entity.MediumStop
import cs10.apps.travels.tracer.modules.live.model.Stage

class MediumStopsManager(val travel: Viaje) {
    val start = travel.nombrePdaInicio
    val end = travel.nombrePdaFin
    var stops = mutableListOf(start, end)
    private val candidatesAsked = mutableListOf<String>()

    suspend fun buildStops(db: MiDB) : MediumStopsManager {
        if (stops.size == 2){
            if (travel.tipo == TransportType.BUS.ordinal){
                travel.linea?.let {
                    buildStopsForBusRamal(it, travel.ramal, travel.nombrePdaFin, db)
                }
            }
        }

        return this
    }

    suspend fun rebuildStops(db: MiDB) {
        stops = mutableListOf(start, end)
        buildStops(db)
    }

    private suspend fun buildStopsForBusRamal(line: Int, ramal: String?, dest: String, db: MiDB){
        val all = db.safeStopsDao().getMediumStopsCreatedForBusTo(line, ramal, dest)
        val aux = mutableListOf<MediumStop>()
        var finish = false
        var i = 0

        while (!finish){
            val target = all.filter { it.prev == stops[i] }
            if (target.size > 1) {
                for(j in 1 until target.size) db.safeStopsDao().deleteMediumStop(target[j])
                throw Exception("Multiple target for ${stops[i]}, deleted all but ${target.first()}")
            }

            if (target.isEmpty()) finish = true        // no more medium stops...
            else {
                stops.add(i+1, target.first().name)
                aux.add(target.first())
                i++
            }
        }

        if (aux.isNotEmpty() && aux.last().next != end) {
            throw Exception("Incomplete path: last 2 stops unmatched - $all")
        }
    }

    fun checkIfCanAdd(candidate: String): Boolean {
        if (candidate == start || candidate == end) return false
        if (stops.contains(candidate)) return false
        return !candidatesAsked.contains(candidate)
    }

    suspend fun getAddQuestion(candidate: String, currentStage: Stage, db: MiDB): String {
        val prevName = db.safeStopsDao().getNameByCoords(currentStage.start.getX(), currentStage.start.getY())
        val nextName = db.safeStopsDao().getNameByCoords(currentStage.end.getX(), currentStage.end.getY())
        val msg = "¿Quieres añadir $candidate como una parada intermedia entre $prevName y $nextName?"

        // avoid to ask again...
        candidatesAsked.add(candidate)
        return msg
    }

    suspend fun add(candidate: String, currentStageStart: String, currentStageEnd: String, db: MiDB): Boolean {
        val ms = MediumStop(0, type = travel.tipo, line = travel.linea, ramal = travel.ramal,
            prev = currentStageStart, name = candidate,
            next = currentStageEnd, destination = travel.nombrePdaFin)

        val place = stops.indexOf(currentStageEnd)
        if (place < 0) return false

        db.safeStopsDao().insertMediumStop(ms)
        stops.add(place, candidate)

        // FIX: ALSO MODIFY NEXT MEDIUM STOP
        if (travel.tipo == TransportType.BUS.ordinal) travel.linea?.let {
            db.safeStopsDao().updateNextBusMediumStop(it, travel.ramal, travel.nombrePdaFin, ms.next, ms.name)
        }

        return true
    }

    suspend fun add(candidate: String, currentStage: Stage, db: MiDB): Boolean {
        val prevName = db.safeStopsDao().getNameByCoords(currentStage.start.getX(), currentStage.start.getY())
        val nextName = db.safeStopsDao().getNameByCoords(currentStage.end.getX(), currentStage.end.getY())
        if (prevName == null || nextName == null) return false

        return add(candidate, prevName, nextName, db)
    }

    fun countStops() : Int = stops.size
}