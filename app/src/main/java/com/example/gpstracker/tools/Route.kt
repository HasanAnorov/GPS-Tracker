package com.example.gpstracker.tools

class Route {

    private var overViewPolyline: OverviewPolyline? = null

     fun getOverViewPolyline(): OverviewPolyline? {
        return overViewPolyline
    }

     fun setOverViewPolyline(overviewPolyline:OverviewPolyline) {
        this.overViewPolyline = overviewPolyline
    }
}