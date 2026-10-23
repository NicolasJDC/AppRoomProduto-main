package br.unisanta.myapplication.db

import androidx.room.Database
import androidx.room.RoomDatabase
import br.unisanta.myapplication.dao.ProductDao
import br.unisanta.myapplication.model.Product

@Database(entities = [Product::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
}