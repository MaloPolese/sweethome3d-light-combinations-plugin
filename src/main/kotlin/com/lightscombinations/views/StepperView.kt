package com.lightscombinations.views

import com.lightscombinations.controllers.LightsCombinationController
import com.lightscombinations.core.View
import java.awt.BorderLayout
import java.awt.CardLayout
import javax.swing.*
import javax.swing.border.EmptyBorder

class StepperView(controller: LightsCombinationController) : View<StepperView, LightsCombinationController>(controller) {

    val stepsLayout = CardLayout(0, 0)
    val stepsPanel = JPanel(stepsLayout)
    private val currentStepLabel = JLabel()

    val imageGeneration = ImageGenerationView(this)
    private val lightsConfigurationView = LightsConfigurationView(this)

    private val steps = arrayOf(
        lightsConfigurationView,
        imageGeneration,
    )

    init {
        this.controller.view = this
        for (step in steps) {
            stepsPanel.add(step)
        }

        this.setLayout(BorderLayout())
        this.setBorder(EmptyBorder(5, 5, 5, 5))
        currentStepLabel.setHorizontalAlignment(SwingConstants.CENTER)
        currentStepLabel.setBorder(EmptyBorder(0, 10, 0, 10))

        this.layout = BorderLayout()
        this.add(currentStepLabel, BorderLayout.NORTH)
        this.add(stepsPanel, BorderLayout.CENTER)

        this.initComponents()
        this.initListeners()
    }

    private fun initComponents() {
        this.currentStepLabel.text = "Step ${this.controller.currentStep + 1} of ${this.steps.size}"
    }

    private fun initListeners() {
        controller.addPropertyChangeListener(LightsCombinationController.Property.CURRENT_STEP) {
            this.currentStepLabel.text = "Step ${this.controller.currentStep + 1} of ${this.steps.size}"
        }
    }

}