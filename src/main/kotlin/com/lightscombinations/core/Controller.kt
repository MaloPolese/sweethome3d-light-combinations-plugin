package com.lightscombinations.core

abstract class Controller<
        Self: Controller<Self, V>,
        V: View<V, Self>> {

    var view: V? = null
}
