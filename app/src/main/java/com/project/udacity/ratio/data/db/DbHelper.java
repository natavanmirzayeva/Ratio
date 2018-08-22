package com.project.udacity.ratio.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by mehseti on 30.7.2018.
 */

public class DbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ratio.db";
    private static final int DATABASE_VERSION = 10;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String create_movie_table = "CREATE TABLE " + CollectionContract.CollectionEntry.MOVIE_TABLE_NAME + " (" +
                CollectionContract.CollectionEntry.id + " TEXT PRIMARY KEY," +
                CollectionContract.CollectionEntry.COLUMN_ORIGINAL_TITLE + " TEXT NOT NULL, " +
                CollectionContract.CollectionEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                CollectionContract.CollectionEntry.COLUMN_POSTER_PATH + " BLOB, " +
                CollectionContract.CollectionEntry.COLUMN_BACKDROP_PATH + " BLOB, " +
                CollectionContract.CollectionEntry.COLUMN_VOTE_AVERAGE + " REAL, " +
                CollectionContract.CollectionEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                CollectionContract.CollectionEntry.COLUMN_LANGUAGE + " TEXT NOT NULL, " +
                CollectionContract.CollectionEntry.COLUMN_VOTE_COUNT + " REAL, " +
                CollectionContract.CollectionEntry.COLUMN_VOTE_SCORE + " REAL" +
                ");";

        final String create_book_table = "CREATE TABLE " + CollectionContract.CollectionEntry.BOOK_TABLE_NAME + " (" +
                CollectionContract.CollectionEntry.id + " TEXT PRIMARY KEY," +
                CollectionContract.CollectionEntry.COLUMN_ORIGINAL_TITLE + " TEXT NOT NULL, " +
                CollectionContract.CollectionEntry.COLUMN_POSTER_PATH + " BLOB, " +
                CollectionContract.CollectionEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                CollectionContract.CollectionEntry.COLUMN_VOTE_AVERAGE + " REAL, " +
                CollectionContract.CollectionEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                CollectionContract.CollectionEntry.COLUMN_LANGUAGE + " TEXT NOT NULL, " +
                CollectionContract.CollectionEntry.COLUMN_AUTHORS + " TEXT NOT NULL, " +
                CollectionContract.CollectionEntry.COLUMN_VOTE_COUNT + " REAL, " +
                CollectionContract.CollectionEntry.COLUMN_VOTE_SCORE + " REAL" +
                ");";

        final String create_serial_table = "CREATE TABLE " + CollectionContract.CollectionEntry.SERIAL_TABLE_NAME + " (" +
                CollectionContract.CollectionEntry.id + " TEXT PRIMARY KEY," +
                CollectionContract.CollectionEntry.COLUMN_ORIGINAL_TITLE + " TEXT NOT NULL, " +
                CollectionContract.CollectionEntry.COLUMN_POSTER_PATH + " BLOB, " +
                CollectionContract.CollectionEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                CollectionContract.CollectionEntry.COLUMN_VOTE_AVERAGE + " REAL, " +
                CollectionContract.CollectionEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                CollectionContract.CollectionEntry.COLUMN_LANGUAGE + " TEXT NOT NULL, " +
                CollectionContract.CollectionEntry.COLUMN_GENRES + " TEXT NOT NULL, " +
                CollectionContract.CollectionEntry.COLUMN_VOTE_COUNT + " REAL, " +
                CollectionContract.CollectionEntry.COLUMN_VOTE_SCORE + " REAL" +
                ");";

        final String create_user_table = "CREATE TABLE " + CollectionContract.CollectionEntry.USER_TABLE_NAME + " (" +
                CollectionContract.CollectionEntry.id + " TEXT PRIMARY KEY," +
                CollectionContract.CollectionEntry.COLUMN_USERNAME + " TEXT NOT NULL, " +
                CollectionContract.CollectionEntry.COLUMN_EMAIL + "  TEXT NOT NULL, " +
                CollectionContract.CollectionEntry.COLUMN_ACTIVE + " INTEGER NOT NULL, " +
                CollectionContract.CollectionEntry.COLUMN_GENDER + "  TEXT NOT NULL, " +
                CollectionContract.CollectionEntry.COLUMN_AGE + "  INTEGER," +
                CollectionContract.CollectionEntry.COLUMN_USER_PHOTO + " BLOB " +
                ");";

        final String create_rate_table = "CREATE TABLE " + CollectionContract.CollectionEntry.RATE_TABLE_NAME + " (" +
                CollectionContract.CollectionEntry.id + " TEXT PRIMARY KEY," +
                CollectionContract.CollectionEntry.COLUMN_ORIGINAL_TITLE + " TEXT NOT NULL, " +
                CollectionContract.CollectionEntry.COLUMN_POSTER_PATH + " BLOB, " +
                CollectionContract.CollectionEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                CollectionContract.CollectionEntry.COLUMN_VOTE_AVERAGE + " REAL, " +
                CollectionContract.CollectionEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                CollectionContract.CollectionEntry.COLUMN_LANGUAGE + " TEXT NOT NULL, " +
                CollectionContract.CollectionEntry.COLUMN_COLLECTION_TYPE + " TEXT NOT NULL, " +
                CollectionContract.CollectionEntry.COLUMN_IS_TOP_RATED + " INTEGER, " +
                CollectionContract.CollectionEntry.COLUMN_AUTHORS + " TEXT, " +
                CollectionContract.CollectionEntry.COLUMN_GENRES + " TEXT, " +
                CollectionContract.CollectionEntry.COLUMN_VOTE_COUNT + " REAL, " +
                CollectionContract.CollectionEntry.COLUMN_VOTE_SCORE + " REAL, " +
                CollectionContract.CollectionEntry.COLUMN_BACKDROP_PATH + " BLOB, " +
                CollectionContract.CollectionEntry.COLUMN_VOTE + " REAL, " +
                CollectionContract.CollectionEntry.COLUMN_USER_ID + " TEXT, " +
                CollectionContract.CollectionEntry.COLUMN_IS_MY_COLLECTION + " INTEGER" + ");";

        db.execSQL(create_movie_table);
        db.execSQL(create_book_table);
        db.execSQL(create_serial_table);
        db.execSQL(create_user_table);
        db.execSQL(create_rate_table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CollectionContract.CollectionEntry.MOVIE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CollectionContract.CollectionEntry.BOOK_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CollectionContract.CollectionEntry.SERIAL_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CollectionContract.CollectionEntry.USER_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CollectionContract.CollectionEntry.RATE_TABLE_NAME);
        onCreate(db);

    }
}
