package com.wirehall.explorer;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

public class FileListFragment extends ListFragment implements AdapterView.OnItemClickListener {
    public interface FileListFragmentListener {
        void onFileItemClicked(String filePath);
    }

    FileListFragmentListener activity;
    List<File> fileList;
    public static final String STORAGE_PATH = Environment.getExternalStorageDirectory().toString() + "/Rec/Collection";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (FileListFragmentListener) context;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fileList = FileUtils.getAllFilesFromDirectory(STORAGE_PATH, new FileExtensionFilter());
        FileListAdapter fileListAdapter = new FileListAdapter(getContext(), fileList);
        setListAdapter(fileListAdapter);
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Toast.makeText(getActivity(), "Item: " + position, Toast.LENGTH_SHORT).show();
        activity.onFileItemClicked(fileList.get(position).getPath());
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
