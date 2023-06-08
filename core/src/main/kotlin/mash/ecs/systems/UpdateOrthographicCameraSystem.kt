package mash.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import ktx.ashley.allOf
import ktx.math.times
import ktx.math.vec3
import mash.injection.GameSettings
import mash.ui.Share3dDebugData
import threedee.ecs.components.IsometricCameraFollowComponent
import threedee.ecs.components.SceneComponent
import twodee.injection.InjectionContext.Companion.inject

class UpdateOrthographicCameraSystem(
    private val orthographicCamera: OrthographicCamera
) :
    IteratingSystem(
        allOf(
            SceneComponent::class,
            IsometricCameraFollowComponent::class
        ).get()
    ) {

    val target = vec3()
    val cameraPos = vec3()
    val settings by lazy { inject<GameSettings>() }
    val worldToViewportRatio by lazy { settings.frameBufferWidth / Gdx.graphics.width }
    override fun processEntity(entity: Entity, deltaTime: Float) {

        SceneComponent.get(entity).scene.modelInstance.transform.getTranslation(target)

        cameraPos.set(target).add(Share3dDebugData.cameraOffset).scl(worldToViewportRatio)

        orthographicCamera.position.set(cameraPos)
        orthographicCamera.lookAt(target.scl(worldToViewportRatio))
        orthographicCamera.update()
    }
}
