package com.example.android.productsinventory.data;

import android.provider.BaseColumns;

/**
 * Created by Yggarcia on 2/9/2017.
 */

public class ProductContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private ProductContract() {}

    /* Inner class that defines the table contents */
    public static class ProductEntry implements BaseColumns {

        public static final String TABLE_NAME = "product";

        public final static String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_PRICE = "price";
        public static final String COLUMN_NAME_PHOTO = "photo";
    }
}
