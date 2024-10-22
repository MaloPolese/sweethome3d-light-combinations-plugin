package com.lightscombinations.controllers

import com.charleskorn.kaml.Yaml
import com.eteks.sweethome3d.j3d.AbstractPhotoRenderer.Quality
import com.eteks.sweethome3d.j3d.PhotoRenderer
import com.eteks.sweethome3d.model.AspectRatio
import com.eteks.sweethome3d.model.Camera.Lens
import com.eteks.sweethome3d.model.Home
import com.eteks.sweethome3d.model.HomeLight
import com.eteks.sweethome3d.plugin.Plugin
import com.lightscombinations.core.Controller
import com.lightscombinations.models.*
import com.lightscombinations.views.StepperView
import java.awt.EventQueue
import java.awt.image.BufferedImage
import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport
import java.io.File
import java.io.FileWriter
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.math.pow


class LightSetting(val homeLight: HomeLight) {

    private val propertyChangeSupport = PropertyChangeSupport(this);

    enum class Property {
        SELECTED,
        HA_ENTITY_NAME,
        HA_ENTITY_POWER,
    }

    var homeLightPower: Float = homeLight.power
        set(value) {
            if (value != field) {
                val oldValue = field
                field = value
                propertyChangeSupport.firePropertyChange(Property.HA_ENTITY_POWER.name, oldValue, value)
            }
        }

    var haEntityName: String = homeLight.name
        set(value) {
            val oldValue = field
            field = value
            propertyChangeSupport.firePropertyChange(Property.SELECTED.name, oldValue, value)
        }

    var isSelected: Boolean = homeLight.power > 0.0
        set(value) {
            if (value != isSelected) {
                val oldValue = field
                field = value
                propertyChangeSupport.firePropertyChange(Property.SELECTED.name, oldValue, value)
            }
        }

    fun addPropertyChangeListener(property: Property, listener: PropertyChangeListener) {
        propertyChangeSupport.addPropertyChangeListener(property.name, listener)
    }

    fun toggleLight(isSwitchedOn: Boolean) {
        this.homeLight.power = if (isSwitchedOn) this.homeLightPower else 0.0f
    }
}
class LightsCombinationController(plugin: Plugin) : Controller<LightsCombinationController, StepperView>() {
    val home: Home = plugin.home

    var lights = emptyMap<String, LightSetting>()

    var selectedLightCount = 0
        set(value) {
            if (value != field) {
                val oldValue = field
                field = value
                propertyChangeSupport.firePropertyChange(Property.SELECTED_LIGHTS.name, oldValue, value)
            }
        }

    var currentStep = 0
        set(value) {
            if (value != field) {
                val oldValue = field
                field = value
                if (oldValue < value) {
                    this.view?.stepsLayout?.next(this.view?.stepsPanel)
                } else {
                    this.view?.stepsLayout?.previous(this.view?.stepsPanel)
                }
                propertyChangeSupport.firePropertyChange(Property.CURRENT_STEP.name, oldValue, value)
            }
        }

    enum class Property {
        ASPECT_RATIO, WIDTH, HEIGHT, QUALITY, OUTPUT_FOLDER_PATH, TIME, CEILING_LIGHT_COLOR, SELECTED_LIGHTS, LENS, CURRENT_STEP, IS_DIALOG_OPEN
    }

    var lens = Lens.NORMAL
        set(value) {
            if (value != field) {
                val oldValue = field
                field = value
                propertyChangeSupport.firePropertyChange(Property.LENS.name, oldValue, value)
                if (lens == Lens.SPHERICAL) {
                    aspectRatio = AspectRatio.RATIO_2_1
                } else if (lens == Lens.FISHEYE) {
                    aspectRatio = AspectRatio.SQUARE_RATIO
                }
                this.home.camera.lens = this.lens
            }
        }

    var aspectRatio = AspectRatio.VIEW_3D_RATIO
        set(value) {
            if (value != field) {
                val oldValue = field
                field = value
                propertyChangeSupport.firePropertyChange(Property.ASPECT_RATIO.name, oldValue, value)
                if (this.notFreeAspectRatio()) {
                    this.height = Math.round(this.width / this.aspectRatio.value)
                }
            }
        }

