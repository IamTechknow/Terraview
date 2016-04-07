package com.iamtechknow.worldview.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;

public class LayerDatabase extends SQLiteOpenHelper {
    private static final String DB_NAME = "layers.sqlite", TABLE_LAYER = "layer",
            COL_LAYER_TITLE = "title", COL_LAYER_FORMAT = "format", COL_LAYER_MATRIX = "matrix",
            COL_LAYER_SUBTITLE = "subtitle", COL_LAYER_START = "start", COL_LAYER_END = "end",
            COL_LAYER_ISBASE = "isbase", COL_LAYER_ID = "identifier";
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
        //create the "layer" table, each entry corresponds to POJO field
        db.execSQL("create table " + TABLE_LAYER + " ( title text, subtitle text, identifier text, format text, matrix text, start text, end text, isbase integer)");
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
        SQLiteStatement st = DB.compileStatement("insert into " + TABLE_LAYER + " (title, subtitle, identifier, format, matrix, start, end, isbase) values (?, ?, ?, ?, ?, ?, ?, ?);");
        for(Layer l: layers)
            insertLayer(l.getTitle(), l.getSubtitle(), l.getIdentifier(), l.getFormat(), l.getTileMatrixSet(), l.getStartDate(), l.getEndDate(), l.isBaseLayer(), st);
        DB.setTransactionSuccessful();
        DB.endTransaction();
    }

    /**
     * Insert a layer into the database efficiently
     * @param title The layer's title
     * @param subtitle The layer's subtitle
     * @param identifier The layer's identifier (not always title)
     * @param start The layer's start date in ISO format
     * @param end The layer's end date in ISO format
     * @param format The layer's image format
     * @param matrix The layer's tile matrix metadata
     * @param st the statement to bind data with
     */
    private void insertLayer(String title, String subtitle, String identifier, String format, String matrix, String start, String end, boolean isBase, SQLiteStatement st) {
        st.bindString(1, title);
        st.bindString(2, subtitle);
        st.bindString(3, identifier);
        st.bindString(4, format);
        st.bindString(5, matrix);
        st.bindString(6, start);
        st.bindString(7, end);
        st.bindLong(8, (isBase ? 1 : 0));

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
            String id = c.getString(c.getColumnIndex(COL_LAYER_ID)), matrix = c.getString(c.getColumnIndex(COL_LAYER_MATRIX)),
                    format = c.getString(c.getColumnIndex(COL_LAYER_FORMAT)), title = c.getString(c.getColumnIndex(COL_LAYER_TITLE)),
                    subtitle = c.getString(c.getColumnIndex(COL_LAYER_SUBTITLE)), end = c.getString(c.getColumnIndex(COL_LAYER_END)),
                    start = c.getString(c.getColumnIndex(COL_LAYER_END));
            boolean isBase = c.getLong(c.getColumnIndex(COL_LAYER_ISBASE)) != 0;
            Layer l = new Layer(id, matrix, format, title, subtitle, end, start, isBase);
            a.add(l);
            c.moveToNext();
        }

        c.close();
        return a;
    }
}
