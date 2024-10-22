package com.lightscombinations.core

import javax.swing.JPanel

abstract class ChildView<
        Self: ChildView<Self, V, C>,
        V: View<V, C>,
        C: Controller<C, V>,
        >(val parentView: V) : JPanel()