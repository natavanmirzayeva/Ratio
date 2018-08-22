package com.project.udacity.ratio.data;

import com.google.gson.annotations.SerializedName;
import com.project.udacity.ratio.models.Items;

import java.util.List;

/**
 * Created by mehseti on 27.7.2018.
 */

public class BookResponse {
    @SerializedName("items")
    private List<Items> items;

    public List<Items> getItems() {
        return items;
    }

    public void setItems(List<Items> items) {
        this.items = items;
    }


}
