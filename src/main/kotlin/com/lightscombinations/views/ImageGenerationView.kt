package com.lightscombinations.views

import com.eteks.sweethome3d.swing.ScaledImageComponent
import com.eteks.sweethome3d.swing.SwingTools
import com.lightscombinations.components.photo_settings_panel.AdvancedImageSizeAndQualityPanel
import com.lightscombinations.components.photo_settings_panel.ImageSizeAndQualityPanel
import com.lightscombinations.components.stepper_panels.ActionPanel
import com.lightscombinations.controllers.LightsCombinationController
import com.lightscombinations.core.ChildView
import java.awt.*
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.util.concurrent.Executors
import javax.swing.*
import kotlin.math.log


class ImageGenerationView(parentView: StepperView) : ChildView<
        ImageGenerationView,
        StepperView,
        LightsCombinationController>
    (parentView) {


    private val generateButton = JButton("Generate ${parentView.controller.getNumberOfImagesToGenerate()} images")
    private val backButton = JButton("Back")

    val photoComponent = ScaledImageComponent()
    private var photoCardLayout = CardLayout()
    private val numberOfImages = JLabel("0")
    private var photoPanel = JPanel(this.photoCardLayout)
    private val animatedWaitLabel = JLabel("Wait for images generation")

    private val sizeAndQualityPanel = ImageSizeAndQualityPanel(parentView)
    private val advancedImageSzeAndQualityPanel = AdvancedImageSizeAndQualityPanel(parentView)
    private val componentsSeparator = JSeparator()

    private val outputPathInput = JTextField()
    private val outputPathLabel = JLabel("Output Directory:")
    private val fileChooserButton = JButton("Choose")
    private val fileChooser = JFileChooser()

    private  val photoCreationExecutor = Executors.newSingleThreadExecutor()

    companion object {
        const val WAIT_CARD: String = "wait"
        const val PHOTO_CARD: String = "photo"
    }

    init {
        this.layout = GridBagLayout()

        this.initComponents()
        this.initListeners()
        this.layoutComponents()
    }

    fun toggleComponentsState(state: Boolean) {
        this.fileChooserButton.isEnabled = state
        this.outputPathInput.isEnabled = state
        this.outputPathLabel.isEnabled = state

        this.generateButton.isEnabled = state
        this.backButton.isEnabled = state
        this.sizeAndQualityPanel.togglePhotoSizeAndQualityComponents(state)
        this.advancedImageSzeAndQualityPanel.toggleAdvancedComponents(state)
    }

    fun showPhotoComponent() {
        photoCardLayout.show(photoPanel, PHOTO_CARD)
    }
    fun showWaitComponent() {
        photoCardLayout.show(photoPanel, WAIT_CARD)
    }

    private fun initComponents() {
        this.numberOfImages.text = "Number of images to generate ${this.parentView.controller.getNumberOfImagesToGenerate()}"
        this.layout = GridBagLayout()


        fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        generateButton.isEnabled = parentView.controller.selectedLightCount > 0 && parentView.controller.outputFolder.isNotEmpty()

        outputPathInput.text = parentView.controller.outputFolder

        animatedWaitLabel.horizontalAlignment = SwingConstants.CENTER

        this.photoCardLayout = CardLayout()
        this.photoPanel = JPanel(this.photoCardLayout)
        photoPanel.add(this.photoComponent, PHOTO_CARD)
        photoPanel.add(this.animatedWaitLabel, WAIT_CARD)
        this.showWaitComponent()
    }

    private fun initListeners() {
        outputPathInput.addActionListener {
            parentView.controller.outputFolder = outputPathInput.text
        }
        outputPathInput.addFocusListener(object : FocusAdapter() {
            override fun focusLost(e: FocusEvent) {
                parentView.controller.outputFolder = outputPathInput.text
            }
        })

        parentView.controller.addPropertyChangeListener(LightsCombinationController.Property.OUTPUT_FOLDER_PATH) {
            outputPathInput.text = parentView.controller.outputFolder
            generateButton.isEnabled = parentView.controller.selectedLightCount > 0 && parentView.controller.outputFolder.isNotEmpty()
        }

        fileChooserButton.addActionListener {
            val returnVal = fileChooser.showOpenDialog(this)
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                outputPathInput.text = fileChooser.selectedFile.absolutePath
                parentView.controller.outputFolder = fileChooser.selectedFile.absolutePath
            }
        }

        parentView.controller.addPropertyChangeListener(LightsCombinationController.Property.SELECTED_LIGHTS) {
            this.generateButton.isEnabled = parentView.controller.selectedLightCount > 0 && parentView.controller.outputFolder.isNotEmpty()
            this.generateButton.text = "Generate ${parentView.controller.getNumberOfImagesToGenerate()} images"
        }

        backButton.addActionListener {
            parentView.controller.currentStep--
        }

        parentView.controller.addPropertyChangeListener(LightsCombinationController.Property.IS_DIALOG_OPEN) {
            if (!parentView.controller.isDialogOpen && !photoCreationExecutor.isTerminated) {
                println("DialogClose, Shutting down executor")
                photoCreationExecutor.shutdownNow()
            }
        }

        generateButton.addActionListener {
            photoCreationExecutor.execute {

                this.parentView.controller.startImagesGeneration()
            }
        }
    }

    private fun layoutComponents() {
        val standardGap = Math.round(5 * SwingTools.getResolutionScale())

        val photoPanelGbc = GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            gridwidth = 3
            fill = GridBagConstraints.BOTH
            weightx = 1.0
            weighty = 1.0
            insets = Insets(0, 0, standardGap, 0)
        }
        this.add(this.photoPanel, photoPanelGbc)

        val sizeAndQualityPanelGbc = GridBagConstraints().apply {
            gridx = 0
            gridy = 1
            gridwidth = 3
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
        }
        this.add(this.sizeAndQualityPanel, sizeAndQualityPanelGbc)

        val advancedPhotoSizeAndQualityPanelGbc = GridBagConstraints().apply {
            gridx = 0
            gridy = 2
            gridwidth = 3
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
        }
        this.add(this.advancedImageSzeAndQualityPanel, advancedPhotoSizeAndQualityPanelGbc)

        val componentsSeparatorGbc = GridBagConstraints().apply {
            gridx = 0
            gridy = 3
            gridwidth = 3
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(8, 0, 8, 0)
            weightx = 1.0
        }
        this.add(this.componentsSeparator, componentsSeparatorGbc)

        val outputPathLabelGbc = GridBagConstraints().apply {
            gridx = 0
            gridy = 4
            gridwidth = 3
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(0, 0,  0, standardGap)
        }
        this.add(this.outputPathLabel, outputPathLabelGbc)

        val outputPathInputGbc = GridBagConstraints().apply {
            gridx = 0
            gridy = 5
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(0, 0,  0, standardGap)
            weightx = 1.0
        }
        this.add(this.outputPathInput, outputPathInputGbc)

        val fileChooserButtonGbc = GridBagConstraints().apply {
            gridx = 1
            gridy = 5
            fill = GridBagConstraints.HORIZONTAL
        }
        this.add(this.fileChooserButton, fileChooserButtonGbc)

        val actionPanel = ActionPanel()
        actionPanel.add(backButton)
        actionPanel.add(generateButton)
        val actionPanelGbc = GridBagConstraints().apply {
            gridx = 0
            gridy = 6
            gridwidth = 3
            weightx = 1.0
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.CENTER
        }
        this.add(actionPanel, actionPanelGbc)
    }
}
