package com.lightscombinations.models

import com.lightscombinations.controllers.LightSetting

class GenerationConfiguration(
    var generations:  MutableList<MutableList<ProcessLightConfiguration>> = mutableListOf(),
)

class ProcessLightConfiguration(
    val lightSetting: LightSetting,
    val isSwitchedOn: Boolean = false,
)