package com.example.shoppinglist.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.shoppinglist.entities.ProductEntity;

import java.util.List;

@Dao
public interface ProductDao {
    @Insert
    void insertProduct(ProductEntity product);

    @Query("SELECT * FROM products WHERE listName = :listName")
    List<ProductEntity> getProductsByListName(String listName);

    @Query("DELETE FROM products WHERE listName = :listName")
    void deleteProductsByListName(String listName);

    @Delete
    void deleteProduct(ProductEntity product);

    @Update
    void updateProduct(ProductEntity product);

    @Query("SELECT * FROM products WHERE name = :productName AND listName = :listName LIMIT 1")
    ProductEntity getProductByNameAndList(String productName, String listName);

    @Query("SELECT * FROM products WHERE listName = :listName AND category = :category")
    List<ProductEntity> getProductsByListNameAndCategory(String listName, String category);
}