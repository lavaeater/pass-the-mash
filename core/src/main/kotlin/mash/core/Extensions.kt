package mash.core


fun <E> MutableSet<E>.addIndexed(element: E): Int {
    this.add(element)
    return this.indexOf(element)
}
