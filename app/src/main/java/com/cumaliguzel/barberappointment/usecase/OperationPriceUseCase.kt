package com.cumaliguzel.barberappointment.usecase

import com.cumaliguzel.barberappointment.data.OperationPrice
import com.cumaliguzel.barberappointment.repository.OperationPriceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map

class OperationPriceUseCase(val operationPriceRepository: OperationPriceRepository) {

    fun getAllOperationPrices(): Flow<Map<String, Double>> =
        operationPriceRepository.getAllOperationPrices().map { prices ->
            prices.associate { it.operation to it.price }
        }

    suspend fun getOperationPrice(operation: String): Double {
        return operationPriceRepository.getOperationPrice(operation)?.price ?: 0.0
    }

    suspend fun saveOperationPrices(prices: Map<String, Double>) {
        prices.forEach { (operation, price) ->
            operationPriceRepository.insertOperationPrice(
                OperationPrice(operation = operation, price = price)
            )
        }
    }
} 