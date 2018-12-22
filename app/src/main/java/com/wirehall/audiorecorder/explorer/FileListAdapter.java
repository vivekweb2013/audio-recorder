package com.wirehall.audiorecorder.explorer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.wirehall.audiorecorder.R;
import com.wirehall.audiorecorder.explorer.model.Recording;
import com.wirehall.audiorecorder.setting.SettingActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ViewHolder> {
    private static final String TAG = FileListAdapter.class.getName();
    private final List<Recording> recordings;
    private Context context;
    private int selectedRowPosition = RecyclerView.NO_POSITION;
    private RecyclerViewClickListener recyclerViewClickListener;

    FileListAdapter(Context context, List<Recording> recordings, RecyclerViewClickListener recyclerViewClickListener) {
        this.context = context;
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
        // Note: Do not use the passed position parameter. Instead use viewHolder.getAdapterPosition()
        // as the position sometimes has the wrong value
        viewHolder.itemView.setSelected(this.selectedRowPosition == viewHolder.getAdapterPosition());

        Recording recording = recordings.get(viewHolder.getAdapterPosition());
        viewHolder.fileNameTextView.setText(recording.getName());
        viewHolder.fileSizeTextView.setText(recording.getSizeInString());
        viewHolder.fileDateModifiedTextView.setText(recording.getModifiedDateInString());
        viewHolder.fileDurationTextView.setText(recording.getDurationShortInString());

        if (recording.isPlaying()) {
            viewHolder.filePlayPauseButton.setImageResource(R.drawable.ic_pause_white);
        } else {
            viewHolder.filePlayPauseButton.setImageResource(R.drawable.ic_play_arrow_white);
        }
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
        if (oldSelectedRowPosition > -1)
            notifyItemChanged(oldSelectedRowPosition);
        notifyItemChanged(selectedRowPosition);
    }

    /**
     * Clears any row selection
     */
    public void resetRowSelection() {
        int oldSelectedRowPosition = this.selectedRowPosition;
        this.selectedRowPosition = RecyclerView.NO_POSITION;
        if (oldSelectedRowPosition > -1)
            notifyItemChanged(oldSelectedRowPosition);
    }

    private void deleteFile(int adapterPosition) {
        FileUtils.deleteFile(recordings.get(adapterPosition).getPath());
        recordings.remove(adapterPosition);
        if (adapterPosition < selectedRowPosition) {
            selectedRowPosition--;
        } else if (adapterPosition == selectedRowPosition) {
            selectedRowPosition = RecyclerView.NO_POSITION;
        }
        notifyItemRemoved(adapterPosition);
    }

    /**
     * Used to keep the reference to list row elements to fetch faster. i.e. to avoid time consuming findViewById
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView fileNameTextView;
        TextView fileSizeTextView;
        TextView fileDateModifiedTextView;
        TextView fileDurationTextView;
        ImageButton filePlayPauseButton;
        ImageButton fileOptionsMenuButton;
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

            filePlayPauseButton = itemView.findViewById(R.id.ib_file_play_pause);
            filePlayPauseButton.setOnClickListener(this);

            final String fileMenuOptionDelete = context.getResources().getString(R.string.file_menu_option_delete);
            final String fileMenuOptionInfo = context.getResources().getString(R.string.file_menu_option_info);
            final String fileMenuOptionShare = context.getResources().getString(R.string.file_menu_option_share);
            final String deleteDialogTitle = context.getResources().getString(R.string.dialog_delete_title);
            final String yes = context.getResources().getString(R.string.yes);
            final String no = context.getResources().getString(R.string.no);
            fileOptionsMenuButton = itemView.findViewById(R.id.ib_file_menu);
            fileOptionsMenuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final ListPopupWindow window;
                    window = new ListPopupWindow(context);
                    final List<String> data = new ArrayList<>();
                    data.add(fileMenuOptionDelete);
                    data.add(fileMenuOptionInfo);
                    data.add(fileMenuOptionShare);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.file_menu_item_layout, data);
                    /* use ur custom layout which has only TextView along with style required*/
                    window.setAdapter(adapter);
                    window.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                    window.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
                    window.setModal(false);
                    window.setAnchorView(fileOptionsMenuButton);/*it will be the overflow view of yours*/
                    window.setContentWidth(FileUtils.measureContentWidth(adapter, context));/* set width based on ur requirement*/
                    window.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String option = data.get(position);
                            final int adapterPosition = getAdapterPosition();
                            if (adapterPosition == RecyclerView.NO_POSITION) {
                                return;
                            }

                            if (option.equals(fileMenuOptionDelete)) {
                                final String deleteDialogMessage = context.getResources().getString(R.string.dialog_delete_message, recordings.get(adapterPosition).getPath());
                                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                                Boolean confirmDelete = sharedPref.getBoolean(SettingActivity.KEY_PREF_CONFIRM_DELETE, false);
                                if (confirmDelete) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle(deleteDialogTitle)
                                            .setMessage(deleteDialogMessage)
                                            .setIcon(R.drawable.ic_warning_black)
                                            .setPositiveButton(yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    deleteFile(adapterPosition);
                                                }
                                            })
                                            .setNegativeButton(no, null)
                                            .show();
                                } else {
                                    deleteFile(adapterPosition);
                                }
                                window.dismiss();
                            } else if (option.equals(fileMenuOptionInfo)) {
                                FileInformationDialog fileInformationDialog = new FileInformationDialog(context, recordings.get(adapterPosition));
                                window.dismiss();
                                fileInformationDialog.show();
                            } else if (option.equals(fileMenuOptionShare)) {
                                Uri uri = FileProvider.getUriForFile(context, "com.wirehall.fileprovider", new File(recordings.get(adapterPosition).getPath()));
                                Intent share = new Intent(Intent.ACTION_SEND);
                                share.setType("audio/*");
                                share.putExtra(Intent.EXTRA_STREAM, uri);
                                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                window.dismiss();
                                String shareRec = context.getResources().getString(R.string.share_recording);
                                context.startActivity(Intent.createChooser(share, shareRec));
                            }
                        }
                    });
                    window.show();
                }
            });
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            refreshRowSelection(position);
            recyclerViewClickListener.onClick(view, position);
        }
    }

}
