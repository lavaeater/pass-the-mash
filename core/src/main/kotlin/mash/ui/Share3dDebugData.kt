package mash.ui

import com.badlogic.gdx.math.Quaternion
import ktx.math.vec3

object Share3dDebugData {
    var selectedNode: UiNode = UiNode.EmptyNode
    var nodeTranslation = vec3()
        get() {
            return when (selectedNode) {
                UiNode.EmptyNode -> field
                is UiNode.SpotLightNode -> field
                is UiNode.ThreeDNode -> {
                    (selectedNode as UiNode.ThreeDNode).node.localTransform.getTranslation(field)
                    field
                }
            }
        }
        set(value) {
            field.set(value)
        }

    var nodeRotation = Quaternion()
        get() {
            return when (selectedNode) {
                UiNode.EmptyNode -> field.set(Quaternion())
                is UiNode.SpotLightNode -> field.set(Quaternion())
                is UiNode.ThreeDNode -> {
                    (selectedNode as UiNode.ThreeDNode).node.localTransform.getRotation(field)
                    field
                }
            }
        }
        set(value) {
            field.set(value)
        }


    var drawSkeleton = false
    var drawDebugNode = true
    var drawBulletDebug = false
}