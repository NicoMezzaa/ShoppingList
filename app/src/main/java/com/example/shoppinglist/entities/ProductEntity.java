package com.example.shoppinglist.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "products", foreignKeys = @ForeignKey(entity = ShoppingListEntity.class,
        parentColumns = "listName",
        childColumns = "listName",
        onDelete = ForeignKey.CASCADE))
public class ProductEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String price;
    private String quantity;
    private String category;
    private String listName;

    public ProductEntity(String name, String price, String quantity, String category, String listName) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.listName = listName;
    }

    @Ignore
    public ProductEntity(int id, String name, String price, String quantity, String category, String listName) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.listName = listName;
    }

    // getter e setter

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }
}