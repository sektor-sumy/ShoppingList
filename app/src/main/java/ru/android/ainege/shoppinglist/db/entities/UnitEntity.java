package ru.android.ainege.shoppinglist.db.entities;

public class UnitEntity {
    private int _id;
    private String mName;

    public UnitEntity(String name){
        mName = name;
    }

    public UnitEntity(int id, String name){
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
}
