package ru.netology.nework.ui.fragments.events

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.data.dto.Coords
import ru.netology.nework.databinding.FragmentMapPickerBinding

/**
 * Полноэкранный диалог-фрагмент с Яндекс картой для выбора места события.
 * Тап по карте ставит маркер, кнопка подтверждения возвращает координаты
 * через setFragmentResult (устойчиво к пересозданию фрагментов).
 *
 * Диалог-фрагмент переживает конфигурационные изменения и пересоздание
 * вызвавшего его экрана, поэтому карта не исчезает, а введенный ранее
 * текст не стирается.
 */
@AndroidEntryPoint
class MapPickerFragment : DialogFragment() {

    companion object {
        const val TAG = "MapPickerFragment"

        // Ключ результата выбора места для FragmentResult API
        const val REQUEST_KEY_PLACE = "map_picker_place_request"

        // Москва по умолчанию как стартовая точка камеры
        private const val DEFAULT_LAT = 55.7522200
        private const val DEFAULT_LONG = 37.6155600
        private const val DEFAULT_ZOOM = 9f

        const val ARG_LAT = "arg_lat"
        const val ARG_LON = "arg_lon"
        const val ARG_CANCELED = "arg_canceled"

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

    private var placemark: PlacemarkMapObject? = null
    private var selectedPoint: Point? = null

    // Сохраняем выбранную точку между пересозданиями фрагмента (поворот экрана)
    private var savedLat: Double? = null
    private var savedLon: Double? = null

    private val tapListener = object : InputListener {
        override fun onMapTap(map: Map, point: Point) {
            // Yandex MapKit гарантирует, что point не null, поэтому передаем его напрямую
            onMapTapped(point)
        }

        override fun onMapLongTap(map: Map, point: Point) {
            onMapTapped(point)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppTheme)
    }

    override fun onStart() {
        super.onStart()
        // Делаем диалог полноэкранным
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )

        MapKitFactory.getInstance().onStart()
        _binding?.mapPickerView?.onStart()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val lat = savedLat
        val lon = savedLon
        if (lat != null && lon != null) {
            outState.putDouble(ARG_LAT, lat)
            outState.putDouble(ARG_LON, lon)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val map = binding.mapPickerView.map

        // Стартовая точка: уже выбранные координаты (режим редактирования) либо Москва
        val startLat = arguments?.getDouble(ARG_LAT, DEFAULT_LAT) ?: DEFAULT_LAT
        val startLon = arguments?.getDouble(ARG_LON, DEFAULT_LONG) ?: DEFAULT_LONG
        map.move(CameraPosition(Point(startLat, startLon), DEFAULT_ZOOM, 0f, 0f))
        map.addInputListener(tapListener)

        // Восстанавливаем выбранную точку после пересоздания фрагмента
        savedInstanceState
            ?.takeIf { it.containsKey(ARG_LAT) }
            ?.let {
                val lat = it.getDouble(ARG_LAT)
                val lon = it.getDouble(ARG_LON)
                onMapTapped(Point(lat, lon))
            }

        binding.confirmPlace.setOnClickListener {
            val point = selectedPoint
            if (point != null) {
                setFragmentResult(
                    REQUEST_KEY_PLACE,
                    Bundle().apply {
                        putDouble(ARG_LAT, point.latitude)
                        putDouble(ARG_LON, point.longitude)
                        putBoolean(ARG_CANCELED, false)
                    }
                )
            } else {
                // Ничего не выбрано — просто закрываем карту без изменений
                setFragmentResult(
                    REQUEST_KEY_PLACE,
                    Bundle().apply { putBoolean(ARG_CANCELED, true) }
                )
            }
            dismissAllowingStateLoss()
        }

        binding.cancelPlace.setOnClickListener {
            setFragmentResult(
                REQUEST_KEY_PLACE,
                Bundle().apply { putBoolean(ARG_CANCELED, true) }
            )
            dismissAllowingStateLoss()
        }
    }

    private fun onMapTapped(point: Point) {
        selectedPoint = point
        savedLat = point.latitude
        savedLon = point.longitude
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

    override fun onDestroyView() {
        _binding?.mapPickerView?.map?.removeInputListener(tapListener)
        placemark = null
        selectedPoint = null
        _binding = null
        super.onDestroyView()
    }
}
