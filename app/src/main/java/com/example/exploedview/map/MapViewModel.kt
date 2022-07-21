package com.example.exploedview.map

import androidx.lifecycle.MutableLiveData
import com.example.exploedview.base.BaseViewModel

open class MapViewModel : BaseViewModel(){

    val getMapEvent: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val getTotalExplodedPolygon: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val getBaseLayers: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val getSelectExplodedPolygon: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val getGroupExplodedPolygon: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val getLayerReadStatus: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }


}