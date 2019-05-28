package me.hiten.completion

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionSorter
import com.intellij.codeInsight.completion.impl.PreferStartMatching
import com.intellij.codeInsight.lookup.*
import com.intellij.util.containers.isNullOrEmpty
import me.hiten.completion.androidsearch.AndroidDependencySearcher
import me.hiten.completion.mavensearch.MavenDependencySearcher
import java.awt.Color

class DependencySearchManager(private val dependencyText: DependencyText) {


    fun search(result: CompletionResultSet) {
        if (dependencyText.isShort) {
            return
        }
        val size = dependencyText.splitText.size
        when (size) {
            3 -> searchVersion(result)
            2 -> searchArtifact(result)
            else -> searchDefault(result)
        }
    }

    private fun searchDefault(result: CompletionResultSet) {
        var searchDefault = AndroidDependencySearcher().searchDefault(dependencyText.text)
        if (needNextSearch(searchDefault)) {
            searchDefault = mergeSearch(searchDefault, MavenDependencySearcher().searchDefault(dependencyText.text))
        }
        val list = searchDefault?.map { createLookupElement(it, dependencyText.text + it.groupId) }?.toList()
        list?.let {
            result.restartCompletionOnPrefixChange(dependencyText.text)
            result.withRelevanceSorter(
                    CompletionSorter.emptySorter().weigh(PreferStartMatching())
            ).addAllElements(list)
        }
    }


    private fun searchArtifact(result: CompletionResultSet) {
        var searchArtifact = AndroidDependencySearcher().searchArtifact(dependencyText.groupId)
        if (needNextSearch(searchArtifact)) {
            searchArtifact = mergeSearch(searchArtifact, MavenDependencySearcher().searchArtifact(dependencyText.groupId))
        }
        val list = searchArtifact?.map { createLookupElement(it, dependencyText.text + it.artifact) }?.toList()
        list?.let {
            result.restartCompletionOnPrefixChange(dependencyText.text)
            result.withRelevanceSorter(
                    CompletionSorter.emptySorter().weigh(PreferStartMatching())
            ).addAllElements(list)
        }
    }

    private fun searchVersion(result: CompletionResultSet) {
        var searchVersion = AndroidDependencySearcher().searchVersion(dependencyText.groupId, dependencyText.artifactId)
        if (needNextSearch(searchVersion)) {
            searchVersion = mergeSearch(searchVersion, MavenDependencySearcher().searchVersion(dependencyText.groupId, dependencyText.artifactId))
        }
        val list = searchVersion?.map { createLookupElement(it, dependencyText.text + it.version) }?.toList()
        list?.let {
            result.restartCompletionOnPrefixChange(dependencyText.text)
            val withRelevanceSorter = result.withRelevanceSorter(
                    CompletionSorter.emptySorter().weigh(object : LookupElementWeigher("versionWeigher") {
                        override fun weigh(element: LookupElement): Comparable<VersionComparable> {
                            val obj = (element as LookupElementBuilder).`object` as DependencySearcher.Result
                            return VersionComparable(searchVersion!!.indexOf(obj))
                        }
                    })
            )
            withRelevanceSorter.addAllElements(list)
        }
    }


    private fun mergeSearch(before: List<DependencySearcher.Result>?, after: List<DependencySearcher.Result>?): List<DependencySearcher.Result>? {
        if (before.isNullOrEmpty() && after.isNullOrEmpty()) {
            return null
        }
        return ArrayList<DependencySearcher.Result>().apply {
            before?.let {
                this.addAll(before)
            }
            after?.let {
                this.addAll(after)
            }
        }
    }

    private fun needNextSearch(before: List<DependencySearcher.Result>?): Boolean {
        if (before.isNullOrEmpty()) {
            return true
        }
        for (result in before!!) {
            return result.artifact.isNullOrEmpty() && result.version.isNullOrEmpty()
        }
        return false
    }


    private fun createLookupElement(result: DependencySearcher.Result, cheatString: String): LookupElement {
        return LookupElementBuilder.create(result, cheatString).withRenderer(CustomLookupElementRenderer(dependencyText)).withInsertHandler { context, item ->
            var quoteArg = dependencyText.quoteArg
            val text: String
            var insertArg: String? = null
            if (quoteArg == null) {
                text = result.getFullTextAndColon()
            } else {
                if (result.version.isNullOrEmpty() || result.artifact.isNullOrEmpty()) {
                    text = result.getFullTextAndColon() + "$Q_SYMBOL$quoteArg"
                } else {
                    if (quoteArg.isEmpty()) {
                        quoteArg = "ver_${result.artifact?.replace("-", "_")}"
                    }
                    text = "${result.groupId}:${result.artifact}:$$quoteArg"
                    insertArg = "ext.$quoteArg = '${result.version}' //please move this code to a unified place.\n"
                }
            }
            val editor = context.editor
            val document = context.document
            val offset = editor.caretModel.offset
            val lineNumber = document.getLineNumber(offset)
            val lineStartOffset = document.getLineStartOffset(lineNumber)
            val lineEndOffset = document.getLineEndOffset(lineNumber)
            val allText = document.text
            var lineText = allText.substring(lineStartOffset, lineEndOffset)

            var i = lineText.indexOf("\"", offset - lineStartOffset)
            if (i == -1) {
                i = lineText.indexOf("'", offset - lineStartOffset)
            }
            i += lineStartOffset
            lineText = if (i > offset) {
                allText.substring(lineStartOffset, offset) + allText.substring(i, lineEndOffset)
            } else {
                allText.substring(lineStartOffset, lineEndOffset)
            }
            lineText = lineText.replace(cheatString, text)
            insertArg?.let {
                if (lineText.contains("'")) {
                    lineText = lineText.replace("'", "\"")
                }
            }
            document.replaceString(lineStartOffset, lineEndOffset, lineText)
            insertArg?.let {
                val stringBuilder = StringBuilder()
                for (char in lineText.toList()) {
                    if (char == ' ') {
                        stringBuilder.append(char)
                    } else {
                        break
                    }
                }
                stringBuilder.append(insertArg)
                document.insertString(lineStartOffset, stringBuilder.toString())
            }
        }
    }


    class CustomLookupElementRenderer(private val dependencyText: DependencyText) : LookupElementRenderer<LookupElement>() {

        override fun renderElement(element: LookupElement?, presentation: LookupElementPresentation?) {
            element?.let {
                val result = (element as LookupElementBuilder).`object` as DependencySearcher.Result
                presentation?.setTypeText(null, Icons.getIcon(result.source))
                var quoteArg = dependencyText.quoteArg
                if (quoteArg == null) {
                    presentation?.itemText = result.getFullTextAndColon()
                } else {
                    if (result.version.isNullOrEmpty() || result.artifact.isNullOrEmpty()) {
                        presentation?.itemText = result.getFullTextAndColon() + "$Q_SYMBOL$quoteArg"
                    } else {
                        if (quoteArg.isEmpty()) {
                            quoteArg = "ver_${result.artifact?.replace("-", "_")}"
                        }
                        presentation?.itemText = "${result.groupId}:${result.artifact}:$$quoteArg=${result.version}"
                    }
                    presentation?.itemTextForeground = Color.BLUE
                }
            }

        }
    }

    companion object {
        const val Q_SYMBOL = "#"

        class VersionComparable(private val index: Int) : Comparable<VersionComparable> {
            override fun compareTo(other: VersionComparable): Int = this.index - other.index
        }
    }

}
