package com.example.exploedview.map

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

    val PROPERTIES_VALUE_ARR = arrayListOf(
        "APT_NO",
        "BD_MGT_SN",
        "ADM_CD",
        "HO_NM",
        "NSO_NM",
        "NSO_NM_DC",
        "HU_NUM_YN",
        "POED",
        "HU_NUM",
        "PRT_NM",
        "PRT_NM_DC",
        "COORD_X",
        "COORD_Y"
    )

    val PROPERTIES_VALUE_MAP= mapOf(
        "아파트일련번호" to "APT_NO",
        "건물일련번호" to "BD_MGT_SN",
        "읍면동코드" to "ADM_CD",
        "호실명" to "HO_NM",
        "15년 전개도 건물명칭" to "NSO_NM",
        "15년 전개도 건물상세명" to "NSO_NM_DC",
        "거처번호부여대상" to "HU_NUM_YN",
        "조사구코드" to "POED",
        "거처번호" to "HU_NUM",
        "출력 건물명" to "PRT_NM",
        "출력 부건물명" to "PRT_NM_DC",
        "좌표X" to "COORD_X",
        "좌표Y" to "COORD_Y",
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