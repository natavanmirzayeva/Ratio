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
import com.project.udacity.ratio.models.Book;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by mehseti on 3.5.2018.
 */

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private List<Book> books;
    private List<byte[]> imageBlobs;
    private Context context;

    private boolean isRatioCollection;
    private boolean isOfflineData;
    private boolean isOfflineTopRated;

    public interface OnItemClickListener {
        void onItemClick(Book movie, byte[] image, ImageView bookImage);
    }

    private OnItemClickListener listener;

    public BookAdapter(@NonNull List<Book> books, List<byte[]> imageBlobs, boolean isRatioCollection, boolean isOfflineData, boolean isOfflineTopRated, Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;

        this.books = books;
        this.isRatioCollection = isRatioCollection;
        this.isOfflineData = isOfflineData;
        this.isOfflineTopRated = isOfflineTopRated;
        this.imageBlobs = imageBlobs;
    }

    class BookViewHolder extends RecyclerView.ViewHolder {
        private ImageView bookImage;
        private TextView bookName;
        private CardView bookContainer;

        BookViewHolder(View v, final Context context) {
            super(v);
            bookName = v.findViewById(R.id.category_item_name);
            bookImage = v.findViewById(R.id.category_item_image);
            bookContainer = v.findViewById(R.id.category_item_container);
        }

        void bind(final Book book, final byte[] image, final OnItemClickListener listener) {
            bookContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(book, image, bookImage);
                }
            });
        }
    }

    @Override
    public BookAdapter.BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new BookViewHolder(view, parent.getContext());
    }

    @Override
    public void onBindViewHolder(BookAdapter.BookViewHolder holder, int position) {
        byte[] imageBlob = null;
        if (imageBlobs != null) {
            imageBlob = imageBlobs.get(position);
        }
        holder.bind(books.get(position), imageBlob, listener);
        Book book = books.get(position);


        ViewCompat.setTransitionName(holder.bookImage, book.getTitle());

        if (!isRatioCollection && !isOfflineData && !isOfflineTopRated) {
            Picasso.get().load(book.getPosterPath().getThumbnail()).into(holder.bookImage);

        } else {

            if (imageBlob != null) {
                Drawable image = new BitmapDrawable(
                        context.getResources(),
                        BitmapFactory.decodeByteArray(imageBlob,
                                0,
                                imageBlob.length)
                );
                holder.bookImage.setImageDrawable(image);
            }
        }

        holder.bookName.setText(book.getTitle());
        holder.bookImage.setContentDescription(book.getTitle());
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
        return books.size();
    }

    public List<Book> getBooks() {
        return books;
    }


    public void setBooks(List<Book> books) {
        this.books = books;
    }
}