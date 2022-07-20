package com.example.exploedview.map

import com.carto.core.Variant
import com.carto.vectorelements.Polygon

object MapConst {

    var SELECT: Boolean = false
    var GROUP: Boolean = false
    var CONTAINS: Boolean = false

    const val FONT_SIZE = 20F

    const val FILL_OPACITY: Short = 125
    const val STROKE_OPACITY: Short = 255

    // EPSG:4326
    const val INCREASE_FLOOR_NUM = 8
    const val INCREASE_LINE_NUM = 10

    val PROPERTIES_VALUE_ARR = arrayListOf("APT_NO", "BD_MGT_SN", "ADM_CD", "HO_NM", "NSO_NM", "NSO_NM_DC", "HU_NUM_YN", "POED", "POED_HEAD", "POED_GRP", "STAIR_GRP", "HU_NUM", "PRT_NM", "PRT_NM_DC", "COORD_X", "COORD_Y")

    val PROPERTIES_VALUE_MAP= mapOf(
        "APT_NO" to Variant("APT_NO"),
        "BD_MGT_SN" to Variant("BD_MGT_SN"),
        "ADM_CD" to Variant("ADM_CD"),
        "HO_NM" to Variant("HO_NM"),
        "NSO_NM" to Variant("NSO_NM"),
        "NSO_NM_DC" to Variant("NSO_NM_DC"),
        "HU_NUM_YN" to Variant("HU_NUM_YN"),
        "POED" to Variant("POED"),
        "POED_HEAD" to Variant("POED_HEAD"),
        "POED_GRP" to Variant("POED_GRP"),
        "STAIR_GRP" to Variant("STAIR_GRP"),
        "HU_NUM" to Variant("HU_NUM"),
        "PRT_NM" to Variant("PRT_NM"),
        "PRT_NM_DC" to Variant("PRT_NM_DC"),
        "COORD_X" to Variant("COORD_X"),
        "COORD_Y" to Variant("COORD_Y"),
    )

    // EPSG:3857
//    const val INCREASE_FLOOR_NUM = 844600.069804
//    const val INCREASE_LINE_NUM = 1224514.398726

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