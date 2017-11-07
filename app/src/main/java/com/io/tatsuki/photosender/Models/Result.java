package com.io.tatsuki.photosender.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 結果クラス
 */

public class Result {

    @Expose
    @SerializedName("status")
    private String status;

    @Expose
    @SerializedName("category")
    private String category;

    public String getStatus() {
        return this.status;
    }

    public String getCategory() {
        return this.category;
    }
}
