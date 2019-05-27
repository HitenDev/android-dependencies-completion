package me.hiten.completion.androidsearch

import me.hiten.completion.DependencySearcher

class AndroidDependencySearcher : DependencySearcher {


    companion object {
        const val SOURCE = "google"
    }

    override fun searchDefault(keyword: String): List<DependencySearcher.Result>? {
        return GoogleMavenGroupIdSearcher.matchByWords(keyword)?.filter {
            it.matched
        }?.mapNotNull { it.groupId }?.map {
            DependencySearcher.Result(SOURCE).apply {
                this.groupId = it
            }
        }?.toList()
    }

    override fun searchArtifact(groupId: String): List<DependencySearcher.Result>? {
        return GoogleMavenArtifactVersionSearcher.searchArtifact(groupId)?.map {
            DependencySearcher.Result(SOURCE).apply {
                this.groupId = groupId
                this.artifact = it
            }
        }?.toList()
    }

    override fun searchVersion(groupId: String, artifact: String): List<DependencySearcher.Result>? {
        return GoogleMavenArtifactVersionSearcher.searchVersion(groupId,artifact)?.map {
            DependencySearcher.Result(SOURCE).apply {
                this.groupId = groupId
                this.artifact = artifact
                this.version = it
            }
        }?.toList()
    }
}