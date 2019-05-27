package me.hiten.completion

import me.hiten.completion.DependencySearchManager.Companion.Q_SYMBOL


class DependencyText(val text: String) {

    val isShort: Boolean = text.length < 2

    val quoteArg: String? = text.split(Q_SYMBOL).getOrNull(1)

    val splitText = text.split(Q_SYMBOL).getOrElse(0) { text }.split(":")
    val groupId = splitText.getOrElse(0) { "" }
    val artifactId = splitText.getOrElse(1) { "" }

}