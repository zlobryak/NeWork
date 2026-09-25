package ru.netology.nework.ui.fragments.events

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.data.dto.Coords
import ru.netology.nework.databinding.FragmentMapPickerBinding
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map

/**
 * Полноэкранный фрагмент с Яндекс картой для выбора места события.
 * Тап по карте ставит маркер, кнопка подтверждения возвращает координаты через listener.
 */
@AndroidEntryPoint
class MapPickerFragment : Fragment() {

    interface MapPickerListener {
        fun onPlaceSelected(coords: Coords)
        fun onCanceled()
    }

    companion object {
        const val TAG = "MapPickerFragment"

        // Москва по умолчанию как стартовая точка камеры
        private const val DEFAULT_LAT = 55.7522200
        private const val DEFAULT_LONG = 37.6155600
        private const val DEFAULT_ZOOM = 9f

        private const val ARG_LAT = "arg_lat"
        private const val ARG_LON = "arg_lon"

        fun newInstance(initial: Coords? = null): MapPickerFragment =
            MapPickerFragment().apply {
                arguments = Bundle().apply {
                    initial?.let {
                        putDouble(ARG_LAT, it.lat)
                        putDouble(ARG_LON, it.long)
                    }
                }
            }
    }

    private var _binding: FragmentMapPickerBinding? = null
    private val binding get() = _binding!!

    private var listener: MapPickerListener? = null

    private var placemark: PlacemarkMapObject? = null
    private var selectedPoint: Point? = null

    private val tapListener = object : InputListener {
        override fun onMapTap(map: Map, point: Point) {
            // Yandex MapKit гарантирует, что point не null, поэтому передаем его напрямую
            onMapTapped(point)
        }

        override fun onMapLongTap(map: Map, point: Point) {
            onMapTapped(point)
        }
    }

    fun setOnPlaceSelectedListener(listener: MapPickerListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val map = binding.mapPickerView.map

        // Стартовая точка: уже выбранные координаты (режим редактирования) либо Москва
        val startLat = arguments?.getDouble(ARG_LAT, DEFAULT_LAT) ?: DEFAULT_LAT
        val startLon = arguments?.getDouble(ARG_LON, DEFAULT_LONG) ?: DEFAULT_LONG
        map.move(CameraPosition(Point(startLat, startLon), DEFAULT_ZOOM, 0f, 0f))
        map.addInputListener(tapListener)

        binding.confirmPlace.setOnClickListener {
            selectedPoint?.let { point ->
                listener?.onPlaceSelected(Coords(point.latitude, point.longitude))
            }
        }

        binding.cancelPlace.setOnClickListener {
            listener?.onCanceled()
        }
    }

    private fun onMapTapped(point: Point) {
        selectedPoint = point
        val map = _binding?.mapPickerView?.map ?: return

        val iconDrawable = ContextCompat.getDrawable(
            requireContext(), R.drawable.ic_location_pin_24dp
        )
        val iconBitmap: Bitmap? = iconDrawable?.toBitmap(width = 96, height = 96)

        // Удаляем старый маркер, если он есть
        placemark?.let { map.mapObjects.remove(it) }

        // Создаем новый маркер
        placemark = map.mapObjects.addPlacemark(point).apply {
            iconBitmap?.let { setIcon(ImageProvider.fromBitmap(it)) }
        }

        _binding?.hint?.visibility = View.GONE
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        _binding?.mapPickerView?.onStart()
    }

    override fun onStop() {
        _binding?.mapPickerView?.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        _binding?.mapPickerView?.map?.removeInputListener(tapListener)
        placemark = null
        selectedPoint = null
        listener = null
        _binding = null
        super.onDestroyView()
    }
}
