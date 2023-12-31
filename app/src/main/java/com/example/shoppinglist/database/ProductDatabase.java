package com.example.shoppinglist.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.shoppinglist.entities.ProductEntity;
import com.example.shoppinglist.entities.ShoppingListEntity;

@Database(entities = {ProductEntity.class, ShoppingListEntity.class}, version = 2, exportSchema = false)
public abstract class ProductDatabase extends RoomDatabase {
    public abstract ProductDao productDao();
    public abstract ShoppingListDao shoppingListDao();
}
