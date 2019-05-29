package me.hiten.completion

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionSorter
import com.intellij.codeInsight.completion.impl.PreferStartMatching
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.LookupElementWeigher
import com.intellij.util.containers.isNullOrEmpty

class CompletionTextHandler(private val dependencyText: DependencyText) {

    fun performShow(result: CompletionResultSet): Boolean {
        if (dependencyText.versionId.isNotEmpty() && dependencyText.artifactId.isNotEmpty() && dependencyText.groupId.isNotEmpty()) {
            if (dependencyText.quoteArg == null) {
                return false
            }

            result.restartCompletionOnPrefixChange(dependencyText.text)
            val fakeResult = DependencySearcher.Result("maven")
            fakeResult.groupId = dependencyText.groupId
            fakeResult.artifact = dependencyText.artifactId
            fakeResult.version = dependencyText.versionId
            result.addElement(LookupElementFactory.createLookupElement(dependencyText, fakeResult, dependencyText.text + dependencyText.versionId))
            return true
        }

        return false
    }

    fun onShow(result: CompletionResultSet, dependencyList: List<DependencySearcher.Result>?) {
        if (dependencyList.isNullOrEmpty()) {
            return
        }
        val size = dependencyText.splitText.size
        when (size) {
            3 -> showVersionResult(result, dependencyList!!)
            2 -> showArtifactResult(result, dependencyList!!)
            else -> showDefaultResult(result, dependencyList!!)
        }
    }

    private fun showDefaultResult(result: CompletionResultSet, dependencyList: List<DependencySearcher.Result>) {
        val list = dependencyList.map { LookupElementFactory.createLookupElement(dependencyText, it, dependencyText.text + it.groupId) }.toList()
        result.restartCompletionOnPrefixChange(dependencyText.text)
        result.withRelevanceSorter(
                CompletionSorter.emptySorter().weigh(PreferStartMatching())
        ).addAllElements(list)
    }

    private fun showArtifactResult(result: CompletionResultSet, dependencyList: List<DependencySearcher.Result>) {
        val list = dependencyList.map { LookupElementFactory.createLookupElement(dependencyText, it, dependencyText.text + it.artifact) }.toList()
        result.restartCompletionOnPrefixChange(dependencyText.text)
        result.withRelevanceSorter(
                CompletionSorter.emptySorter().weigh(PreferStartMatching())
        ).addAllElements(list)
    }

    private fun showVersionResult(result: CompletionResultSet, dependencyList: List<DependencySearcher.Result>) {
        val list = dependencyList.map { LookupElementFactory.createLookupElement(dependencyText, it, dependencyText.text + it.version) }.toList()
        result.restartCompletionOnPrefixChange(dependencyText.text)
        val withRelevanceSorter = result.withRelevanceSorter(
                CompletionSorter.emptySorter().weigh(object : LookupElementWeigher("versionWeigher") {
                    override fun weigh(element: LookupElement): Comparable<VersionComparable> {
                        val obj = (element as LookupElementBuilder).`object` as DependencySearcher.Result
                        return VersionComparable(dependencyList.indexOf(obj))
                    }
                })
        )
        withRelevanceSorter.addAllElements(list)
    }


    companion object {
        class VersionComparable(private val index: Int) : Comparable<VersionComparable> {
            override fun compareTo(other: VersionComparable): Int = this.index - other.index
        }
    }

}