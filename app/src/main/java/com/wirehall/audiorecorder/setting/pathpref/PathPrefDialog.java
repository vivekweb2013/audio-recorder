package com.wirehall.audiorecorder.setting.pathpref;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;

import com.wirehall.audiorecorder.R;
import com.wirehall.audiorecorder.explorer.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class PathPrefDialog extends PreferenceDialogFragmentCompat {
  private static final String TAG = PathPrefDialog.class.getName();
  public static final String TAG_NEW_FOLDER_BUTTON = "TAG_NEW_FOLDER_BUTTON";
  public static final String TAG_NEW_FOLDER_INPUT = "TAG_NEW_FOLDER_INPUT";
  private static final boolean IS_NEW_FOLDER_ENABLED = true;
  private final List<StorageItem> storageItemList = new ArrayList<>();
  private final List<StorageVolumeItem> storageVolumeItems = new ArrayList<>();
  private StorageItem dir =
      new StorageItem(FileUtils.getBaseStorageName(), FileUtils.getBaseStoragePath());
  private ArrayAdapter<StorageItem> listAdapter;
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
    titleView.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
    TextViewCompat.setTextAppearance(titleView, android.R.style.TextAppearance_Large);
    titleView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
    titleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

    newFolderButton = new Button(getContext());
    newFolderButton.setTag(TAG_NEW_FOLDER_BUTTON);
    newFolderButton.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
    newFolderButton.setText(R.string.pref_path_new_folder);
    newFolderButton.setEnabled(false);

    newFolderButton.setOnClickListener(
        v -> {
          final EditText input = new EditText(getContext());
          input.setTag(TAG_NEW_FOLDER_INPUT);
          input.setSingleLine();
          input.setMinHeight((int) getResources().getDimension(R.dimen.size_standard_min_height));

          // Show new folder name input dialog
          final AlertDialog newFolderDialog =
              new AlertDialog.Builder(requireContext())
                  .setTitle(R.string.pref_path_new_folder_dialog_title)
                  .setView(input)
                  .setPositiveButton(
                      android.R.string.ok,
                      (dialog, whichButton) -> {
                        Editable newDir = input.getText();
                        String newDirName = newDir.toString();
                        // Create new directory
                        String newDirPath = dir.getPath() + File.separator + newDirName;
                        if (createSubDir(newDirPath)) {
                          // Navigate into the new directory
                          dir.setPath(newDirPath);
                          updateDirectory();
                        } else {
                          Toast.makeText(
                                  getContext(),
                                  getString(R.string.toast_folder_creation_failed, newDirName),
                                  Toast.LENGTH_SHORT)
                              .show();
                        }
                      })
                  .setNegativeButton(android.R.string.cancel, null)
                  .create();

          newFolderDialog.setOnShowListener(
              dialog -> {
                newFolderDialog.getButton(BUTTON_POSITIVE).setEnabled(false);
                input.addTextChangedListener(
                    new TextWatcher() {
                      @Override
                      public void beforeTextChanged(
                          CharSequence s, int start, int count, int after) {
                        // No implementation required
                      }

                      @Override
                      public void onTextChanged(CharSequence s, int start, int before, int count) {
                        // No implementation required
                      }

                      @Override
                      public void afterTextChanged(Editable s) {
                        newFolderDialog
                            .getButton(BUTTON_POSITIVE)
                            .setEnabled(
                                s != null
                                    && !s.toString().equals("..")
                                    && !s.toString().equals(".")
                                    && !s.toString().isEmpty());
                      }
                    });
              });

          newFolderDialog.show();
        });

    if (!IS_NEW_FOLDER_ENABLED) {
      newFolderButton.setVisibility(View.GONE);
    }

    titleLayout.addView(titleView);
    titleLayout.addView(newFolderButton);

    dialogBuilder.setCustomTitle(titleLayout);
    storageItemList.addAll(
        storageVolumeItems.size() > 1
            ? storageVolumeItems
            : getStorageItems(storageVolumeItems.get(0)));
    listAdapter = createListAdapter(storageItemList);

    dialogBuilder.setSingleChoiceItems(listAdapter, -1, this);
    dialogBuilder.setCancelable(false);
  }

  @Override
  public void onDialogClosed(boolean positiveResult) {
    if (positiveResult) {
      DialogPreference preference = getPreference();
      if (preference instanceof PathPreference) {
        PathPreference pathPreference = ((PathPreference) preference);
        pathPreference.persistString(dir.getPath());
        pathPreference.setSummary(dir.getPath());
        Toast.makeText(
                getContext(),
                getString(R.string.toast_recording_storage_path_updated) + dir.getPath(),
                Toast.LENGTH_SHORT)
            .show();
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

    if ((storageVolumeItems.size() == 1
            && dir != null
            && !dir.getPath().equals(storageVolumeItems.get(0).getPath()))
        || (storageVolumeItems.size() > 1 && !isParentOfVolumes(dir))) {
      StorageItem item = new StorageItem(StorageItem.PARENT_DIR_NAME, dir.getParent());
      dirs.add(item);
    } else if (dir == null
        || (storageVolumeItems.size() > 1
            && dir.getName().equals(StorageItem.PARENT_DIR_NAME)
            && isParentOfVolumes(dir))) {
      return storageVolumeItems;
    }

    try {
      File dirFile = new File(dir.getPath());
      if (!dirFile.exists() || !dirFile.isDirectory()) {
        return dirs;
      }

      for (File file : Objects.requireNonNull(dirFile.listFiles())) {
        if (file.isDirectory() && file.canRead() && !file.isHidden()) {
          dirs.add(new StorageItem(file.getName(), file.getPath()));
        }
      }
    } catch (Exception e) {
      Log.e(TAG, "ERROR: Exception: " + e.getMessage());
    }

    Collections.sort(dirs, (o1, o2) -> o1.getName().compareTo(o2.getName()));

    return dirs;
  }

  private boolean isParentOfVolumes(StorageItem dir) {
    for (StorageVolumeItem storageVolumeItem : storageVolumeItems) {
      String volumeParent = storageVolumeItem.getParent();
      if ((dir != null && dir.getPath().equals(volumeParent))
          || dir == null && volumeParent == null) {
        return true;
      }
    }
    return false;
  }

  private void initVolumes() {
    if (storageVolumeItems.isEmpty()) {
      storageVolumeItems.add(
          new StorageVolumeItem(FileUtils.getBaseStorageName(), FileUtils.getBaseStoragePath()));
    }
  }

  private boolean createSubDir(String newDir) {
    File newDirFile = new File(newDir);
    if (!newDirFile.exists()) {
      return newDirFile.mkdir();
    }

    return true; // since file already exists
  }

  private ArrayAdapter<StorageItem> createListAdapter(final List<StorageItem> items) {
    return new ArrayAdapter<StorageItem>(
        requireContext(), android.R.layout.select_dialog_item, android.R.id.text1, items) {
      @NonNull
      @Override
      public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        View v = super.getView(position, convertView, parent);

        v.setOnClickListener(
            view -> {
              // Navigate into the sub-directory
              StorageItem selectedDir = getItem(position);
              if (selectedDir != null) {
                dir = selectedDir;
                updateDirectory();
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
    ((AlertDialog) Objects.requireNonNull(getDialog()))
        .getButton(BUTTON_POSITIVE)
        .setEnabled(!isShowingVolumes);
  }
}
