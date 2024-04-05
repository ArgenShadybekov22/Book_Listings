package com.davidread.booklistings;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BookLoader extends AsyncTaskLoader<List<Book>> {

    private static final String BASE_URL = "https://www.googleapis.com/books/v1/volumes";

    private static final String MAX_RESULTS_URL_PARAMETER = "maxResults=40";

    private static final String FIELDS_URL_PARAMETER = "fields=items(volumeInfo/title,volumeInfo/authors,volumeInfo/infoLink)";

    private final String query;
    private final int startIndex;
    private List<Book> books;

    public BookLoader(@NonNull Context context, String query, int startIndex) {
        super(context);
        this.query = query;
        this.startIndex = startIndex;
        this.books = null;
    }
    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Nullable
    @Override
    public List<Book> loadInBackground() {

        if (books != null) {
            return books;
        }

        URL url = constructQueryUrl(query, startIndex);

        String json = null;
        try {
            json = getJsonFromUrl(url);
        } catch (IOException e) {
            Log.e(BookLoader.class.getSimpleName(), "Error closing input stream", e);
        }

        return extractBooksFromJson(json);
    }

    public String getQuery() {
        return query;
    }

    public int getStartIndex() {
        return startIndex;
    }


    private URL constructQueryUrl(String query, int startIndex) {

        String stringUrl = "";
        try {
            String queryUrlParameter = "q=" + URLEncoder.encode(query, StandardCharsets.UTF_8.name());
            String startIndexUrlParameter = "startIndex=" + startIndex;
            stringUrl = BASE_URL + "?" + queryUrlParameter + "&" + startIndexUrlParameter + "&" + MAX_RESULTS_URL_PARAMETER + "&" + FIELDS_URL_PARAMETER;
        } catch (UnsupportedEncodingException e) {
            Log.e(BookLoader.class.getSimpleName(), "Error encoding query term for string URL", e);
        }

        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(BookLoader.class.getSimpleName(), "Error constructing URL object", e);
        }

        return url;
    }

    private String getJsonFromUrl(URL url) throws IOException {

        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        String json = "";

        try {
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();

            if (httpURLConnection.getResponseCode() == 200) {
                inputStream = httpURLConnection.getInputStream();
                json = getJsonFromInputStream(inputStream);
            } else {
                Log.e(BookLoader.class.getSimpleName(), "Network request returned with response code " + httpURLConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(BookLoader.class.getSimpleName(), "Error making network request", e);
        } finally {

            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return json;
    }

    private String getJsonFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String bufferedReaderLine = bufferedReader.readLine();
        while (bufferedReaderLine != null) {
            stringBuilder.append(bufferedReaderLine);
            bufferedReaderLine = bufferedReader.readLine();
        }
        return stringBuilder.toString();
    }

    private List<Book> extractBooksFromJson(String json) {

        List<Book> books = new ArrayList<>();

        JSONArray itemsJsonArray = null;
        try {
            JSONObject rootJsonObject = new JSONObject(json);
            itemsJsonArray = rootJsonObject.getJSONArray("items");
        } catch (JSONException e) {
            Log.e(BookLoader.class.getSimpleName(), "Error parsing items JSON array", e);
        }

        if (itemsJsonArray == null) {
            return books;
        }

        for (int itemsIndex = 0; itemsIndex < itemsJsonArray.length(); itemsIndex++) {

            JSONObject volumeInfoJsonObject = null;
            try {
                volumeInfoJsonObject = itemsJsonArray.getJSONObject(itemsIndex).getJSONObject("volumeInfo");
            } catch (JSONException e) {
                Log.e(BookLoader.class.getSimpleName(), "Error parsing the volumeInfo JSON object for the item with index " + itemsIndex, e);
            }

            if (volumeInfoJsonObject == null) {
                continue;
            }

            String title = "";
            try {
                title = volumeInfoJsonObject.getString("title");
            } catch (JSONException e) {
                Log.e(BookLoader.class.getSimpleName(), "Error parsing the title JSON property for the item with index " + itemsIndex, e);
            }

            String[] authors = new String[]{""};
            try {
                JSONArray authorsJsonArray = volumeInfoJsonObject.getJSONArray("authors");
                authors = new String[authorsJsonArray.length()];
                for (int authorsIndex = 0; authorsIndex < authorsJsonArray.length(); authorsIndex++) {
                    authors[authorsIndex] = authorsJsonArray.getString(authorsIndex);
                }
            } catch (JSONException e) {
                Log.e(BookLoader.class.getSimpleName(), "Error parsing the authors JSON property for the item with index " + itemsIndex, e);
            }

            String url = "";
            try {
                url = volumeInfoJsonObject.getString("infoLink");
            } catch (JSONException e) {
                Log.e(BookLoader.class.getSimpleName(), "Error parsing the url JSON property for the item with index " + itemsIndex, e);
            }

            books.add(new Book(title, authors, url));
        }

        this.books = books;
        return books;
    }
}
