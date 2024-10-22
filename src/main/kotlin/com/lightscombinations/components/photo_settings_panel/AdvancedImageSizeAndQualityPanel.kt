package com.lightscombinations.components.photo_settings_panel

import com.eteks.sweethome3d.model.Camera
import com.eteks.sweethome3d.model.Camera.Lens
import com.eteks.sweethome3d.swing.PhotoPanel
import com.eteks.sweethome3d.swing.SwingTools
import com.eteks.sweethome3d.tools.OperatingSystem
import com.lightscombinations.controllers.LightsCombinationController
import com.lightscombinations.core.ChildView
import com.lightscombinations.utils.mergeDateAndTimeToUTC
import com.lightscombinations.views.StepperView
import java.awt.*
import java.beans.PropertyChangeListener
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.*
import javax.swing.JSpinner.DateEditor
import javax.swing.event.ChangeListener


class AdvancedImageSizeAndQualityPanel (parentView: StepperView) : ChildView<
        AdvancedImageSizeAndQualityPanel,
        StepperView,
        LightsCombinationController>
    (parentView) {

    private val dateLabel = JLabel("Date:")
    private val dateSpinnerModel = SpinnerDateModel()
    private val dateSpinner = JSpinner(dateSpinnerModel)
    private val timeLabel = JLabel("Time:")
    private val timeSpinnerModel = SpinnerDateModel()
    private val timeSpinner = JSpinner(timeSpinnerModel)
    private val dayNightLabel = JLabel()
    private val lensLabel: JLabel = JLabel("Lens:")
    private val lensComboBox = JComboBox(Camera.Lens.entries.toTypedArray());
    private val ceilingLightEnabledCheckBox = JCheckBox("Add celling lights")
    private val advancedComponentsSeparator = JSeparator()

    private val dayIcon = SwingTools.getScaledImageIcon(PhotoPanel::class.java.getResource("resources/day.png"))
    private val nightIcon = SwingTools.getScaledImageIcon(PhotoPanel::class.java.getResource("resources/night.png"))

    init {
        this.layout = GridBagLayout()

        this.createComponents()
        this.initListeners()
        this.layoutComponents()
    }

    fun toggleAdvancedComponents(state: Boolean) {
        this.dateLabel.isEnabled = state
        this.dateSpinner.isEnabled = state
        this.timeLabel.isEnabled = state
        this.timeSpinner.isEnabled = state
        this.lensLabel.isEnabled = state
        this.lensComboBox.isEnabled = state
        this.ceilingLightEnabledCheckBox.isEnabled = state
    }

    private fun createComponents() {
        val time = Date(Camera.convertTimeToTimeZone(parentView.controller.home.camera.time, parentView.controller.home.compass.timeZone))
        dateSpinnerModel.setValue(time)
        timeSpinnerModel.setValue(time)
        val timeInstance = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.UK) as SimpleDateFormat
        val timeEditor = DateEditor(this.timeSpinner, timeInstance.toPattern())
        timeSpinner.editor = timeEditor

        var datePattern = (DateFormat.getDateInstance(DateFormat.SHORT) as SimpleDateFormat).toPattern()
        if (datePattern.indexOf("yyyy") == -1) {
            datePattern = datePattern.replace("yy", "yyyy")
        }
        val dateEditor = DateEditor(this.dateSpinner, datePattern)
        dateSpinner.editor = dateEditor

        parentView.controller.lens = parentView.controller.home.camera.lens

        lensComboBox.setRenderer(object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?, value: Any,
                index: Int, isSelected: Boolean, cellHasFocus: Boolean
            ): Component {
                val displayedValue: String = when (value as Lens) {
                    Lens.NORMAL -> "Depth of Field"
                    Lens.SPHERICAL -> "Spherical"
                    Lens.FISHEYE -> "Fisheye"
                    else -> "Default"
                }
                return super.getListCellRendererComponent(
                    list, displayedValue, index, isSelected,
                    cellHasFocus
                )
            }
        })

        ceilingLightEnabledCheckBox.isSelected = parentView.controller.ceilingLightColor > 0
    }
    private fun initListeners() {
        val timeChangeListener = PropertyChangeListener {
            val date = Date(Camera.convertTimeToTimeZone(parentView.controller.time, TimeZone.getDefault().id))
            dateSpinnerModel.value = date
            timeSpinnerModel.value = date
        }
        parentView.controller.addPropertyChangeListener(LightsCombinationController.Property.TIME, timeChangeListener)
        val dateTimeChangeListener = ChangeListener {
            val utcCalendar = mergeDateAndTimeToUTC(dateSpinnerModel.value as Date, timeSpinnerModel.value as Date)
            parentView.controller.time = utcCalendar.timeInMillis
        }
        dateSpinnerModel.addChangeListener(dateTimeChangeListener)
        timeSpinnerModel.addChangeListener(dateTimeChangeListener)

        val dayNightListener = PropertyChangeListener {
            val isDay = parentView.controller.home.compass.getSunElevation(Camera.convertTimeToTimeZone(parentView.controller.time, parentView.controller.home.compass.timeZone)) > 0
            if (isDay) dayNightLabel.icon = dayIcon else dayNightLabel.icon = nightIcon
        }

        parentView.controller.addPropertyChangeListener(LightsCombinationController.Property.TIME, dayNightListener)
        parentView.controller.home.compass.addPropertyChangeListener(dayNightListener)
        dayNightListener.propertyChange(null)

        parentView.controller.addPropertyChangeListener(LightsCombinationController.Property.CEILING_LIGHT_COLOR) {
            ceilingLightEnabledCheckBox.isSelected = parentView.controller.ceilingLightColor > 0
        }
        ceilingLightEnabledCheckBox.addItemListener { parentView.controller.ceilingLightColor = if (ceilingLightEnabledCheckBox.isSelected) 0xD0D0D0 else 0 }

        lensComboBox.selectedItem = parentView.controller.lens
        parentView.controller.addPropertyChangeListener(LightsCombinationController.Property.LENS) { lensComboBox.selectedItem = parentView.controller.lens }
        lensComboBox.addItemListener {
            parentView.controller.lens = lensComboBox.selectedItem as Lens
        }
    }

    private fun layoutComponents() {
        val labelAlignment = if (OperatingSystem.isMacOSX()) JLabel.TRAILING
                             else JLabel.LEADING

        val standardGap = Math.round(5 * SwingTools.getResolutionScale())

        val advancedComponentsSeparatorGbc = GridBagConstraints(0, 0, 5, 1, 1.0, 0.0, GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL, Insets(8, 0, 8, 0), 0, 0)
        this.add(this.advancedComponentsSeparator, advancedComponentsSeparatorGbc)

        val dateLabelGbc = GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL, Insets(0, 0, standardGap, standardGap), 0, 0)
        add(this.dateLabel, dateLabelGbc)
        this.dateLabel.setHorizontalAlignment(labelAlignment)

        val dateSpinnerGbc = GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START,GridBagConstraints.HORIZONTAL, Insets(0, 0, standardGap, 10), 0, 0)
        add(this.dateSpinner, dateSpinnerGbc)

        val timeLabelGbc = GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL, Insets(0, 0, standardGap, standardGap), 0, 0)
        add(this.timeLabel, timeLabelGbc)

        this.timeLabel.setHorizontalAlignment(labelAlignment)
        val timeSpinnerGbc = GridBagConstraints(3, 1, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START,GridBagConstraints.HORIZONTAL, Insets(0, 0, standardGap, standardGap), 0, 0)
        add(this.timeSpinner, timeSpinnerGbc)

        val dayNightLabelGbc = GridBagConstraints(4, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START,GridBagConstraints.NONE, Insets(0, 0, standardGap, 0), 0, 0)
        add(this.dayNightLabel, dayNightLabelGbc)

        val lensLabelGbc = GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL, Insets(0, 0, 0, standardGap), 0, 0)
        add(this.lensLabel, lensLabelGbc)
        this.lensLabel.setHorizontalAlignment(labelAlignment)

        val lensComboBoxGbc = GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START,GridBagConstraints.HORIZONTAL, Insets(0, 0, 0, 10), 0, 0)
        add(this.lensComboBox, lensComboBoxGbc)

        val ceilingLightEnabledCheckBoxGbc = GridBagConstraints(2, 2, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER,GridBagConstraints.NONE, Insets(0, 0, 0, 0), 0, 0)
        add(this.ceilingLightEnabledCheckBox, ceilingLightEnabledCheckBoxGbc)
    }
}