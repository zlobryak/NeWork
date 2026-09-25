package ru.netology.nework.ui.fragments.events

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import ru.netology.nework.R
import java.text.SimpleDateFormat
import java.util.*

class EventOptionsBottomSheetFragment : BottomSheetDialogFragment() {

    private var listener: EventOptionsListener? = null

    private lateinit var dateEditText: TextInputEditText
    private lateinit var typeRadioGroup: RadioGroup
    private lateinit var onlineRadioButton: RadioButton
    private lateinit var offlineRadioButton: RadioButton
    private lateinit var cancelButton: Button
    private lateinit var okButton: Button

    private var selectedDate: Calendar = Calendar.getInstance()
    private var isOnline: Boolean = true

    interface EventOptionsListener {
        fun onEventOptionsConfirmed(date: Date, isOnline: Boolean)
    }

    fun setListener(listener: EventOptionsListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_options_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupListeners()
        updateDateText()
    }

    private fun initViews(view: View) {
        dateEditText = view.findViewById(R.id.dateEditText)
        typeRadioGroup = view.findViewById(R.id.typeRadioGroup)
        onlineRadioButton = view.findViewById(R.id.onlineRadioButton)
        offlineRadioButton = view.findViewById(R.id.offlineRadioButton)
        cancelButton = view.findViewById(R.id.cancelButton)
        okButton = view.findViewById(R.id.okButton)
    }

    private fun setupListeners() {
        // Клик по полю даты - открываем DatePicker
        dateEditText.setOnClickListener {
            showDateTimePicker()
        }

        // Выбор типа события
        typeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            isOnline = checkedId == R.id.onlineRadioButton
        }

        // Отмена
        cancelButton.setOnClickListener {
            dismiss()
        }

        // Подтверждение
        okButton.setOnClickListener {
            listener?.onEventOptionsConfirmed(selectedDate.time, isOnline)
            dismiss()
        }
    }

    private fun showDateTimePicker() {
        // DatePicker
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // После выбора даты показываем TimePicker
                showTimePicker()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedDate.set(Calendar.MINUTE, minute)
                updateDateText()
            },
            selectedDate.get(Calendar.HOUR_OF_DAY),
            selectedDate.get(Calendar.MINUTE),
            true // 24-hour format
        ).show()
    }

    private fun updateDateText() {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault())
        dateEditText.setText(dateFormat.format(selectedDate.time))
    }

    companion object {
        const val TAG = "EventOptionsBottomSheetFragment"

        fun newInstance(): EventOptionsBottomSheetFragment {
            return EventOptionsBottomSheetFragment()
        }
    }
}