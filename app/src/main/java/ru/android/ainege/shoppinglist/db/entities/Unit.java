package ru.android.ainege.shoppinglist.db.entities;

import java.io.Serializable;

public class Unit implements Serializable {
    private long mId;
    private String mName;

    public Unit(String name) {
        mName = name;
    }

    public Unit(long id, String name) {
        this(name);
        this.mId = id;
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

    @Override
    public String toString() {
        return mName;
    }
}
