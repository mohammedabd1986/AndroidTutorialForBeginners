package com.example.inventory.data

import androidx.room.Embedded
import androidx.room.Relation

data class ItemWithStock(
    @Embedded val item: Item,
    @Relation(parentColumn = "id", entityColumn = "itemId")
    val stock: List<WarehouseStock>
)
