package com.lightscombinations.components.stepper_panels

import java.awt.FlowLayout
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class ActionPanel: JPanel() {
    init {
        this.layout = FlowLayout(FlowLayout.RIGHT)
        this.border = EmptyBorder(5, 0, 5, 0)
    }
}