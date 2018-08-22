package com.project.udacity.ratio.util;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.project.udacity.ratio.R;
import com.project.udacity.ratio.models.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by mehseti on 3.5.2018.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private List<Movie> movies;
    private List<byte[]> imageBlobs;
    private boolean isRatioCollection;
    private boolean isOfflineData;
    private boolean isOfflineTopRated;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(Movie movie, byte[] image, ImageView movieImage);
    }

    private OnItemClickListener listener;

    public MovieAdapter(@NonNull List<Movie> movies, List<byte[]> imageBlobs, boolean isRatioCollection, boolean isOfflineData, boolean isOfflineTopRated, Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;

        this.movies = movies;
        this.isRatioCollection = isRatioCollection;
        this.isOfflineData = isOfflineData;
        this.isOfflineTopRated = isOfflineTopRated;
        this.imageBlobs = imageBlobs;
    }

    class MovieViewHolder extends RecyclerView.ViewHolder {
        private ImageView movieImage;
        private TextView movieName;
        private CardView movieContainer;

        MovieViewHolder(View v, final Context context) {
            super(v);
            movieName = v.findViewById(R.id.category_item_name);
            movieImage = v.findViewById(R.id.category_item_image);
            movieContainer = v.findViewById(R.id.category_item_container);
        }

        void bind(final Movie movie, final byte[] image, final OnItemClickListener listener) {
            movieContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(movie, image, movieImage);
                }
            });
        }
    }

    @Override
    public MovieAdapter.MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new MovieViewHolder(view, parent.getContext());
    }

    @Override
    public void onBindViewHolder(MovieAdapter.MovieViewHolder holder, int position) {
        byte[] imageBlob = null;
        if (imageBlobs != null) {
            imageBlob = imageBlobs.get(position);
        }

        holder.bind(movies.get(position), imageBlob, listener);
        Movie movie = movies.get(position);

        ViewCompat.setTransitionName(holder.movieImage, movie.getTitle());

        if (!isRatioCollection && !isOfflineData && !isOfflineTopRated) {
            Picasso.get().load(ApiVariables.imagePathForMovie + movie.getPosterPath()).into(holder.movieImage);

        } else {

            if (imageBlob != null) {
                Drawable image = new BitmapDrawable(
                        context.getResources(),
                        BitmapFactory.decodeByteArray(imageBlob,
                                0,
                                imageBlob.length)
                );
                holder.movieImage.setImageDrawable(image);
            }
        }
        holder.movieName.setText(movie.getTitle());
        holder.movieImage.setContentDescription(movie.getTitle());
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }

}