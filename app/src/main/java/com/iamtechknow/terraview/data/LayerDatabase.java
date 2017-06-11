package com.iamtechknow.terraview.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.iamtechknow.terraview.model.Layer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;
import java.util.Set;

import static android.app.SearchManager.*;

public class LayerDatabase extends SQLiteOpenHelper {
    //General and layer table constants
    private static final String DB_NAME = "layers.sqlite", TABLE_LAYER = "layer", TABLE_SEARCH = "search",
            COL_LAYER_TITLE = "title", COL_LAYER_FORMAT = "format", COL_LAYER_MATRIX = "matrix",
            COL_LAYER_SUBTITLE = "subtitle", COL_LAYER_START = "start", COL_LAYER_END = "endDate",
            COL_LAYER_ISBASE = "isbase", COL_LAYER_ID = "identifier", COL_LAYER_META = "meta",
            COL_LAYER_PALETTE = "palette", COL_ID = "_id",
            LAYER_COLS_NEW = "( title text, subtitle text, identifier text, format text, matrix text, start text, endDate text, meta text, palette text, isbase integer)",
            LAYER_COLS = "(title, subtitle, identifier, format, matrix, start, endDate, meta, palette, isbase)";

    private static LayerDatabase mInstance = null; //Ensure only one instance in app lifecycle

    //Measurement and category table constants
    private static final String TABLE_CAT = "category", TABLE_MEASURE = "measurement", COL_NAME = "name", COL_VAL = "value",
                                NAME_COLS_NEW = "( name text, value text)", NAME_COLS = "(name, value)";
    private static final int VERSION = 2;

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
        db.execSQL("create table " + TABLE_LAYER + LAYER_COLS_NEW);

        //Tables with the same format, contains a key string and value concatenated string
        db.execSQL("create table " + TABLE_CAT + NAME_COLS_NEW);
        db.execSQL("create table " + TABLE_MEASURE + NAME_COLS_NEW);

