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
import com.project.udacity.ratio.models.Serial;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by mehseti on 3.5.2018.
 */

public class SerialAdapter extends RecyclerView.Adapter<SerialAdapter.SerialViewHolder> {

    private List<Serial> tvSeries;
    private List<byte[]> imageBlobs;

    private boolean isRatioCollection;
    private boolean isOfflineData;
    private boolean isOfflineTopRated;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(Serial serial, byte[] image, ImageView serialImage);
    }

    private OnItemClickListener listener;

    public SerialAdapter(@NonNull List<Serial> tvSeries, List<byte[]> imageBlobs, boolean isRatioCollection,
                         boolean isOfflineData, boolean isOfflineTopRated, Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;

        this.tvSeries = tvSeries;
        this.isRatioCollection = isRatioCollection;
        this.isOfflineData = isOfflineData;
        this.isOfflineTopRated = isOfflineTopRated;
        this.imageBlobs = imageBlobs;
    }

    class SerialViewHolder extends RecyclerView.ViewHolder {
        private ImageView serialImage;
        private TextView serialName;
        private CardView serialContainer;

        SerialViewHolder(View v, final Context context) {
            super(v);
            serialName = v.findViewById(R.id.category_item_name);
            serialImage = v.findViewById(R.id.category_item_image);
            serialContainer = v.findViewById(R.id.category_item_container);
        }

        void bind(final Serial serial, final byte[] image, final OnItemClickListener listener) {
            serialContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(serial, image, serialImage);
                }
            });
        }
    }

    @Override
    public SerialAdapter.SerialViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new SerialViewHolder(view, parent.getContext());
    }

    @Override
    public void onBindViewHolder(SerialAdapter.SerialViewHolder holder, int position) {
        byte[] imageBlob = null;
        if (imageBlobs != null) {
            imageBlob = imageBlobs.get(position);
        }

        holder.bind(tvSeries.get(position), imageBlob, listener);
        Serial serial = tvSeries.get(position);

        ViewCompat.setTransitionName(holder.serialImage, serial.getTitle());

        if (!isRatioCollection && !isOfflineData && !isOfflineTopRated) {

            Picasso.get().load(serial.getPosterPath().getSmallThumbnail()).into(holder.serialImage);

        } else {

            if (imageBlob != null) {
                Drawable image = new BitmapDrawable(
                        context.getResources(),
                        BitmapFactory.decodeByteArray(imageBlob,
                                0,
                                imageBlob.length)
                );
                holder.serialImage.setImageDrawable(image);
            }
        }

        holder.serialName.setText(serial.getTitle());
        holder.serialImage.setContentDescription(serial.getTitle());
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
        return tvSeries.size();
    }

    public List<Serial> getTvSeries() {
        return tvSeries;
    }


    public void setTvSeries(List<Serial> tvSeries) {
        this.tvSeries = tvSeries;
    }
}