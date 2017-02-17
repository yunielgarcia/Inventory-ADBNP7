package com.example.android.productsinventory;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import static com.example.android.productsinventory.R.id.fab;

public class ProductEditor extends AppCompatActivity {

    private static int PICK_IMAGE_REQUEST = 1;

    //private ProductDbHelper mDbHelper;
    private EditText mNameEditText;
    private EditText mPriceEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_editor);

        //mDbHelper = new ProductDbHelper(this);


        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.produc_name);
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

    private void saveProduct(){
        // Gets the data repository in write mode
        //SQLiteDatabase db = mDbHelper.getWritableDatabase();

        //name
        String name = mNameEditText.getText().toString().trim();
        String price_et = mPriceEditText.getText().toString().trim();
        Integer price = Integer.parseInt(price_et);

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_NAME_NAME, name);
        values.put(ProductContract.ProductEntry.COLUMN_NAME_PRICE, price);

//        // Insert the new row, returning the primary key value of the new row
//        Long newRowId = db.insert(ProductContract.ProductEntry.TABLE_NAME, null, values);

        Uri newUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);

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
}
