package cs10.apps.travels.tracer.modules.live.model

import android.location.Location
import cs10.apps.common.android.Localizable
import cs10.apps.common.android.NumberUtils
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.Point
import cs10.apps.travels.tracer.model.Viaje
import kotlin.Exception

class StagedTravel(val stages: List<Stage>) {
    val totalDist = (0.0).plus(stages.sumOf { it.kmDistance })
    val start = stages[0].start
    val end = stages.last().end

    fun calculateCurrentStage(currentPos: Localizable) : Stage {
        val currentProg = currentProgress(currentPos)
        var found: Stage? = null

        for (s in stages){
            if (found != null) s.progress = 0
            else {
                val d1 = s.end.coordsDistanceTo(start)
                val d2 = s.end.coordsDistanceTo(end)
                val endProg = 100 * d1 / (d1+d2)

                if (endProg < currentProg) s.progress = 100
                else {
                    s.updateProgressFor(currentPos)
                    found = s
                }
            }
        }

        found?.let { return it }
        return stages.last()
    }

    fun currentProgress(currentPos: Localizable) : Double {
        val startDist = currentPos.coordsDistanceTo(start)
        val endDist = currentPos.coordsDistanceTo(end)
        return 100 * startDist / (startDist + endDist)
    }

    fun calculateCloserPoint(currentPos: Localizable) : Localizable {
        var minDist = currentPos.coordsDistanceTo(start)
        var minPoint = start

        for (s in stages){
            val dist = currentPos.coordsDistanceTo(s.end)
            if (dist < minDist) {
                minDist = dist
                minPoint = s.end
            }
        }

        return minPoint
    }

    companion object {

        fun from(t: Viaje, db: MiDB) : StagedTravel {
            val st = from(t.nombrePdaInicio, t.nombrePdaFin, db)
            st.stages[0].startTime = t.startHour * 60 + t.startMinute
            return st
        }

        fun from(startName: String, endName: String, db: MiDB) : StagedTravel {
            val start = db.paradasDao().getByName(startName)
            val end = db.paradasDao().getByName(endName)

            if (start.nombre == "Cruce Varela" && end.nombre == "Av. 1 y 48") {
                val alpargatas = db.paradasDao().getByName("Alpargatas")
                val pzaItalia = db.paradasDao().getByName("Plaza Italia")
                return withStops(arrayOf(start, alpargatas, pzaItalia, end))
            }

            if (start.nombre == "Estación La Plata" && end.nombre == "Estación Varela"){
                val bera = db.paradasDao().getByName("Estación Berazategui")
                return withStops(arrayOf(start, bera, end))
            }

            if (start.nombre == "Km 26" && end.nombre == "Estación Adrogué"){
                val temperley = db.paradasDao().getByName("Estación Temperley")
                return withStops(arrayOf(start, temperley, end))
            }

            return StagedTravel(listOf(Stage(start, end)))
        }

        fun withStops(pdas: Array<Localizable>) : StagedTravel {
            if (pdas.size < 2) throw Exception("Origin and destination undefined")
            val stages = mutableListOf<Stage>()
            val n = pdas.size

            for (i in 1 until n) stages.add(Stage(pdas[i-1], pdas[i]))
            return StagedTravel(stages)
        }
    }
}