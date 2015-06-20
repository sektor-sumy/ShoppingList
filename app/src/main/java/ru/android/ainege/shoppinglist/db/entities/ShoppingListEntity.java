package ru.android.ainege.shoppinglist.db.entities;

public class ShoppingListEntity {
    private int mIdItem;
    private int mIdList;
    private boolean mIsBought;
    private ItemEntity mItem;
    private ListEntity mList;

    public ShoppingListEntity(int idItem, int idList, int isBought){
        mIdItem = idItem;
        mIdList = idList;
        mIsBought = isBought == 1;
    }

    public ShoppingListEntity(int idItem, int idList, boolean isBought){
        mIdItem = idItem;
        mIdList = idList;
        mIsBought = isBought;
    }

    public ShoppingListEntity(int idItem, int idList, boolean isBought, ItemEntity item){
        this(idItem, idList, isBought);
        mItem = item;
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

    public boolean isBought() {
        return mIsBought;
    }

    public void setBought(boolean bought) {
        mIsBought = bought;
    }

    public ItemEntity getItem() {
        return mItem;
    }

    public void setItem(ItemEntity item) {
        this.mItem = item;
    }

    public ListEntity getList() {
        return mList;
    }

    public void setList(ListEntity list) {
        this.mList = list;
    }
}
