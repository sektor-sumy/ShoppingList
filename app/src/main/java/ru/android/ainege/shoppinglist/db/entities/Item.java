package ru.android.ainege.shoppinglist.db.entities;

public class Item {
    private long mId;
    private String mName;
    private double mAmount;
    private long mIdUnit;
    private double mPrice;
    private Unit mUnit;

    public Item(String name) {
        mName = name;
    }

    public Item(String name, double price) {
        this(name);
        mPrice = price;
    }

    public Item(String name, double amount, long idUnit) {
        this(name);
        mAmount = amount;
        mIdUnit = idUnit;
    }

    public Item(String name, double amount, long idUnit, double price) {
        this(name, amount, idUnit);
        mPrice = price;
    }

    public Item(String name, double amount, Unit unit, double price) {
        this(name, price);
        mAmount = amount;
        mUnit = unit;
        if (mUnit != null) {
            mIdUnit = unit.getId();
        }
    }

    public Item(long id, String name, double amount, long idUnit, double price) {
        this(name, amount, idUnit, price);
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
