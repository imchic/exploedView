package com.example.exploedview

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapView
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import kotlinx.coroutines.launch

/*
class NaverMapActivity : BaseActivity<ActivityNaverMapBinding, BaseViewModel>() {

    override val layoutId: Int = R.layout.activity_naver_map
    override val vm: BaseViewModel = BaseViewModel()

    override fun initViewStart() {
    }

    override fun initDataBinding() {

        runOnUiThread {
            binding.naverMapView.getMapAsync { naverMap ->
                naverMap.addOnCameraChangeListener { _, _ ->
                    //vm.getCoordinates(naverMap.cameraPosition.target.toString())
                }
                // 네이버 맵 최초위치 지정
                naverMap.cameraPosition = CameraPosition(
                    LatLng(35.15664464076588, 129.14510989614843),
                    18.0
                )

                naverMap.setOnMapClickListener { point, coord ->
                    LogUtil.d("onMapClick: point=$point, coord=$coord")
                }

                val marker = Marker()
                marker.position = LatLng(35.15664464076588, 129.14510989614843)

                marker.map = naverMap
                val infoWindow = InfoWindow()

                infoWindow.adapter = object : InfoWindow.DefaultTextAdapter(
                    this
                ) {
                    override fun getText(infoWindow: InfoWindow): CharSequence {
                        return "해운대두산위브"
                    }
                }

                infoWindow.open(marker)

            }
        }

    }

    override fun initViewFinal() {
        // TODO: 2021-08-26 네이버 지도 띄우기
    }

}*/

class NaverMapActivity : AppCompatActivity() {

    private val viewModel = NaverMapViewModel()

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_naver_map)

        lifecycleScope.launch {
//            vm.eventFlow.collect { event -> handleEvent(event) }
            //vm.liveData.observe(this@BaseActivity) { event -> handleEvent(event)

            mapView = findViewById(R.id.naverMapView)

            viewModel.eventFlow.observe(this@NaverMapActivity) { event ->
                when (event) {
                    is NaverMapViewModel.NaverMapEvent.GetCoordinates -> {
                        println("GetCoordinates: ${event.coordinates}")
                    }
                }
            }

            mapView.getMapAsync { naverMap ->
                naverMap.addOnCameraChangeListener { _, _ ->
                    viewModel.getCoordinates(naverMap.cameraPosition.target.toString())
                }
                // 네이버 맵 최초위치 지정
                naverMap.cameraPosition = CameraPosition(
                    LatLng(35.15664464076588, 129.14510989614843), 18.0
                )

                naverMap.setOnMapClickListener { point, coord ->
//                    LogUtil.d("onMapClick: point=$point, coord=$coord")
                }

                val marker = Marker()
                marker.position = LatLng(35.15664464076588, 129.14510989614843)

                marker.map = naverMap
                val infoWindow = InfoWindow()

                infoWindow.adapter = object : InfoWindow.DefaultTextAdapter(
                    this@NaverMapActivity
                ) {
                    override fun getText(infoWindow: InfoWindow): CharSequence {
                        return "해운대두산위브"
                    }
                }

                infoWindow.open(marker)

                infoWindow.setOnClickListener {
                    println("infoWindow clicked")
                    startActivity(Intent(this@NaverMapActivity, MapActivity::class.java))
                    true
                }


            }

        }

    }

    // 화면 회전 등으로 액티비티가 재생성될 때, 상태를 저장
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }


    // 액티비티가 화면에 보여질 때
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    // 액티비티가 화면에 보여지지 않을 때
    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    // 액티비티가 화면에 보여질 때
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    // 액티비티가 화면에 보여지지 않을 때
    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    @Deprecated("Deprecated in Java")
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    class NaverMapViewModel : ViewModel() {

        private val _eventFlow = MutableLiveData<NaverMapEvent>()
        val eventFlow = _eventFlow

        fun getCoordinates(coordinates: String) = event(NaverMapEvent.GetCoordinates(coordinates))

        private fun event(event: NaverMapEvent) {
            _eventFlow.value = event
        }

        sealed class NaverMapEvent {
            data class GetCoordinates(val coordinates: String) : NaverMapEvent()
        }
    }

}
