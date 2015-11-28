package ru.android.ainege.shoppinglist.db.entities;

public class Currency extends Dictionary {
    private String mSymbol;

    public Currency(String name, String symbol) {
        super(name);
        mSymbol = symbol;
    }

    public Currency(long id, String name, String symbol) {
        super(id, name);
        mSymbol = symbol;
    }

    public String getSymbol() {
        return mSymbol;
    }

    public void setSymbol(String symbol) {
        mSymbol = symbol;
    }
}
