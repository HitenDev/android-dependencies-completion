package me.hiten.completion

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.ProcessingContext
import org.apache.http.util.TextUtils
import org.jetbrains.plugins.gradle.util.GradleConstants
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrStringContent


class GradleDependenciesCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.SMART, GRADLE_STRING_PATTERN, GradleDependenciesCompletionProvider())
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
            val dependencyText = DependencyText(trimQuote(text))
            val completionTextHandler = CompletionTextHandler(dependencyText)
            if (completionTextHandler.performShow(result)) {
                return
            }
            val searchList = DependencySearchManager(dependencyText).search()
            completionTextHandler.onShow(result, searchList)
        }
    }

    companion object {
        private val GRADLE_FILE_PATTERN = psiElement().and(
                psiElement().inFile(psiFile().withName(string().endsWith('.' + GradleConstants.EXTENSION))))

        private val GRADLE_STRING_PATTERN = GRADLE_FILE_PATTERN.andOr(psiElement()
                .withParent(GrLiteral::class.java), psiElement().withParent(GrStringContent::class.java))
    }
}
