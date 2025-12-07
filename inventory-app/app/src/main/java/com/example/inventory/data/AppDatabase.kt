package com.example.inventory.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Item::class, WarehouseStock::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun warehouseStockDao(): WarehouseStockDao
}
