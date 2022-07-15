package com.example.exploedview.map

import com.carto.vectorelements.Polygon

object MapConst {

    var SELECT: Boolean = false
    var GROUP: Boolean = false
    var CONTAINS: Boolean = false

    const val FILL_OPACITY: Short = 125
    const val STROKE_OPACITY: Short = 255

    const val INCREASE_FLOOR_NUM = 8
    const val INCREASE_LINE_NUM = 10

    var BASE_EXPLODEDVIEW = mutableListOf<Polygon>()

    /**
     *  Google Mercator: 구글지도/빙지도/야후지도/OSM 등 에서 사용중인 좌표계
     */
    val PROJ4_3857 = arrayOf(
        "+proj=merc",
        "+a=6378137",
        "+b=6378137",
        "+lat_ts=0.0",
        "+lon_0=0.0",
        "+x_0=0.0",
        "+y_0=0",
        "+k=1.0",
        "+units=m",
        "+nadgrids=@null",
        "+Fwktext",
        "+no_defs"
    )
}