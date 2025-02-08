package com.tastecompass.data.entity

import com.tastecompass.data.common.AnalyzeStep

class RestaurantSample {
    private val metadataSample = RestaurantMetadataSample()
    private val metadataSampleData = metadataSample.data()

    val idList = listOf("test-0", "test-1", "test-2")
    fun data(): List<Restaurant> {
        return listOf(
            Restaurant(
                id = idList[0],
                metadata = metadataSampleData[0],
                embedding = null,
            ),
            Restaurant(
                id = idList[1],
                metadata = metadataSampleData[1],
                embedding = null,
            ),
            Restaurant(
                id = idList[2],
                metadata = metadataSampleData[2],
                embedding = null,
            ),
        )
    }
}