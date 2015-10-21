package ru.android.ainege.shoppinglist.db.entities;

import java.io.Serializable;
import java.util.Date;

public class ShoppingList implements Serializable {
    private long mIdItem;
    private long mIdList;
    private boolean mIsBought;
    private double mAmount;
    private long mIdUnit;
    private double mPrice;
    private Date mDate;
    private Item mItem;
    private Unit mUnit;

    public ShoppingList(long idItem, long idList, boolean isBought, Item item) {
        mItem = item;
    }

    public ShoppingList(long idItem, boolean isBought, double amount, double price, Date date) {
        mIdItem = idItem;
        mIsBought = isBought;
        mAmount = amount;
        mPrice = price;
        mDate = date;
    }

    public ShoppingList(long idItem, long idList, long isBought, double amount, long idUnit, double price, Date date) {
        mIdItem = idItem;
        mIdList = idList;
        mIsBought = isBought == 1;
        mAmount = amount;
        mIdUnit = idUnit;
        mPrice = price;
        mDate = date;
    }

    public long getIdItem() {
        return mIdItem;
    }

    public void setIdItem(long idItem) {
        mIdItem = idItem;
    }

    public long getIdList() {
        return mIdList;
    }

    public void setIdList(long idList) {
        mIdList = idList;
    }

    public boolean isBought() {
        return mIsBought;
    }

    public void setBought(boolean bought) {
        mIsBought = bought;
    }

    public double getAmount() {
        return mAmount;
    }

    public void setAmount(double amount) {
        mAmount = amount;
    }

    public long getIdUnit() {
        return mIdUnit;
    }

    public void setIdUnit(long idUnit) {
        mIdUnit = idUnit;
    }

    public double getPrice() {
        return mPrice;
    }

    public void setPrice(double price) {
        this.mPrice = price;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public Item getItem() {
        return mItem;
    }

    public void setItem(Item item) {
        mItem = item;
        if (mItem != null) {
            mIdItem = mItem.getId();
        }
    }

    public Unit getUnit() {
        return mUnit;
    }

    public void setUnit(Unit unit) {
        mUnit = unit;
        if (mUnit != null) {
            mIdUnit = mUnit.getId();
        }
    }
}
