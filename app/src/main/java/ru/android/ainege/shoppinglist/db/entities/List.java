package ru.android.ainege.shoppinglist.db.entities;

import java.util.ArrayList;

public class List {
    private long mId;
    private String mName;
    private ArrayList<ShoppingList> mItemsInList;

    public List(String name){
        mName = name;
    }

    public List(long id, String name){
        this(name);
        mId = id;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public ArrayList<ShoppingList> getItemsInList() {
        return mItemsInList;
    }

    public void setItemsInList(ArrayList<ShoppingList> itemsInList) {
        this.mItemsInList = itemsInList;
    }
}

