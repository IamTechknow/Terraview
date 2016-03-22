package com.iamtechknow.worldview.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;

public class LayerDatabase extends SQLiteOpenHelper {
    private static final String DB_NAME = "layers.sqlite", TABLE_LAYER = "layer",
            COL_LAYER_TITLE = "title", COL_LAYER_FORMAT = "format", COL_LAYER_MATRIX = "matrix";
    private static final int VERSION = 1;

    public LayerDatabase(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    /**
     * Upon creation of the database, a table is made to represent serialized Layers
     * @param db The database created
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        //create the "trail" table, each entry has a title, image format, and tile matrix set
        db.execSQL("create table " + TABLE_LAYER + " ( title text, format text, matrix text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // implement schema changes and data massage here when upgrading
    }

    /**
     * Inserts a layer into the database
     * @param layers the Layers to store
     */
    public void insertLayers(ArrayList<Layer> layers) {
        SQLiteDatabase DB = getWritableDatabase();
        DB.beginTransaction();
        SQLiteStatement st = DB.compileStatement("insert into " + TABLE_LAYER + " (title, format, matrix) values (?, ?, ?);");
        for(Layer l: layers)
            insertLayer(l.getTitle(), l.getFormat(), l.getTileMatrixSet(), st);
        DB.setTransactionSuccessful();
        DB.endTransaction();
    }

    /**
     * Insert a layer into the database efficiently
     * @param title The layer's title
     * @param format The layer's image format
     * @param matrix The layer's tile matrix metadata
     * @param st the statement to bind data with
     */
    private void insertLayer(String title, String format, String matrix, SQLiteStatement st) {
        st.bindString(1, title);
        st.bindString(2, format);
        st.bindString(3, matrix);

        st.executeInsert(); //returns the entry ID (unused)
        st.clearBindings();
    }

    /**
     * Goes through the leg work of parsing all the Layers from the database,
     * creating Layer objects for each row, then populating them
     * @return ArrayList of the Layers
     */
    public ArrayList<Layer> queryLayers() {
        ArrayList<Layer> a = new ArrayList<>();
        Cursor c = getReadableDatabase().query(TABLE_LAYER, null, null, null, null, null, null);
        c.moveToFirst();

        while(!c.isAfterLast()) {
            Layer l = new Layer(c.getString(c.getColumnIndex(COL_LAYER_TITLE)), c.getString(c.getColumnIndex(COL_LAYER_MATRIX)), c.getString(c.getColumnIndex(COL_LAYER_FORMAT)));
            a.add(l);
            c.moveToNext();
        }

        c.close();
        return a;
    }
}
