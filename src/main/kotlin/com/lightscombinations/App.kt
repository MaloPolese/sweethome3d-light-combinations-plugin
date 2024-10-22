package com.lightscombinations

import com.eteks.sweethome3d.plugin.Plugin
import com.eteks.sweethome3d.plugin.PluginAction
import com.lightscombinations.actions.LightsCombinations

class App: Plugin() {
    override fun getActions(): Array<PluginAction> {
        return arrayOf(LightsCombinations(this))
    }
}