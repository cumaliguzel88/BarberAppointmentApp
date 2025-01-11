package com.cumaliguzel.barberappointment.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OperationPriceDao {
    @Query("SELECT * FROM operation_prices ORDER BY operation ASC")
    fun getAllOperationPrices(): Flow<List<OperationPrice>>
    
    @Query("SELECT * FROM operation_prices WHERE operation = :operation")
    suspend fun getOperationPrice(operation: String): OperationPrice?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperationPrice(operationPrice: OperationPrice)

    @Query("DELETE FROM operation_prices WHERE operation = :operation")
    suspend fun deleteOperationPrice(operation: String)

    @Query("UPDATE operation_prices SET price = :price WHERE operation = :operation")
    suspend fun updateOperationPrice(operation: String, price: Double)
} 