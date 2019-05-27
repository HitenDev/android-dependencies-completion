package me.hiten.completion


class DependencyText(val text: String) {

    val isShort: Boolean = text.length < 2

    val quoteArg: String? = text.split("$").getOrNull(1)

    val splitText = text.split("$").getOrElse(0) { text }.split(":")
    val groupId = splitText.getOrElse(0) { "" }
    val artifactId = splitText.getOrElse(1) { "" }

}