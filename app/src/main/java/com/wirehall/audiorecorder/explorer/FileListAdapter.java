package com.wirehall.audiorecorder.explorer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.wirehall.audiorecorder.R;

import java.io.File;
import java.util.List;

public class FileListAdapter extends ArrayAdapter<File> {


    private final Context context;
    private final List<File> fileList;

    public FileListAdapter(@NonNull Context context, List<File> fileList) {
        super(context, R.layout.file_row_layout, fileList);
        this.fileList = fileList;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.file_row_layout, parent, false);
        TextView fileNameTextView = rowView.findViewById(R.id.tvFileName);
        fileNameTextView.setText(fileList.get(position).getName());
        return rowView;
    }
}
