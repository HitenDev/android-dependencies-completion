package me.hiten.completion.mavensearch

import me.hiten.completion.Client
import me.hiten.completion.DependencySearcher

class MavenDependencySearcher : DependencySearcher {

    override fun searchDefault(keyword: String): List<DependencySearcher.Result>? {
        val result = Client.get("https://search.maven.org/solrsearch/select?q=$keyword&rows=100&wt=json")
        return ID_PATTERN.findAll(result).mapNotNull { it.groups[1]?.value }.map {
            DependencySearcher.Result(SOURCE).apply {
                val splitText: List<String> = it.split(":")
                this.groupId = splitText.getOrElse(0) { null }
                this.artifact = splitText.getOrElse(1) { null }
                this.version = splitText.getOrElse(2) { null }
            }
        }.toList()
    }

    override fun searchArtifact(groupId: String): List<DependencySearcher.Result>? {
        val result = Client.get(
                "https://search.maven.org/solrsearch/select?q=g:$groupId" +
                        "&rows=200&wt=json")
        return ARTIFACT_PATTERN.findAll(result)
                .map {
                    it.groups[1]!!.value
                }
                .distinct()
                .map {
                    DependencySearcher.Result(SOURCE).apply {
                        this.groupId = groupId
                        this.artifact = it
                    }
                }.toList()
    }

    override fun searchVersion(groupId: String, artifact: String): List<DependencySearcher.Result>? {
        val result = Client.get(
                "https://search.maven.org/solrsearch/select?q=g:$groupId+AND+a:$artifact" +
                        "&rows=100&core=gav&wt=json")

        return VERSION_PATTERN.findAll(result).mapNotNull {
            it.groups[1]?.value
        }.distinct().map {
            DependencySearcher.Result(SOURCE).apply {
                this.groupId = groupId
                this.artifact = artifact
                this.version = it.split(":").getOrNull(2)
            }
        }.toList()
    }


    companion object {
        const val SOURCE = "maven"
        private val ID_PATTERN = Regex("\\{\"id\":\"([^\"]+)\"")
        private val ARTIFACT_PATTERN = Regex(",\"a\":\"([^\"]+)\"")
        private val VERSION_PATTERN = Regex("\\{\"id\":\"([^\"]+)\"")
    }
}