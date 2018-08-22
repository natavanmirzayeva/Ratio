package com.project.udacity.ratio.data.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Objects;

/**
 * Created by mehseti on 30.7.2018.
 */

public class
CollectionContentProvider extends ContentProvider {
    private DbHelper dbHelper;
    public static final int MOVIES = 100;
    public static final int MOVIES_WITH_ID = 101;
    public static final int BOOKS = 102;
    public static final int BOOKS_WITH_ID = 103;
    public static final int TVSeries = 104;
    public static final int TVSeries_WITH_ID = 105;
    public static final int USERS = 106;
    public static final int USERS_WITH_ID = 107;
    public static final int RATE = 108;
    public static final int RATE_WITH_ID = 109;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(CollectionContract.AUTHORITY, CollectionContract.PATH_MOVIES, MOVIES);
        uriMatcher.addURI(CollectionContract.AUTHORITY, CollectionContract.PATH_MOVIES + "/#", MOVIES_WITH_ID);
        uriMatcher.addURI(CollectionContract.AUTHORITY, CollectionContract.PATH_BOOKS, BOOKS);
        uriMatcher.addURI(CollectionContract.AUTHORITY, CollectionContract.PATH_BOOKS + "/#", BOOKS_WITH_ID);
        uriMatcher.addURI(CollectionContract.AUTHORITY, CollectionContract.PATH_TVSeries, TVSeries);
        uriMatcher.addURI(CollectionContract.AUTHORITY, CollectionContract.PATH_TVSeries + "/#", TVSeries_WITH_ID);
        uriMatcher.addURI(CollectionContract.AUTHORITY, CollectionContract.PATH_USER, USERS);
        uriMatcher.addURI(CollectionContract.AUTHORITY, CollectionContract.PATH_USER + "/#", USERS_WITH_ID);
        uriMatcher.addURI(CollectionContract.AUTHORITY, CollectionContract.PATH_RATE, RATE);
        uriMatcher.addURI(CollectionContract.AUTHORITY, CollectionContract.PATH_RATE + "/#", RATE_WITH_ID);
        return uriMatcher;
    }


    @Override
    public boolean onCreate() {
        Context context = getContext();
        dbHelper = new DbHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor returnCursor;
        switch (match) {
            case MOVIES:
                returnCursor = sqLiteDatabase.query(CollectionContract.CollectionEntry.MOVIE_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case MOVIES_WITH_ID:
                returnCursor = sqLiteDatabase.query(
                        CollectionContract.CollectionEntry.MOVIE_TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case BOOKS:
                returnCursor = sqLiteDatabase.query(
                        CollectionContract.CollectionEntry.BOOK_TABLE_NAME,
                        projection, selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case BOOKS_WITH_ID:
                returnCursor = sqLiteDatabase.query(
                        CollectionContract.CollectionEntry.BOOK_TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case TVSeries:
                returnCursor = sqLiteDatabase.query(
                        CollectionContract.CollectionEntry.SERIAL_TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case TVSeries_WITH_ID:
                returnCursor = sqLiteDatabase.query(
                        CollectionContract.CollectionEntry.SERIAL_TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case USERS:
                returnCursor = sqLiteDatabase.query(
                        CollectionContract.CollectionEntry.USER_TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case RATE:
                returnCursor = sqLiteDatabase.query(
                        CollectionContract.CollectionEntry.RATE_TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
        returnCursor.setNotificationUri(Objects.requireNonNull(getContext()).getContentResolver(), uri);
        return returnCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case MOVIES:
                long id = sqLiteDatabase.insert(
                        CollectionContract.CollectionEntry.MOVIE_TABLE_NAME,
                        null,
                        values);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(CollectionContract.CollectionEntry.CONTENT_URI_Movies, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + id);
                }
                break;
            case BOOKS:
                long id_trailer = sqLiteDatabase.insert(
                        CollectionContract.CollectionEntry.BOOK_TABLE_NAME,
                        null,
                        values);
                if (id_trailer > 0) {
                    returnUri = ContentUris.withAppendedId(CollectionContract.CollectionEntry.CONTENT_URI_BOOKS, id_trailer);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + id_trailer);
                }
                break;
            case TVSeries:
                long id_review = sqLiteDatabase.insert(
                        CollectionContract.CollectionEntry.SERIAL_TABLE_NAME,
                        null,
                        values);
                if (id_review > 0) {
                    returnUri = ContentUris.withAppendedId(
                            CollectionContract.CollectionEntry.CONTENT_URI_TVSeries,
                            id_review);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + id_review);
                }
                break;
            case USERS:
                long id_user = sqLiteDatabase.insert(
                        CollectionContract.CollectionEntry.USER_TABLE_NAME,
                        null,
                        values);
                if (id_user > 0) {
                    returnUri = ContentUris.withAppendedId(
                            CollectionContract.CollectionEntry.CONTENT_URI_Users,
                            id_user);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + id_user);
                }
                break;
            case RATE:
                long id_rate = sqLiteDatabase.insert(
                        CollectionContract.CollectionEntry.RATE_TABLE_NAME,
                        null,
                        values);
                if (id_rate > 0) {
                    returnUri = ContentUris.withAppendedId(
                            CollectionContract.CollectionEntry.CONTENT_URI_Rate,
                            id_rate);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + id_rate);
                }
                break;
            default:
                throw new UnsupportedOperationException("Not yet implemented");

        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;

    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case MOVIES:
                rowsDeleted = sqLiteDatabase.delete(
                        CollectionContract.CollectionEntry.MOVIE_TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case RATE:
                rowsDeleted = sqLiteDatabase.delete(
                        CollectionContract.CollectionEntry.RATE_TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case BOOKS:
                rowsDeleted = sqLiteDatabase.delete(
                        CollectionContract.CollectionEntry.BOOK_TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case TVSeries:
                rowsDeleted = sqLiteDatabase.delete(
                        CollectionContract.CollectionEntry.SERIAL_TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        int rowsUpdated = 0;
        switch (match) {
            case RATE:
                rowsUpdated = sqLiteDatabase.update(
                        CollectionContract.CollectionEntry.RATE_TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case USERS:
                rowsUpdated = sqLiteDatabase.update(
                        CollectionContract.CollectionEntry.USER_TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        return rowsUpdated;
    }
}
