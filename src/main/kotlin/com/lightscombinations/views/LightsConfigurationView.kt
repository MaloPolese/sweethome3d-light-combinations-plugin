package com.lightscombinations.views

import com.lightscombinations.components.light_configuration.LightConfiguration
import com.lightscombinations.components.stepper_panels.ActionPanel
import com.lightscombinations.components.stepper_panels.ContentPanel
import com.lightscombinations.controllers.LightsCombinationController
import com.lightscombinations.core.ChildView
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*

class LightsConfigurationView(parentView: StepperView) : ChildView<
        LightsConfigurationView,
        StepperView,
        LightsCombinationController>
    (parentView) {

    private val contentPanelLayout = GridBagLayout()
    private val contentPanel = JPanel(contentPanelLayout)
    private val scrollPane = JScrollPane()
    private var rows: List<JPanel> = emptyList()
    private val nextButton = JButton("Next")

    private val noLightsPlaceholder = JLabel("No lights found, please add some.")

    init {
        this.layout = BorderLayout()
        this.background = Color.WHITE

        this.initComponents()
        this.initListeners()
        this.layoutComponents()
    }

    private fun initComponents() {
        noLightsPlaceholder.horizontalAlignment = SwingConstants.CENTER
        parentView.controller.lights.forEach { light ->
            rows += LightConfiguration(parentView, light.value)
        }
        nextButton.isEnabled = parentView.controller.selectedLightCount > 0
    }

    private fun initListeners() {
        nextButton.addActionListener {
            this.parentView.controller.currentStep++
        }

        parentView.controller.addPropertyChangeListener(LightsCombinationController.Property.SELECTED_LIGHTS) {
            println("selectedLightCount ${parentView.controller.selectedLightCount}")
            nextButton.isEnabled = parentView.controller.selectedLightCount > 0
        }
    }

    private fun layoutComponents() {
        scrollPane.setViewportView(contentPanel)
        this.add(scrollPane, BorderLayout.CENTER)
        contentPanel.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)

        contentPanelLayout.rowWeights = DoubleArray(rows.size + 1) { 0.0 }.apply {
            this[rows.size] = 1.0
        }
        contentPanelLayout.rowHeights =  IntArray(rows.size + 1) { 0 }

        if (rows.isEmpty()) {
            contentPanel.add(noLightsPlaceholder, GridBagConstraints().apply {
                gridx = 0
                gridy = 0
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
            })
        }

        rows.forEachIndexed { index, panel ->
            contentPanel.add(panel, GridBagConstraints().apply {
                gridx = 0
                gridy = index
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
            })
        }


        val actionPanel = ActionPanel()
        actionPanel.add(nextButton)
        this.add(actionPanel, BorderLayout.SOUTH)
    }

}