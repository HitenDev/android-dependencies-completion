package me.hiten.completion

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.codeInsight.lookup.LookupElementRenderer
import me.hiten.completion.Config.Q_SYMBOL
import me.hiten.completion.Config.Q_SYMBOL_ALL

object LookupElementFactory {

    fun createLookupElement(dependencyText: DependencyText, result: DependencySearcher.Result, cheatString: String): LookupElement {
        return LookupElementBuilder.create(result, cheatString).withRenderer(CustomLookupElementRenderer(dependencyText)).withInsertHandler { context, _ ->
            var quoteArg = dependencyText.quoteArg
            val text: String
            var insertArg: String? = null
            if (quoteArg == null) {
                text = result.getFullTextAndColon()
            } else {
                if (result.version.isNullOrEmpty() || result.artifact.isNullOrEmpty()) {
                    text = result.getFullTextAndColon() + "${if (dependencyText.convertAll) Q_SYMBOL_ALL else Q_SYMBOL}$quoteArg"
                } else {
                    if (quoteArg.isEmpty()) {
                        quoteArg = "${if (dependencyText.convertAll) "dep_" else "ver_"}${result.artifact?.replace("-", "_")}"
                    }
                    if (!dependencyText.convertAll) {
                        text = "${result.groupId}:${result.artifact}:$$quoteArg"
                        insertArg = "ext.$quoteArg = '${result.version}' //please move this code to a unified place.\n"
                    } else {
                        text = "$$quoteArg"
                        insertArg = "ext.$quoteArg = '${result.groupId}:${result.artifact}:${result.version}' //please move this code to a unified place.\n"
                    }
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

    private class CustomLookupElementRenderer(private val dependencyText: DependencyText) : LookupElementRenderer<LookupElement>() {

        override fun renderElement(element: LookupElement?, presentation: LookupElementPresentation?) {
            element?.let {
                val result = (element as LookupElementBuilder).`object` as DependencySearcher.Result
                presentation?.setTypeText(null, Icons.getIcon(result.source))
                var quoteArg = dependencyText.quoteArg
                if (quoteArg == null) {
                    presentation?.itemText = result.getFullTextAndColon()
                } else {
                    if (result.version.isNullOrEmpty() || result.artifact.isNullOrEmpty()) {
                        presentation?.itemText = result.getFullTextAndColon() + "${if (dependencyText.convertAll) Q_SYMBOL_ALL else Q_SYMBOL}$quoteArg"
                    } else {
                        if (quoteArg.isEmpty()) {
                            quoteArg = "${if (dependencyText.convertAll) "dep_" else "ver_"}${result.artifact?.replace("-", "_")}"
                        }
                        if (!dependencyText.convertAll) {
                            presentation?.itemText = "${result.groupId}:${result.artifact}:$$quoteArg=${result.version}"
                        } else {
                            presentation?.itemText = "$$quoteArg=${result.groupId}:${result.artifact}:${result.version}"
                        }
                    }
                }
            }

        }
    }

}