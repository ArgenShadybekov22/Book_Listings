package com.davidread.booklistings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import java.util.ArrayList;
import java.util.List;


public class ResultsActivity extends AppCompatActivity {


    private static final String BUNDLE_NEXT_BOOK_LOADER_ID = "bundle_next_book_loader_id";

    private final AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            // Get Book object associated with the clicked item.
            Book book = (Book) parent.getAdapter().getItem(position);

            // Do nothing if the Book object has an invalid URL.
            if (!URLUtil.isValidUrl(book.getUrl())) {
                return;
            }

            // Start intent to open the browser for the URL.
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(book.getUrl()));
            startActivity(intent);
        }
    };

    private final AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            boolean lastItemVisible = view.getLastVisiblePosition() == totalItemCount - 1;
            if (!lastItemVisible) {
                return;
            }


            if (!bookLoadingEnabled) {
                return;
            }

            if (getLoaderManager().getLoader(nextBookLoaderId) != null) {
                return;
            }

            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            boolean isDeviceConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
            if (!isDeviceConnected) {
                bookLoadingEnabled = false;
                return;
            }

            LoaderManager.getInstance(ResultsActivity.this).initLoader(nextBookLoaderId, null, loaderCallbacks);
            nextBookLoaderId++;
        }
    };

    private final LoaderManager.LoaderCallbacks<List<Book>> loaderCallbacks = new LoaderManager.LoaderCallbacks<List<Book>>() {

        @NonNull
        @Override
        public Loader<List<Book>> onCreateLoader(int id, @Nullable Bundle args) {

            bookLoadingEnabled = false;

            if (loadingAlertDialog == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ResultsActivity.this);
                builder.setView(getLayoutInflater().inflate(R.layout.dialog_loading, null));
                builder.setCancelable(false);
                loadingAlertDialog = builder.create();
            }
            loadingAlertDialog.show();

            ListView listView = findViewById(R.id.book_list_view);
            int startIndex = listView.getCount();
            return new BookLoader(ResultsActivity.this, query, startIndex);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<List<Book>> loader, List<Book> data) {

            if (loadingAlertDialog != null) {
                loadingAlertDialog.hide();
            }

            BookLoader bookLoader = (BookLoader) loader;
            ListView listView = findViewById(R.id.book_list_view);
            if (bookLoader.getStartIndex() != listView.getCount()) {
                return;
            }

            if (data.isEmpty()) {
                TextView emptyTextView = findViewById(R.id.empty_book_list_text_view);
                listView.setEmptyView(emptyTextView);
                return;
            }

            BookAdapter bookAdapter = (BookAdapter) listView.getAdapter();
            bookAdapter.addAll(data);
            bookLoadingEnabled = true;
        }

        @Override
        public void onLoaderReset(@NonNull Loader<List<Book>> loader) {
        }
    };

    private String query;

    private boolean bookLoadingEnabled;

    private int nextBookLoaderId;

    private AlertDialog loadingAlertDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        query = getIntent().getStringExtra(SearchActivity.INTENT_EXTRA_QUERY);
        bookLoadingEnabled = true;
        nextBookLoaderId = 0;

        setContentView(R.layout.activity_results);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_bar_title_results, query));
        }
        ListView listView = findViewById(R.id.book_list_view);
        listView.setAdapter(new BookAdapter(this, new ArrayList<>()));
        listView.setOnItemClickListener(onItemClickListener);
        listView.setOnScrollListener(onScrollListener);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BUNDLE_NEXT_BOOK_LOADER_ID, nextBookLoaderId);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        nextBookLoaderId = savedInstanceState.getInt(BUNDLE_NEXT_BOOK_LOADER_ID);
        for (int id = 0; id < nextBookLoaderId; id++) {
            LoaderManager.getInstance(ResultsActivity.this).initLoader(id, null, loaderCallbacks);
        }
    }
}