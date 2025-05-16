package com.example.exploedview.map

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.carto.datasources.LocalVectorDataSource
import com.example.exploedview.base.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * MapViewModel
 * @property _mapEventFlow MutableSharedFlow<MapEvent>
 * @property mapEventFlow SharedFlow<MapEvent>
 */
open class MapViewModel : BaseViewModel() {

    private val _mapEventFlow = MutableSharedFlow<MapEvent>()
    val mapEventFlow = _mapEventFlow.asSharedFlow()

    private val _seq = MutableLiveData<String>()
    val seq: MutableLiveData<String> get() = _seq

    private val _complexPk = MutableLiveData<String>()
    val complexPk: MutableLiveData<String> get() = _complexPk

    fun getTotalExplodedPolygon(data: Int) = mapEvent(MapEvent.GetExplodedViewLayer(data))
    fun getBaseLayersCount(layers: Int) = mapEvent(MapEvent.GetBaseLayers(layers))
    fun getSelectExplodedPolygon(cnt: Int) = mapEvent(MapEvent.GetSelectExplodedPolygon(cnt))
    fun getGroupExplodedPolygon(cnt: Int) = mapEvent(MapEvent.GetGroupExplodedPolygon(cnt))
    fun setLayerReadStatus(status: Boolean) = mapEvent(MapEvent.SetLayerReadStatus(status))
    fun getCoordinates(coordinates: String) = mapEvent(MapEvent.GetCoordinates(coordinates))
    fun getAddFloorValue(cnt: Int) = mapEvent(MapEvent.GetAddFloorCnt(cnt))
    fun getAddLIneValue(cnt: Int) = mapEvent(MapEvent.GetAddLineCnt(cnt))
    fun getAddHo(cnt: Int) = mapEvent(MapEvent.GetAddHoCnt(cnt))
    fun getContains(cnt: Int) = mapEvent(MapEvent.GetContainsCnt(cnt))
    fun setBaseMap(flag: Boolean) = mapEvent(MapEvent.SetBaseMap(flag))
    fun clearMap(flag: Boolean) = mapEvent(MapEvent.ClearMap(flag))
    fun saveMap(
        addFloorDataSource: LocalVectorDataSource,
        addLineDataSource: LocalVectorDataSource,
        addHoDataSource: LocalVectorDataSource,
    ) = mapEvent(MapEvent.SaveMap(addFloorDataSource, addLineDataSource, addHoDataSource))

    private fun mapEvent(event: MapEvent) {
        viewModelScope.launch {
            _mapEventFlow.emit(event)
        }
    }

    sealed class MapEvent {
        data class GetExplodedViewLayer(val data: Int) : MapEvent()
        data class GetBaseLayers(val layers: Int) : MapEvent()
        data class GetSelectExplodedPolygon(val cnt: Int) : MapEvent()
        data class GetGroupExplodedPolygon(val cnt: Int) : MapEvent()
        data class SetLayerReadStatus(val status: Boolean) : MapEvent()
        data class GetCoordinates(val coordinates: String) : MapEvent()
        data class GetAddFloorCnt(val cnt: Int) : MapEvent()
        data class GetAddLineCnt(val cnt: Int) : MapEvent()
        data class GetAddHoCnt(val cnt: Int) : MapEvent()
        data class GetContainsCnt(val cnt: Int) : MapEvent()
        data class SetBaseMap(val flag: Boolean) : MapEvent()
        data class ClearMap(val flag: Boolean) : MapEvent()
        data class SaveMap(
            val addFloorDataSource: LocalVectorDataSource,
            val addLineDataSource: LocalVectorDataSource,
            val addHoDataSource: LocalVectorDataSource,
        ) : MapEvent()
//        data class SaveMap(
//            val flag: MutableList<Polygon>,
//            val addFloorDataSource: LocalVectorDataSource,
//            val addLineDataSource: LocalVectorDataSource,
//            val addHoDataSource: LocalVectorDataSource,
//        ) : MapEvent()
    }


}