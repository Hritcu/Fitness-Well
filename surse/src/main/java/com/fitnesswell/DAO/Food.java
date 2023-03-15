package com.fitnesswell.DAO;

import com.fitnesswell.enums.Gender;
import com.fitnesswell.enums.SizeUnit;

import java.util.Date;

public class Food {
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    private long id;

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getName() {
        return mName;
    }

    private String mName = "";

    public int getCalories() {
        return mCalories;
    }

    public void setCalories(int mCalories) {
        this.mCalories = mCalories;
    }

    private int mCalories = 0;

    public int getProtein() {
        return mProtein;
    }

    public void setProtein(int mProtein) {
        this.mProtein = mProtein;
    }

    private int mProtein = 0;

    public String getPhoto() {
        return mPhoto;
    }

    public void setPhoto(String mPhoto) {
        this.mPhoto = mPhoto;
    }

    public int getCarb() {
        return mCarb;
    }

    private String mPhoto = "";

    public void setCarb(int mCarb) {
        this.mCarb = mCarb;
    }

    private int mCarb = 0;

    public int getFat() {
        return mFat;
    }

    public void setFat(int mFat) {
        this.mFat = mFat;
    }

    private int mFat = 0;

    public Food(long mId, Date mDate, String pName, int pCalories, int pProtein, String pPhoto, int pCarb, int pFat ) {
        //super();
        this.id = mId;
        this.mCalories = pCalories;
        this.mName = pName;
        this.mPhoto = pPhoto;
        this.mProtein = pProtein;
        this.mCarb = pCarb;
        this.mFat = pFat;
    }


}
