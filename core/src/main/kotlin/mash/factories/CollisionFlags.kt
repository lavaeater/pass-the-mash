package mash.factories

object CollisionFlags {
    const val CHARACTER = 1 shl 8
    const val WALL = 1 shl 9
    const val FLOOR = 1 shl 10
    const val ALL = -1
}