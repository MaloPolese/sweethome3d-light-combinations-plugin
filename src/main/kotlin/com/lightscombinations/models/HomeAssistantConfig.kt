package com.lightscombinations.models

import kotlinx.serialization.Serializable

@Serializable
data class HomeAssistantConfig(
    val type: String,
    val image: String,
    var elements: List<Element> = emptyList(),
)

@Serializable
data class Element(
    var conditions: List<Condition> = emptyList(),
    var elements: List<SubElement> = emptyList(),
    val type: String,
)

@Serializable
data class Condition(
    val entity: String,
    val state: String,
)

@Serializable
data class SubElement(
    val entity: List<String> = emptyList(),
    val filter: String,
    val image: String,
    val style: Style,
    val type: String,
)

@Serializable
data class Style(
    val left: String,
    val top: String,
    val width: String,
)
