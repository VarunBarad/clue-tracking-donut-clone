package com.varunbarad.cluetrackingdonutclone

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private const val CYCLE_ARC_SWEEP_ANGLE = 340.toFloat()
private const val ANGLE_OF_EACH_DAY = (CYCLE_ARC_SWEEP_ANGLE / 28.toFloat())
private const val CYCLE_ARC_EMPTY_ANGLE = 360 - CYCLE_ARC_SWEEP_ANGLE
private const val CYCLE_ARC_START_ANGLE = (CYCLE_ARC_EMPTY_ANGLE / 2f) - 90

class PeriodCycleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paintBase = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = ContextCompat.getColor(this@PeriodCycleView.context, R.color.donut_baseline)
    }
    private val paintBaseArrow = Paint(paintBase).apply {
        style = Paint.Style.FILL
    }
    private val paintPeriod = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = ContextCompat.getColor(this@PeriodCycleView.context, R.color.donut_period)
    }
    private val paintPeriodDot = Paint(paintPeriod).apply {
        style = Paint.Style.FILL
    }
    private val paintFertile = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = ContextCompat.getColor(this@PeriodCycleView.context, R.color.donut_fertile)
    }
    private val paintDayCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(this@PeriodCycleView.context, R.color.donut_day)
    }
    private val paintPms = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = ContextCompat.getColor(this@PeriodCycleView.context, R.color.donut_pms)
    }
    private val paintPmsCloud = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(this@PeriodCycleView.context, R.color.donut_pms)
    }
    private val paintText = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
        textSize = (this@PeriodCycleView.strokeWidth * 0.5f)
        color = ContextCompat.getColor(this@PeriodCycleView.context, R.color.donut_text)
    }
    private val paintTextInverted = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
        textSize = (this@PeriodCycleView.strokeWidth * 0.5f)
        color = ContextCompat.getColor(this@PeriodCycleView.context, R.color.donut_textInverted)
    }

    private var periodPeriod = TimePeriod(0, 0)
    private var fertilePeriod = TimePeriod(0, 0)
    private var circleDay: Int = 0
    private var pmsPeriod = TimePeriod(0, 0)

    private val cloudMatrix = Matrix()
    private val arrowTipMatrix = Matrix()
    private var center: PointF = PointF(0.0f, 0.0f)
    private var radius = 0.0f
    private var strokeWidth = 0.0f
    private var circleDayCenter: PointF = getPointOfDayOnRing(
        center,
        radius,
        circleDay
    )
    private var periodDotCenters: List<PointF> = (periodPeriod.startDay..periodPeriod.endDay).map {
        getPointOfDayOnRing(
            center,
            (radius * 0.8f),
            it
        )
    }

    private val textPathPeriod = Path()
    private val textPathFertile = Path()
    private val textPathPms = Path()
    private val arrowTipDrawableTemp: Drawable = DrawableCompat.wrap(
        VectorDrawableCompat.create(
            resources,
            R.drawable.ic_arrow_tip,
            null
        ) as Drawable
    ).also {
        DrawableCompat.setTint(it, ContextCompat.getColor(this.context, R.color.donut_baseline))
    }
    private var arrowTipBitmap: Bitmap? = null
    private val cloudDrawableTemp: Drawable = DrawableCompat.wrap(
        VectorDrawableCompat.create(
            resources,
            R.drawable.ic_cloud,
            null
        ) as Drawable
    ).also {
        DrawableCompat.setTint(it, ContextCompat.getColor(this.context, R.color.donut_pms))
    }
    private var cloudBitmap: Bitmap? = null

    fun setPeriodRange(startDay: Int, endDay: Int) {
        assert((startDay > 0) && (startDay < 29))
        assert((endDay > 0) && (endDay < 29))
        assert(startDay < endDay)

        this.periodPeriod = TimePeriod(startDay, endDay)
        this.periodDotCenters = (periodPeriod.startDay..periodPeriod.endDay).map {
            getPointOfDayOnRing(
                center,
                (radius * 0.8f),
                it
            )
        }

        textPathPeriod.reset()
        textPathPeriod.addArc(
            (center.x - radius),
            (center.y - radius),
            (center.x + radius),
            (center.y + radius),
            getAngleForDay(periodPeriod.startDay),
            (getAngleForDay(periodPeriod.endDay) - getAngleForDay(periodPeriod.startDay))
        )

        invalidate()
    }

    fun setFertileRange(startDay: Int, endDay: Int) {
        assert((startDay > 0) && (startDay < 29))
        assert((endDay > 0) && (endDay < 29))
        assert(startDay < endDay)

        this.fertilePeriod = TimePeriod(startDay, endDay)

        textPathFertile.reset()
        textPathFertile.addArc(
            (center.x - radius),
            (center.y - radius),
            (center.x + radius),
            (center.y + radius),
            getAngleForDay(fertilePeriod.startDay),
            (getAngleForDay(fertilePeriod.endDay) - getAngleForDay(fertilePeriod.startDay))
        )

        invalidate()
    }

    fun setCircleDay(day: Int) {
        assert((day >= 1) && (day <= 28))

        this.circleDay = day
        this.circleDayCenter = getPointOfDayOnRing(center, radius, circleDay)

        invalidate()
    }

    fun setPmsRange(startDay: Int, endDay: Int) {
        assert((startDay > 0) && (startDay < 29))
        assert((endDay > 0) && (endDay < 29))
        assert(startDay < endDay)

        this.pmsPeriod = TimePeriod(startDay, endDay)

        textPathPms.reset()
        textPathPms.addArc(
            ((center.x - radius) - ((paintText.ascent() + paintText.descent()) * 2.5f)),
            ((center.y - radius) - ((paintText.ascent() + paintText.descent()) * 2.5f)),
            ((center.x + radius) + ((paintText.ascent() + paintText.descent()) * 2.5f)),
            ((center.y + radius) + ((paintText.ascent() + paintText.descent()) * 2.5f)),
            getAngleForDay(pmsPeriod.startDay),
            (getAngleForDay(pmsPeriod.endDay) - getAngleForDay(pmsPeriod.startDay))
        )

        invalidate()
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        val smallerSide = min(width, height).toFloat()
        radius = ((smallerSide / 2f) * 0.8f)
        center = PointF(
            (width.toFloat() / 2f),
            (height.toFloat() / 2f)
        )
        circleDayCenter = getPointOfDayOnRing(
            center,
            radius,
            circleDay
        )
        this.periodDotCenters = (periodPeriod.startDay..periodPeriod.endDay).map {
            getPointOfDayOnRing(
                center,
                (radius * 0.8f),
                it
            )
        }

        textPathPeriod.reset()
        textPathPeriod.addArc(
            (center.x - radius),
            (center.y - radius),
            (center.x + radius),
            (center.y + radius),
            getAngleForDay(periodPeriod.startDay),
            (getAngleForDay(periodPeriod.endDay) - getAngleForDay(periodPeriod.startDay))
        )
        textPathFertile.reset()
        textPathFertile.addArc(
            (center.x - radius),
            (center.y - radius),
            (center.x + radius),
            (center.y + radius),
            getAngleForDay(fertilePeriod.startDay),
            (getAngleForDay(fertilePeriod.endDay) - getAngleForDay(fertilePeriod.startDay))
        )
        textPathPms.reset()
        textPathPms.addArc(
            ((center.x - radius) - ((paintText.ascent() + paintText.descent()) * 2.5f)),
            ((center.y - radius) - ((paintText.ascent() + paintText.descent()) * 2.5f)),
            ((center.x + radius) + ((paintText.ascent() + paintText.descent()) * 2.5f)),
            ((center.y + radius) + ((paintText.ascent() + paintText.descent()) * 2.5f)),
            getAngleForDay(pmsPeriod.startDay),
            (getAngleForDay(pmsPeriod.endDay) - getAngleForDay(pmsPeriod.startDay))
        )

        strokeWidth = (smallerSide * 0.075f)
        paintBase.strokeWidth = strokeWidth
        paintPeriod.strokeWidth = strokeWidth
        paintFertile.strokeWidth = strokeWidth
        paintPms.strokeWidth = strokeWidth
        paintPeriodDot.strokeWidth = (strokeWidth * 0.1f)
        paintText.textSize = (strokeWidth * 0.5f)
        paintTextInverted.textSize = (strokeWidth * 0.5f)

        this.arrowTipBitmap = this.arrowTipDrawableTemp.toBitmap(
            width = (strokeWidth * 3.2f).toInt(),
            height = (strokeWidth * 3.2f).toInt(),
            config = Bitmap.Config.ARGB_8888
        )
        this.cloudBitmap = this.cloudDrawableTemp.toBitmap(
            width = strokeWidth.toInt(),
            height = strokeWidth.toInt(),
            config = Bitmap.Config.ARGB_8888
        )
    }

    override fun onDraw(c: Canvas?) {
        super.onDraw(c)

        c?.let { canvas ->
            // Draw the base arc
            canvas.drawArc(
                (center.x - radius),
                (center.y - radius),
                (center.x + radius),
                (center.y + radius),
                CYCLE_ARC_START_ANGLE,
                CYCLE_ARC_SWEEP_ANGLE,
                false,
                paintBase
            )
            // Draw base arc tip arrow
            val nonNullArrowTipBitmap = arrowTipBitmap
            if (nonNullArrowTipBitmap != null) {
                val arrowTipCenter = getPointOfDayOnRing(
                    center,
                    radius,
                    29
                )
                arrowTipMatrix.reset()
                arrowTipMatrix.postTranslate(
                    (-(nonNullArrowTipBitmap.width / 2f)),
                    (-(nonNullArrowTipBitmap.width / 2f))
                )
                arrowTipMatrix.postRotate(((getAngleForDay(28) + getAngleForDay(29)) / 2f) + 90)
                arrowTipMatrix.postTranslate(arrowTipCenter.x, arrowTipCenter.y)
                canvas.drawBitmap(
                    nonNullArrowTipBitmap,
                    arrowTipMatrix,
                    paintBaseArrow
                )
            }

            // Draw period arc
            canvas.drawArc(
                (center.x - radius),
                (center.y - radius),
                (center.x + radius),
                (center.y + radius),
                getAngleForDay(periodPeriod.startDay),
                (getAngleForDay(periodPeriod.endDay) - getAngleForDay(periodPeriod.startDay)),
                false,
                paintPeriod
            )

            // Period day dots
            for (dotCenter in periodDotCenters) {
                canvas.drawCircle(
                    dotCenter.x,
                    dotCenter.y,
                    (strokeWidth * 0.1f),
                    paintPeriodDot
                )
            }

            // Draw Text Period
            canvas.drawTextOnPath(
                resources.getString(R.string.label_period),
                textPathPeriod,
                0f,
                (-((paintTextInverted.descent() + paintTextInverted.ascent()) / 2)),
                paintTextInverted
            )

            // Draw fertility arc
            canvas.drawArc(
                (center.x - radius),
                (center.y - radius),
                (center.x + radius),
                (center.y + radius),
                getAngleForDay(fertilePeriod.startDay),
                (getAngleForDay(fertilePeriod.endDay) - getAngleForDay(fertilePeriod.startDay)),
                false,
                paintFertile
            )

            // Draw Text Fertile
            canvas.drawTextOnPath(
                resources.getString(R.string.label_fertileWindow),
                textPathFertile,
                0f,
                (-((paintTextInverted.descent() + paintTextInverted.ascent()) / 2)),
                paintTextInverted
            )

            // Draw PMS clouds
            val nonNullCloudBitmap = cloudBitmap
            if (nonNullCloudBitmap != null) {
                for (day in pmsPeriod.startDay..pmsPeriod.endDay) {
                    val cloudCenter = getPointOfDayOnRing(
                        center,
                        radius,
                        day
                    )
                    cloudMatrix.reset()
                    cloudMatrix.postTranslate(
                        (-(nonNullCloudBitmap.width / 2f)),
                        (-(nonNullCloudBitmap.width / 2f))
                    )
                    cloudMatrix.postRotate(getAngleForDay(day) + 90)
                    cloudMatrix.postTranslate(cloudCenter.x, cloudCenter.y)
                    canvas.drawBitmap(
                        nonNullCloudBitmap,
                        cloudMatrix,
                        paintPmsCloud
                    )
                }

                // Draw Text PMS
                canvas.drawTextOnPath(
                    this.context.resources.getString(R.string.label_pms),
                    textPathPms,
                    0f,
                    (-((paintText.descent() + paintText.ascent()) / 2)),
                    paintText
                )
            }

            // Draw day circle
            canvas.drawCircle(
                circleDayCenter.x,
                circleDayCenter.y,
                strokeWidth,
                paintDayCircle
            )

            // Text Circle day (2 lines)
            canvas.drawText(
                "Date ${circleDay.toString().padStart(2, '0')}",
                circleDayCenter.x,
                (circleDayCenter.y - ((paintTextInverted.descent() + paintTextInverted.ascent()) / 2)),
                paintTextInverted
            )
        }
    }
}

/**
 * @param day Ranges from 1 to 28
 */
private fun getAngleForDay(day: Int): Float {
    return CYCLE_ARC_START_ANGLE + ((day - 1).toFloat() * ANGLE_OF_EACH_DAY)
}

private fun getPointOfDayOnRing(ringCenter: PointF, ringRadius: Float, day: Int): PointF {
    val angle = getAngleForDay(day)
    val x = (cos(angle.toRadians()) * ringRadius) + ringCenter.x
    val y = (sin(angle.toRadians()) * ringRadius) + ringCenter.y
    return PointF(x, y)
}
