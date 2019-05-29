package me.hiten.completion

import me.hiten.completion.Config.Q_SYMBOL
import me.hiten.completion.Config.Q_SYMBOL_ALL


class DependencyText(val text: String) {

    val isShort: Boolean = text.length < 2

    val convertAll: Boolean = text.contains(Q_SYMBOL_ALL)

    val quoteArg: String? = text.split(if (convertAll) Q_SYMBOL_ALL else Q_SYMBOL).getOrNull(1)

    val splitText = text.split(if (convertAll) Q_SYMBOL_ALL else Q_SYMBOL).getOrElse(0) { text }.split(":")
    val groupId = splitText.getOrElse(0) { "" }
    val artifactId = splitText.getOrElse(1) { "" }
    val versionId = splitText.getOrElse(2) { "" }
}