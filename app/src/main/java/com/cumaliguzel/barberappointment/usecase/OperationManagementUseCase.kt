package com.cumaliguzel.barberappointment.usecase

import com.cumaliguzel.barberappointment.repository.OperationPriceRepository
import java.lang.Exception

class OperationManagementUseCase(private val operationPriceRepository: OperationPriceRepository) {

    suspend fun deleteOperation(operation: String) {
        try {
            operationPriceRepository.deleteOperationPrice(operation)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateOperationPrice(operation: String, price: Double) {
        try {
            operationPriceRepository.updateOperationPrice(operation, price)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
} 