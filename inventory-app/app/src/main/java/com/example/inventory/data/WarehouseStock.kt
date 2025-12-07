package com.example.inventory.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "warehouse_stock",
    primaryKeys = ["itemId", "warehouseName"],
    foreignKeys = [
        ForeignKey(
            entity = Item::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("itemId"), Index("warehouseName")]
)
data class WarehouseStock(
    val itemId: Long,
    val warehouseName: String,
    val quantity: Int
)
