package com.lightscombinations.components.light_configuration

import com.eteks.sweethome3d.swing.SwingTools
import com.eteks.sweethome3d.tools.OperatingSystem
import com.lightscombinations.controllers.LightSetting
import com.lightscombinations.controllers.LightsCombinationController
import com.lightscombinations.core.ChildView
import com.lightscombinations.views.StepperView
import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.*
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import javax.swing.border.TitledBorder

class LightConfiguration (parentView: StepperView, val lightSetting: LightSetting) : ChildView<
        LightConfiguration,
        StepperView,
        LightsCombinationController>
    (parentView) {

    val selectedCheckbox = JCheckBox()
    val powerLabel = JLabel("Light power (%):")
    val powerSpinnerModel = SpinnerNumberModel(0, 0, 100, 5)
    val powerSpinner = JSpinner(powerSpinnerModel)
    val nameLabel = JLabel("HA entity name:")
    val nameField = JTextField(lightSetting.haEntityName)

    init {
        this.layout = GridBagLayout().apply {
            columnWeights = doubleArrayOf(0.0, 1.0)
        }
        this.initComponents()
        this.initListeners()
        this.layoutComponents()
    }

    private fun initComponents() {
        this.border = CompoundBorder(
            TitledBorder(LineBorder(Color.GRAY, 2, true), lightSetting.homeLight.name),
            EmptyBorder(5, 5, 5, 5)
        )
        powerSpinner.value = (lightSetting.homeLightPower * 100).toInt()
        selectedCheckbox.isSelected = lightSetting.isSelected
        updateSelectedCheckboxLabel()
        updateBorderStyle()

    }

    private fun initListeners() {
        selectedCheckbox.addActionListener {
            lightSetting.isSelected = selectedCheckbox.isSelected
            parentView.controller.selectedLightCount += if (selectedCheckbox.isSelected) 1 else -1
        }

        lightSetting.addPropertyChangeListener(LightSetting.Property.SELECTED) {
            selectedCheckbox.isSelected = lightSetting.isSelected
            updateSelectedCheckboxLabel()
            updateBorderStyle()
        }

        nameField.addActionListener {
            lightSetting.haEntityName = nameField.text
        }

        lightSetting.addPropertyChangeListener(LightSetting.Property.HA_ENTITY_NAME) {
            nameField.text = lightSetting.haEntityName
        }

        powerSpinner.addChangeListener {
            lightSetting.homeLightPower = powerSpinner.value as Int / 100f
        }

        nameField.addFocusListener(object : FocusAdapter() {
            override fun focusLost(e: FocusEvent) {
                lightSetting.haEntityName = nameField.text
            }
        })
    }

    private fun updateBorderStyle() {
        val spacing = EmptyBorder(5, 5, 5, 5)
        if (lightSetting.isSelected) {
            this.border = CompoundBorder(TitledBorder(LineBorder(Color.decode("#18bcf2"), 2, true), lightSetting.homeLight.name),spacing)
        } else {
            this.border = CompoundBorder(TitledBorder(LineBorder(Color.GRAY, 2, true), lightSetting.homeLight.name),spacing)
        }
    }
    private fun updateSelectedCheckboxLabel() {
        if (lightSetting.isSelected) {
            selectedCheckbox.text = "Selected"
        } else {
            selectedCheckbox.text = "Not selected"
        }
    }

    private fun layoutComponents() {
        val labelAlignment = if (OperatingSystem.isMacOSX()
        ) JLabel.TRAILING
        else JLabel.LEADING
        val standardGap = Math.round(5 * SwingTools.getResolutionScale())

        val selectedCheckboxGbc  = GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            gridwidth = 3
            anchor = GridBagConstraints.NORTH
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(0, 0, 0, standardGap)
        }
        this.add(selectedCheckbox, selectedCheckboxGbc)

        // power
        val powerLabelGbc  = GridBagConstraints().apply {
            gridx = 0
            gridy = 1
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(0, 0, 0, standardGap)
        }
        powerLabel.horizontalAlignment = labelAlignment
        this.add(powerLabel, powerLabelGbc)

        val powerFieldGbc = GridBagConstraints().apply {
            gridx = 1
            gridy = 1
            gridwidth = 2
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(0, 0, standardGap, 0)
        }
        this.add(powerSpinner, powerFieldGbc)

        // Name
        val nameLabelGbc  = GridBagConstraints().apply {
            gridx = 0
            gridy = 2
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(0, 0, standardGap, standardGap)
        }
        nameLabel.horizontalAlignment = labelAlignment
        this.add(nameLabel, nameLabelGbc)


        val nameFieldGbc = GridBagConstraints().apply {
            gridx = 1
            gridy = 2
            gridwidth = 2
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(0, 0, 0, 0)
        }
        this.add(nameField, nameFieldGbc)
        nameField.columns = 10
    }
}