package me.hiten.completion

import com.intellij.util.containers.isNullOrEmpty
import me.hiten.completion.androidsearch.AndroidDependencySearcher
import me.hiten.completion.mavensearch.MavenDependencySearcher

class DependencySearchManager(private val dependencyText: DependencyText) {


    fun search(): List<DependencySearcher.Result>? {
        if (dependencyText.isShort) {
            return null
        }
        val size = dependencyText.splitText.size
        return when (size) {
            3 -> searchVersion()
            2 -> searchArtifact()
            else -> searchDefault()
        }
    }

    private fun searchDefault(): List<DependencySearcher.Result>? {
        var searchDefault = AndroidDependencySearcher().searchDefault(dependencyText.text)
        if (needNextSearch(searchDefault)) {
            searchDefault = mergeSearch(searchDefault, MavenDependencySearcher().searchDefault(dependencyText.text))
        }
        return searchDefault
    }


    private fun searchArtifact(): List<DependencySearcher.Result>? {
        var searchArtifact = AndroidDependencySearcher().searchArtifact(dependencyText.groupId)
        if (needNextSearch(searchArtifact)) {
            searchArtifact = mergeSearch(searchArtifact, MavenDependencySearcher().searchArtifact(dependencyText.groupId))
        }
        return searchArtifact
    }

    private fun searchVersion(): List<DependencySearcher.Result>? {
        var searchVersion = AndroidDependencySearcher().searchVersion(dependencyText.groupId, dependencyText.artifactId)
        if (needNextSearch(searchVersion)) {
            searchVersion = mergeSearch(searchVersion, MavenDependencySearcher().searchVersion(dependencyText.groupId, dependencyText.artifactId))
        }
        return searchVersion
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


}
