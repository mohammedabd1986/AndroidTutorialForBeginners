package com.example.inventory.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.inventory.data.dao.InventoryDao
import com.example.inventory.data.entity.Item
import com.example.inventory.data.entity.WarehouseStock

@Database(entities = [Item::class, WarehouseStock::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao
}
