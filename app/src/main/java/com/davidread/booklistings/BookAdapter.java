package com.davidread.booklistings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
public class BookAdapter extends ArrayAdapter<Book> {
    public BookAdapter(@NonNull Context context, @NonNull List<Book> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_book, parent, false);
        }

        Book book = getItem(position);

        TextView titleTextView = convertView.findViewById(R.id.title_text_view);
        titleTextView.setText(book.getTitle());

        TextView authorsTextView = convertView.findViewById(R.id.authors_text_view);
        authorsTextView.setText(getFormattedAuthorsString(book.getAuthors()));

        return convertView;
    }

    public List<Book> getObjects() {
        List<Book> books = new ArrayList<>();
        if (getCount() > 0) {
            for (int index = 0; index < getCount(); index++) {
                books.add(getItem(index));
            }
        }
        return books;
    }

    private String getFormattedAuthorsString(String[] authors) {
        StringBuilder formattedAuthorsString = new StringBuilder();
        for (int index = 0; index < authors.length; index++) {
            if (index > 0) {
                formattedAuthorsString.append(", ");
            }
            formattedAuthorsString.append(authors[index]);
        }
        return formattedAuthorsString.toString();
    }
}
