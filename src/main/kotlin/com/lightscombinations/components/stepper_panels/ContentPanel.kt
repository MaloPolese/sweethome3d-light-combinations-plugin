package com.lightscombinations.components.stepper_panels

import java.awt.BorderLayout
import java.awt.Component
import java.awt.LayoutManager
import java.awt.LayoutManager2
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.border.BevelBorder
import javax.swing.border.TitledBorder

class ContentPanel(title: String, layout: LayoutManager = BorderLayout()): JPanel(layout) {
    private val scrollPane = JScrollPane()

    init {
        // this.setBorder(TitledBorder(BevelBorder(BevelBorder.LOWERED), title))
        
        scrollPane.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
        this.add(scrollPane)
    }

    fun setViewportView(view: Component) {
        scrollPane.setViewportView(view)
    }
}