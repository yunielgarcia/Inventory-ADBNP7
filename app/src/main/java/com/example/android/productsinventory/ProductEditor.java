package com.example.android.productsinventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.productsinventory.data.ProductContract;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static android.R.attr.data;
import static android.R.attr.order;
import static com.example.android.productsinventory.R.id.add_shipment_edit;
import static com.example.android.productsinventory.R.id.imageView;
import static com.example.android.productsinventory.R.id.sale_amount_et;
import static com.example.android.productsinventory.R.id.shipment_amount_et;
import static com.example.android.productsinventory.R.id.shipment_qty_btn;

public class ProductEditor extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static String PRODUCT_SUPPLIER_EMAIL = "supplierEmail@email.com";
    private static int PICK_IMAGE_REQUEST = 1;
    private Bitmap imgBitmap = null;
    private ImageView imageView;

    private static final int EXISTING_PRODUCT_LOADER = 1;
    private static final String[] PROJECTION = {
            ProductContract.ProductEntry._ID,
            ProductContract.ProductEntry.COLUMN_NAME_NAME,
            ProductContract.ProductEntry.COLUMN_NAME_PRICE,
            ProductContract.ProductEntry.COLUMN_NAME_QUANTITY,
            ProductContract.ProductEntry.COLUMN_NAME_IMG
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
        final TextView shipment_cancel_btn = (TextView) findViewById(R.id.shipment_cancel);
        final TextView sale_cancel_btn = (TextView) findViewById(R.id.sale_cancel);
        final TextView shipment_ok_btn = (TextView) findViewById(R.id.shipment_ok);
        final TextView sale_ok_btn = (TextView) findViewById(R.id.sale_ok);
        save_btn = (Button) findViewById(R.id.add_product_btn);
        imageView = (ImageView) findViewById(R.id.imageView);

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

        //order more from supplier
        Button order = (Button) findViewById(R.id.order_supplier);
        order.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (itemUri != null) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{PRODUCT_SUPPLIER_EMAIL});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "MORE ITEMS NEEDED");
                    intent.putExtra(Intent.EXTRA_TEXT, "Product name: " + mNameEditText.getText().toString());
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            }
        });

    }

    private void calculateNewQty(int new_amount) {
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
            //WE ARE INSERTING...

            //hide 'Add shipment' btn
            Button add_shipment_btn = (Button) findViewById(R.id.shipment_qty_btn);
            add_shipment_btn.setVisibility(View.GONE);
            register_sale_btn.setVisibility(View.GONE);
            //hide order from supplier
            Button supplier_btn = (Button) findViewById(R.id.order_supplier);
            supplier_btn.setVisibility(View.GONE);


            setTitle(R.string.editor_activity_title_new_product);

            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();

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
        byte[] dataImg;
        if (imgBitmap != null) {
            dataImg = getBitmapAsByteArray(imgBitmap);
        } else {
            dataImg = null;
        }

        //if everything is empty let's just end the activity without inserting
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(price_et) || TextUtils.isEmpty(qyt_et) || imgBitmap == null) {
            Toast.makeText(this, getString(R.string.empty_fields),
                    Toast.LENGTH_SHORT).show();
            return;
        }


        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_NAME_NAME, name);
        values.put(ProductContract.ProductEntry.COLUMN_NAME_PRICE, price_et);
        values.put(ProductContract.ProductEntry.COLUMN_NAME_QUANTITY, qyt_et);
        values.put(ProductContract.ProductEntry.COLUMN_NAME_IMG, dataImg);

        if (itemUri == null) {
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
                finish();
            }
        } else {
            //WE ARE UPDATING
            //we have to take into account whether there's a modification to any field ...
            //checking for new quantity value
            if (!new_amount_of_products.equals("0")) {
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

    private byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                imgBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                //Log.d("ImageBitmap", String.valueOf(imgBitmap));

                imageView.setImageBitmap(imgBitmap);
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
        int imgIdx = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_NAME_IMG);

        if (cursor.moveToFirst()) {
            mNameEditText.setText(cursor.getString(nameIdx));
            mPriceEditText.setText(String.valueOf(cursor.getInt(priceIdx)));
            mQtyEditText.setText(String.valueOf(cursor.getInt(qtyIdx)));
            //capture the value in case of updating the quantity
            current_qty_value = cursor.getInt(qtyIdx);
            //retrive image
            byte[] imgByte = cursor.getBlob(imgIdx);
            imageView.setImageBitmap(getImgBitmap(imgByte));
        }
    }

    private Bitmap getImgBitmap(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText(String.valueOf(0));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    //Then (after invalidateOptionsMenu), in onPrepareOptionsMenu will get called and you can modify the Menu object
    // by hiding the delete menu option if it’s a new pet.
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (itemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        int rowsDeleted = getContentResolver().delete(itemUri, null, null);
        if (rowsDeleted > 0) {
            finish();
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                    Toast.LENGTH_SHORT).show();
        }
    }

}
