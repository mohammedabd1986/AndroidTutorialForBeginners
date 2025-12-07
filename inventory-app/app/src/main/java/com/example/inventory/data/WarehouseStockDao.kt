package com.example.inventory.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WarehouseStockDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stocks: List<WarehouseStock>)

    @Query("DELETE FROM warehouse_stock")
    suspend fun clearStock()
}
