package com.example.android.productsinventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        prodListView.setEmptyView(emptyView);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ProductEditor.class);
                startActivity(intent);
            }
        });

        prodListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //from the content URI that represents the specific product thath was clicked on,
                //by appending the "id" (passed as input to this method )
                //for example, the URI would be "content://com.example.android.products/products/2"
                //if the product with ID 2 was clicked on.
                Uri itemUri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, id);
                //Create new intent to go to {@link EditorActivity}
                Intent editorIntent = new Intent(MainActivity.this, ProductEditor.class);
                //Set the URI on the data field of the intent
                editorIntent.setData(itemUri);
                //Launch the activity to display the data for the current product
                startActivity(editorIntent);
            }
        });

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
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
}
