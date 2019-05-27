package me.hiten.completion

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.codeInsight.lookup.LookupElementRenderer
import com.intellij.util.containers.isNullOrEmpty
import me.hiten.completion.androidsearch.AndroidDependencySearcher
import me.hiten.completion.mavensearch.MavenDependencySearcher

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
            searchDefault = MavenDependencySearcher().searchDefault(dependencyText.text)
        }
        val list = searchDefault?.map { createLookupElement(it, dependencyText.text + it.groupId) }?.toList()
        list?.let {
            result.addAllElements(list)
        }
    }


    private fun searchArtifact(result: CompletionResultSet) {
        var searchArtifact = AndroidDependencySearcher().searchArtifact(dependencyText.groupId)
        if (needNextSearch(searchArtifact)) {
            searchArtifact = MavenDependencySearcher().searchArtifact(dependencyText.groupId)
        }
        val list = searchArtifact?.map { createLookupElement(it, dependencyText.text + it.artifact) }?.toList()
        list?.let {
            result.addAllElements(list)
        }
    }

    private fun searchVersion(result: CompletionResultSet) {
        var searchVersion = AndroidDependencySearcher().searchVersion(dependencyText.groupId, dependencyText.artifactId)
        if (needNextSearch(searchVersion)) {
            searchVersion = MavenDependencySearcher().searchVersion(dependencyText.groupId, dependencyText.artifactId)
        }
        val list = searchVersion?.map { createLookupElement(it, dependencyText.text + it.version) }?.toList()
        list?.let {
            result.addAllElements(list)
        }
    }

    private fun needNextSearch(before: List<DependencySearcher.Result>?): Boolean {
        if (before.isNullOrEmpty()) {
            return true
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
                    text = result.getFullText()
                } else {
                    if (quoteArg.isEmpty()) {
                        quoteArg = "ver_${result.artifact}"
                    }
                    text = "${result.groupId}:${result.artifact}:$$quoteArg"
                    insertArg = "// ext.$quoteArg = '${result.version}' "
                }
            }
            val editor = context.editor
            val document = context.document
            val offset = editor.caretModel.offset
            val lineNumber = document.getLineNumber(offset)
            val lineStartOffset = document.getLineStartOffset(lineNumber)
            val lineEndOffset = document.getLineEndOffset(lineNumber)
            val allText = document.text
            var lineText = allText.substring(lineStartOffset, offset)
            lineText = lineText.replace(cheatString, text)

            if (lineText.contains("'")) {
                lineText += "'"
            } else if (lineText.contains("\"")) {
                lineText += "\""
            }

            if (lineText.contains("(")) {
                lineText += ")"
            }
            insertArg?.let {
                if (lineText.contains("'")) {
                    lineText = lineText.replace("'", "\"")
                }
                if (!lineText.contains(insertArg)) {
                    lineText += insertArg
                }
            }
            document.replaceString(lineStartOffset, lineEndOffset, lineText)
        }
    }


    class CustomLookupElementRenderer(private val dependencyText: DependencyText) : LookupElementRenderer<LookupElement>() {

        override fun renderElement(element: LookupElement?, presentation: LookupElementPresentation?) {
            element?.let {
                val result = (element as LookupElementBuilder).`object` as DependencySearcher.Result
                presentation?.typeText = result.source
                val quoteArg = dependencyText.quoteArg
                if (quoteArg == null) {
                    presentation?.itemText = result.getFullTextAndColon()
                } else {
                    if (result.version.isNullOrEmpty() || result.artifact.isNullOrEmpty()) {
                        presentation?.itemText = result.getFullText() + "$$quoteArg"
                    } else {
                        presentation?.itemText = "${result.groupId}:${result.artifact}:$$quoteArg=${result.version}"
                    }
                }
            }

        }
    }

    companion object {
        const val Q_SYMBOL = "#"
    }

}
