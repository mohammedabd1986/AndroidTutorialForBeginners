package com.example.inventory.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<Item>): List<Long>

    @Query("SELECT * FROM items WHERE code = :code LIMIT 1")
    suspend fun findByCode(code: String): Item?

    @Transaction
    @Query("SELECT * FROM items WHERE code = :code LIMIT 1")
    suspend fun findWithStock(code: String): ItemWithStock?

    @Query("DELETE FROM items")
    suspend fun clearItems()
}