    var width = 480
        set(value) {
            if (value != field) {
                val oldValue = field
                field = value
                propertyChangeSupport.firePropertyChange(Property.WIDTH.name, oldValue, value)
                if (this.notFreeAspectRatio()) {
                    this.height = Math.round(this.width / this.aspectRatio.value)
                }
            }
        }

    var height = 480
        set(value) {
            if (value != field) {
                val oldValue = field
                field = value
                propertyChangeSupport.firePropertyChange(Property.HEIGHT.name, oldValue, value)
                if (this.notFreeAspectRatio()) {
                    this.width = Math.round(this.height * this.aspectRatio.value)
                }
            }
        }

    var quality = 0
        set(value) {
            if (value != field) {
                val oldValue = field
                field = value
                propertyChangeSupport.firePropertyChange(Property.QUALITY.name, oldValue, value)
            }
        }

    var time: Long = this.home.camera.time
        set(value) {
            if (value != field) {
                val oldValue = field
                field = value
                propertyChangeSupport.firePropertyChange(Property.TIME.name, oldValue, value)
            }
        }

    var ceilingLightColor = 0
        set(value) {
            if (value != field) {
                val oldValue = field
                field = value
                propertyChangeSupport.firePropertyChange(Property.CEILING_LIGHT_COLOR.name, oldValue, value)
            }
        }

    var outputFolder = ""
        set(value) {
            if (value != field) {
                val oldValue = field
                field = value
                propertyChangeSupport.firePropertyChange(Property.OUTPUT_FOLDER_PATH.name, oldValue, value)
            }
        }

    var isDialogOpen = false
        set(value) {
            if (value != field) {
                val oldValue = field
                field = value
                propertyChangeSupport.firePropertyChange(Property.IS_DIALOG_OPEN.name, oldValue, value)
            }
        }

    private val propertyChangeSupport = PropertyChangeSupport(this);

    init {
        this.home.furniture.filterIsInstance<HomeLight>().forEach {
            val lightSettings = LightSetting(it.clone())
            lights += it.id to lightSettings
        }
        selectedLightCount = lights.values.filter { it.isSelected }.size
    }

    fun addPropertyChangeListener(property: Property, listener: PropertyChangeListener) {
        propertyChangeSupport.addPropertyChangeListener(property.name, listener)
    }

    fun getNumberOfImagesToGenerate(): Int {
        return 2.0.pow(this.getSelectedLights().size).toInt()
    }

    private fun getSelectedLights(): Map<String, LightSetting> {
        return lights.filter { it.value.isSelected }
    }

    fun isLensFreeAspectRation(): Boolean {
        return (lens != Lens.FISHEYE && lens != Lens.SPHERICAL)
    }

    fun startImagesGeneration() {
        val processConfiguration = generateProcessConfiguration()

        generateImages(processConfiguration)

        val homeAssistantConfig = generateHomeAssistantConfig(processConfiguration)
        saveHomeAssistantConfig("$outputFolder/home_assistant_config.yaml", homeAssistantConfig)
    }

    private fun generateProcessConfiguration(): GenerationConfiguration {
        val generationConfiguration = GenerationConfiguration()

        val numberOfImagesToGenerate = getNumberOfImagesToGenerate()
        val selectedImages = getSelectedLights().values

        for (imageGenerationIndex in 0 until numberOfImagesToGenerate) {
            val currentGeneration = mutableListOf<ProcessLightConfiguration>()

            for (imageIndex in selectedImages.size - 1 downTo 0) {
                val bit = (imageGenerationIndex and (1 shl imageIndex)) shr imageIndex
                currentGeneration += ProcessLightConfiguration(
                    lightSetting = selectedImages.elementAt(imageIndex),
                    isSwitchedOn = bit == 1
                )
            }
            generationConfiguration.generations += currentGeneration
        }

        return generationConfiguration
    }

