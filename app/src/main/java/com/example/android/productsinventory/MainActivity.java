package com.example.android.productsinventory;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.productsinventory.data.ProductContract;
import com.example.android.productsinventory.data.ProductDbHelper;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int PRODUCT_LOADER = 0;

    // Define a projection that specifies which columns from the database
    // you will actually use after this query.
    static final String[] PROJECTION = {
            ProductContract.ProductEntry._ID,
            ProductContract.ProductEntry.COLUMN_NAME_NAME,
            ProductContract.ProductEntry.COLUMN_NAME_PRICE,};

    // This is the Adapter being used to display the list's data
    ProductCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //displayDbInfo();

        ListView prodListView = (ListView) findViewById(R.id.list);
        mCursorAdapter = new ProductCursorAdapter(this, null);
        prodListView.setAdapter(mCursorAdapter);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ProductEditor.class);
                startActivity(intent);
            }
        });

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
       // displayDbInfo();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(this, ProductContract.ProductEntry.CONTENT_URI,
                PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mCursorAdapter.swapCursor(null);
    }

//    private void displayDbInfo(){
//
////        ProductDbHelper mDHelper = new ProductDbHelper(this);
////        SQLiteDatabase db = mDHelper.getReadableDatabase();
//
//        // Define a projection that specifies which columns from the database
//        // you will actually use after this query.
//        String[] projection = {
//                ProductContract.ProductEntry._ID,
//                ProductContract.ProductEntry.COLUMN_NAME_NAME,
//                ProductContract.ProductEntry.COLUMN_NAME_PRICE
//        };
//
//        Cursor cursor = getContentResolver().query(ProductContract.ProductEntry.CONTENT_URI, projection, null, null, null);
//
//        TextView count_tv = (TextView) findViewById(R.id.count);
//        try{
//            while (cursor.moveToNext()){
//                int nameIdx = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_NAME_NAME);
//                String name = cursor.getString(nameIdx);
//                count_tv.append( ("\n" + name) );
//            }
//        }finally {
//            cursor.close();
//        }
//
//
//    }
}
