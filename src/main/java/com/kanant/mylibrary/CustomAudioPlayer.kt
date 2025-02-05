package com.kanant.mylibrary

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.kanant.mylibrary.databinding.VoiceViewBinding
import com.masoudss.lib.WaveformSeekBar
import com.masoudss.lib.utils.Utils
import java.io.File
import kotlin.math.pow
import kotlin.math.sqrt

@SuppressLint("ClickableViewAccessibility")
class CustomAudioPlayer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var playButton: ImageView
        private set
    var seekBar: SeekBar
        private set
    var waveView: WaveformSeekBar
        private set
    var errorMessageTextView: TextView? = null
        private set

    var isSelectionMode: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    var onPlayClick: () -> Unit = {}
        set(value) {
            field = value
            invalidate()
        }

    var onStartPlaying: () -> Unit = {}
        set(value) {
            field = value
            invalidate()
        }

    var onStopPlaying: () -> Unit = {}
        set(value) {
            field = value
            invalidate()
        }

    var onResume: () -> Unit = {}
        set(value) {
            field = value
            invalidate()
        }

    var onStartTracking: () -> Unit = {}
        set(value) {
            field = value
            invalidate()
        }

    var onStopTracking: () -> Unit = {}
        set(value) {
            field = value
            invalidate()
        }

    var onProgressChanged: () -> Unit = {}
        set(value) {
            field = value
            invalidate()
        }

    var parent: View? = null
        set(value) {
            field = value
            invalidate()
        }

    var filePath: String? = null
        set(value) {
            field = value
            if (filePath == null || !File(filePath!!).exists()) {
                showErrorMessage("Bu səs yazısı tapılmadı")
            } else {
                hideErrorMessage()
                waveView.setSampleFrom(filePath!!)
            }
        }

    var player: MyPlayer? = null
        set(value) {
            field = value
            invalidate()
        }

    var progress: Float = 0F
        set(value) {
            field = value
            invalidate()
        }

    var waveBackgroundColor: Int = Color.LTGRAY
        set(value) {
            field = value
            invalidate()
        }

    var waveProgressColor: Int = Color.WHITE
        set(value) {
            field = value
            invalidate()
        }

    var seekBarBackgroundColor: Int = Color.TRANSPARENT
        set(value) {
            field = value
            invalidate()
        }

    var seekBarProgressColor: Int = Color.TRANSPARENT
        set(value) {
            field = value
            invalidate()
        }

    var seekBarThumbColor: Int = Color.BLACK
        set(value) {
            field = value
            invalidate()
        }

    var seekBarThumbSize: Float = Utils.dp(context, 13)
        set(value) {
            field = value
            invalidate()
        }

    var playButtonColor: Int = Color.BLUE
        set(value) {
            field = value
            invalidate()
        }

    var waveWidth: Float = Utils.dp(context, 5)
        set(value) {
            field = value
            invalidate()
        }

    var waveMinHeight: Float = waveWidth
        set(value) {
            field = value
            invalidate()
        }

    var waveCornerRadius: Float = Utils.dp(context, 2)
        set(value) {
            field = value
            invalidate()
        }

    init {
        if (isInEditMode) {
            val preview = LayoutInflater.from(context).inflate(R.layout.voice_view, this, true)
            playButton = preview.findViewById(R.id.playButton)
            seekBar = preview.findViewById(R.id.seekbar)
            waveView = preview.findViewById(R.id.waveForm)
        } else {
            val binding = VoiceViewBinding.inflate(LayoutInflater.from(context), this, true)
            playButton = binding.playButton
            seekBar = binding.seekbar
            waveView = binding.waveForm

            binding.isPlaying = false
            seekBar.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {

                    override fun onProgressChanged(
                        seek: SeekBar,
                        prog: Int,
                        fromUser: Boolean
                    ) {
                        onProgressChanged.invoke()
                        progress = prog.toFloat()

                        if (fromUser && binding.isPlaying == true) {
                            player!!.stopPlaying(paused = true)
                        }
                        waveView.progress = progress
                    }

                    override fun onStartTrackingTouch(seek: SeekBar) {
                        onStartTracking.invoke()
                    }

                    override fun onStopTrackingTouch(seek: SeekBar) {
                        onStopTracking.invoke()
                        if (player!!.paused) {
                            player!!.startPlayingFrom(progress)
                        }
                    }
                })

            playButton.setOnLongClickListener { _ ->
                parent?.performLongClick()
                true
            }

            playButton.setOnClickListener {
                onPlayClick.invoke()
                if (!isSelectionMode) {
                    if (binding.isPlaying == false) {
                        player!!.injectMedia(filePath,
                            onStartPlaying = {
                                onStartPlaying.invoke()
                                binding.isPlaying = true
                            },
                            onStopPlaying = {
                                onStopPlaying.invoke()
                                binding.isPlaying = false
                            },
                            onResume = {
                                onResume.invoke()
                                seekBar.progress = it.toInt()
                            })
                        player!!.startPlayingFrom(progress)
                    } else {
                        player!!.stopPlaying()
                    }
                } else {
                    parent?.performClick()
                }
            }

            seekBar.setOnTouchListener(object :
                OnTouchListener {
                var startX = 0F
                var startY = 0F
                val handler = Handler(Looper.getMainLooper())
                val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            handler.postDelayed({
                                parent?.performLongClick() ?: seekBar.performLongClick()
                            }, ViewConfiguration.getLongPressTimeout().toLong())

                            startX = event.rawX
                            startY = event.rawY
                            return isSelectionMode
                        }

                        MotionEvent.ACTION_MOVE -> {
                            val distance =
                                sqrt((event.rawX - startX).pow(2) + (event.rawY - startY).pow(2))
                            if (distance > touchSlop) {
                                handler.removeCallbacksAndMessages(null)
                            }
                            return isSelectionMode
                        }

                        MotionEvent.ACTION_UP -> {
                            handler.removeCallbacksAndMessages(null)
                            if (isSelectionMode) {
                                parent?.performClick() ?: seekBar.performClick()
                            }
                            return isSelectionMode
                        }

                        else -> {
                            handler.removeCallbacksAndMessages(null)
                            return false
                        }
                    }
                }
            })
        }
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.CustomAudioPlayer)

            waveView.progress =
                typedArray.getFloat(R.styleable.CustomAudioPlayer_progress, waveView.progress)
            waveView.waveBackgroundColor = typedArray.getColor(
                R.styleable.CustomAudioPlayer_waveBackgroundColor,
                waveView.waveBackgroundColor
            )
            waveView.waveProgressColor = typedArray.getColor(
                R.styleable.CustomAudioPlayer_waveProgressColor,
                waveView.waveProgressColor
            )

            seekBarBackgroundColor = typedArray.getColor(
                R.styleable.CustomAudioPlayer_seekBarBackgroundColor,
                seekBarBackgroundColor
            )
            seekBarProgressColor = typedArray.getColor(
                R.styleable.CustomAudioPlayer_seekBarProgressColor,
                seekBarProgressColor
            )
            seekBarThumbColor = typedArray.getColor(
                R.styleable.CustomAudioPlayer_seekBarThumbColor,
                seekBarThumbColor
            )

            seekBarThumbSize =
                typedArray.getDimension(R.styleable.CustomAudioPlayer_seekBarThumbSize, 13F)

            playButtonColor =
                typedArray.getColor(
                    R.styleable.CustomAudioPlayer_playButtonColor,
                    playButtonColor
                )

            waveView.waveWidth =
                typedArray.getDimension(
                    R.styleable.CustomAudioPlayer_waveWidth,
                    waveView.waveWidth
                )

            waveView.waveMinHeight = typedArray.getDimension(
                R.styleable.CustomAudioPlayer_waveMinHeight,
                waveView.waveMinHeight
            )
            waveView.waveCornerRadius = typedArray.getDimension(
                R.styleable.CustomAudioPlayer_waveCornerRadius,
                waveView.waveCornerRadius
            )

