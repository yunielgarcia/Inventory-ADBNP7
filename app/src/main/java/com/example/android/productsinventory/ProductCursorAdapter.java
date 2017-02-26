package com.example.android.productsinventory;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.productsinventory.data.ProductContract;

import static android.R.attr.id;

/**
 * Created by ygarcia on 2/17/2017.
 */

public class ProductCursorAdapter extends CursorAdapter {

    private ContentResolver mContent;

    private ContentValues mValues;
    private Context mCtx;

    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        mContent = context.getContentResolver();
        mCtx = context;
    }


    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    /* The newView method is used to inflate a new view and return it,
     you don't bind any data to the view at this point.*/
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView tvName = (TextView) view.findViewById(R.id.name);
        TextView tvPrice = (TextView) view.findViewById(R.id.price_li);
        TextView tvQty = (TextView) view.findViewById(R.id.li_quantity);

        // Extract properties from cursor
        final String name = cursor.getString(cursor.getColumnIndexOrThrow(ProductContract.ProductEntry.COLUMN_NAME_NAME));
        final Integer price = cursor.getInt(cursor.getColumnIndexOrThrow(ProductContract.ProductEntry.COLUMN_NAME_PRICE));
        final Integer qty = cursor.getInt(cursor.getColumnIndexOrThrow(ProductContract.ProductEntry.COLUMN_NAME_QUANTITY));
        final Integer item_id = cursor.getInt(cursor.getColumnIndexOrThrow(ProductContract.ProductEntry._ID));

        // Populate fields with extracted properties
        tvName.setText(name);
        tvPrice.setText(price.toString());
        tvQty.setText(qty.toString());

        //setting onClick on buy button
        Button buy_btn = (Button) view.findViewById(R.id.buy_btn);
        buy_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri itemUri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, item_id);
                int new_quantity;

                if (qty > 0) {
                    new_quantity = qty - 1;
                    mValues = new ContentValues();
                    mValues.put(ProductContract.ProductEntry.COLUMN_NAME_NAME, name);
                    mValues.put(ProductContract.ProductEntry.COLUMN_NAME_PRICE, price);
                    mValues.put(ProductContract.ProductEntry.COLUMN_NAME_QUANTITY, new_quantity);

                    mContent.update(itemUri, mValues, null, null);
                }else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(mCtx, R.string.zero_products,
                            Toast.LENGTH_SHORT).show();
                }


            }
        });
    }
}
