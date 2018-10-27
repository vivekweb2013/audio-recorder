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
import android.widget.Toast;

import com.wirehall.audiorecorder.R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

public class FileListFragment extends Fragment { // implements AdapterView.OnItemClickListener {
    public static final String STORAGE_PATH = Environment.getExternalStorageDirectory().toString() + "/Rec/Collection";
    private FileListFragmentListener activity;
    private FileListAdapter fileListAdapter;
    private List<File> fileList;

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
        fileList = FileUtils.getAllFilesFromDirectory(STORAGE_PATH, new FileExtensionFilter());

        RecyclerView recyclerView = getActivity().findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        RecyclerViewClickListener recyclerViewClickListener = new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                Toast.makeText(getActivity(), "Item: " + position, Toast.LENGTH_SHORT).show();
                activity.onFileItemClicked(fileList.get(position).getPath());
            }
        };
        fileListAdapter = new FileListAdapter(fileList, recyclerViewClickListener);
        recyclerView.setAdapter(fileListAdapter);


    }

    public void refreshAdapter() {
        List<File> fileList = FileUtils.getAllFilesFromDirectory(STORAGE_PATH, new FileExtensionFilter());
        fileListAdapter.updateData(fileList);
        fileListAdapter.notifyDataSetChanged();
    }

    public interface FileListFragmentListener {
        void onFileItemClicked(String filePath);
    }

    /**
     * Class to filter files with .mp3 extension
     */
    class FileExtensionFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".rec") || name.endsWith(".REC"));
        }
    }
}