//        seekBar.progressDrawable.setColorFilter(seekBarProgressColor, PorterDuff.Mode.SRC_IN)
            seekBar.progressDrawable?.setTint(seekBarProgressColor)
            seekBar.progressTintList = ColorStateList.valueOf(seekBarProgressColor)
//            seekBar.backgroundTintList = ColorStateList.valueOf(seekBarBackgroundColor)

            val rectShape = OvalShape()

            val shapeDrawable = ShapeDrawable(rectShape)
            shapeDrawable.paint.color = seekBarThumbColor
            shapeDrawable.paint.style = Paint.Style.FILL
            shapeDrawable.paint.isAntiAlias = true
            shapeDrawable.paint.flags = Paint.ANTI_ALIAS_FLAG

            shapeDrawable.setIntrinsicWidth(seekBarThumbSize.toInt())
            shapeDrawable.setIntrinsicHeight(seekBarThumbSize.toInt())

            seekBar.thumb = shapeDrawable

            playButton.backgroundTintList = ColorStateList.valueOf(playButtonColor)

            typedArray.recycle()
        }
    }

    private fun showErrorMessage(message: String) {
        if (errorMessageTextView != null) {
            hideErrorMessage()
        }
        errorMessageTextView = TextView(context).apply {
            text = message
            textSize = 16f
            setTypeface(null, Typeface.ITALIC)
            setTextColor(Color.RED)
            id = View.generateViewId()
        }

        val deletedTextParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        deletedTextParams.topToBottom = this.id
        deletedTextParams.startToStart = LayoutParams.PARENT_ID

        seekBar.visibility = GONE
        waveView.visibility = GONE
        playButton.visibility = GONE

        (this.getChildAt(0) as ConstraintLayout).addView(errorMessageTextView, deletedTextParams)
    }

    private fun hideErrorMessage() {
        seekBar.visibility = VISIBLE
        waveView.visibility = VISIBLE
        playButton.visibility = VISIBLE

        errorMessageTextView?.let {
            (this.getChildAt(0) as ConstraintLayout).removeView(errorMessageTextView)
            errorMessageTextView = null
        }
    }
}