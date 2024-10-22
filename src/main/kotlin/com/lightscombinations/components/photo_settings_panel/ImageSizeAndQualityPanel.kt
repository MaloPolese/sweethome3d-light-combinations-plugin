package com.lightscombinations.components.photo_settings_panel

import com.eteks.sweethome3d.j3d.Component3DManager
import com.eteks.sweethome3d.model.AspectRatio
import com.eteks.sweethome3d.swing.AutoCommitSpinner
import com.eteks.sweethome3d.swing.PhotoSizeAndQualityPanel
import com.eteks.sweethome3d.swing.SwingTools
import com.eteks.sweethome3d.tools.OperatingSystem
import com.eteks.sweethome3d.tools.ResourceURLContent
import com.lightscombinations.controllers.LightsCombinationController
import com.lightscombinations.core.ChildView
import com.lightscombinations.views.StepperView
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.IOException
import java.net.URL
import javax.swing.*
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min


class ImageSizeAndQualityPanel(parentView: StepperView) : ChildView<
        ImageSizeAndQualityPanel,
        StepperView,
        LightsCombinationController>
    (parentView) {

    private val widthLabel: JLabel = JLabel("Width: (pixels)")
    private val widthSpinnerModel = SpinnerNumberModel(480, 10, 10000, 10)
    private val widthSpinner = AutoCommitSpinner(widthSpinnerModel)

    private val heightLabel: JLabel = JLabel("Height: (pixels)")
    private val heightSpinnerModel = SpinnerNumberModel(480, 10, 10000, 10)
    private val heightSpinner = AutoCommitSpinner(heightSpinnerModel)

    private var applyProportionsCheckBox: JCheckBox = JCheckBox("Apply proportions:")
    private val aspectRatioComboBox: JComboBox<AspectRatio> = JComboBox<AspectRatio>(
        arrayOf(
            AspectRatio.VIEW_3D_RATIO,
            AspectRatio.SQUARE_RATIO,
            AspectRatio.RATIO_4_3,
            AspectRatio.RATIO_3_2,
            AspectRatio.RATIO_16_9,
            AspectRatio.RATIO_2_1,
            AspectRatio.RATIO_24_10
        )
    )

    private val qualityLabel: JLabel = JLabel("Quality:")
    var qualitySlider: JSlider = JSlider()

    private val fastQualityLabel: JLabel = JLabel("Fast")
    private val bestQualityLabel: JLabel = JLabel("Best")

    init {
        this.layout = GridBagLayout()

        this.createComponents()
        this.initListeners()
        this.layoutComponents()
    }

    fun togglePhotoSizeAndQualityComponents(state: Boolean) {
        this.widthLabel.isEnabled = state
        this.widthSpinner.isEnabled = state
        this.heightLabel.isEnabled = state
        this.heightSpinner.isEnabled = state
        this.applyProportionsCheckBox.isEnabled = state
        this.aspectRatioComboBox.isEnabled = state
        this.qualityLabel.isEnabled = state
        this.qualitySlider.isEnabled = state
    }

    private fun createComponents() {
        this.aspectRatioComboBox.setSelectedItem(parentView.controller.aspectRatio);
        aspectRatioComboBox.setRenderer(object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?, value: Any,
                index: Int, isSelected: Boolean, cellHasFocus: Boolean
            ): Component {
                val aspectRatio = value as AspectRatio
                var displayedValue: String? = ""
                if (aspectRatio != AspectRatio.FREE_RATIO) {
                    when (aspectRatio) {
                        AspectRatio.VIEW_3D_RATIO -> displayedValue = "3D view"
                        AspectRatio.SQUARE_RATIO -> displayedValue = "Square"
                        AspectRatio.RATIO_4_3 -> displayedValue = "4:3"
                        AspectRatio.RATIO_3_2 -> displayedValue = "3:2"
                        AspectRatio.RATIO_16_9 -> displayedValue = "16:9"
                        AspectRatio.RATIO_2_1 -> displayedValue = "2:1"
                        AspectRatio.RATIO_24_10 -> displayedValue = "2.40:1"
                        else -> {}
                    }
                }
                return super.getListCellRendererComponent(
                    list, displayedValue, index, isSelected,
                    cellHasFocus
                )
            }
        })


        val imageSize = try {
            SwingTools.getImageSizeInPixels(ResourceURLContent(PhotoSizeAndQualityPanel::class.java,"resources/quality0.jpg"))
        } catch (ex: IOException) {
            // Shouldn't happen since resource exists
            null
        }
        val resolutionScale = SwingTools.getResolutionScale()
        val imageWidth = (imageSize!!.width * resolutionScale).toInt()
        val imageHeight = (imageSize.height * resolutionScale).toInt()
        this.qualitySlider = object : JSlider(1, 2) {
            override fun getToolTipText(ev: MouseEvent): String? {
                val valueUnderMouse: Float = getSliderValueAt(this, ev.x)
                val valueToTick = valueUnderMouse - floor(valueUnderMouse.toDouble()).toFloat()
                if (valueToTick < 0.25f || valueToTick > 0.75f) {
                    val imageUrl: URL = ResourceURLContent(PhotoSizeAndQualityPanel::class.java,"resources/quality" + Math.round(valueUnderMouse - qualitySlider.minimum) + ".jpg").url
                    val imageHtmlCell = "<td><img border='1' width='$imageWidth' height='$imageHeight' src='$imageUrl'></td>"
                    val description = when (Math.round(valueUnderMouse - qualitySlider.minimum)) {
                        0 -> "Fast global illumination with shadows <br>computed from lights placed in the plan <br>and optional lights in the middle of ceilings <br>(might take a long time to compute)"
                        1 -> "Global illumination with shadows <br>computed from lights placed in the plan <br>and optional lights in the middle of ceilings <br>(might take hours to compute)"
                        else -> "n/a"
                    }
                    val leftToRightOrientation = qualitySlider.componentOrientation.isLeftToRight
                    val descriptionHtmlCell = "<td align='" + (if (leftToRightOrientation) "left" else "right") + "'>" + description + "</td>"

                    return """
                        <html><table><tr valign='middle'>
                        ${if (leftToRightOrientation) imageHtmlCell + descriptionHtmlCell else descriptionHtmlCell + imageHtmlCell}
                        </tr></table>
                        """.trimIndent()
                } else {
                    return null
                }
            }
        }
        qualitySlider.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(ev: MouseEvent) {
                EventQueue.invokeLater(Runnable {
                    val valueUnderMouse: Float = getSliderValueAt(qualitySlider, ev.x)
                    if (qualitySlider.value == Math.round(valueUnderMouse)) {
                        val toolTipManager = ToolTipManager.sharedInstance()
                        val initialDelay = toolTipManager.initialDelay
                        toolTipManager.initialDelay = min(initialDelay.toDouble(), 150.0).toInt()
                        toolTipManager.mouseMoved(ev)
                        toolTipManager.initialDelay = initialDelay
                    }
                })
            }
        })
        qualitySlider.setPaintTicks(true)
        qualitySlider.setMajorTickSpacing(1)
        qualitySlider.setSnapToTicks(true)
        val offScreenImageSupported = Component3DManager.getInstance().isOffScreenImageSupported
        qualitySlider.addChangeListener {
            if (!offScreenImageSupported) {
                qualitySlider.setValue(max((qualitySlider.minimum + 2).toDouble(), qualitySlider.value.toDouble()).toInt())
            }
        }

        val notFreeAspectRatio = parentView.controller.aspectRatio !== AspectRatio.FREE_RATIO
        applyProportionsCheckBox.isSelected = notFreeAspectRatio
    }

    private fun initListeners() {
        widthSpinnerModel.value = parentView.controller.width
        widthSpinnerModel.addChangeListener { parentView.controller.width = (widthSpinnerModel.value as Number).toInt() }
        parentView.controller.addPropertyChangeListener(LightsCombinationController.Property.WIDTH) {
            widthSpinnerModel.value = parentView.controller.width
        }

        heightSpinnerModel.value = parentView.controller.height
        heightSpinnerModel.addChangeListener { parentView.controller.height = (heightSpinnerModel.value as Number).toInt() }
        parentView.controller.addPropertyChangeListener(LightsCombinationController.Property.HEIGHT) {
            heightSpinnerModel.value = parentView.controller.height
        }

        this.aspectRatioComboBox.addItemListener { parentView.controller.aspectRatio = this.aspectRatioComboBox.selectedItem as AspectRatio }
        parentView.controller.addPropertyChangeListener(LightsCombinationController.Property.ASPECT_RATIO) {
            val notFreeAspectRatio = parentView.controller.aspectRatio != AspectRatio.FREE_RATIO
            this.applyProportionsCheckBox.setSelected(notFreeAspectRatio)
            this.aspectRatioComboBox.setEnabled(notFreeAspectRatio && this.parentView.controller.isLensFreeAspectRation())
            this.aspectRatioComboBox.setSelectedItem(parentView.controller.aspectRatio)
        }

        qualitySlider.addChangeListener {
            parentView.controller.quality = qualitySlider.value - qualitySlider.minimum
        }
        parentView.controller.addPropertyChangeListener(LightsCombinationController.Property.QUALITY) {
            qualitySlider.value = qualitySlider.minimum + parentView.controller.quality
            updateRatioComponents()
        }
        qualitySlider.value = qualitySlider.minimum + parentView.controller.quality

        applyProportionsCheckBox.addItemListener {
            parentView.controller.aspectRatio = if (applyProportionsCheckBox.isSelected) {
                aspectRatioComboBox.selectedItem as AspectRatio
            } else {
                AspectRatio.FREE_RATIO
            }
        }

        parentView.controller.addPropertyChangeListener(LightsCombinationController.Property.LENS) {
            updateRatioComponents()
        }
    }

    private fun updateRatioComponents() {
        // val fixedProportions = this.parentView.controller.canShowAdvancedSettings() && !this.parentView.controller.isLensFreeAspectRation()
        val fixedProportions = !this.parentView.controller.isLensFreeAspectRation()

        this.applyProportionsCheckBox.setEnabled(!fixedProportions)
        this.aspectRatioComboBox.setEnabled(!fixedProportions && this.applyProportionsCheckBox.isSelected)
    }

    private fun layoutComponents() {
        val labelAlignment = if (OperatingSystem.isMacOSX()
        ) JLabel.TRAILING
        else JLabel.LEADING
        val standardGap = Math.round(5 * SwingTools.getResolutionScale())

        val widthLabelGbc  = GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
            insets = Insets(0, 0, 0, standardGap)
        }
        this.add(this.widthLabel, widthLabelGbc)

        widthLabel.horizontalAlignment = labelAlignment
        val widthSpinnerGbc = GridBagConstraints().apply {
            gridx = 1
            gridy = 0
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.LINE_START
            weightx = 1.0
            insets = Insets(0, 0, 0, 10)
        }
        this.add(this.widthSpinner, widthSpinnerGbc)

        val heightLabelGbc = GridBagConstraints().apply {
            gridx = 2
            gridy = 0
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
            insets = Insets(0, 0, 0, standardGap)
        }
        this.add(this.heightLabel, heightLabelGbc)
        heightLabel.horizontalAlignment = labelAlignment

        val heightSpinnerGbc = GridBagConstraints().apply {
            gridx = 3
            gridy = 0
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.LINE_START
            weightx = 1.0
        }
        this.add(this.heightSpinner, heightSpinnerGbc)

        val proportionsPanel = JPanel()
        proportionsPanel.add(this.applyProportionsCheckBox)
        proportionsPanel.add(this.aspectRatioComboBox)
        val proportionsPanelGbc = GridBagConstraints().apply {
            gridx = 0
            gridy = 1
            gridwidth = 4
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
        }
        this.add(proportionsPanel, proportionsPanelGbc)

        val qualityLabelGbc = GridBagConstraints().apply {
            gridx = 0
            gridy = 3
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
            insets = Insets(0, 0, 0, standardGap)
        }
        this.add(this.qualityLabel, qualityLabelGbc)
        qualityLabel.horizontalAlignment = labelAlignment

        val qualitySlierGbc = GridBagConstraints().apply {
            gridx = 1
            gridy = 3
            gridwidth = 3
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.LINE_START
            weightx = 1.0
        }
        this.add(this.qualitySlider, qualitySlierGbc)

        // Fourth row
        val qualityLabelsPanel = JPanel(BorderLayout(20, 0))
        qualityLabelsPanel.add(this.fastQualityLabel, BorderLayout.WEST)
        qualityLabelsPanel.add(this.bestQualityLabel, BorderLayout.EAST)
        val qualityLabelsPanelGbc = GridBagConstraints().apply {
            gridx = 1
            gridy = 4
            gridwidth = 3
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
        }
        this.add(qualityLabelsPanel, qualityLabelsPanelGbc)
    }

    private fun getSliderValueAt(qualitySlider: JSlider, x: Int): Float {
        val fastLabelOffset = 0
        val bestLabelOffset = 0
        val sliderWidth = qualitySlider.width - fastLabelOffset - bestLabelOffset
        return (qualitySlider.minimum + (
                    if (qualitySlider.componentOrientation.isLeftToRight) x - fastLabelOffset
                    else sliderWidth - x + bestLabelOffset).toFloat() / sliderWidth * (qualitySlider.maximum - qualitySlider.minimum)
               )
    }
}
