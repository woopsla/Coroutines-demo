package com.scarlet.model

data class Article(val id: String, val author: String, val title: String) {
    companion object {
        val articleSamples = listOf(
            Article("A001", "Robert Martin", "Clean Code"),
            Article("A002", "Jungsun Kim", "Android Testing in Kotlin"),
            Article("A003", "Kent Beck", "Extreme Programming"),
            Article("A004", "Robert Martin", "Agile Patterns"),
            Article("A005", "Sean McQuillan", "Android Testing"),
            Article("A006", "Roman Elizarov", "Kotlin Coroutines")
        )
    }
}
