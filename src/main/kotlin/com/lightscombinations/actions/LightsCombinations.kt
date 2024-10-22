package com.lightscombinations.actions

import com.eteks.sweethome3d.plugin.Plugin
import com.eteks.sweethome3d.plugin.PluginAction
import com.lightscombinations.controllers.LightsCombinationController
import com.lightscombinations.views.StepperView
import javax.imageio.ImageIO
import javax.swing.JDialog
import com.eteks.sweethome3d.tools.ResourceURLContent
import java.awt.Color
import java.awt.Dimension

class LightsCombinations(private val plugin: Plugin) : PluginAction() {
    private val resourceLogoName = "ha-logo.png"

    init {
        this.putPropertyValue(Property.NAME, "Images Lights Combinations")
        this.putPropertyValue(Property.MENU, "Tools")
        this.putPropertyValue(Property.SMALL_ICON, ResourceURLContent(this.plugin.pluginClassLoader, resourceLogoName))
        this.isEnabled = true
    }

    override fun execute() {
        try {
            val dialog = JDialog()
            val controller = LightsCombinationController(plugin)
            val view = StepperView(controller)

            dialog.addWindowListener(object : java.awt.event.WindowAdapter() {
                override fun windowClosing(e: java.awt.event.WindowEvent) {
                    controller.isDialogOpen = false
                }
                override fun windowOpened(e: java.awt.event.WindowEvent) {
                    controller.isDialogOpen = true
                }
            })

            dialog.background = Color.WHITE
            dialog.setIconImage(ImageIO.read(javaClass.getResourceAsStream("/$resourceLogoName")))
            dialog.add(view)
            dialog.preferredSize = Dimension(350, 600)
            dialog.isResizable = false
            dialog.pack()
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setLocationRelativeTo(null)
            dialog.isModal = true
            dialog.isVisible = true
        } catch (e: Exception) {
            println("Error: " + e.message)
            e.printStackTrace()
        }
    }
}