        //Table for searching
        db.execSQL(String.format("create table %s ( _id integer primary key autoincrement, %s text, %s text)", TABLE_SEARCH, SUGGEST_COLUMN_TEXT_1, SUGGEST_COLUMN_INTENT_EXTRA_DATA));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // implement schema changes and data massage here when upgrading
        switch(oldVersion) {
            case 1: //Create new tables to change key words used as columns: desc -> meta, end -> endDate
                String layerTemp = "layer_temp", catTemp = "cat_temp", measureTemp = "measure_temp";
                String alter = "ALTER TABLE %s RENAME TO %s", create = "CREATE TABLE %s %s", insert = "INSERT INTO %s %s SELECT %s from %s";
                String old_cols = "title, subtitle, identifier, format, matrix, start, end, desc, palette, isbase";
                db.beginTransaction();

                db.execSQL(String.format(alter, TABLE_LAYER, layerTemp));
                db.execSQL(String.format(alter, TABLE_CAT, catTemp));
                db.execSQL(String.format(alter, TABLE_MEASURE, measureTemp));
                db.execSQL(String.format(create, TABLE_LAYER, LAYER_COLS_NEW));
                db.execSQL(String.format(create, TABLE_CAT, NAME_COLS_NEW));
                db.execSQL(String.format(create, TABLE_MEASURE, NAME_COLS_NEW));
                db.execSQL(String.format(insert, TABLE_LAYER, LAYER_COLS, old_cols, layerTemp));
                db.execSQL(String.format(insert, TABLE_CAT, NAME_COLS, "key, value", catTemp));
                db.execSQL(String.format(insert, TABLE_MEASURE, NAME_COLS, "key, value", measureTemp));

                for(String table : new String[] {layerTemp, catTemp, measureTemp})
                    db.execSQL(String.format("DROP TABLE %s", table));

                db.setTransactionSuccessful();
                db.endTransaction();
        }
    }

    /**
     * Clear all database tables, used for getting data from remote source
     */
    public void reset() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        for(String table : new String[] {TABLE_MEASURE, TABLE_CAT, TABLE_SEARCH, TABLE_LAYER})
            db.execSQL(String.format("DELETE FROM %s", table));
        db.execSQL("DELETE FROM sqlite_sequence where name='search'");

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    /**
     * Inserts a layer into the database
     * @param layers the Layers to store
     */
    public void insertLayers(ArrayList<Layer> layers) {
        SQLiteDatabase DB = getWritableDatabase();
        DB.beginTransaction();
        SQLiteStatement st = DB.compileStatement("insert into " + TABLE_LAYER + " " + LAYER_COLS + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
        SQLiteStatement st_search = DB.compileStatement(String.format("insert into %s (%s, %s) values (?, ?);", TABLE_SEARCH, SUGGEST_COLUMN_TEXT_1, SUGGEST_COLUMN_INTENT_EXTRA_DATA));
        for(Layer l: layers)
            insertLayer(l.getTitle(), l.getSubtitle(), l.getIdentifier(), l.getFormat(), l.getTileMatrixSet(), l.getStartDateRaw(), l.getEndDateRaw(), l.getDescription(), l.getPalette(), l.isBaseLayer(), st, st_search);
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
     * @param endDate The layer's end date in ISO format
     * @param meta The layer's metadata web page
     * @param palette The layer's palette id if any
     * @param format The layer's image format
     * @param matrix The layer's tile matrix set
     * @param st The statement to bind data with
     * @param search The search table statement
     */
    private void insertLayer(String title, String subtitle, String identifier, String format, String matrix, String start, String endDate, String meta, String palette, boolean isBase, SQLiteStatement st, SQLiteStatement search) {
        st.bindString(1, title);
        if(subtitle != null)
            st.bindString(2, subtitle);
        st.bindString(3, identifier);
        st.bindString(4, format);
        st.bindString(5, matrix);
        if(start != null)
            st.bindString(6, start);
        if(endDate != null)
            st.bindString(7, endDate);
        if(meta != null)
            st.bindString(8, meta);
        if(palette != null)
            st.bindString(9, palette);
        st.bindLong(10, (isBase ? 1 : 0));

        st.executeInsert(); //returns the entry ID (unused)
        st.clearBindings();

        search.bindString(1, title);
        search.bindString(2, identifier);
        search.executeInsert();
        search.clearBindings();
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
        SQLiteStatement st = DB.compileStatement("insert into " + table + " " + NAME_COLS + " values(?, ?);" );

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
                    subtitle = c.getString(c.getColumnIndex(COL_LAYER_SUBTITLE)),
                    end = c.isNull(c.getColumnIndex(COL_LAYER_END)) ? null : c.getString(c.getColumnIndex(COL_LAYER_END)),
                    start = c.isNull(c.getColumnIndex(COL_LAYER_START)) ? null : c.getString(c.getColumnIndex(COL_LAYER_START)),
                    desc = c.isNull(c.getColumnIndex(COL_LAYER_META)) ? null : c.getString(c.getColumnIndex(COL_LAYER_META)),
                    palette = c.isNull(c.getColumnIndex(COL_LAYER_PALETTE)) ? null : c.getString(c.getColumnIndex(COL_LAYER_PALETTE));
            boolean isBase = c.getLong(c.getColumnIndex(COL_LAYER_ISBASE)) != 0;
            Layer l = new Layer(id, matrix, format, title, subtitle, end, start, desc, palette, isBase);
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
            String key = c.getString(c.getColumnIndex(COL_NAME)), val = c.getString(c.getColumnIndex(COL_VAL));
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

    /**
     * Called by LayerProvider to query the database with the argument for autocomplete search
     * @param arg The String query
     * @return A cursor that contains the titles for the autocomplete query
     */
    public Cursor searchQuery(String arg) {
        String sql = String.format("SELECT %s, %s, %s FROM %s WHERE %s like '%s%%'", COL_ID, SUGGEST_COLUMN_TEXT_1, SUGGEST_COLUMN_INTENT_EXTRA_DATA, TABLE_SEARCH, SUGGEST_COLUMN_TEXT_1, arg);
        Cursor c = getReadableDatabase().rawQuery(sql, null);

        if (c == null)
            return null;
        else if (!c.moveToFirst()) {
            c.close();
            return null;
        }
        return c;
    }
}
