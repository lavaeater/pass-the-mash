package mash.ui

import com.badlogic.gdx.graphics.g3d.model.Node
import net.mgsx.gltf.scene3d.lights.SpotLightEx

sealed class UiNode(var selected: Boolean = false) {
    open var name: String = ""

    class ThreeDNode(val node: Node, val parent: Node?) : UiNode() {
        init {
            node.id = node.id.replace("mixamorig:", "")
        }

        override var name: String
            get() = node.id
            set(value) {
                node.id = value
            }
    }

    class SpotLightNode(val spotLightEx: SpotLightEx) : UiNode()
    object EmptyNode : UiNode() {
        override var name: String
            get() = "Empty"
            set(value) {}
    }
}