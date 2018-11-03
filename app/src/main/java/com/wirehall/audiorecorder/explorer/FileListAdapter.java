package com.wirehall.audiorecorder.explorer;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wirehall.audiorecorder.R;
import com.wirehall.audiorecorder.explorer.model.Recording;

import java.util.List;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ViewHolder> {
    private static final String TAG = FileListAdapter.class.getName();

    private final List<Recording> recordings;
    private int selectedRowPosition = RecyclerView.NO_POSITION;
    private RecyclerViewClickListener recyclerViewClickListener;

    FileListAdapter(List<Recording> recordings, RecyclerViewClickListener recyclerViewClickListener) {
        this.recordings = recordings;
        this.recyclerViewClickListener = recyclerViewClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.file_row_layout, parent, false);
        return new ViewHolder(view, recyclerViewClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.itemView.setSelected(selectedRowPosition == position);

        Recording recording = recordings.get(position);
        viewHolder.fileNameTextView.setText(recording.getName());
        viewHolder.fileSizeTextView.setText(recording.getSizeInString());
        viewHolder.fileDateModifiedTextView.setText(recording.getModifiedDateInString());
        viewHolder.fileDurationTextView.setText(recording.getDurationInString());
    }

    @Override
    public int getItemCount() {
        return this.recordings.size();
    }

    /**
     * Uses the list passed as a argument to this method for showing in file list view
     *
     * @param newRecordings The list of recordings to use in file list view
     */
    public void updateData(List<Recording> newRecordings) {
        resetRowSelection();
        recordings.clear();
        recordings.addAll(newRecordings);
        notifyDataSetChanged();
    }

    private void refreshRowSelection(int selectedRowPosition) {
        int oldSelectedRowPosition = this.selectedRowPosition;
        this.selectedRowPosition = selectedRowPosition;
        notifyItemChanged(oldSelectedRowPosition);
        notifyItemChanged(selectedRowPosition);
    }

    /**
     * Clears any row selection
     */
    public void resetRowSelection() {
        int oldSelectedRowPosition = this.selectedRowPosition;
        this.selectedRowPosition = RecyclerView.NO_POSITION;
        notifyItemChanged(oldSelectedRowPosition);
    }

    /**
     * Used to keep the reference to list row elements to fetch faster. i.e. to avoid time consuming findViewById
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView fileNameTextView;
        TextView fileSizeTextView;
        TextView fileDateModifiedTextView;
        TextView fileDurationTextView;
        private RecyclerViewClickListener recyclerViewClickListener;

        private ViewHolder(@NonNull View itemView, RecyclerViewClickListener recyclerViewClickListener) {
            super(itemView);
            this.recyclerViewClickListener = recyclerViewClickListener;
            itemView.setOnClickListener(this);

            //Note: you can also use the setOnClickListener on below child views
            //and perform the actions in onClick method using the instanceof check

            fileNameTextView = itemView.findViewById(R.id.tv_filename);
            fileSizeTextView = itemView.findViewById(R.id.tv_file_size);
            fileDateModifiedTextView = itemView.findViewById(R.id.tv_file_date_modified);
            fileDurationTextView = itemView.findViewById(R.id.tv_file_duration);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            refreshRowSelection(position);
            recyclerViewClickListener.onClick(view, position);
        }
    }

}
