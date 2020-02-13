package com.varunbarad.cluetrackingdonutclone

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.jem.rubberpicker.RubberRangePicker
import com.jem.rubberpicker.RubberSeekBar
import com.varunbarad.cluetrackingdonutclone.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()

        this.viewBinding.rangePeriodDays.setOnRubberRangePickerChangeListener(object :
            RubberRangePicker.OnRubberRangePickerChangeListener {
            override fun onProgressChanged(
                rangePicker: RubberRangePicker,
                startValue: Int,
                endValue: Int,
                fromUser: Boolean
            ) {
                this@MainActivity.viewBinding.displayRangePeriodDays.text =
                    "Periods: $startValue - $endValue"

                this@MainActivity.viewBinding.cycleView.setPeriodRange(startValue, endValue)
            }

            override fun onStartTrackingTouch(
                rangePicker: RubberRangePicker,
                isStartThumb: Boolean
            ) {

            }

            override fun onStopTrackingTouch(
                rangePicker: RubberRangePicker,
                isStartThumb: Boolean
            ) {

            }
        })

        this.viewBinding.rangeFertileDays.setOnRubberRangePickerChangeListener(object :
            RubberRangePicker.OnRubberRangePickerChangeListener {
            override fun onProgressChanged(
                rangePicker: RubberRangePicker,
                startValue: Int,
                endValue: Int,
                fromUser: Boolean
            ) {
                this@MainActivity.viewBinding.displayRangeFertileDays.text =
                    "Fertile Window: $startValue - $endValue"

                this@MainActivity.viewBinding.cycleView.setFertileRange(startValue, endValue)
            }

            override fun onStartTrackingTouch(
                rangePicker: RubberRangePicker,
                isStartThumb: Boolean
            ) {
            }

            override fun onStopTrackingTouch(
                rangePicker: RubberRangePicker,
                isStartThumb: Boolean
            ) {
            }
        })

        this.viewBinding.quantityDay.setOnRubberSeekBarChangeListener(object :
            RubberSeekBar.OnRubberSeekBarChangeListener {
            override fun onProgressChanged(seekBar: RubberSeekBar, value: Int, fromUser: Boolean) {
                this@MainActivity.viewBinding.displayQuantityDay.text =
                    "Date: ${value.toString().padStart(2, '0')}"

                this@MainActivity.viewBinding.cycleView.setCircleDay(value)
            }

            override fun onStartTrackingTouch(seekBar: RubberSeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: RubberSeekBar) {
            }
        })

        this.viewBinding.rangePms.setOnRubberRangePickerChangeListener(object :
            RubberRangePicker.OnRubberRangePickerChangeListener {
            override fun onProgressChanged(
                rangePicker: RubberRangePicker,
                startValue: Int,
                endValue: Int,
                fromUser: Boolean
            ) {
                this@MainActivity.viewBinding.displayRangePms.text =
                    "PMS: $startValue - $endValue"

                this@MainActivity.viewBinding.cycleView.setPmsRange(startValue, endValue)
            }

            override fun onStartTrackingTouch(
                rangePicker: RubberRangePicker,
                isStartThumb: Boolean
            ) {
            }

            override fun onStopTrackingTouch(
                rangePicker: RubberRangePicker,
                isStartThumb: Boolean
            ) {
            }
        })

        this.viewBinding.rangePeriodDays.setCurrentStartValue(3)
        this.viewBinding.rangePeriodDays.setCurrentEndValue(6)

        this.viewBinding.rangeFertileDays.setCurrentStartValue(12)
        this.viewBinding.rangeFertileDays.setCurrentEndValue(16)

        this.viewBinding.quantityDay.setCurrentValue(22)

        this.viewBinding.rangePms.setCurrentStartValue(24)
        this.viewBinding.rangePms.setCurrentEndValue(28)
    }
}
