package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import com.udacity.R.color
import kotlinx.android.synthetic.main.content_main.view.*
import timber.log.Timber
import kotlin.properties.Delegates

val CIRCLE_SIZE = 70f

//present the available button label
private enum class ButtonLabel(val label: Int) {
    LOADING(R.string.button_loading),
    DOWNLOAD(R.string.button_download);

    // changes the current button label to the next button label in the list
    fun next() = when (this) {
        DOWNLOAD -> LOADING
        LOADING -> DOWNLOAD
    }
}

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    // set init text for the button label
    private var buttonText = ButtonLabel.DOWNLOAD

    // cache the attribute values.
    private var buttonDownloadColor = 0
    private var buttonLoadingColor = 0

    // width and height of the button
    private var widthSize = 0
    private var heightSize = 0

    // width and angle of the circle
    private var currentWidth = 0f
    private var currentAngle = 0f

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        Timber.i("ButtonState")
    }

    // set the paint
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        Timber.i("paint")
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.NORMAL)
    }

    // initialize
    init {
        // Setting the view's isClickable property to true enables that view to accept user input.
        isClickable = true

        // supply the attributes and view, and and set your local variables.
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            buttonDownloadColor = getColor(R.styleable.LoadingButton_buttonColorDownload, 0)
            buttonLoadingColor = getColor(R.styleable.LoadingButton_buttonColorLoading, 0)
        }
    }

    // the functionaly for the button click
    override fun performClick(): Boolean {
        Timber.i("performClick")

        // Show button label
        buttonText = buttonText.next()
        contentDescription = resources.getString(buttonText.label)

        // Animation of the button and circle
        animateButton()

        // enable onClickListener
        return super.performClick()
    }

    // perform by size change
    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        Timber.i("onSizeChanged")
        widthSize = width
        heightSize = height
    }


    // draw the button
    override fun onDraw(canvas: Canvas?) {
        Timber.i("onDraw")
        super.onDraw(canvas)

        // paint Loading left side
        paint.color = buttonLoadingColor
        canvas?.drawRect(0f, 0f, currentWidth, heightSize.toFloat(), paint)

        // paint download right side
        paint.color = buttonDownloadColor
        canvas?.drawRect(currentWidth, 0f, widthSize.toFloat(), heightSize.toFloat(), paint)

        // draw the text
        paint.color = Color.WHITE
        canvas?.drawText(
            resources.getString(buttonText.label),
            widthSize / 2f,
            (heightSize + paint.textSize) / 2f,
            paint
        )

        // draw the circle
        paint.color = resources.getColor(color.colorAccent, null)
        val rect = RectF(
            (widthSize + paint.textSize) * 2f / 3f,
            (heightSize - CIRCLE_SIZE) / 2f,
            (widthSize + paint.textSize) * 2 / 3f + CIRCLE_SIZE,
            (heightSize + CIRCLE_SIZE) / 2f
        )
        canvas?.drawArc(rect, 0f, currentAngle, true, paint)
    }

    // measure of the button
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Timber.i("onMeasure")
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            View.MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    // Enable and disable button
    private fun ValueAnimator.disableViewDuringAnimation(view: View) {
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                view.isEnabled = false
            }

            override fun onAnimationEnd(animation: Animator?) {
                view.isEnabled = true
            }
        })
    }

    private fun animateButton() {
        // Create Animator for the Button
        val buttonAnimator = ValueAnimator.ofFloat(0f, widthSize.toFloat())

        // disbale button click
        buttonAnimator.disableViewDuringAnimation(custom_button)

        // change button width for animation
        buttonAnimator.addUpdateListener { animation ->
            currentWidth = animation.animatedValue as Float
            invalidate()
        }

        // Create Animator for the circle
        val circleAnimator = ValueAnimator.ofFloat(0f, 360f)

        // change angel of the circle
        circleAnimator.addUpdateListener { animation ->
            currentAngle = animation.animatedValue as Float
            invalidate()
        }

        // Running the animations in parallel with AnimatorSet
        val set = AnimatorSet()
        set.playTogether(buttonAnimator, circleAnimator)
        set.duration = 3000

        // redraw button download after animation
        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                buttonText = buttonText.next()
                contentDescription = resources.getString(buttonText.label)
                currentWidth = 0f
                currentAngle = 0f
                invalidate()
            }
        })

        // Start animation
        set.start()
    }
}