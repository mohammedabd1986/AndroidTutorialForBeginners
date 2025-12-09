package com.example.inventory.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.inventory.data.entity.Item
import com.example.inventory.data.entity.WarehouseStock

@Dao
interface InventoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<Item>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStocks(stocks: List<WarehouseStock>)

    @Query("SELECT * FROM items WHERE code = :code LIMIT 1")
    suspend fun getItemByCode(code: String): Item?

    @Query("SELECT * FROM warehouse_stocks WHERE itemId = :itemId AND quantity > 0")
    suspend fun getStocksByItemId(itemId: Long): List<WarehouseStock>

    @Query("DELETE FROM items")
    suspend fun clearAllItems()

    @Query("DELETE FROM warehouse_stocks")
    suspend fun clearAllStocks()

    @Transaction
    suspend fun clearAndInsertBatch(items: List<Item>, stocks: Map<String, List<WarehouseStock>>) {
        // This method is tricky because we need IDs for stocks.
        // It's better to handle logic in Repository to insert items, get IDs, then insert stocks.
        // But for batch processing, we might want to do it differently.
        // Actually, if we use code as a reference in stock it might be easier but requirement says:
        // "Item (id, code, name, price) and WarehouseStock (itemId, warehouseName, quantity)"

        // So we will just provide basic insert methods and handle logic in repository/usecase.
    }
}
