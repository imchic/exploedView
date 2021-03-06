package com.example.exploedview.map

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.carto.components.Options
import com.carto.core.MapPos
import com.carto.core.MapRange
import com.carto.core.Variant
import com.carto.datasources.LocalVectorDataSource
import com.carto.geometry.*
import com.carto.layers.EditableVectorLayer
import com.carto.layers.Layer
import com.carto.projections.Projection
import com.carto.ui.MapView
import com.carto.utils.AssetPackage
import com.carto.utils.BitmapUtils
import com.carto.vectorelements.Polygon
import com.example.exploedview.MapActivity
import com.example.exploedview.base.BaseException
import com.example.exploedview.enums.ColorEnum
import com.example.exploedview.map.listener.MapCustomEventListener
import com.example.exploedview.map.listener.VectorElementSelectEventListener
import com.example.exploedview.util.LogUtil

@SuppressLint("StaticFieldLeak")
object BaseMap {

    private lateinit var _context: Context
    private lateinit var _activity: MapActivity

    // map
    private lateinit var _mapView: MapView
    private lateinit var _mapOpt: Options
    lateinit var _proj: Projection

    // source
    private var explodedViewSource: LocalVectorDataSource? = null
    var groupLayerSource: LocalVectorDataSource? = null
    var floorUpDataSource: LocalVectorDataSource? = null
    var addLineDataSource: LocalVectorDataSource? = null
    var addHoDataSource: LocalVectorDataSource? = null

    // layer
    private var explodedViewLayer: EditableVectorLayer? = null
    private var groupLayer: EditableVectorLayer? = null
    private var floorUpLayer: EditableVectorLayer? = null
    private var addLineLayer: EditableVectorLayer? = null
    private var addHoLayer: EditableVectorLayer? = null

    // element arr
    var createPolygonArr = mutableListOf<Polygon>()
    var containsPolygonArr = mutableListOf<Polygon>()
    var selectPolygonArr = mutableListOf<Polygon>()

    var clickPosArr = mutableListOf<MapPos>()

    // listener
    var selectListener: VectorElementSelectEventListener? = null

    // feature
    var featureCollection: FeatureCollection? = null


    /**
     * ?????? ??? ??????
     * @param mapView MapView
     * @param activity Activity
     * @param context Context
     */
    fun init(mapView: MapView, activity: Activity, context: Context) {

        _context = context
        _activity = activity as MapActivity

        _mapView = mapView
        _mapOpt = _mapView.options
        _proj = _mapOpt.baseProjection

        // ??? ??????
        _mapOpt.apply {
            tiltRange = MapRange(90f, 90f) // ?????? ??????
            isRotatable = false // ??????
            isZoomGestures = false
//            backgroundBitmap = BitmapUtils.loadBitmapFromAssets("ci.png")
        }

//        setInitZoomAndPos(22.054665.toFloat(), MapPos(10.226771, 13.399454), 0.5F)
        setInitZoomAndPos(5.toFloat(), _proj.fromWgs84(MapPos(10.226771, 13.399454)), 0.5F)

        explodedViewSource = LocalVectorDataSource(_proj)
        groupLayerSource = LocalVectorDataSource(_proj)
        floorUpDataSource = LocalVectorDataSource(_proj)
        addLineDataSource = LocalVectorDataSource(_proj)
        addHoDataSource = LocalVectorDataSource(_proj)

//        val layerNameArr = mutableListOf("explodedView", "group", "floorUp", "addLine")
        val layerArr = mutableListOf(explodedViewLayer, groupLayer, floorUpLayer, addLineLayer, addHoLayer)

        setLayer(layerArr)

        MapLayer.explodedView(_activity, explodedViewSource, createPolygonArr)

        _mapView.mapEventListener = MapCustomEventListener(_mapView, groupLayerSource, clickPosArr)

        MapStyle.createGeoJSONLayer(_activity, _mapView)


    }

    /**
     * ?????? ??? ?????? ??? ?????? ??????
     * @param zoom Float
     * @param pos MapPos
     * @param duration Float
     */
    private fun setInitZoomAndPos(zoom: Float, pos: MapPos, duration: Float) {
        _mapView.apply {
            setZoom(zoom, duration)
            setFocusPos(pos, duration)
//            setFocusPos(_proj.fromWgs84(pos), duration)
        }
    }

    /**
     * GeoJson ??? ????????????
     * @param fileName  Asset Package ??? ????????? ????????? (json type)
     * @return FeatureCollection?
     */
    fun getGeoJsonFeature(context: Context, fileName: String): FeatureCollection? {
        val assetManager = context.resources.assets
        val stream = assetManager.open(fileName)
        val size = stream.available()
        val buffer = ByteArray(size)
        stream.read(buffer)
        stream.close()
        val json = String(buffer, charset("UTF-8"))
        LogUtil.i("Result GeoJSON => $json")
        val reader = GeoJSONGeometryReader()
        reader.targetProjection = _proj
        return reader.readFeatureCollection(json)
    }

