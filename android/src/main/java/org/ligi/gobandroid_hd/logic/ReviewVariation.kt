package org.ligi.gobandroid_hd.logic

import org.ligi.gobandroid_hd.logic.markers.GoMarker
import org.ligi.gobandroid_hd.logic.markers.TextMarker
import org.ligi.gobandroid_hd.logic.markers.functions.findFirstFreeNumber


class ReviewVariation(val mainLine: GoMove) {
    val markers = mutableListOf<GoMarker>()
    val orgMainlineVariation = mainLine.nextMoveVariations.toMutableList()

    fun addMarker(cell: Cell) : GoMarker{
        return TextMarker(cell, markers.findFirstFreeNumber().toString())
            .also{ markers.add(it) }
    }
}