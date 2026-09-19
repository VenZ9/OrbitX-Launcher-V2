package com.orbitx.launcher.controls

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.orbitx.launcher.core.OrbitXApplication
import com.orbitx.launcher.utils.PreferencesManager
import com.orbitx.launcher.utils.Logging

/**
 * TouchControlsView - Custom view for rendering and handling touch controls
 * Similar to Zalith Launcher 2's touch control system
 */
class TouchControlsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    companion object {
        private const val TAG = "TouchControlsView"
        
        // Control types
        enum class ControlType {
            JOYSTICK,
            BUTTON,
            HOTBAR,
            SWIPE
        }
        
        // Default control sizes
        private const val DEFAULT_BUTTON_SIZE = 60f
        private const val DEFAULT_JOYSTICK_SIZE = 100f
        private const val DEFAULT_HOTBAR_SIZE = 200f
        private const val DEFAULT_OPACITY = 0.8f
    }
    
    // Paints for drawing
    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val joystickPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    // Control data
    private val controls = mutableListOf<TouchControl>()
    private var activeControl: TouchControl? = null
    private var isEditing = false
    private var selectedControl: TouchControl? = null
    
    // Touch tracking
    private var touchStartX = 0f
    private var touchStartY = 0f
    private var isDragging = false
    
    // Preferences
    private val preferencesManager: PreferencesManager by lazy {
        OrbitXApplication.getInstance().getPreferencesManager()
    }
    
    // Callback
    private var controlCallback: ControlCallback? = null
    
    init {
        // Initialize paints
        buttonPaint.color = Color.parseColor("#6200EE")
        buttonPaint.alpha = (DEFAULT_OPACITY * 255).toInt()
        buttonPaint.style = Paint.Style.FILL
        
        joystickPaint.color = Color.parseColor("#03DAC6")
        joystickPaint.alpha = (DEFAULT_OPACITY * 255).toInt()
        joystickPaint.style = Paint.Style.FILL
        
        textPaint.color = Color.WHITE
        textPaint.textSize = 12f * resources.displayMetrics.density
        textPaint.textAlign = Paint.Align.CENTER
        
        backgroundPaint.color = Color.parseColor("#80000000")
        backgroundPaint.alpha = 128
        backgroundPaint.style = Paint.Style.FILL
        
        // Load controls from preferences
        loadControls()
    }
    
    /**
     * Load controls from preferences
     */
    private fun loadControls() {
        val config = preferencesManager.getTouchControlsConfig()
        
        config?.let { touchConfig ->
            // Add joystick
            addJoystickControl(
                x = touchConfig.joystick.x,
                y = touchConfig.joystick.y,
                size = DEFAULT_JOYSTICK_SIZE * touchConfig.joystick.scale,
                opacity = touchConfig.joystick.opacity,
                visible = touchConfig.joystick.visible
            )
            
            // Add jump button
            addButtonControl(
                id = "jump",
                x = touchConfig.jumpButton.x,
                y = touchConfig.jumpButton.y,
                size = DEFAULT_BUTTON_SIZE * touchConfig.jumpButton.scale,
                icon = "↑",
                label = "Jump",
                opacity = touchConfig.jumpButton.opacity,
                visible = touchConfig.jumpButton.visible
            )
            
            // Add sneak button
            addButtonControl(
                id = "sneak",
                x = touchConfig.sneakButton.x,
                y = touchConfig.sneakButton.y,
                size = DEFAULT_BUTTON_SIZE * touchConfig.sneakButton.scale,
                icon = "↓",
                label = "Sneak",
                opacity = touchConfig.sneakButton.opacity,
                visible = touchConfig.sneakButton.visible
            )
            
            // Add attack button
            addButtonControl(
                id = "attack",
                x = touchConfig.attackButton.x,
                y = touchConfig.attackButton.y,
                size = DEFAULT_BUTTON_SIZE * touchConfig.attackButton.scale,
                icon = "✕",
                label = "Attack",
                opacity = touchConfig.attackButton.opacity,
                visible = touchConfig.attackButton.visible
            )
            
            // Add use button
            addButtonControl(
                id = "use",
                x = touchConfig.useButton.x,
                y = touchConfig.useButton.y,
                size = DEFAULT_BUTTON_SIZE * touchConfig.useButton.scale,
                icon = "●",
                label = "Use",
                opacity = touchConfig.useButton.opacity,
                visible = touchConfig.useButton.visible
            )
            
            // Add inventory button
            addButtonControl(
                id = "inventory",
                x = touchConfig.inventoryButton.x,
                y = touchConfig.inventoryButton.y,
                size = DEFAULT_BUTTON_SIZE * touchConfig.inventoryButton.scale,
                icon = "□",
                label = "Inventory",
                opacity = touchConfig.inventoryButton.opacity,
                visible = touchConfig.inventoryButton.visible
            )
            
            // Add hotbar
            addHotbarControl(
                x = touchConfig.hotbar.x,
                y = touchConfig.hotbar.y,
                width = DEFAULT_HOTBAR_SIZE * touchConfig.hotbar.scale,
                height = 40f * touchConfig.hotbar.scale,
                opacity = touchConfig.hotbar.opacity,
                visible = touchConfig.hotbar.visible
            )
        } ?: run {
            // Use default controls if no config
            setupDefaultControls()
        }
    }
    
    /**
     * Setup default controls
     */
    private fun setupDefaultControls() {
        // Left side - Joystick
        addJoystickControl(
            x = 100f,
            y = height - 200f,
            size = DEFAULT_JOYSTICK_SIZE,
            opacity = DEFAULT_OPACITY,
            visible = true
        )
        
        // Right side - Buttons
        addButtonControl(
            id = "jump",
            x = width - 80f,
            y = height - 200f,
            size = DEFAULT_BUTTON_SIZE,
            icon = "↑",
            label = "Jump",
            opacity = DEFAULT_OPACITY,
            visible = true
        )
        
        addButtonControl(
            id = "sneak",
            x = width - 80f,
            y = height - 120f,
            size = DEFAULT_BUTTON_SIZE,
            icon = "↓",
            label = "Sneak",
            opacity = DEFAULT_OPACITY,
            visible = true
        )
        
        addButtonControl(
            id = "attack",
            x = width - 160f,
            y = height - 120f,
            size = DEFAULT_BUTTON_SIZE,
            icon = "✕",
            label = "Attack",
            opacity = DEFAULT_OPACITY,
            visible = true
        )
        
        addButtonControl(
            id = "use",
            x = width - 160f,
            y = height - 200f,
            size = DEFAULT_BUTTON_SIZE,
            icon = "●",
            label = "Use",
            opacity = DEFAULT_OPACITY,
            visible = true
        )
        
        addButtonControl(
            id = "inventory",
            x = width - 80f,
            y = height - 280f,
            size = DEFAULT_BUTTON_SIZE,
            icon = "□",
            label = "Inventory",
            opacity = DEFAULT_OPACITY,
            visible = true
        )
        
        // Bottom center - Hotbar
        addHotbarControl(
            x = (width - DEFAULT_HOTBAR_SIZE) / 2,
            y = height - 80f,
            width = DEFAULT_HOTBAR_SIZE,
            height = 40f,
            opacity = DEFAULT_OPACITY,
            visible = true
        )
    }
    
    /**
     * Add a joystick control
     */
    fun addJoystickControl(
        x: Float,
        y: Float,
        size: Float,
        opacity: Float = DEFAULT_OPACITY,
        visible: Boolean = true
    ): TouchControl {
        val control = TouchControl(
            id = "joystick",
            type = ControlType.JOYSTICK,
            x = x,
            y = y,
            width = size,
            height = size,
            opacity = opacity,
            visible = visible,
            icon = "+",
            label = "Joystick"
        )
        controls.add(control)
        return control
    }
    
    /**
     * Add a button control
     */
    fun addButtonControl(
        id: String,
        x: Float,
        y: Float,
        size: Float,
        icon: String,
        label: String,
        opacity: Float = DEFAULT_OPACITY,
        visible: Boolean = true
    ): TouchControl {
        val control = TouchControl(
            id = id,
            type = ControlType.BUTTON,
            x = x,
            y = y,
            width = size,
            height = size,
            opacity = opacity,
            visible = visible,
            icon = icon,
            label = label
        )
        controls.add(control)
        return control
    }
    
    /**
     * Add a hotbar control
     */
    fun addHotbarControl(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        opacity: Float = DEFAULT_OPACITY,
        visible: Boolean = true
    ): TouchControl {
        val control = TouchControl(
            id = "hotbar",
            type = ControlType.HOTBAR,
            x = x,
            y = y,
            width = width,
            height = height,
            opacity = opacity,
            visible = visible,
            label = "Hotbar"
        )
        controls.add(control)
        return control
    }
    
    /**
     * Set control callback
     */
    fun setControlCallback(callback: ControlCallback) {
        this.controlCallback = callback
    }
    
    /**
     * Set editing mode
     */
    fun setEditingMode(editing: Boolean) {
        isEditing = editing
        invalidate()
    }
    
    /**
     * Get control at position
     */
    fun getControlAt(x: Float, y: Float): TouchControl? {
        return controls.find { control ->
            control.visible && 
            x >= control.x - control.width / 2 &&
            x <= control.x + control.width / 2 &&
            y >= control.y - control.height / 2 &&
            y <= control.y + control.height / 2
        }
    }
    
    /**
     * Save controls to preferences
     */
    fun saveControls() {
        val config = PreferencesManager.TouchControlsConfig(
            joystick = getControlConfig("joystick"),
            jumpButton = getControlConfig("jump"),
            sneakButton = getControlConfig("sneak"),
            attackButton = getControlConfig("attack"),
            useButton = getControlConfig("use"),
            inventoryButton = getControlConfig("inventory"),
            dropButton = getControlConfig("drop"),
            sprintButton = getControlConfig("sprint"),
            flyButton = getControlConfig("fly"),
            hotbar = getControlConfig("hotbar"),
            chatButton = getControlConfig("chat"),
            commandButton = getControlConfig("command"),
            pauseButton = getControlConfig("pause"),
            settingsButton = getControlConfig("settings")
        )
        
        preferencesManager.setTouchControlsConfig(config)
        Logging.d(TAG, "Controls saved to preferences")
    }
    
    /**
     * Get control config from control
     */
    private fun getControlConfig(id: String): PreferencesManager.ControlConfig {
        val control = controls.find { it.id == id }
        return control?.let {
            PreferencesManager.ControlConfig(
                x = it.x,
                y = it.y,
                width = it.width,
                height = it.height,
                opacity = it.opacity,
                scale = 1f,
                visible = it.visible,
                icon = it.icon,
                label = it.label
            )
        } ?: PreferencesManager.ControlConfig()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw controls
        for (control in controls) {
            if (control.visible) {
                drawControl(canvas, control)
            }
        }
        
        // Draw editing mode background
        if (isEditing) {
            canvas.drawRect(
                RectF(0f, 0f, width.toFloat(), height.toFloat()),
                backgroundPaint
            )
        }
    }
    
    /**
     * Draw a control
     */
    private fun drawControl(canvas: Canvas, control: TouchControl) {
        when (control.type) {
            ControlType.JOYSTICK -> drawJoystick(canvas, control)
            ControlType.BUTTON -> drawButton(canvas, control)
            ControlType.HOTBAR -> drawHotbar(canvas, control)
            ControlType.SWIPE -> drawSwipeArea(canvas, control)
        }
        
        // Draw label if in editing mode
        if (isEditing && control.label.isNotEmpty()) {
            drawLabel(canvas, control)
        }
    }
    
    /**
     * Draw joystick
     */
    private fun drawJoystick(canvas: Canvas, control: TouchControl) {
        val centerX = control.x
        val centerY = control.y
        val radius = control.width / 2
        
        // Draw outer circle
        joystickPaint.alpha = (control.opacity * 150).toInt()
        canvas.drawCircle(centerX, centerY, radius, joystickPaint)
        
        // Draw inner circle
        joystickPaint.alpha = (control.opacity * 255).toInt()
        canvas.drawCircle(centerX, centerY, radius * 0.6f, joystickPaint)
        
        // Draw center point
        canvas.drawCircle(centerX, centerY, radius * 0.2f, joystickPaint)
    }
    
    /**
     * Draw button
     */
    private fun drawButton(canvas: Canvas, control: TouchControl) {
        val centerX = control.x
        val centerY = control.y
        val radius = control.width / 2
        
        // Draw button background
        buttonPaint.alpha = (control.opacity * 200).toInt()
        canvas.drawCircle(centerX, centerY, radius, buttonPaint)
        
        // Draw icon
        textPaint.color = Color.WHITE
        textPaint.textSize = radius * 0.8f
        canvas.drawText(
            control.icon,
            centerX,
            centerY + textPaint.textSize / 3,
            textPaint
        )
    }
    
    /**
     * Draw hotbar
     */
    private fun drawHotbar(canvas: Canvas, control: TouchControl) {
        val left = control.x - control.width / 2
        val top = control.y - control.height / 2
        val right = control.x + control.width / 2
        val bottom = control.y + control.height / 2
        
        // Draw background
        buttonPaint.alpha = (control.opacity * 150).toInt()
        canvas.drawRoundRect(
            RectF(left, top, right, bottom),
            control.height / 2,
            control.height / 2,
            buttonPaint
        )
        
        // Draw selected item indicator
        val selectedLeft = control.x - 20f
        val selectedRight = control.x + 20f
        buttonPaint.alpha = (control.opacity * 255).toInt()
        canvas.drawRoundRect(
            RectF(selectedLeft, top + 5f, selectedRight, bottom - 5f),
            5f,
            5f,
            buttonPaint
        )
    }
    
    /**
     * Draw swipe area
     */
    private fun drawSwipeArea(canvas: Canvas, control: TouchControl) {
        // Implementation for swipe area
    }
    
    /**
     * Draw label
     */
    private fun drawLabel(canvas: Canvas, control: TouchControl) {
        val y = control.y - control.height / 2 - 20f
        textPaint.color = Color.WHITE
        textPaint.textSize = 12f * resources.displayMetrics.density
        canvas.drawText(
            control.label,
            control.x,
            y,
            textPaint
        )
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!preferencesManager.areTouchControlsEnabled()) {
            return false
        }
        
        val x = event.x
        val y = event.y
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = x
                touchStartY = y
                isDragging = false
                
                if (isEditing) {
                    // Check if we're selecting a control
                    selectedControl = getControlAt(x, y)
                    if (selectedControl != null) {
                        isDragging = true
                        invalidate()
                        return true
                    }
                } else {
                    // Handle control press
                    activeControl = getControlAt(x, y)
                    if (activeControl != null) {
                        controlCallback?.onControlPressed(activeControl!!)
                        return true
                    }
                }
            }
            
            MotionEvent.ACTION_MOVE -> {
                if (isEditing && isDragging && selectedControl != null) {
                    // Move the control
                    selectedControl!!.x = x
                    selectedControl!!.y = y
                    invalidate()
                    return true
                } else if (activeControl?.type == ControlType.JOYSTICK) {
                    // Handle joystick movement
                    val dx = x - touchStartX
                    val dy = y - touchStartY
                    val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                    
                    controlCallback?.onJoystickMoved(dx, dy, distance)
                    return true
                }
            }
            
            MotionEvent.ACTION_UP -> {
                if (isEditing && isDragging && selectedControl != null) {
                    isDragging = false
                    selectedControl = null
                    invalidate()
                    return true
                } else if (activeControl != null) {
                    // Handle control release
                    controlCallback?.onControlReleased(activeControl!!)
                    activeControl = null
                    return true
                }
            }
        }
        
        return false
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        // Adjust control positions if needed
        if (oldw == 0 || oldh == 0) {
            // First layout - adjust default positions
            for (control in controls) {
                if (control.x == 0f && control.y == 0f) {
                    // Center the control if it hasn't been positioned
                    control.x = w / 2f
                    control.y = h / 2f
                }
            }
        }
    }
    
    /**
     * Data class for touch controls
     */
    data class TouchControl(
        val id: String,
        val type: ControlType,
        var x: Float,
        var y: Float,
        var width: Float,
        var height: Float,
        var opacity: Float = DEFAULT_OPACITY,
        var visible: Boolean = true,
        val icon: String = "",
        val label: String = ""
    )
    
    /**
     * Callback interface for control events
     */
    interface ControlCallback {
        fun onControlPressed(control: TouchControl)
        fun onControlReleased(control: TouchControl)
        fun onJoystickMoved(dx: Float, dy: Float, distance: Float)
    }
}
