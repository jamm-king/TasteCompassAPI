package com.tastecompass.data.exception

import com.tastecompass.data.entity.Restaurant
import com.tastecompass.data.entity.RestaurantEmbedding
import com.tastecompass.data.entity.RestaurantMetadata

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
        fun invalidInsertState(): InvalidRequestException {
            return InvalidRequestException("Only PREPARED restaurants can be inserted.")
        }
        fun invalidInsertState(restaurant: Restaurant): InvalidRequestException {
            return InvalidRequestException("Only PREPARED restaurants can be inserted. Given status: ${restaurant.status}")
        }

        fun invalidUpdateState(): InvalidRequestException {
            return InvalidRequestException("ANALYZED, EMBEDDED restaurants can be inserted.")
        }
        fun invalidUpdateState(restaurant: Restaurant): InvalidRequestException {
            return InvalidRequestException("ANALYZED, EMBEDDED restaurants can be inserted. Given status: ${restaurant.status}")
        }

        fun duplicateKey(restaurant: Restaurant): InvalidRequestException {
            return InvalidRequestException("Restaurant with id ${restaurant.id} already exists.")
        }
        fun duplicateKey(restaurantEmbedding: RestaurantEmbedding): InvalidRequestException {
            return InvalidRequestException("Restaurant embedding with id ${restaurantEmbedding.id} already exists.")
        }
        fun duplicateKey(restaurantMetadata: RestaurantMetadata): InvalidRequestException {
            return InvalidRequestException("Restaurant metadata with id ${restaurantMetadata.id} already exists.")
        }
    }
}