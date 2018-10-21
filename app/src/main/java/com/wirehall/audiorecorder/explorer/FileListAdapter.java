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
import java.util.Date;
import java.util.List;

public class FileListAdapter extends ArrayAdapter<File> {


    private final Context context;
    private final List<File> fileList;

    FileListAdapter(@NonNull Context context, List<File> fileList) {
        super(context, R.layout.file_row_layout, fileList);
        this.fileList = fileList;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.file_row_layout, parent, false);

            viewHolder = new ViewHolder();

            viewHolder.fileNameTextView = convertView.findViewById(R.id.tv_filename);
            viewHolder.fileSizeTextView = convertView.findViewById(R.id.tv_file_size);
            viewHolder.fileDateModifiedTextView = convertView.findViewById(R.id.tv_file_date_modified);
            viewHolder.fileDurationTextView = convertView.findViewById(R.id.tv_file_duration);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        File fileItem = fileList.get(position);

        viewHolder.fileNameTextView.setText(fileItem.getName());

        String fileSize = FileUtils.humanReadableByteCount(fileItem.length(), true);
        viewHolder.fileSizeTextView.setText(fileSize);

        String formattedDate = FileUtils.humanReadableDate(fileItem.lastModified());
        viewHolder.fileDateModifiedTextView.setText(formattedDate);

        String fileDuration = FileUtils.humanReadableDuration(fileItem.getPath());
        viewHolder.fileDurationTextView.setText(fileDuration);

        return convertView;
    }

    static class ViewHolder {
        TextView fileNameTextView;
        TextView fileSizeTextView;
        TextView fileDateModifiedTextView;
        TextView fileDurationTextView;

    }

}
