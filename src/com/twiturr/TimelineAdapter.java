package com.twiturr;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class TimelineAdapter extends SimpleCursorAdapter {

    private static final String[] FROM = { StatusData.C_CREATED_AT, StatusData.C_USER, StatusData.C_TEXT };
    private static final int[] TO = { R.id.textCreatedAt, R.id.textUser, R.id.textText };

    public TimelineAdapter(Context context, Cursor c) {
	super(context, R.layout.row, c, FROM, TO);
    }

    @Override public void bindView(View row, Context context, Cursor cursor) {
	super.bindView(row, context, cursor);
	long timestamp = cursor.getLong(cursor.getColumnIndex(StatusData.C_CREATED_AT));
	TextView showTime = (TextView)row.findViewById(R.id.textCreatedAt);
	showTime.setText(DateUtils.getRelativeTimeSpanString(timestamp));
    }

}