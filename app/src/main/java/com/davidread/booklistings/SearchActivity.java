package com.davidread.booklistings;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

public class SearchActivity extends AppCompatActivity {

    public static final String INTENT_EXTRA_QUERY = "intent_extra_query";

    private final SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {

        @Override
        public boolean onQueryTextSubmit(String query) {
            searchMenuItem.collapseActionView();
            Intent resultsIntent = new Intent(SearchActivity.this, ResultsActivity.class);
            resultsIntent.putExtra(INTENT_EXTRA_QUERY, query);
            startActivity(resultsIntent);
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return true;
        }
    };

    private MenuItem searchMenuItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        searchMenuItem = menu.findItem(R.id.search_view);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_view).getActionView();
        searchView.setOnQueryTextListener(onQueryTextListener);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        return true;
    }
}