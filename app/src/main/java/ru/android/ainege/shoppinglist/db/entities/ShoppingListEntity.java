package ru.android.ainege.shoppinglist.db.entities;

/**
 * Created by i on 20.06.2015.
 */
public class ShoppingListEntity {
    private int mIdItem;
    private int mIdList;
    private int mIsBought;

    public ShoppingListEntity(int idItem, int idList, boolean isBought){
        mIdItem = idItem;
        mIdList = idList;
        mIsBought = isBought ? 1 : 0;
    }

    public int getIdItem() {
        return mIdItem;
    }

    public void setIdItem(int idItem) {
        mIdItem = idItem;
    }

    public int getIdList() {
        return mIdList;
    }

    public void setIdList(int idList) {
        mIdList = idList;
    }

    public int getIsBought() {
        return mIsBought;
    }

    public void setIsBought(int bought) {
        mIsBought = bought;
    }
}
