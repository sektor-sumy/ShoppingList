package ru.android.ainege.shoppinglist.db.entities;

public class Currency {
    private long mId;
    private String mName;
    private String mSymbol;

    public Currency(String name, String symbol) {
        mName = name;
        mSymbol = symbol;
    }

    public Currency(long id, String name, String symbol) {
        this(name, symbol);
        this.mId = id;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public String getSymbol() {
        return mSymbol;
    }

    public void setSymbol(String symbol) {
        mSymbol = symbol;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    @Override
    public String toString() {
        return mSymbol;
    }
}
