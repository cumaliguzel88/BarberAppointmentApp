package com.cumaliguzel.barberappointment.repository

import com.cumaliguzel.barberappointment.data.OperationPrice
import com.cumaliguzel.barberappointment.data.OperationPriceDao
import kotlinx.coroutines.flow.Flow

class OperationPriceRepository(private val operationPriceDao: OperationPriceDao) {
    
    fun getAllOperationPrices(): Flow<List<OperationPrice>> = 
        operationPriceDao.getAllOperationPrices()
    
    suspend fun getOperationPrice(operation: String): OperationPrice? =
        operationPriceDao.getOperationPrice(operation)
    
    suspend fun insertOperationPrice(operationPrice: OperationPrice) {
        operationPriceDao.insertOperationPrice(operationPrice)
    }
    
    suspend fun deleteOperationPrice(operation: String) {
        operationPriceDao.deleteOperationPrice(operation)
    }
    
    suspend fun updateOperationPrice(operation: String, price: Double) {
        operationPriceDao.updateOperationPrice(operation, price)
    }
} 