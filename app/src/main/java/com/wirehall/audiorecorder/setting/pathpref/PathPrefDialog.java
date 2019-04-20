package com.wirehall.audiorecorder.setting.pathpref;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PathPrefDialog extends PreferenceDialogFragmentCompat {
    private final String TAG = PathPrefDialog.class.getName();
    private boolean isNewFolderEnabled = true;
    private StorageItem dir =
            new StorageItem(Environment.getExternalStorageDirectory().getName(), Environment.getExternalStorageDirectory().getAbsolutePath());
    private List<StorageItem> storageItemList = new ArrayList<>();
    private ArrayAdapter<StorageItem> listAdapter;
    private List<StorageVolumeItem> storageVolumeItems = new ArrayList<>();
    private TextView titleView;
    private Button newFolderButton;

    public static PathPrefDialog newInstance(String key) {
        final PathPrefDialog fragment = new PathPrefDialog();
        final Bundle bundle = new Bundle(1);
        bundle.putString(ARG_KEY, key);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder dialogBuilder) {
        super.onPrepareDialogBuilder(dialogBuilder);
        initVolumes();
        // Create custom view for AlertDialog title containing
        // current directory TextView and possible 'New folder' button.
        // Current directory TextView allows long directory path to be wrapped to multiple lines.
        LinearLayout titleLayout = new LinearLayout(getContext());
        titleLayout.setOrientation(LinearLayout.VERTICAL);

        titleView = new TextView(getContext());
        titleView.setText("⌂");
        titleView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        TextViewCompat.setTextAppearance(titleView, android.R.style.TextAppearance_Large);
        titleView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
        titleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

        newFolderButton = new Button(getContext());
        newFolderButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        newFolderButton.setText(R.string.pref_path_new_folder);
        newFolderButton.setEnabled(false);

        newFolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText input = new EditText(getContext());
                input.setSingleLine();

                // Show new folder name input dialog
                final AlertDialog newFolderDialog = new AlertDialog.Builder(requireContext()).setTitle(R.string.pref_path_new_folder_dialog_title).
                        setView(input).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Editable newDir = input.getText();
                        String newDirName = newDir.toString();
                        // Create new directory
                        String newDirPath = dir.getPath() + "/" + newDirName;
                        if (createSubDir(newDirPath)) {
                            // Navigate into the new directory
                            dir.setPath(newDirPath);
                            updateDirectory();
                        } else {
                            Toast.makeText(getContext(), getString(R.string.toast_folder_creation_failed, newDirName), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton(android.R.string.cancel, null).create();

                newFolderDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        newFolderDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        input.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                if (s == null || s.toString().equals("..") || s.toString().equals(".") || s.toString().isEmpty()) {
                                    newFolderDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                                } else {
                                    newFolderDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                }
                            }
                        });
                    }
                });


                newFolderDialog.show();
            }
        });

        if (!isNewFolderEnabled) {
            newFolderButton.setVisibility(View.GONE);
        }

        titleLayout.addView(titleView);
        titleLayout.addView(newFolderButton);

        dialogBuilder.setCustomTitle(titleLayout);
        storageItemList.addAll(storageVolumeItems.size() > 1 ? storageVolumeItems : getStorageItems(storageVolumeItems.get(0)));
        listAdapter = createListAdapter(storageItemList);

        dialogBuilder.setSingleChoiceItems(listAdapter, -1, this);
        dialogBuilder.setCancelable(false);

        dialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    // Back button pressed, and its not the root directory
                    // Navigate back to an upper directory
                    dir = dir.getParentStorageItem();
                    updateDirectory();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            DialogPreference preference = getPreference();
            if (preference instanceof PathPreference) {
                PathPreference pathPreference = ((PathPreference) preference);
                pathPreference.persistString(dir.getPath());
                pathPreference.setSummary(dir.getPath());
                Toast.makeText(getContext(), getString(R.string.toast_recording_storage_path_updated) + dir.getPath(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateDirectory() {
        storageItemList.clear();
        storageItemList.addAll(getStorageItems(dir));

        listAdapter.notifyDataSetChanged();
    }

    private List<? extends StorageItem> getStorageItems(StorageItem dir) {
        List<StorageItem> dirs = new ArrayList<>();

        if ((storageVolumeItems.size() == 1 && dir != null && !dir.getPath().equals(storageVolumeItems.get(0).getPath())) ||
                (storageVolumeItems.size() > 1 && !isParentOfVolumes(dir))) {
            StorageItem item = new StorageItem(StorageItem.PARENT_DIR_NAME, dir.getParent());
            dirs.add(item);
        } else if (dir == null || (storageVolumeItems.size() > 1 && dir.getName().equals(StorageItem.PARENT_DIR_NAME) && isParentOfVolumes(dir))) {
            return storageVolumeItems;
        }

        try {
            File dirFile = new File(dir.getPath());
            if (!dirFile.exists() || !dirFile.isDirectory()) {
                return dirs;
            }

            for (File file : dirFile.listFiles()) {
                if (file.isDirectory() && file.canRead() && !file.isHidden()) {
                    dirs.add(new StorageItem(file.getName(), file.getPath()));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "ERROR: Exception: " + e.getMessage());
        }

        Collections.sort(dirs, new Comparator<StorageItem>() {
            public int compare(StorageItem o1, StorageItem o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        return dirs;
    }

    private boolean isParentOfVolumes(StorageItem dir) {
        for (StorageVolumeItem storageVolumeItem : storageVolumeItems) {
            String volumeParent = storageVolumeItem.getParent();
            if ((dir != null && dir.getPath().equals(volumeParent)) || dir == null && volumeParent == null) {
                return true;
            }
        }
        return false;
    }

    private void initVolumes() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            //get the list of all storage StorageVolumeItem
            StorageManager storageManager = (StorageManager) getContext().getSystemService(Context.STORAGE_SERVICE);
            for (android.os.storage.StorageVolume storageVolume : storageManager.getStorageVolumes()) {
                if (storageVolume.getState().equals(Environment.MEDIA_MOUNTED)) {
                    String desc = storageVolume.getDescription(getContext());
                    try {
                        @SuppressLint("PrivateApi") Method getPath = android.os.storage.StorageVolume.class.getDeclaredMethod("getPath");
                        String path = (String) getPath.invoke(storageVolume);
                        StorageVolumeItem storageVolumeItem = new StorageVolumeItem(desc, path);
                        storageVolumeItems.add(storageVolume.isPrimary() ? 0 : storageVolumeItems.size(), storageVolumeItem);
                    } catch (Exception e) {
                        storageVolumeItems.clear();
                        break;
                    }
                }
            }
        }

        if (storageVolumeItems.isEmpty()) {
            File storageFir = Environment.getExternalStorageDirectory();
            storageVolumeItems.add(new StorageVolumeItem(storageFir.getName(), storageFir.getAbsolutePath()));
        }
    }

    private boolean createSubDir(String newDir) {
        File newDirFile = new File(newDir);
        if (!newDirFile.exists()) {
            return newDirFile.mkdir();
        }

        return true; //since file already exists
    }

    private ArrayAdapter<StorageItem> createListAdapter(final List<StorageItem> items) {
        return new ArrayAdapter<StorageItem>(requireContext(), android.R.layout.select_dialog_item, android.R.id.text1, items) {
            @NonNull
            @Override
            public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Navigate into the sub-directory
                        StorageItem selectedDir = getItem(position);
                        if (selectedDir != null) {
                            dir = selectedDir;
                            updateDirectory();
                        }
                    }
                });

                if (v instanceof TextView) {
                    // Enable list item (directory) text wrapping
                    TextView tv = (TextView) v;
                    tv.setSingleLine();
                    StorageItem storageItem = getItem(position);
                    if (storageItem != null) {
                        tv.setText(storageItem.getStylishName());
                    }
                }
                return v;
            }

            @Override
            public void notifyDataSetChanged() {
                isShowingVolumes(getCount() > 0 && (getItem(0) instanceof StorageVolumeItem));
                super.notifyDataSetChanged();
            }

            @Override
            public void registerDataSetObserver(DataSetObserver observer) {
                super.registerDataSetObserver(observer);

                isShowingVolumes(getCount() > 0 && (getItem(0) instanceof StorageVolumeItem));
            }

        };
    }

    public void isShowingVolumes(boolean isShowingVolumes) {
        titleView.setText(isShowingVolumes ? "⌂" : dir.getPath());
        newFolderButton.setEnabled(!isShowingVolumes);
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!isShowingVolumes);
    }
}
