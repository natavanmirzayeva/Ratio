package com.project.udacity.ratio.data.db;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by mehseti on 30.7.2018.
 */

public class CollectionContract {
    CollectionContract() {
    }

    ;
    public static final String AUTHORITY = "com.project.udacity.ratio";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_MOVIES = "movies";
    public static final String PATH_BOOKS = "books";
    public static final String PATH_TVSeries = "tvseries";
    public static final String PATH_USER = "users";
    public static final String PATH_RATE = "rate";

    public static class CollectionEntry implements BaseColumns {
        public static final Uri CONTENT_URI_Movies = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();
        public static final Uri CONTENT_URI_BOOKS = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOKS).build();
        public static final Uri CONTENT_URI_TVSeries = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TVSeries).build();
        public static final Uri CONTENT_URI_Users = BASE_CONTENT_URI.buildUpon().appendPath(PATH_USER).build();
        public static final Uri CONTENT_URI_Rate = BASE_CONTENT_URI.buildUpon().appendPath(PATH_RATE).build();
        public static final String MOVIE_TABLE_NAME = "movies";
        public static final String BOOK_TABLE_NAME = "books";
        public static final String SERIAL_TABLE_NAME = "tvseries";
        public static final String USER_TABLE_NAME = "users";
        public static final String RATE_TABLE_NAME = "rate";

        //Movie
        public static final String COLUMN_ORIGINAL_TITLE = "title";
        public static final String COLUMN_POSTER_PATH = "posterPath";
        public static final String COLUMN_BACKDROP_PATH = "backdropPath";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_VOTE_AVERAGE = "voteAverage";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_LANGUAGE = "language";

        //Book
        public static final String COLUMN_AUTHORS = "authors";

        //Serial
        public static final String COLUMN_GENRES = "genres";

        //Rate
        public static final String COLUMN_COLLECTION_ID = "collectionId";
        public static final String COLUMN_USER_ID = "userId";
        public static final String COLUMN_COLLECTION_TYPE = "collectionType";
        public static final String COLUMN_VOTE_COUNT = "voteCount";
        public static final String COLUMN_VOTE_SCORE = "voteScore";
        public static final String COLUMN_VOTE = "vote";
        public static final String COLUMN_IS_TOP_RATED = "isTopRated";
        public static final String COLUMN_IS_MY_COLLECTION = "isMyCollection";


        //User

        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_ACTIVE = "active";
        public static final String COLUMN_GENDER = "gender";
        public static final String COLUMN_AGE = "age";
        public static final String COLUMN_USER_PHOTO = "photo";


        public static final String id = "id";
    }

}
