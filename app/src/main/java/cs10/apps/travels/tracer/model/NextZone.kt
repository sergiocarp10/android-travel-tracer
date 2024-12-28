package cs10.apps.travels.tracer.model

import cs10.apps.travels.tracer.pages.manage_zones.model.Zone

data class NextZone(val zone: Zone, val minutesLeft: Int)