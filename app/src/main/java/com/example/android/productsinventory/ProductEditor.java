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

import static com.example.android.productsinventory.R.id.add_shipment_edit;
import static com.example.android.productsinventory.R.id.sale_amount_et;
import static com.example.android.productsinventory.R.id.shipment_amount_et;
import static com.example.android.productsinventory.R.id.shipment_qty_btn;

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
    private LinearLayout add_shipment_edit;
    private Button register_sale_btn;
    private EditText shipment_amount_et;
    private EditText sale_amount_et;
    private Button shipment_qty_btn;
    private Button save_btn;

    private Uri itemUri;

    //New value to be added in case of edit shipment/sale
    String new_amount_of_products = "0";
    int current_qty_value;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_editor);


        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.product_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mQtyEditText = (EditText) findViewById(R.id.quantity_field);
        shipment_qty_btn = (Button) findViewById(R.id.shipment_qty_btn);
        register_sale_btn = (Button) findViewById(R.id.register_sale_btn);
        add_shipment_edit = (LinearLayout) findViewById(R.id.add_shipment_edit);
        final LinearLayout add_sale_ll = (LinearLayout) findViewById(R.id.add_sale_ll);
        final TextView shipment_cancel_btn =  (TextView) findViewById(R.id.shipment_cancel);
        final TextView sale_cancel_btn =  (TextView) findViewById(R.id.sale_cancel);
        final TextView shipment_ok_btn = (TextView) findViewById(R.id.shipment_ok);
        final TextView sale_ok_btn = (TextView) findViewById(R.id.sale_ok);
        save_btn = (Button) findViewById(R.id.add_product_btn);

        //Examine the intent that was used to lauch this activity,
        // in order to figure out if we're creating a new product or editing an existing one.
        itemUri = getIntent().getData();
        //check for item uri to see which mode "add pet/edit pet"
        updateLayout();

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

        // Save the product
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProduct();
                finish();
            }
        });

        //onclick for add shipment
        shipment_qty_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shipment_qty_btn.setVisibility(View.GONE);
                add_shipment_edit.setVisibility(View.VISIBLE);
            }
        });
        //Cancel addition (shipment)
        shipment_cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add_shipment_edit.setVisibility(View.GONE);
                shipment_qty_btn.setVisibility(View.VISIBLE);
            }
        });

        //onclick for REGISTER SALE
        register_sale_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register_sale_btn.setVisibility(View.GONE);
                add_sale_ll.setVisibility(View.VISIBLE);
            }
        });
        //Cancel register sale (sale)
        sale_cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register_sale_btn.setVisibility(View.VISIBLE);
                add_sale_ll.setVisibility(View.GONE);
            }
        });

        //Ok addition
        shipment_ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shipment_amount_et = (EditText) findViewById(R.id.shipment_amount_et);
                new_amount_of_products = shipment_amount_et.getText().toString().trim();
                int int_new_amount_of_products = Integer.parseInt(new_amount_of_products);
                calculateNewQty(int_new_amount_of_products);
                shipment_amount_et.setText("0");
                add_shipment_edit.setVisibility(View.GONE);
                shipment_qty_btn.setVisibility(View.VISIBLE);
            }
        });

        //Ok sale
        sale_ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sale_amount_et = (EditText) findViewById(R.id.sale_amount_et);
                new_amount_of_products = sale_amount_et.getText().toString().trim();
                int int_new_sale_of_products = Integer.parseInt(new_amount_of_products);
                int_new_sale_of_products *= -1;
                calculateNewQty(int_new_sale_of_products);
                sale_amount_et.setText("0");
                add_sale_ll.setVisibility(View.GONE);
                register_sale_btn.setVisibility(View.VISIBLE);
            }
        });

    }

    private void calculateNewQty(int new_amount){
        Integer final_qty = new_amount + current_qty_value;

        //create the new ContentValue obj
        ContentValues objValues = new ContentValues();
        String name = mNameEditText.getText().toString().trim();
        objValues.put(ProductContract.ProductEntry.COLUMN_NAME_NAME, name);
        String price = mPriceEditText.getText().toString().trim();
        objValues.put(ProductContract.ProductEntry.COLUMN_NAME_PRICE, price);
        String new_quantity = final_qty.toString();
        objValues.put(ProductContract.ProductEntry.COLUMN_NAME_QUANTITY, new_quantity);

        int rowsUpdated = getContentResolver().update(itemUri, objValues, null, null);

        if (rowsUpdated > 0) {
            Toast.makeText(getBaseContext(), getString(R.string.product_updated),
                    Toast.LENGTH_SHORT).show();
            //reset value of shipment and hide view

        } else {
            Toast.makeText(getBaseContext(), getString(R.string.product_updated_failed),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void updateLayout() {
        if (itemUri == null) {
            //hide 'Add shipment' btn
            Button add_shipment_btn = (Button) findViewById(R.id.shipment_qty_btn);
            add_shipment_btn.setVisibility(View.GONE);
            register_sale_btn.setVisibility(View.GONE);

            setTitle(R.string.editor_activity_title_new_product);

        } else {
            //WE ARE EDITING...

            //hide add image btn
            Button load_pict_btn = (Button) findViewById(R.id.buttonLoadPicture);
            load_pict_btn.setVisibility(View.GONE);
            //hide save btn
            save_btn.setVisibility(View.GONE);


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
            //WE ARE UPDATING
            //we have to take into account whether there's a modification to any field ...
            //checking for new quantity value
            if ( !new_amount_of_products.equals("0")){
                int new_amount_int = Integer.parseInt(new_amount_of_products);
                int qty_int = Integer.parseInt(qyt_et);
                int qty_added = new_amount_int + qty_int;
                //update values obj
                values.put(ProductContract.ProductEntry.COLUMN_NAME_QUANTITY, qty_added);
            }
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
            //capture the value in case of updating the quantity
            current_qty_value = cursor.getInt(qtyIdx);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText(String.valueOf(0));
    }
}
