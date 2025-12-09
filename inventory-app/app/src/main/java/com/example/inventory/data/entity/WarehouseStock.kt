package com.example.inventory.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "warehouse_stocks",
    foreignKeys = [
        ForeignKey(
            entity = Item::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["itemId"])]
)
data class WarehouseStock(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemId: Long,
    val warehouseName: String,
    val quantity: Int
)
