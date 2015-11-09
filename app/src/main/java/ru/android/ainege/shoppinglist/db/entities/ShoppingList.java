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
    private String mComment;
    private Date mDate;
    private Item mItem;
    private Unit mUnit;

    public ShoppingList(boolean isBought, double amount, double price,  String comment) {
        mIsBought = isBought;
        mAmount = amount;
        mPrice = price;
        mComment = comment;
    }

    public ShoppingList(long idItem, boolean isBought, double amount, long idUnit, double price,  String comment, Date date) {
        this(isBought, amount, price, comment);
        mIdItem = idItem;
        mIdUnit = idUnit;
        mDate = date;
    }

    public ShoppingList(long idItem, long idList, boolean isBought, double amount, long idUnit, double price, String comment, Date date) {
        this(idItem, isBought, amount, idUnit, price, comment, date);
        mIdList = idList;
    }

    public ShoppingList(Item item, long idList, boolean isBought, double amount, Unit unit, double price, String comment) {
        this(isBought, amount, price, comment);
        mItem = item;
        mIdItem = mItem.getId();
        mIdList = idList;
        mUnit = unit;
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

    public String getComment() {
        return mComment;
    }

    public void setComment(String comment) {
        mComment = comment;
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