    /**
     * ????????? ??????
     */
    private fun setLayer(layers: MutableList<EditableVectorLayer?>) {

        var source: LocalVectorDataSource?
        val nameArr = mutableListOf<String>()

        val enums = enumValues<MapLayerName>()

        enums.mapIndexed { index, name ->
            when (name) {
                MapLayerName.EXPLODED_VIEW -> {
                    source = explodedViewSource
                    layers[index] = EditableVectorLayer(source)
                }
                MapLayerName.GROUP -> {
                    source = groupLayerSource
                    layers[index] = EditableVectorLayer(source)
                }
                MapLayerName.ADD_FLOOR -> {
                    source = floorUpDataSource
                    layers[index] = EditableVectorLayer(source)
                }
                MapLayerName.ADD_LINE -> {
                    source = addLineDataSource
                    layers[index] = EditableVectorLayer(source)
                }
                MapLayerName.ADD_HO -> {
                    source = addHoDataSource
                    layers[index] = EditableVectorLayer(source)
                }
                else -> throw BaseException("????????? ????????? ????????????.")
            }

            setLayerName(layers[index], "name", Variant(name.value))
            _mapView.layers.add(layers[index])

        }
        .also {
            for (i in 0 until getLayerCount()) {
                nameArr.add(_mapView.layers.get(i).metaData.get("name").string)
            }
            LogUtil.i("????????? ????????? ?????? : ${nameArr}, ?????? ????????? ???????????? ?????? : ${getLayerCount()}")
        }
    }


    /**
     * ????????? ??? ??????
     * @param layer VectorLayer
     * @param key String
     * @param name Variant
     */
    private fun setLayerName(layer: Layer?, key: String, name: Variant) = layer?.setMetaDataElement(key, name)

    /**
     * ????????? ??? ????????????
     * @param index Int
     * @param key String
     * @return String?
     */
    fun getLayerName(index: Int, key: String): String = _mapView.layers.get(index).getMetaDataElement(key).string

    /**
     * ???????????? ??????
     * @return Int
     */
    fun getLayerCount(): Int = _mapView.layers.count()

    /**
     * ?????????
     */
    fun clear() {

        clickPosArr.clear()

        groupLayerSource?.clear()
        floorUpDataSource?.clear()
        addLineDataSource?.clear()

        MapLayer.floorElement.clear()
        MapLayer.lineElement.clear()

        explodedViewSource?.clear()
        MapLayer.explodedView(_context, explodedViewSource, createPolygonArr)

        selectPolygonArr.clear()

        _activity.showToast("clear")
    }

    /**
     * ???????????? ??? ?????? ?????? ?????? ?????? ?????? (?????????)
     * @param parents MutableList<Polygon>
     * @param child MutableList<Polygon>
     */
    fun contains(parents: MutableList<Polygon>, child: MutableList<Polygon>) {

        val bool = child.isNotEmpty()

        runCatching {
            if (!bool) throw BaseException("??????????????? ???????????? ???????????????. \n ??????????????? ?????????????????????.")
        }.onSuccess {
            clickPosArr.clear()
            groupLayerSource?.clear()

            group(parents, child)

        }.onFailure { it: Throwable ->
            LogUtil.e("group status: $bool, $it")
            _activity.showToast(it.message)
        }
    }

    /**
     * Feature ??????
     * @param it Polygon
     * @param value Int
     */
    fun addFeatures(geom: Geometry?, value: Int) {

        runCatching {
            geom ?: throw BaseException("Feature ????????? ????????? ?????????????????? ???????????? ????????????.")
        }.onSuccess {
            val featureBuilder = FeatureBuilder()
            featureBuilder.geometry = geom as PolygonGeometry
            featureBuilder.setPropertyValue("new_yn", Variant("Y"))
            featureBuilder.setPropertyValue("ho_nm", Variant(value.toString()))
            val newFeature = featureBuilder.buildFeature()

            LogUtil.d(newFeature.properties.getObjectElement("new_yn").string)
            LogUtil.d(newFeature.properties.getObjectElement("ho_nm").string)
            LogUtil.d(newFeature.geometry.bounds.toString())

            val featureVector = FeatureVector()
            featureVector.add(newFeature)

            featureCollection = FeatureCollection(featureVector)
            LogUtil.d("featureCount => ${featureCollection?.featureCount}")

        }.onFailure {
            LogUtil.e(it.toString())

        }.also {
            val geoJSONGeometryWriter = GeoJSONGeometryWriter()
            LogUtil.d(geoJSONGeometryWriter.writeFeatureCollection(featureCollection))
        }

    }

    /**
     * ?????? ?????? ??? ?????????
     * @param geometry Geometry
     */
    fun select(geometry: Geometry) {
        createPolygonArr
            .filter { it.geometry == geometry }
            .map {

                when (it.getMetaDataElement("select").string) {
                    "n" -> {
                        it.style = MapStyle.setPolygonStyle(
                            MapElementColor.set(ColorEnum.YELLOW),
                            MapElementColor.set(ColorEnum.YELLOW),
                            2F
                        )
                        it.setMetaDataElement("select", Variant("y"))
                        selectPolygonArr.add(it)
                    }
                    "y" -> {
                        it.style = MapStyle.setPolygonStyle(
                            MapElementColor.set(ColorEnum.GREEN),
                            MapElementColor.set(ColorEnum.GREEN),
                            2F
                        )
                        it.setMetaDataElement("select", Variant("n"))
                        selectPolygonArr.remove(it)
                    }
                    else -> {
                        throw BaseException("????????? select Event ??????")
                    }
                }

            }

            LogUtil.d("????????? ????????? ???????????? ?????? : ${selectPolygonArr.size}")
    }

    /**
     * ?????? ?????? ??? ????????? ????????????
     * @param parnnts MutableList<Polygon>
     * @param child MutableList<Polygon>
     */
    fun group(parnnts: MutableList<Polygon>, child: MutableList<Polygon>){
        parnnts
            .filter { child.contains(it) }
            .map{
                it.style = MapStyle.setPolygonStyle(
                    MapElementColor.set(ColorEnum.PURPLE),
                    MapElementColor.set(ColorEnum.PURPLE),
                    2F
                )
            }
        }


}