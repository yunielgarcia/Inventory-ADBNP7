package com.example.android.productsinventory;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.android.productsinventory.data.ProductContract;
import com.example.android.productsinventory.data.ProductDbHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        displayDbInfo();

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ProductEditor.class);
                startActivity(intent);
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDbInfo();
    }

    private void displayDbInfo(){

        ProductDbHelper mDHelper = new ProductDbHelper(this);
        SQLiteDatabase db = mDHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_NAME_NAME,
                ProductContract.ProductEntry.COLUMN_NAME_PRICE
        };

        Cursor cursor = db.query(
                ProductContract.ProductEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        TextView count_tv = (TextView) findViewById(R.id.count);
        try{
            while (cursor.moveToNext()){
                int nameIdx = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_NAME_NAME);
                String name = cursor.getString(nameIdx);
                count_tv.append( ("\n" + name) );
            }
        }finally {
            cursor.close();
        }


    }
}
