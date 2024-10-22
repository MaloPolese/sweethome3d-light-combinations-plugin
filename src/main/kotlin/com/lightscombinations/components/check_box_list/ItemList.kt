package com.lightscombinations.components.check_box_list

import java.beans.PropertyChangeSupport

abstract class ItemList(
    open var isSelected: Boolean = false,
) {
    abstract fun text(): String
}