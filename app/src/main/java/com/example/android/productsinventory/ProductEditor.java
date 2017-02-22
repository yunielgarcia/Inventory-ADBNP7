package com.example.android.productsinventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.productsinventory.data.ProductContract;
import com.example.android.productsinventory.data.ProductDbHelper;

import java.io.IOException;

import static android.R.attr.data;
import static com.example.android.productsinventory.MainActivity.PROJECTION;
import static com.example.android.productsinventory.R.id.fab;

public class ProductEditor extends AppCompatActivity  implements LoaderManager.LoaderCallbacks<Cursor> {

    private static int PICK_IMAGE_REQUEST = 1;
    private static final int EXISTING_PRODUCT_LOADER = 1;
    private static final String[] PROJECTION = {
            ProductContract.ProductEntry._ID,
            ProductContract.ProductEntry.COLUMN_NAME_NAME,
            ProductContract.ProductEntry.COLUMN_NAME_PRICE,
    };

    //private ProductDbHelper mDbHelper;
    private EditText mNameEditText;
    private EditText mPriceEditText;

    private Uri itemUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_editor);

        //Examine the intent that was used to lauch this activity,
        // in order to figure out if we're creating a new product or editing an existing one.
        itemUri = getIntent().getData();
        //check for item uri to see which mode "add pet/edit pet"
        changeTitle();


        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.product_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);

        //Load the picture onClick
        Button load_img_btn = (Button) findViewById(R.id.buttonLoadPicture);
        load_img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create intent to Open Image applications like Gallery, Google Photos
                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        // Setup FAB to open EditorActivity
        Button save_btn = (Button) findViewById(R.id.add_product_btn);
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProduct();
                finish();
            }
        });
    }

    private void changeTitle() {
        if (itemUri == null) {
            setTitle(R.string.editor_activity_title_new_product);
        } else {
            setTitle(R.string.editor_activity_title_edit_product);
            /*The difference from when we last used a CursorLoader is that instead of taking the
             cursor and putting it into a CursorAdapter, we’ll take all of the items from the cursor
             and use them to populate the EditTextFields. We will use almost the same steps as before,
             except that when we make the loader, the uri will be for one pet, and not all of the pets.*/
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }
    }

    private void saveProduct() {
        // Gets the data repository in write mode
        //SQLiteDatabase db = mDbHelper.getWritableDatabase();

        //name
        String name = mNameEditText.getText().toString().trim();
        String price_et = mPriceEditText.getText().toString().trim();
        Integer price;
        if (TextUtils.isEmpty(price_et)) {
            price = 0;
        } else {
            price = Integer.parseInt(price_et);
        }
        //if everything is empty let's just end the activity without inserting
        if (TextUtils.isEmpty(name) && TextUtils.isEmpty(price_et) && itemUri == null) {
            finish();
            return;
        }

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_NAME_NAME, name);
        values.put(ProductContract.ProductEntry.COLUMN_NAME_PRICE, price);

        if(itemUri == null){
            Uri newUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);
            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_prod_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_prod_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }else {
            //we're updating product
            int rowsUpdated = getContentResolver().update(itemUri, values, null, null);
            if (rowsUpdated > 0) {
                Toast.makeText(this, getString(R.string.product_updated),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.product_updated_failed),
                        Toast.LENGTH_SHORT).show();
            }
        }



        //Log.i("ROWS_INNSERTED", newUri.toString());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));

                ImageView imageView = (ImageView) findViewById(R.id.imageView);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(this, itemUri,
                PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        int nameIdx = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_NAME_NAME);
        int priceIdx = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_NAME_PRICE);

        if (cursor.moveToFirst()) {
            mNameEditText.setText(cursor.getString(nameIdx));
            mPriceEditText.setText(String.valueOf(cursor.getInt(priceIdx)));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText(String.valueOf(0));
    }
}
