package com.example.exploedview.map

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exploedview.MapActivity
import com.example.exploedview.NaverMapActivity
import com.example.exploedview.R
import com.example.exploedview.db.AppDatabase
import com.example.exploedview.db.BuildingInfo
import com.example.exploedview.db.BuildingInfoDao
import com.example.exploedview.util.LogUtil
import com.example.exploedview.util.ProgressDialogUtil
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class NaverMapViewModel : ViewModel() {

    private val _buildingInfoArray = MutableLiveData<List<BuildingInfo>>()
    val buildingInfoArray: LiveData<List<BuildingInfo>> get() = _buildingInfoArray

    private val _aptInfo = MutableLiveData<String>()
    val aptInfo: MutableLiveData<String> get() = _aptInfo

    private val _address = MutableLiveData<String>()
    val address: MutableLiveData<String> get() = _address

    private val _markers = mutableListOf<Marker>()
    var markers: MutableList<Marker> = _markers

    // 위도
    private val _locationLat = MutableLiveData<Double>()
    val locationLat: LiveData<Double> get() = _locationLat

    // 경도
    private val _locationLng = MutableLiveData<Double>()
    val locationLng: LiveData<Double> get() = _locationLng

    private val _buildingInfo = MutableLiveData<BuildingInfo>()
    val buildingInfo: LiveData<BuildingInfo> get() = _buildingInfo

    // 아파트 여부
    private val _isApt = MutableLiveData<Boolean>()
    val isApt: MutableLiveData<Boolean> get() = _isApt

    /**
     * 좌표 -> 주소 변환 API 호출
     */
    fun fetchNaverReverseGeoCoding(
        latitude: Double,
        longitude: Double,
        naverMapActivity: NaverMapActivity,
    ) {
        viewModelScope.launch {
            val result = runCatching {
                // 좌표 -> 주소 변환 API 호출
                getAddressFromCoordinates(latitude, longitude)
            }.getOrElse {
                "조회 실패: ${it.message}"
            }
            //println("getAddressFromCoordinates API 호출 결과: $result")
            LogUtil.i("getAddressFromCoordinates API 호출 결과: $result")
            _address.postValue(result)
        }
    }

    /**
     * 공동주택 정보 조회 API 호출
     */
    fun findAptInfo(address: String, naverMapActivity: NaverMapActivity) {
        viewModelScope.launch {
            val result = runCatching {
                // 공동주택 정보 조회 API 호출
                getAptInfoFromApi(address, naverMapActivity)
            }.getOrElse {
                "조회 실패: ${it.message}"
            }
            //println("getAptInfoFromApi API 호출 결과: $result")
            LogUtil.i("getAptInfoFromApi API 호출 결과: $result")
            _aptInfo.postValue(result)
        }
    }

    /**
     * 마커 추가
     */
    fun addMarker(
        marker: Marker,
        naverMap: NaverMap,
    ): Boolean {
        _markers.add(marker)
        marker.map = naverMap
        updateDrawerWithMarkers()
        return true
    }

    /**
     * 드로어에 마커 정보 업데이트
     */
    private fun updateDrawerWithMarkers() {
        val currentItems = _buildingInfoArray.value?.toMutableList() ?: mutableListOf()

        val seq = _buildingInfo.value?.seq ?: 0
        val complexPk = _buildingInfo.value?.complexPk ?: "단지PK 없음"
        val complexNm1 = _buildingInfo.value?.complexNm1 ?: "건물명 없음"
        val complexNm2 = _buildingInfo.value?.complexNm2 ?: "건물명 없음"
        val complexNm3 = _buildingInfo.value?.complexNm3 ?: "건물명 없음"
        val isApt = _buildingInfo.value?.complexGbCd ?: "아파트X"
        val address = _buildingInfo.value?.address ?: "주소 없음"
        val dongCnt = _buildingInfo.value?.dongCnt ?: "동수 없음"
        val unitCnt = _buildingInfo.value?.unitCnt ?: "세대수 없음"
        val useaprDt = _buildingInfo.value?.useaprDt ?: "사용승인일 없음"

        // 중복 확인
        if (currentItems.any { it.complexNm1 == complexNm1 }) {
            LogUtil.e("중복된 항목입니다: $complexNm1")
            return
        }

        addBuildingInfoItem(
            seq = seq,
            complexPk = complexPk,
            buildingName = complexNm1,
            isApt = isApt,
            address = address,
            dongCnt = dongCnt,
            unitCnt = unitCnt,
            useaprDt = useaprDt,
            longitude = _locationLng.value ?: 0.0,
            latitude = _locationLat.value ?: 0.0,
        )
    }

    /**
     * 건물 정보 추가
     */

    fun addBuildingInfoItem(
        buildingName: String,
        isApt: String,
        address: String,
        dongCnt: String,
        unitCnt: String,
        useaprDt: String,
        longitude: Double,
        latitude: Double,
        seq: Int,
        complexPk: String,
    ) {

        val newBuildingInfo = BuildingInfo(
            seq = seq,
            complexPk = complexPk,
            complexNm1 = buildingName,
            complexGbCd = isApt,
            address = address,
            dongCnt = dongCnt,
            unitCnt = unitCnt,
            longitude = longitude,
            latitude = latitude,
            complexNm2 = "",
            complexNm3 = "",
            useaprDt = useaprDt,
            filename = "",
        )

        val updatedItems = _buildingInfoArray.value?.toMutableList() ?: mutableListOf()
        updatedItems.add(newBuildingInfo)

        _buildingInfoArray.postValue(updatedItems)
    }

    /**
     * 좌표 -> 주소 변환 API 호출
     */
    private suspend fun getAddressFromCoordinates(latitude: Double, longitude: Double): String =
        suspendCancellableCoroutine { continuation ->
            val url =
                "https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc?coords=$longitude,$latitude&output=json&orders=legalcode,addr,roadaddr"

            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .headers(
                    Headers.Builder()
                        .add("Content-Type", "application/json")
                        .add("X-NCP-APIGW-API-KEY-ID", "ilm1l1ctqq")
                        .add("X-NCP-APIGW-API-KEY", "d4BhumaBIZwkf7Kg7aJtaGR1wGdng7IUJL2MSuZ3")
                        .build()
                )
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isActive) {
                        continuation.resumeWithException(e)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val responseData = response.body?.string()
                        if (responseData.isNullOrEmpty()) {
                            if (continuation.isActive) {
                                continuation.resumeWithException(IOException("Empty response body"))
                            }
                            return
                        }

                        try {
                            val result = JSONObject(responseData).optJSONArray("results")
                            if (result == null || result.length() < 2) {
                                if (continuation.isActive) {
                                    continuation.resumeWithException(IOException("Invalid response structure"))
                                }
                                return
                            }

                            val regionObj = result.optJSONObject(1)?.optJSONObject("region")
                            val landObj = result.optJSONObject(1)?.optJSONObject("land")
                            val roadAddrObj = result.optJSONObject(2)?.optJSONObject("land")

                            if (regionObj == null || landObj == null || roadAddrObj == null) {
                                if (continuation.isActive) {
                                    continuation.resumeWithException(IOException("Missing required fields in response"))
                                }
                                return
                            }

                            val area1 =
                                regionObj.optJSONObject("area1")?.optString("name", "") ?: ""
                            val area2 =
                                regionObj.optJSONObject("area2")?.optString("name", "") ?: ""
                            val area3 =
                                regionObj.optJSONObject("area3")?.optString("name", "") ?: ""
                            val area4 =
                                regionObj.optJSONObject("area4")?.optString("name", "") ?: ""

                            val roadAddrName = roadAddrObj.optString("name", "")
                            val roadAddrBuildingName =
                                roadAddrObj.optJSONObject("addition0")?.optString("value", "")
                                    ?: ""

                            val jibun = landObj.optString("number1", "")
                            val ho = landObj.optString("number2", "")

                            var fullAddress = if (ho.isNotEmpty() && ho != "0") {
                                "$area1 $area2 $area3 $area4 $jibun-$ho"
                            } else {
                                "$area1 $area2 $area3 $area4 $jibun"
                            }

                            val roadFullAddress = if (roadAddrBuildingName.isNotEmpty()) {
                                "$roadAddrName $roadAddrBuildingName"
                            } else {
                                roadAddrName
                            }

                            fullAddress = fullAddress.trim().replace("\\s+".toRegex(), " ")

                            if (continuation.isActive) {
                                //continuation.resume("$fullAddress\n$roadFullAddress")
                                // 개행
                                continuation.resume("$fullAddress, $roadFullAddress")
                            }
                        } catch (e: Exception) {
                            if (continuation.isActive) {
                                continuation.resumeWithException(e)
                            }
                        }
                    } else {
                        if (continuation.isActive) {
                            continuation.resumeWithException(IOException("HTTP ${response.code}: ${response.message}"))
                        }
                    }
                }
            })
        }

    /**
     * 공동주택 정보 조회 API 호출
     */
    private suspend fun getAptInfoFromApi(
        address: String,
        activity: NaverMapActivity,
    ): String = suspendCancellableCoroutine { continuation ->

        // 중복
        if (_buildingInfoArray.value?.any { it.address == address } == true) {
            continuation.resume("이미 조회된 주소입니다.")
            return@suspendCancellableCoroutine
        }

        val progressDialog = ProgressDialogUtil.showProgressDialog(activity)

        val page = 1
        val perPage = 10
        val returnType = "json"
        val serviceKey =
            "Sua5LWTnm9KejH0Ay8tVAj3jM1SGvYnbyVuGmp1P8AlPxtkBTjp8VJm5DBUuc%2B65ueL9%2F%2BG7K5MEk3NWWUkBNA%3D%3D"

        /**
         * 한국부동산원_공동주택 단지 식별정보 조회 서비스
         * https://www.data.go.kr/data/15106817/openapi.do
         */
        val url =
            "https://api.odcloud.kr/api/AptIdInfoSvc/v1/getAptInfo?" +
                    "page=$page" +
                    "&perPage=$perPage" +
                    "&returnType=$returnType" +
                    "&cond%5BADRES%3A%3ALIKE%5D=$address" +
                    "&serviceKey=$serviceKey"

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isActive) {
                    continuation.resumeWithException(e)
                }
                ProgressDialogUtil.dismissProgressDialog(progressDialog)
            }

            override fun onResponse(call: Call, response: Response) {
                ProgressDialogUtil.dismissProgressDialog(progressDialog)

                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    if (responseData.isNullOrEmpty()) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(IOException("Empty response body"))
                        }
                        return
                    }

                    try {
                        val json = JSONObject(responseData)
                        val result = json.optJSONArray("data")
                        if (result == null || result.length() == 0) {
                            if (continuation.isActive) {
                                continuation.resume("조회된 데이터가 없습니다.")
                            }
                            return
                        }

                        val firstItem = result.optJSONObject(0)
                        if (firstItem == null) {
                            if (continuation.isActive) {
                                continuation.resume("조회된 데이터가 없습니다.")
                            }
                            return
                        }

                        val address = firstItem.optString("ADRES", "N/A")
                        val complexPk = firstItem.optString("COMPLEX_PK", "N/A")
                        val complexNm1 = firstItem.optString("COMPLEX_NM1", "N/A")
                        val complexNm2 = firstItem.optString("COMPLEX_NM2", "N/A")
                        val complexNm3 = firstItem.optString("COMPLEX_NM3", "N/A")
                        val complexGbCd =
                            if (firstItem.optString("COMPLEX_GB_CD") == "1") "아파트" else "아파트X"
                        val dongCnt = firstItem.optString("DONG_CNT", "N/A")
                        val unitCnt = firstItem.optString("UNIT_CNT", "N/A")
                        val useaprDt = firstItem.optString("USEAPR_DT", "N/A")

                        val resultString = """
                        주소: $address
                        단지명_도로명주소: $complexNm3
                        단지종류: $complexGbCd
                        동수: $dongCnt
                        세대수: ${addCommaToNumber(unitCnt)}
                        사용승인일: ${convertDateFormat(useaprDt)}
                    """.trimIndent()

                        if (continuation.isActive) {
                            continuation.resume(resultString)

                            if (complexGbCd == "아파트") {
                                _isApt.postValue(true)
                            } else {
                                _isApt.postValue(false)
                            }

                            // DB에 저장
                            val buildingInfo = BuildingInfo(
                                address = address,
                                complexPk = complexPk,
                                complexNm1 = complexNm1,
                                complexNm2 = complexNm2,
                                complexNm3 = complexNm3,
                                complexGbCd = complexGbCd,
                                dongCnt = dongCnt,
                                unitCnt = unitCnt,
                                latitude = locationLat.value ?: 0.0,
                                longitude = locationLng.value ?: 0.0,
                                useaprDt = useaprDt,
                                filename = "",
                            )

                            _buildingInfo.postValue(buildingInfo)

                            LogUtil.i("DB에 저장된 건물 정보: $buildingInfo")


                        }
                    } catch (e: Exception) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(e)
                        }
                    }
                } else {
                    if (continuation.isActive) {
                        continuation.resumeWithException(IOException("HTTP ${response.code}: ${response.message}"))
                    }
                }
            }
        })
    }

    fun removeMenuItemAndMarker(position: Int) {
        val updatedItems = _buildingInfoArray.value?.toMutableList() ?: return

        if (position in updatedItems.indices && position in _markers.indices) {
            updatedItems.removeAt(position)

            // 원본 _markers 리스트에서 직접 수정
            _markers[position].map = null
            _markers.removeAt(position)

            _buildingInfoArray.postValue(updatedItems)


        }
    }

    fun setCoord(latitude: Double, longitude: Double) {
        _locationLat.postValue(latitude)
        _locationLng.postValue(longitude)

    }

    fun addBuildingInfo(
        seq: Int,
        address: String,
        complex_pk: String,
        complex_nm1: String,
        complex_nm2: String,
        complex_nm3: String,
        complex_gb_cd: String,
        dong_cnt: String,
        unit_cnt: String,
        latitude: Double,
        longitude: Double,
        useapr_dt: String,
    ) {

        _buildingInfo.value = BuildingInfo(
            seq = seq,
            address = address,
            complexPk = complex_pk,
            complexNm1 = complex_nm1,
            complexNm2 = complex_nm2,
            complexNm3 = complex_nm3,
            complexGbCd = complex_gb_cd,
            dongCnt = dong_cnt,
            unitCnt = unit_cnt,
            latitude = latitude,
            longitude = longitude,
            useaprDt = useapr_dt,
            filename = "",
        )

    }

    suspend fun clearMenuItems() {
        _buildingInfoArray.value = emptyList()
    }

    suspend fun clearDatabase(activity: NaverMapActivity) {
        viewModelScope.launch {
            val progressDialog = ProgressDialogUtil.showProgressDialog(activity)
            progressDialog.setMessage("DB 데이터 삭제 중...")

            val db = AppDatabase.getInstance(activity)
            val buildingInfoDao = db.buildingInfoDao()

            CoroutineScope(Dispatchers.IO).launch {
                buildingInfoDao.deleteAll() // DB의 모든 데이터 삭제
            }
            _buildingInfoArray.postValue(emptyList()) // 메뉴 아이템 초기화

            progressDialog.dismiss() // ProgressDialog 종료

        }
    }

    suspend fun clearMarkers() {
        for (marker in _markers) {
            marker.map = null
        }
        _markers.clear()
    }

    /**
     * DB에서 건물 정보 조회 후 마커 추가
     */
    fun getBuildingInfo(
        dao: BuildingInfoDao?,
        naverMapActivity: NaverMapActivity,
        naverMap: NaverMap,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            // DB에서 건물 정보 조회
            val buildingInfoList = dao?.getAll()

            // DB에서 건물 정보가 존재하는 경우 마커 추가
            if (buildingInfoList != null) {
                for (buildingInfo in buildingInfoList) {

                    val marker = Marker().apply {
                        position = LatLng(buildingInfo.latitude, buildingInfo.longitude)
                        icon =
                            OverlayImage.fromResource(com.naver.maps.map.R.drawable.navermap_default_marker_icon_blue)

                    }

                    // UI 업데이트는 Main 디스패처에서 실행
                    withContext(Dispatchers.Main) {
                        marker.map = naverMap

                        marker.setOnClickListener {
                            naverMapActivity.textLocation?.text = buildingInfo.address
                            naverMapActivity.textLocationDetail?.text = buildingInfo.toString()

                            val bottomSheetView = LayoutInflater.from(naverMapActivity)
                                .inflate(R.layout.bottom_sheet_marker, null)
                            BottomSheetDialog(naverMapActivity).apply {
                                setContentView(bottomSheetView)
                                window?.setLayout(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                            }.show()

                            bottomSheetView.findViewById<TextView>(R.id.marker_add_location)?.text =
                                buildingInfo.address

                            val textLocationDetailText = """
                                단지명: ${buildingInfo.complexNm1}
                                단지종류: ${buildingInfo.complexGbCd}
                                동수: ${buildingInfo.dongCnt}
                                세대수: ${addCommaToNumber(buildingInfo.unitCnt)}
                                사용승인일: ${convertDateFormat(buildingInfo.useaprDt)}                                
                                """.trimIndent()

                            val lines = textLocationDetailText.split("\n")
                            val bulletText = lines.joinToString("\n") { "• $it" } // 또는 " . $it"

                            bottomSheetView.findViewById<TextView>(R.id.marker_add_location)?.text =
                                buildingInfo.address
                            bottomSheetView.findViewById<TextView>(R.id.marker_add_location_detail)?.text =
                                bulletText

                            val addMarkerButton =
                                bottomSheetView.findViewById<Button>(R.id.marker_add_button)

                            val moveButton =
                                bottomSheetView.findViewById<Button>(R.id.marker_move_button)

                            CoroutineScope(Dispatchers.IO).launch {
                                if (dao != null) {
                                    val existingItem = dao.getBuildingInfoByComplexPk(
                                        buildingInfo.complexPk
                                    )
                                    CoroutineScope(Dispatchers.Main).launch {
                                        if (existingItem != null) {
                                            addMarkerButton.visibility = View.GONE
                                        }
                                    }
                                }
                            }

                            moveButton?.setOnClickListener {
                                CoroutineScope(Dispatchers.IO).launch {
                                    val buildingInfo =
                                        dao.getBuildingInfoByComplexPk(
                                            buildingInfo.complexPk
                                        )
                                    withContext(Dispatchers.Main) {
                                        if (buildingInfo == null) {
                                            Toasty.error(
                                                naverMapActivity,
                                                "공동주택을 먼저 추가해주세요.",
                                                Toasty.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            // mapActivity 이동
                                            val intent = Intent(
                                                naverMapActivity,
                                                MapActivity::class.java
                                            ).apply {
                                                putExtra("seq", buildingInfo.seq)
                                                putExtra("address", buildingInfo.address)
                                                putExtra("complexNm1", buildingInfo.complexNm1)
                                                putExtra("complexGbCd", buildingInfo.complexGbCd)
                                                putExtra("dongCnt", buildingInfo.dongCnt)
                                                putExtra("unitCnt", buildingInfo.unitCnt)
                                                putExtra("useaprDt", buildingInfo.useaprDt)
                                                putExtra("complexPk", buildingInfo.complexPk)
                                                putExtra("complexNm2", buildingInfo.complexNm2)
                                                putExtra("complexNm3", buildingInfo.complexNm3)
                                                putExtra("latitude", buildingInfo.latitude)
                                                putExtra("longitude", buildingInfo.longitude)
                                                putExtra("filename", buildingInfo.filename)
                                            }

                                            startActivity(
                                                naverMapActivity,
                                                intent,
                                                null
                                            )
                                        }
                                    }
                                }
                            }

                            true
                        }

                        // ViewModel에 마커 및 데이터 추가
                        addBuildingInfo(
                            seq = buildingInfo.seq ?: 0,
                            address = buildingInfo.address,
                            complex_pk = buildingInfo.complexPk,
                            complex_nm1 = buildingInfo.complexNm1,
                            complex_nm2 = buildingInfo.complexNm2,
                            complex_nm3 = buildingInfo.complexNm3,
                            complex_gb_cd = buildingInfo.complexGbCd,
                            dong_cnt = buildingInfo.dongCnt,
                            unit_cnt = buildingInfo.unitCnt,
                            latitude = buildingInfo.latitude,
                            longitude = buildingInfo.longitude,
                            useapr_dt = buildingInfo.useaprDt,
                        )

                        if (buildingInfo.complexGbCd == "아파트") {
                            _isApt.postValue(true)
                        } else {
                            _isApt.postValue(false)
                        }

                        addMarker(marker, naverMap)
                    }

                }
            }
        }
    }

    fun convertDateFormat(useaprDt: String): String {
        return if (useaprDt.length >= 8) {
            val year = useaprDt.substring(0, 4)
            val month = useaprDt.substring(4, 6)
            val day = useaprDt.substring(6, 8)
            "$year-$month-$day"
        } else {
            useaprDt
        }
    }

    // 세자리 콤마
    fun addCommaToNumber(number: String): String {
        val numberString = number.replace(",", "")
        return if (numberString.isNotEmpty()) {
            val formattedNumber = numberString.toLong()
            String.format("%,d", formattedNumber)
        } else {
            ""
        }
    }
}