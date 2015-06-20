package ru.android.ainege.shoppinglist.db.entities;

/**
 * Created by i on 20.06.2015.
 */
public class ItemEntity {
    private int _id;
    private String mName;
    private int mAmount;
    private int mIdUnit;
    private double mPrice;

    public ItemEntity(String name){
        mName = name;
    }

    public ItemEntity(String name, double price){
        this(name);
        mPrice = price;
    }

    public ItemEntity(String name, int amount, int idUnit){
        this(name);
        mAmount = amount;
        mIdUnit = idUnit;
    }

    public ItemEntity(String name, int amount, int idUnit, double price){
        this(name, amount, idUnit);
        mPrice = price;
    }

    public ItemEntity(int id, String name, int amount, int idUnit, double price){
        this(name, amount, idUnit, price);
        _id = id;
    }

    public int getId() {
        return _id;
    }

    public void setId(int _id) {
        this._id = _id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getAmount() {
        return mAmount;
    }

    public void setAmount(int amount) {
        mAmount = amount;
    }

    public int getIdUnit() {
        return mIdUnit;
    }

    public void setIdUnit(int idUnit) {
        mIdUnit = idUnit;
    }

    public double getPrice() {
        return mPrice;
    }

    public void setPrice(double price) {
        this.mPrice = price;
    }
}
