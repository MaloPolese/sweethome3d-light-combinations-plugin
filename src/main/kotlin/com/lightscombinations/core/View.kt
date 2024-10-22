package com.lightscombinations.core

import javax.swing.JPanel


abstract class View<Self: View<Self, C>, C: Controller<C, Self>>(val controller: C) : JPanel()
