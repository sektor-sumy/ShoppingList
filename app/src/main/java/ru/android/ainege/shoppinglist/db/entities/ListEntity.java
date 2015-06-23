package ru.android.ainege.shoppinglist.db.entities;

import java.util.ArrayList;

public class ListEntity {
    private int _id;
    private String mName;
    private ArrayList<ShoppingListEntity> mItemsInList;

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
        return mItemsInList;
    }

    public void setItemsInList(ArrayList<ShoppingListEntity> itemsInList) {
        this.mItemsInList = itemsInList;
    }

    public double sumSpentMoney(){
        double sum = 0;
        for(ShoppingListEntity item : mItemsInList) {
            if(item.isBought()) {
                sum += item.getItem().getPrice();
            }
        }
        return sum;
    }

    public double sumTotalMoney(){
        double sum = 0;
        for(ShoppingListEntity item : mItemsInList) {
            sum += item.getItem().getPrice();
        }
        return sum;
    }
}

