package com.baochu.androidassignment;

import java.io.IOException;
import java.net.HttpURLConnection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapFetcher extends HttpFetcher<Bitmap> {

    /** BitmapFetcher constructor */
    public BitmapFetcher(final String baseUrl, final HttpFetcher.Subscriber<Bitmap> subscriber) {
        super(new HttpFetcher.Params<Bitmap>(baseUrl, subscriber), true);
    }

    @Override
    public Bitmap handleResponse(HttpURLConnection httpConnection) throws IOException {
        if (isCancelled()) {
            return null;
        }
        return BitmapFactory.decodeStream(httpConnection.getInputStream());
    }
}
