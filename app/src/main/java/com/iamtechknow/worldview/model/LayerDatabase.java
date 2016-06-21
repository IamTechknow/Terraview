package com.iamtechknow.worldview.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;
import java.util.Set;

public class LayerDatabase extends SQLiteOpenHelper {
    //General and layer table constants
    private static final String DB_NAME = "layers.sqlite", TABLE_LAYER = "layer",
            COL_LAYER_TITLE = "title", COL_LAYER_FORMAT = "format", COL_LAYER_MATRIX = "matrix",
            COL_LAYER_SUBTITLE = "subtitle", COL_LAYER_START = "start", COL_LAYER_END = "end",
            COL_LAYER_ISBASE = "isbase", COL_LAYER_ID = "identifier";

    private static LayerDatabase mInstance = null; //Ensure only one instance in app lifecycle

    //Measurement and category table constants
    private static final String TABLE_CAT = "category", TABLE_MEASURE = "measurement", COL_KEY = "key", COL_VAL = "value";
    private static final int VERSION = 1;

    public static LayerDatabase getInstance(Context c) {
        if (mInstance == null)
            mInstance = new LayerDatabase(c);

        return mInstance;
    }

    private LayerDatabase(Context context) {
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

        //Tables with the same format, contains a key string and value concatenated string
        db.execSQL("create table " + TABLE_CAT + " ( key text, value text)");
        db.execSQL("create table " + TABLE_MEASURE + " ( key text, value text )");
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
     * All layers have a title, identifier, format, tile matrix set, but may or may not have the rest
     * @param title The layer's title
     * @param subtitle The layer's subtitle
     * @param identifier The layer's identifier (not always title)
     * @param start The layer's start date in ISO format
     * @param end The layer's end date in ISO format
     * @param format The layer's image format
     * @param matrix The layer's tile matrix set
     * @param st the statement to bind data with
     */
    private void insertLayer(String title, String subtitle, String identifier, String format, String matrix, String start, String end, boolean isBase, SQLiteStatement st) {
        st.bindString(1, title);
        if(subtitle != null)
            st.bindString(2, subtitle);
        st.bindString(3, identifier);
        st.bindString(4, format);
        st.bindString(5, matrix);
        if(start != null)
            st.bindString(6, start);
        if(end != null)
            st.bindString(7, end);
        st.bindLong(8, (isBase ? 1 : 0));

        st.executeInsert(); //returns the entry ID (unused)
        st.clearBindings();
    }

    /**
     * Saves a map of measurements or categories. First obtains the keys for the map, then
     * gets each ArrayList and concatenates them with a semicolon in between to make them splitable
     * @param map The map to save
     * @param table The table to save the map to
     */
    private void insertMap(TreeMap<String, ArrayList<String>> map, String table) {
        SQLiteDatabase DB = getWritableDatabase();
        DB.beginTransaction();
        SQLiteStatement st = DB.compileStatement("insert into " + table + " (key, value) values(?, ?);" );

        Set<String> keys = map.keySet();
        for(String key : keys) {
            String concat_val = "";

            ArrayList<String> val = map.get(key);
            for(String layer_id : val)
                concat_val = concat_val.concat(layer_id + ";");
            st.bindString(1, key);
            st.bindString(2, concat_val);
            st.executeInsert(); //returns the entry ID (unused)
            st.clearBindings();
        }

        DB.setTransactionSuccessful();
        DB.endTransaction();
    }

    public void insertMeasurements(TreeMap<String, ArrayList<String>> map) {
        insertMap(map, TABLE_MEASURE);
    }

    public void insertCategories(TreeMap<String, ArrayList<String>> map) {
        insertMap(map, TABLE_CAT);
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
                    start = c.getString(c.getColumnIndex(COL_LAYER_START));
            boolean isBase = c.getLong(c.getColumnIndex(COL_LAYER_ISBASE)) != 0;
            Layer l = new Layer(id, matrix, format, title, subtitle, end, start, isBase);
            a.add(l);
            c.moveToNext();
        }

        c.close();
        return a;
    }

    /**
     * Returns the hash table form of the SQLite table by splitting the value string, adding them
     * to an ArrayList, and then inserting them to the hash table.
     * @param table The specified table, can be measurements or categories
     * @return the SQLite table in hash table form
     */
    private TreeMap<String, ArrayList<String>> queryMap(String table) {
        TreeMap<String, ArrayList<String>> map = new TreeMap<>();
        Cursor c = getReadableDatabase().query(table, null, null, null, null, null, null);
        c.moveToFirst();

        while(!c.isAfterLast()) {
            String key = c.getString(c.getColumnIndex(COL_KEY)), val = c.getString(c.getColumnIndex(COL_VAL));
            ArrayList<String> list = new ArrayList<>();
            Collections.addAll(list, val.split(";"));
            map.put(key, list);
            c.moveToNext();
        }

        c.close();
        return map;
    }

    public TreeMap<String, ArrayList<String>> queryMeasurements() {
        return queryMap(TABLE_MEASURE);
    }

    public TreeMap<String, ArrayList<String>> queryCategories() {
        return queryMap(TABLE_CAT);
    }
}
