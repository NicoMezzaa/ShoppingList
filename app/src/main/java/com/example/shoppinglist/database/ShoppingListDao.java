package com.example.shoppinglist.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.shoppinglist.entities.ShoppingListEntity;

import java.util.List;

@Dao
public interface ShoppingListDao {
    @Insert
    void insertShoppingList(ShoppingListEntity shoppingListEntity);

    @Query("SELECT * FROM shopping_lists WHERE listName = :listName LIMIT 1")
    ShoppingListEntity getShoppingListByName(String listName);

    @Query("SELECT * FROM shopping_lists")
    List<ShoppingListEntity> getAllShoppingLists();

    @Query("SELECT * FROM shopping_lists WHERE listName NOT IN (SELECT DISTINCT listName FROM products)")
    List<ShoppingListEntity> getOrphanedLists();

    @Delete
    void deleteShoppingList(ShoppingListEntity shoppingList);
}