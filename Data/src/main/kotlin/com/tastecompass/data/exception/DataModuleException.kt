package com.tastecompass.data.exception

import com.tastecompass.domain.entity.Restaurant
import com.tastecompass.domain.entity.Embedding
import com.tastecompass.domain.entity.Metadata

sealed class DataModuleException(message: String): RuntimeException(message)

class DataAccessException(message: String): DataModuleException(message) {
    companion object {
        fun milvusAccessUnavailable(): DataAccessException {
            return DataAccessException("Failed to access to Milvus.")
        }
        fun mongoAccessUnavailable(): DataAccessException {
            return DataAccessException("Failed to access to MongoDB.")
        }
        fun databaseNotExist(databaseName: String): DataAccessException {
            return DataAccessException("There is no database named '$databaseName'")
        }
    }
}

class EntityNotFoundException(message: String): DataModuleException(message) {
    companion object {
        fun restaurantNotFound(id: String): EntityNotFoundException {
            return EntityNotFoundException("Restaurant with id $id is not found.")
        }
        fun restaurantEmbeddingNotFound(id: String): EntityNotFoundException {
            return EntityNotFoundException("Restaurant embedding with id $id is not found.")
        }
        fun restaurantMetadataNotFound(id: String): EntityNotFoundException {
            return EntityNotFoundException("Restaurant metadata with id $id is not found.")
        }
    }
}

class InvalidRequestException(message: String): DataModuleException(message) {
    companion object {
        fun invalidSaveState(restaurant: Restaurant): InvalidRequestException {
            return InvalidRequestException(
                "Only EMBEDDED restaurants can be saved. The restaurant's status is ${restaurant.status}"
            )
        }

        fun duplicateKey(restaurant: Restaurant): InvalidRequestException {
            return InvalidRequestException("Restaurant with id ${restaurant.id} already exists.")
        }
        fun duplicateKey(embedding: Embedding): InvalidRequestException {
            return InvalidRequestException("Restaurant embedding with id ${embedding.id} already exists.")
        }
        fun duplicateKey(metadata: Metadata): InvalidRequestException {
            return InvalidRequestException("Restaurant metadata with id ${metadata.id} already exists.")
        }
    }
}