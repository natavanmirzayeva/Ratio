package com.project.udacity.ratio.util;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import com.project.udacity.ratio.MainActivity;
import com.project.udacity.ratio.R;
import com.project.udacity.ratio.data.db.CollectionContract;

import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class WidgetProvider extends AppWidgetProvider {

    Context mContext;
    final int TYPE_COUNT = 3;
    final int GET_COLLECTION_COUNT = 3;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (Objects.equals(intent.getAction(), AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName thisAppWidget = new ComponentName(context.getPackageName(), WidgetProvider.class.getName());
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

                onUpdate(context, appWidgetManager, appWidgetIds);
            }
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        mContext = context;
        getCollectionFromLocal(appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    public void updateAppWidget(AppWidgetManager appWidgetManager,
                                int appWidgetId, HashMap<String, Object> values, int type) {

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.app_widget);
        views.setTextViewText(R.id.collectionNameText, values.get("original_title") + "");
        views.setTextViewText(R.id.language_txt, values.get("language") + "");
        views.setTextViewText(R.id.releaseDate_txt, values.get("release_date") + "");
        views.setTextViewText(R.id.vote_count_text, values.get("voteCount") + "");
        views.setTextViewText(R.id.vote_average_text, values.get("voteAverage") + "");

        int iconId = 0;
        if (type == 1) {
            iconId = R.drawable.baseline_movie_white_36;
            views.setViewVisibility(R.id.author_txt, View.GONE);
            views.setViewVisibility(R.id.genres_txt, View.GONE);
        }
        if (type == 2) {
            iconId = R.drawable.baseline_book_white_36;
            views.setTextViewText(R.id.author_txt, values.get("authors") + "");
            views.setViewVisibility(R.id.genres_txt, View.GONE);

            String authors = (String) values.get("authors");
            assert authors != null;
            if (!authors.equals("")) {
                views.setTextViewText(R.id.author_txt, authors);
                views.setViewVisibility(R.id.author_txt, View.VISIBLE);
            }

        } else if (type == 3) {
            iconId = R.drawable.baseline_live_tv_white_36;
            views.setViewVisibility(R.id.author_txt, View.GONE);

            String genres = (String) values.get("genres");
            assert genres != null;
            if (!genres.equals("")) {
                views.setTextViewText(R.id.genres_txt, genres);
                views.setViewVisibility(R.id.genres_txt, View.VISIBLE);
            }
        }

        views.setImageViewResource(R.id.collectionIcon, iconId);

        int vote = (int) values.get("vote");
        if (vote != 0) {
            views.setViewVisibility(R.id.your_vote_container, View.VISIBLE);
            views.setTextViewText(R.id.your_vote, vote + "");
        }

        byte[] imagePoster = (byte[]) values.get("posterPath");
        if (imagePoster != null) {
            Bitmap image = BitmapFactory.decodeByteArray(
                    imagePoster,
                    0,
                    imagePoster.length);

            views.setImageViewBitmap(R.id.collectionImage, image);
        }
        // Instruct the widget manager to update the widget
        Intent configIntent = new Intent(mContext, MainActivity.class);
        configIntent.putExtra("id", (String) values.get("id"));

        PendingIntent configPendingIntent = PendingIntent.getActivity(mContext, 0,
                configIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.show_in_app_button, configPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }


    private void getCollectionFromLocal(AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            Random randomGenerator = new Random();
            int type = randomGenerator.nextInt(TYPE_COUNT) + 1;
            int recordIndex = randomGenerator.nextInt(GET_COLLECTION_COUNT) + 1;

            String args[] = {"1", type + ""};
            String selection = "isTopRated=? AND collectionType=?";
            Uri uri = CollectionContract.CollectionEntry.CONTENT_URI_Rate;

            Cursor cursor = mContext.getContentResolver().query(
                    uri,
                    null,
                    selection,
                    args,
                    null);
            assert cursor != null;
            int i = 0;

            HashMap<String, Object> values = new HashMap<>();
            if (cursor.moveToFirst()) {
                do {
                    i++;
                    int index;
                    index = cursor.getColumnIndexOrThrow("id");
                    values.put("id", cursor.getString(index));
                    index = cursor.getColumnIndexOrThrow("title");
                    values.put("original_title", cursor.getString(index));
                    index = cursor.getColumnIndexOrThrow("posterPath");
                    values.put("posterPath", cursor.getBlob(index));
                    index = cursor.getColumnIndexOrThrow("voteAverage");
                    values.put("voteAverage", cursor.getDouble(index));
                    index = cursor.getColumnIndexOrThrow("release_date");
                    values.put("release_date", cursor.getString(index));
                    index = cursor.getColumnIndexOrThrow("language");
                    values.put("language", cursor.getString(index));
                    index = cursor.getColumnIndexOrThrow("voteCount");
                    values.put("voteCount", cursor.getInt(index));
                    index = cursor.getColumnIndexOrThrow("authors");
                    values.put("authors", cursor.getString(index));
                    index = cursor.getColumnIndexOrThrow("genres");
                    values.put("genres", cursor.getString(index));
                    index = cursor.getColumnIndexOrThrow("vote");
                    values.put("vote", cursor.getInt(index));
                }
                while ((cursor.moveToNext() && recordIndex != i));
                cursor.close();
                updateAppWidget(appWidgetManager, appWidgetId, values, type);
            }
        }

    }
}