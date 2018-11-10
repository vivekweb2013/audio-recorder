package com.wirehall.audiorecorder.explorer;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wirehall.audiorecorder.R;
import com.wirehall.audiorecorder.explorer.model.Recording;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

public class FileListFragment extends Fragment {
    public static final String STORAGE_PATH = Environment.getExternalStorageDirectory().toString() + "/Recording/Collection";
    private static final String TAG = FileListFragment.class.getName();
    private FileListFragmentListener activity;
    private FileListAdapter fileListAdapter;
    private List<Recording> recordings;

    /**
     * @return The singleton instance of FileListFragment
     */
    public static FileListFragment newInstance() {
        return new FileListFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (FileListFragmentListener) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.file_list_fragment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recordings = FileUtils.getAllFilesFromDirectory(getContext(), STORAGE_PATH, new FileExtensionFilter());

        RecyclerView recyclerView = getActivity().findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        RecyclerViewClickListener recyclerViewClickListener = new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                activity.onFileItemClicked(recordings.get(position));
            }
        };
        fileListAdapter = new FileListAdapter(getContext(), recordings, recyclerViewClickListener);
        recyclerView.setAdapter(fileListAdapter);
    }

    /**
     * Refresh the file list view by updating the adapter associated with it
     */
    public void refreshAdapter() {
        List<Recording> recordings = FileUtils.getAllFilesFromDirectory(getContext(), STORAGE_PATH, new FileExtensionFilter());
        fileListAdapter.updateData(recordings);
    }

    /**
     * Clears any row selection
     */
    public void resetRowSelection() {
        fileListAdapter.resetRowSelection();
    }

    /**
     * Interface used to invoke the file item's click handler from activity
     */
    public interface FileListFragmentListener {
        void onFileItemClicked(Recording filePath);
    }

    /**
     * Class used to filter files with .rec extension
     */
    class FileExtensionFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(FileUtils.DEFAULT_FILENAME_EXTENSION));
        }
    }
}
