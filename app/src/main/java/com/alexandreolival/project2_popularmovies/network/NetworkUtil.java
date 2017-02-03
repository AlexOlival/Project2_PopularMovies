package com.alexandreolival.project2_popularmovies.network;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtil {

    private static final String TAG = "NetworkUtil";

    public static final String BASE_MOVIEDB_POSTER_URL = "http://image.tmdb.org/t/p/w185";
    public static final String BASE_MOVIEDB_METADATA_URL = "http://api.themoviedb.org/3/movie/";

    public static final String PATH_SORT_BY_POPULAR = "popular";
    public static final String PATH_SORT_BY_RATING = "top_rated";

    public static final String API_KEY = "API_KEY_GOES_HERE";
    public static final String PARAMETER_API_KEY = "api_key";
    public static final String PARAMETER_APPEND_MOVIE_DETAILS = "append_to_response";
    public static final String MOVIE_DETAILS = "videos,reviews";

    public static final String BASE_YOUTUBE_VIDEO_URL = "https://www.youtube.com/watch?v=";

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        Log.d(TAG, "Performing network request with URL " + url.toString());
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            if (scanner.hasNext()) {
                return scanner.next();
            } else {
                return null;
            }

        } finally {
            urlConnection.disconnect();
        }
    }

}
