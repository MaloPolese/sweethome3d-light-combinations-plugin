package com.lightscombinations.components.check_box_list

import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.ListSelectionModel


class JCheckBoxList<T>(customListModel: DefaultListModel<T>)  :  JList<T>(customListModel) where T : ItemList {
    init {
        this.setCellRenderer(CheckboxListRenderer())
        this.selectionMode = ListSelectionModel.SINGLE_SELECTION
    }
}