package me.hiten.completion


interface DependencySearcher {

    fun searchDefault(keyword: String): List<Result>?

    fun searchArtifact(groupId: String): List<Result>?

    fun searchVersion(groupId: String, artifact: String): List<Result>?


    class Result(var source: String) {
        var groupId: String? = null
        var artifact: String? = null
        var version: String? = null

        fun getFullText(): String {
            var fullText = groupId
            if (!groupId.isNullOrEmpty() && !artifact.isNullOrEmpty()) {
                fullText += ":$artifact"
            }
            if (!artifact.isNullOrEmpty() && !version.isNullOrEmpty()) {
                fullText += ":$version"
            }
            if (fullText.isNullOrEmpty()) {
                return ""
            }
            return fullText!!
        }

        fun getFullTextAndColon(): String {
            var fullText = groupId
            if (!groupId.isNullOrEmpty()) {
                fullText += ":"
            }
            if (!artifact.isNullOrEmpty()) {
                fullText += "$artifact:"
            }
            if (!version.isNullOrEmpty()) {
                fullText += "$version"
            }
            if (fullText.isNullOrEmpty()) {
                return ""
            }
            return fullText!!
        }

    }

}