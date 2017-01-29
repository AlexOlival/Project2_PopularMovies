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

    public static final String API_KEY = "6d1bcca4ac9f8068563dc28464b30779";
    public static final String PARAMETER_API_KEY = "api_key";
    //public static final String PARAMETER_NUMBER_OF_PAGE = "page";

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
