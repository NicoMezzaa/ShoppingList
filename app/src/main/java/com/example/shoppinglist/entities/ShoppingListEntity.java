package com.example.shoppinglist.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "shopping_lists", indices = {@Index(value = "listName", unique = true)})
public class ShoppingListEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    private String listName;

    public ShoppingListEntity(String listName) {
        this.listName = listName;
    }

    public int getId() {
        return id;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    @NonNull
    @Override
    public String toString() {
        return listName;
    }
}