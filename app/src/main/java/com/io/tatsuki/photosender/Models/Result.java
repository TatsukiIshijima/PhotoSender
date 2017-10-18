package com.io.tatsuki.photosender.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

/**
 * 結果クラス
 */

public class Result {

    @Getter
    @Expose
    @SerializedName("status")
    private String status;
}