    private fun generateImages(processConfiguration: GenerationConfiguration){
        val home = this.home.clone()
        val imageGenerationView = this.view?.imageGeneration ?: return
        EventQueue.invokeLater(Runnable {
            imageGenerationView.showPhotoComponent()
            imageGenerationView.toggleComponentsState(state = false)
        })

        home.furniture.filterIsInstance<HomeLight>().forEach {
            it.power = 0.0f
        }

        this.getSelectedLights().values.forEach {
            home.addPieceOfFurniture(it.homeLight)
        }

        try {
            for ((imageGenerationIndex, generation) in processConfiguration.generations.withIndex()) {
                if (Thread.currentThread().isInterrupted) {
                    println("Task was interrupted, stopping image generation.")
                    return
                }
                if (!isDialogOpen) {
                    println("Dialog was closed, stopping image generation.")
                    return
                }
                this.generateImageAndSave(home, generation, imageGenerationIndex)

            }
        }  catch (e: InterruptedException) {
            println("Image generation was interrupted.")
            Thread.currentThread().interrupt()
        }
        finally {
            EventQueue.invokeLater(Runnable {
                imageGenerationView.showWaitComponent()
                imageGenerationView.toggleComponentsState(state = true)
            })
        }

    }

    private fun generateImageAndSave(home: Home, generation: MutableList<ProcessLightConfiguration>, imageGenerationIndex: Int) {
        val imageGenerationView = this.view?.imageGeneration ?: return

        generation.forEachIndexed { index, lightSettings ->
            lightSettings.lightSetting.toggleLight(lightSettings.isSwitchedOn)
            println("Light $index for gen$imageGenerationIndex = ${lightSettings.lightSetting.homeLight.power}")
        }

        val camera = home.camera
        val bestImageHeight = when (camera.lens) {
            Lens.FISHEYE -> this.width
            Lens.SPHERICAL -> this.width / 2
            else -> this.height
        }
        try {
            val renderer = PhotoRenderer(home, if (quality == 0) Quality.LOW else Quality.HIGH)
            val image = BufferedImage(
                this.width, bestImageHeight,
                BufferedImage.TYPE_INT_RGB
            )
            imageGenerationView.photoComponent.image = image
            renderer.render(image, camera, imageGenerationView.photoComponent)
            renderer.dispose()
            saveImage(image, imageGenerationIndex)

        } catch (ex: OutOfMemoryError) {
            println("Out of memory error")
            throw ex
        } catch (ex: IllegalStateException) {
            println("Illegal state exception")
            throw ex
        } catch (ex: IOException) {
            println("IO exception")
            throw ex
        } finally {
            println("Image-$imageGenerationIndex successfully generated")
        }

    }

    private fun saveImage(image: BufferedImage, combinationIndex: Int) {
        val outfile = File("${this.outputFolder}/ha-generated-$combinationIndex.jpg")
        ImageIO.write(image, "jpg", outfile)
    }

    private fun notFreeAspectRatio(): Boolean {
        return this.aspectRatio.value != null
    }

    private fun generateHomeAssistantConfig(generationConfiguration: GenerationConfiguration): HomeAssistantConfig {
        val config = HomeAssistantConfig(
            type = "picture-elements",
            image = "/local/planes/ha-generated-0.jpg",
        )

        for ((i, generation) in generationConfiguration.generations.withIndex()) {
            val imageName = "ha-generated-$i"
            val element = Element(
                type = "conditional",
            )
            val subElement = SubElement(
                filter = "brightness(100%)",
                image = "/local/planes/$imageName.jpg",
                style = Style(
                    left = "50%",
                    top = "50%",
                    width = "100%"
                ),
                type = "image"
            )

            for (light in generation) {
                element.conditions += Condition(
                    entity = light.lightSetting.haEntityName,
                    state = if (light.isSwitchedOn) "on" else "off"
                )
                subElement.entity + light.lightSetting.haEntityName
            }
            element.elements += subElement
            config.elements += element
        }
        return config
    }

    private fun saveHomeAssistantConfig(path: String, config: HomeAssistantConfig) {
        val yamlString = Yaml.default.encodeToString(HomeAssistantConfig.serializer(), config)
        val fileWriter = FileWriter(path)
        fileWriter.write(yamlString)
        fileWriter.close()
    }
}