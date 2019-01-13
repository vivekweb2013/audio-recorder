package com.wirehall.audiorecorder.setting.pathpref;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.text.Editable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wirehall.audiorecorder.R;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PathPrefDialog extends PreferenceDialogFragmentCompat {
    private boolean isNewFolderEnabled = true;
    private String sdcardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
    private String dir = Environment.getExternalStorageDirectory().toString();
    private List<String> subDirs;
    private ArrayAdapter<String> listAdapter;
    private TextView titleView;

    //Filter we use to get visible, readable directories only
    private FileFilter directoryFilter = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return file.isDirectory() && !file.isHidden() && file.canRead();
        }
    };

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder dialogBuilder) {
        super.onPrepareDialogBuilder(dialogBuilder);
        {

            // Create custom view for AlertDialog title containing
            // current directory TextView and possible 'New folder' button.
            // Current directory TextView allows long directory path to be wrapped to multiple lines.
            LinearLayout titleLayout = new LinearLayout(getContext());
            titleLayout.setOrientation(LinearLayout.VERTICAL);

            titleView = new TextView(getContext());
            titleView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            titleView.setTextAppearance(getContext(), android.R.style.TextAppearance_Large);
            titleView.setTextColor(getContext().getResources().getColor(android.R.color.black));
            titleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            titleView.setText(R.string.pref_path_dialog_title);

            Button newDirButton = new Button(getContext());
            newDirButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            newDirButton.setText(R.string.pref_path_new_folder);
            newDirButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final EditText input = new EditText(getContext());

                    // Show new folder name input dialog
                    new AlertDialog.Builder(getContext()).setTitle(R.string.pref_path_new_folder_dialog_title).
                            setView(input).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Editable newDir = input.getText();
                            String newDirName = newDir.toString();
                            // Create new directory
                            if (createSubDir(dir + "/" + newDirName)) {
                                // Navigate into the new directory
                                dir += "/" + newDirName;
                                updateDirectory();
                            } else {
                                Toast.makeText(getContext(), "Failed to create '" + newDirName + "' folder", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).setNegativeButton(android.R.string.cancel, null).show();
                }
            });

            if (!isNewFolderEnabled) {
                newDirButton.setVisibility(View.GONE);
            }

            titleLayout.addView(titleView);
            titleLayout.addView(newDirButton);

            dialogBuilder.setCustomTitle(titleLayout);
            subDirs = getDirectories(dir);
            listAdapter = createListAdapter(subDirs);

            dialogBuilder.setSingleChoiceItems(listAdapter, -1, this);
            dialogBuilder.setCancelable(false);

            dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (!dir.equals(sdcardDirectory) && keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                        // Back button pressed, and its not the root directory
                        // Navigate back to an upper directory
                        dir = new File(dir).getParent();
                        updateDirectory();
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    public static PathPrefDialog newInstance(String key) {
        final PathPrefDialog fragment = new PathPrefDialog();
        final Bundle bundle = new Bundle(1);
        bundle.putString(ARG_KEY, key);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            DialogPreference preference = getPreference();
            if (preference instanceof PathPreference) {
                PathPreference pathPreference = ((PathPreference) preference);
                pathPreference.persistString(dir);
                pathPreference.setSummary(dir);
            }
        }
    }

    public void setNewFolderEnabled(boolean isNewFolderEnabled) {
        this.isNewFolderEnabled = isNewFolderEnabled;
    }

    public boolean getNewFolderEnabled() {
        return isNewFolderEnabled;
    }


    private void updateDirectory() {
        subDirs.clear();
        subDirs.addAll(getDirectories(dir));
        titleView.setText(dir);

        listAdapter.notifyDataSetChanged();
    }

    private List<String> getDirectories(String dir) {
        List<String> dirs = new ArrayList<>();
        if (!dir.equals(sdcardDirectory)) {
            dirs.add("..");
        }

        try {
            File dirFile = new File(dir);
            if (!dirFile.exists() || !dirFile.isDirectory()) {
                return dirs;
            }

            for (File file : dirFile.listFiles()) {
                if (file.isDirectory()) {
                    dirs.add(file.getName());
                }
            }
        } catch (Exception e) {
            //TODO: Add logger
        }

        Collections.sort(dirs, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        return dirs;
    }

    private boolean createSubDir(String newDir) {
        File newDirFile = new File(newDir);
        if (!newDirFile.exists()) {
            return newDirFile.mkdir();
        }

        return false;
    }

    private ArrayAdapter<String> createListAdapter(List<String> items) {
        return new ArrayAdapter<String>(getContext(), android.R.layout.select_dialog_item, android.R.id.text1, items) {
            @NonNull
            @Override
            public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Navigate into the sub-directory
                        String selectedDir = getItem(position);
                        dir = selectedDir.equals("..") ? new File(dir).getParent() : dir + "/" + selectedDir;
                        updateDirectory();
                    }
                });

                if (v instanceof TextView) {
                    // Enable list item (directory) text wrapping
                    TextView tv = (TextView) v;
                    tv.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    tv.setEllipsize(null);
                }
                return v;
            }
        };
    }
}
