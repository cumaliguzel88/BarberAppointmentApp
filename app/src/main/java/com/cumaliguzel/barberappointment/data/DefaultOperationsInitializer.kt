package com.cumaliguzel.barberappointment.data

import com.cumaliguzel.barberappointment.repository.OperationPriceRepository
import kotlinx.coroutines.flow.first

class DefaultOperationsInitializer(
    private val operationPriceRepository: OperationPriceRepository
) {
    private val defaultOperations = mapOf(
        "✂️ Saç Traşı" to 0.0,
        "✂️ Sakal Traşı" to 0.0,
        "✂️ Saç & Sakal Traş" to 0.0,
        "💆‍♂️ Cilt Bakımı" to 0.0,
        "✂️ Çocuk Traşı" to 0.0
    )

    suspend fun initializeDefaultOperationsIfNeeded() {
        val existingOperations = operationPriceRepository.getAllOperationPrices().first()
        
        if (existingOperations.isEmpty()) {
            defaultOperations.forEach { (operation, price) ->
                operationPriceRepository.insertOperationPrice(
                    OperationPrice(operation = operation, price = price)
                )
            }
        }
    }
} 