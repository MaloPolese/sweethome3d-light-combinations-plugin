package com.lightscombinations.components.check_box_list


import javax.swing.JCheckBox
import javax.swing.JList
import javax.swing.ListCellRenderer
import java.awt.Component;

class CheckboxListRenderer: JCheckBox(), ListCellRenderer<ItemList> {
    override fun getListCellRendererComponent(
        list: JList<out ItemList>,
        item: ItemList,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        isEnabled = list.isEnabled
        setSelected(item.isSelected)
        font = list.font
        background = list.background
        foreground = list.foreground
        text = item.text()
        return this
    }
}