package me.hiten.completion

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import org.apache.http.util.TextUtils
import org.jetbrains.plugins.gradle.util.GradleConstants


class GradleDependenciesCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.SMART, GRADLE_FILE_PATTERN, GradleDependenciesCompletionProvider())
    }


    internal class GradleDependenciesCompletionProvider : CompletionProvider<CompletionParameters>() {

        override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
            val originalPosition = parameters.originalPosition
            if (originalPosition == null || !originalPosition.isValid) {
                return
            }
            val text = originalPosition.text
            if (TextUtils.isEmpty(text)) {
                return
            }
            DependencySearchManager(DependencyText(trimQuote(text))).search(result)
        }
    }

    companion object {
        private val GRADLE_FILE_PATTERN = PlatformPatterns.psiElement().and(
                PlatformPatterns.psiElement().inFile(PlatformPatterns.psiFile().withName(PlatformPatterns.string().endsWith('.' + GradleConstants.EXTENSION))))
    }
}
