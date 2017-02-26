package com.example.android.productsinventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.productsinventory.data.ProductContract;

import java.io.IOException;

public class ProductEditor extends AppCompatActivity  implements LoaderManager.LoaderCallbacks<Cursor> {

    private static int PICK_IMAGE_REQUEST = 1;
    private static final int EXISTING_PRODUCT_LOADER = 1;
    private static final String[] PROJECTION = {
            ProductContract.ProductEntry._ID,
            ProductContract.ProductEntry.COLUMN_NAME_NAME,
            ProductContract.ProductEntry.COLUMN_NAME_PRICE,
            ProductContract.ProductEntry.COLUMN_NAME_QUANTITY,
    };

    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQtyEditText;

    private Uri itemUri;

    //New value to be added in case of edit shipment/sale
    String new_amount_of_products = "0";


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
        mQtyEditText = (EditText) findViewById(R.id.quantity_field);

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

        //Add shipment btn
        final Button shipment_qty_btn = (Button) findViewById(R.id.shipment_qty_btn);
        final LinearLayout add_shipment_edit = (LinearLayout) findViewById(R.id.add_shipment_edit);
        final TextView shipment_cancel_btn = (TextView) findViewById(R.id.shipment_cancel);
        final TextView shipment_ok_btn = (TextView) findViewById(R.id.shipment_ok);

        shipment_qty_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shipment_qty_btn.setVisibility(View.GONE);
                add_shipment_edit.setVisibility(View.VISIBLE);
            }
        });
        //Cancel addition
        shipment_cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add_shipment_edit.setVisibility(View.GONE);
                shipment_qty_btn.setVisibility(View.VISIBLE);
            }
        });
        //Ok addition
        shipment_ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText shipment_amount_et = (EditText) findViewById(R.id.shipment_amount_et);
                new_amount_of_products = shipment_amount_et.getText().toString().trim();
            }
        });
    }

    private void changeTitle() {
        if (itemUri == null) {
            //hide 'Add shipment' btn
            Button add_shipment_btn = (Button) findViewById(R.id.shipment_qty_btn);
            add_shipment_btn.setVisibility(View.GONE);

            setTitle(R.string.editor_activity_title_new_product);

        } else {

            setTitle(R.string.editor_activity_title_edit_product);
            //Disable each edit field
            mNameEditText = (EditText) findViewById(R.id.product_name);
            mNameEditText.setFocusable(false);
            mNameEditText.setClickable(false);

            mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
            mPriceEditText.setFocusable(false);
            mPriceEditText.setClickable(false);

            mQtyEditText = (EditText) findViewById(R.id.quantity_field);
            mQtyEditText.setFocusable(false);
            mQtyEditText.setClickable(false);

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
        //TODO: tratar the update el product una vez ok , so hagamos un update dl producto con la nueva cantidad entrada q esta en new_amount_of_products

        //name
        String name = mNameEditText.getText().toString().trim();
        String price_et = mPriceEditText.getText().toString().trim();
        String qyt_et = mQtyEditText.getText().toString().trim();



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
        values.put(ProductContract.ProductEntry.COLUMN_NAME_QUANTITY, qyt_et);

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
        int qtyIdx = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_NAME_QUANTITY);

        if (cursor.moveToFirst()) {
            mNameEditText.setText(cursor.getString(nameIdx));
            mPriceEditText.setText(String.valueOf(cursor.getInt(priceIdx)));
            mQtyEditText.setText(String.valueOf(cursor.getInt(qtyIdx)));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText(String.valueOf(0));
    }
}
