package com.tastecompass.data.repository.mongo

import com.tastecompass.domain.common.AnalyzeStep
import com.tastecompass.data.config.MongoConfig
import com.tastecompass.domain.entity.Metadata
import com.tastecompass.domain.entity.RestaurantProperty
import com.tastecompass.data.exception.EntityNotFoundException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes=[MongoConfig::class, MetadataRepository::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MetadataRepositoryIntegrativeTest {

    @Autowired
    private lateinit var metadataRepository: MetadataRepository

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val insertedIds = mutableListOf<String>()
    private val testId1 = "test-1"
    private val testId2 = "test-2"

    @BeforeEach
    fun setup(): Unit = runBlocking {
        val metadata1 = Metadata(
            id = testId1,
            status = AnalyzeStep.PREPARED,
            name = "name-1"
        )
        val metadata2 = Metadata(
            id = testId2,
            status = AnalyzeStep.PREPARED,
            name = "name-2"
        )

        metadataRepository.delete(listOf(testId1, testId2))
        metadataRepository.insert(listOf(metadata1, metadata2))
        insertedIds.addAll(listOf(testId1, testId2))
    }

    @AfterEach
    fun cleanup(): Unit = runBlocking {
        if (insertedIds.isNotEmpty()) {
            metadataRepository.delete(insertedIds)
            insertedIds.clear()
        }
    }

    @Test
    fun `should return restaurant metadata with given id`() = runBlocking {
        val result = metadataRepository.get(testId1)

        assertEquals(testId1, result.id)
    }

    @Test
    fun `should throw exception when getting not existing id`() = runBlocking {
        val wrongId = "id-wrong"

        val exception = assertThrows<EntityNotFoundException> {
            metadataRepository.get(wrongId)
        }
        logger.info(exception.message)
    }

    @Test
    fun `should return list of restaurant metadata with given ids`() = runBlocking {
        val result = metadataRepository.get(listOf(testId1, testId2))

        assertEquals(2, result.size)
        assertTrue(result.any { it.id == testId1 })
        assertTrue(result.any { it.id == testId2 })
        assertTrue(result.all { it.category == RestaurantProperty.CATEGORY.defaultValue })
        assertTrue(result.all { it.menus == RestaurantProperty.MENUS.defaultValue})
    }

    @Test
    fun `should return list of whole restaurant metadata`() = runBlocking {
        val result = metadataRepository.getAll()

        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `should insert new data`() = runBlocking {
        val newTestId = "test-new"
        val newMetadata = Metadata(
            id = newTestId,
            name = "name-new",
            mood = listOf("mood-new"),
            status = AnalyzeStep.PREPARED
        )

        metadataRepository.insert(listOf(newMetadata))
        insertedIds.add(newTestId)

        val result = metadataRepository.get(listOf(newTestId))
        assertEquals("test-new", result.first().id)
    }

    @Test
    fun `should update present data`() = runBlocking {
        val updatedMetadata1 = Metadata(
            id = testId1,
            name = "name-updated1",
            mood = listOf("mood-updated1"),
            status = AnalyzeStep.ANALYZED
        )

        metadataRepository.update(updatedMetadata1)

        val result = metadataRepository.get(testId1)

        assertEquals("name-updated1", result.name)
    }

    @Test
    fun `should upsert new data`() = runBlocking {
        val newTestId = "test-new"
        val newMetadata = Metadata(
            id = newTestId,
            name = "name-new",
            mood = listOf("mood-new"),
            status = AnalyzeStep.PREPARED
        )

        metadataRepository.upsert(listOf(newMetadata))
        insertedIds.add(newTestId)

        val result = metadataRepository.get(listOf(newTestId))
        assertEquals("test-new", result.first().id)
    }

    @Test
    fun `should upsert present data`() = runBlocking {
        val updatedMetadata1 = Metadata(
            id = testId1,
            name = "name-updated1",
            mood = listOf("mood-updated1") ,
            status = AnalyzeStep.ANALYZED
        )

        metadataRepository.upsert(updatedMetadata1)

        val result = metadataRepository.get(testId1)

        assertEquals("name-updated1", result.name)
    }

    @Test
    fun `should delete present data`() = runBlocking {
        metadataRepository.delete(listOf(testId1))

        val result = metadataRepository.get(listOf(testId1))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return whether entity exists with given id`() = runBlocking {
        val wrongId = "id-wrong"

        val result1 = metadataRepository.exists(testId1)
        val result2 = metadataRepository.exists(wrongId)

        assertEquals(true, result1)
        assertEquals(false, result2)
    }
}