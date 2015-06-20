package ru.android.ainege.shoppinglist.db.entities;

import java.util.ArrayList;

public class ListEntity {
    private int _id;
    private String mName;
    private ArrayList<ShoppingListEntity> itemsInList;

    public ListEntity(String name){
        mName = name;
    }

    public ListEntity(int id, String name){
        this(name);
        _id = id;
    }

    public int getId() {
        return _id;
    }

    public void setId(int id) {
        this._id = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public ArrayList<ShoppingListEntity> getItemsInList() {
        return itemsInList;
    }

    public void setItemsInList(ArrayList<ShoppingListEntity> itemsInList) {
        this.itemsInList = itemsInList;
    }
}

