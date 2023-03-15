package com.fitnesswell.bodymeasures;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.fitnesswell.BtnClickListener;
import com.fitnesswell.DAO.Profile;
import com.fitnesswell.DAO.bodymeasures.BodyMeasure;
import com.fitnesswell.DAO.bodymeasures.BodyPart;
import com.fitnesswell.DAO.bodymeasures.BodyPartExtensions;
import com.fitnesswell.DAO.bodymeasures.DAOBodyMeasure;
import com.fitnesswell.DAO.bodymeasures.DAOBodyPart;
import com.fitnesswell.MainActivity;
import com.fitnesswell.AppViMo;
import com.fitnesswell.R;
import com.fitnesswell.SettingsFragment;
import com.fitnesswell.ValuesEditorDialogbox;
import com.fitnesswell.enums.Unit;
import com.fitnesswell.enums.UnitType;
import com.fitnesswell.utils.DateConverter;
import com.fitnesswell.utils.ExpandedListView;
import com.fitnesswell.utils.UnitConverter;
import com.fitnesswell.utils.Value;
import com.fitnesswell.views.EditableInputView;
import com.fitnesswell.views.GraphView;
import com.github.mikephil.charting.data.Entry;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class BodyPartDetailsFragment extends Fragment implements DatePickerDialog.OnDateSetListener {
    private final String mCurrentPhotoPath = null;
    private TextView addButton = null;
    private EditableInputView nameEdit = null;
    private ExpandedListView measureList = null;
    private Toolbar bodyToolbar = null;
    private GraphView mDateGraph = null;
    private DAOBodyMeasure mBodyMeasureDb = null;
    private DAOBodyPart mDbBodyPart;
    private BodyPart mInitialBodyPart;
    private final EditableInputView.OnTextChangedListener onTextChangeListener = this::requestForSave;
    private AppViMo appViMo;
    private final BtnClickListener itemClickDeleteRecord = view -> {
        switch (view.getId()) {
            case R.id.deleteButton:
                showDeleteDialog((long) view.getTag());
                break;
            case R.id.editButton:
                showEditDialog((long) view.getTag());
                break;

        }
    };
    private final OnClickListener onClickAddMeasure = new OnClickListener() {
        @Override
        public void onClick(View v) {
            BodyMeasure lastBodyMeasure = mBodyMeasureDb.getLastBodyMeasures(mInitialBodyPart.getId(), getProfile());
            final Value lastValue = lastBodyMeasure == null
                    ? new Value(0f, getValidUnit(null))
                    : getValueWithValidUnit(lastBodyMeasure);
            ValuesEditorDialogbox editorDialogbox = new ValuesEditorDialogbox(getActivity(), new Date(), "", new Value[]{lastValue});
            editorDialogbox.setTitle(R.string.AddLabel);
            editorDialogbox.setPositiveButton(R.string.AddLabel);
            editorDialogbox.setOnDismissListener(dialog -> {
                if (!editorDialogbox.isCancelled()) {
                    Date date = DateConverter.localDateStrToDate(editorDialogbox.getDate(), getContext());
                    final Value newValue = editorDialogbox.getValues()[0];
                    mBodyMeasureDb.addBodyMeasure(date, mInitialBodyPart.getId(), newValue, getProfile().getId());
                    refreshData();
                }
            });
            editorDialogbox.show();
        }
    };
    private final OnItemLongClickListener itemlongclickDeleteRecord = (listView, view, position, id) -> {


        final long selectedID = id;

        String[] profilListArray = new String[1]; // un seul choix
        profilListArray[0] = getActivity().getResources().getString(R.string.DeleteLabel);

        AlertDialog.Builder itemActionBuilder = new AlertDialog.Builder(getActivity());
        itemActionBuilder.setTitle("").setItems(profilListArray, (dialog, which) -> {

            // Delete
            if (which == 0) {
                mBodyMeasureDb.deleteMeasure(selectedID);
                refreshData();
                KToast.infoToast(getActivity(), getActivity().getResources().getText(R.string.removedid).toString() + " " + selectedID, Gravity.BOTTOM, KToast.LENGTH_SHORT);
            }
        });
        itemActionBuilder.show();

        return true;
    };
    private final View.OnClickListener onClickToolbarItem = v -> {
        // Handle presses on the action bar items
        if (v.getId() == R.id.deleteButton) {
            delete();
        }
    };
    private ImageButton deleteButton;
    private TextView editDate;
    private EditText editText;
    private ImageView bodyPartImageView;

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static BodyPartDetailsFragment newInstance(long bodyPartID, boolean showInput) {
        BodyPartDetailsFragment f = new BodyPartDetailsFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putLong("bodyPartID", bodyPartID);
        args.putBoolean("showInput", showInput);
        f.setArguments(args);

        return f;
    }

    private void delete() {
        AlertDialog.Builder deleteDialogBuilder = new AlertDialog.Builder(this.getActivity());

        deleteDialogBuilder.setTitle(getActivity().getResources().getText(R.string.global_confirm));
        deleteDialogBuilder.setMessage(getActivity().getResources().getText(R.string.delete_bodypart_confirm));

        deleteDialogBuilder.setPositiveButton(this.getResources().getString(R.string.global_yes), (dialog, which) -> {
            mDbBodyPart.delete(mInitialBodyPart.getId());
            deleteRecordsAssociatedToMachine();
            getActivity().onBackPressed();
        });

        deleteDialogBuilder.setNegativeButton(this.getResources().getString(R.string.global_no), (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog deleteDialog = deleteDialogBuilder.create();
        deleteDialog.show();
    }

    private void deleteRecordsAssociatedToMachine() {
        DAOBodyMeasure mDbBodyMeasure = new DAOBodyMeasure(getContext());

        Profile lProfile = getProfile();

        List<BodyMeasure> listBodyMeasure = mDbBodyMeasure.getBodyPartMeasuresList(mInitialBodyPart.getId(), lProfile);
        for (BodyMeasure record : listBodyMeasure) {
            mDbBodyMeasure.deleteMeasure(record.getId());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bodytracking_details_fragment, container, false);

        mDbBodyPart = new DAOBodyPart(getContext());

        addButton = view.findViewById(R.id.buttonAdd);
        nameEdit = view.findViewById(R.id.BODYPART_NAME);
        measureList = view.findViewById(R.id.listWeightProfil);
        bodyToolbar = view.findViewById(R.id.bodyTrackingDetailsToolbar);
        bodyPartImageView = view.findViewById(R.id.BODYPART_LOGO);
        CardView nameCardView = view.findViewById(R.id.nameCardView);
        mDateGraph = view.findViewById(R.id.bodymeasureChart);

        long bodyPartID = getArguments().getLong("bodyPartID", 0);
        mInitialBodyPart = mDbBodyPart.getBodyPart(bodyPartID);

        if (mInitialBodyPart.getBodyPartResKey() != -1) {
            bodyPartImageView.setVisibility(View.VISIBLE);
            bodyPartImageView.setImageDrawable(mInitialBodyPart.getPicture(getContext()));
        } else {
            bodyPartImageView.setImageDrawable(null);
            bodyPartImageView.setVisibility(View.GONE);
        }

        if (mInitialBodyPart.getType() == BodyPartExtensions.TYPE_WEIGHT) {
            nameEdit.ActivateDialog(false);
        }
        nameEdit.setOnTextChangeListener(onTextChangeListener);
        addButton.setOnClickListener(onClickAddMeasure);
        measureList.setOnItemLongClickListener(itemlongclickDeleteRecord);

        mBodyMeasureDb = new DAOBodyMeasure(view.getContext());

        ((MainActivity) getActivity()).getActivityToolbar().setVisibility(View.GONE);

        nameEdit.setText(mInitialBodyPart.getName(getContext()));
        bodyToolbar.setNavigationIcon(R.drawable.ic_back);
        bodyToolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());

        deleteButton = view.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(onClickToolbarItem);
        if (mInitialBodyPart.getType() == BodyPartExtensions.TYPE_WEIGHT) {
            deleteButton.setVisibility(View.GONE);
        }

        appViMo = new ViewModelProvider(requireActivity()).get(AppViMo.class);

        appViMo.getProfile().observe(getViewLifecycleOwner(), profile -> {
            refreshData();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        refreshData();
    }

    private void DrawGraph(List<BodyMeasure> valueList) {

        if (valueList.size() < 1) {
            mDateGraph.clear();
            return;
        }

        ArrayList<Entry> yVals = new ArrayList<>();

        float minBodyMeasure = -1;

        for (int i = valueList.size() - 1; i >= 0; i--) {
            float normalizedMeasure;
            switch (valueList.get(i).getBodyMeasure().getUnit().getUnitType()) {
                case WEIGHT:
                    normalizedMeasure = UnitConverter.weightConverter(valueList.get(i).getBodyMeasure().getValue(), valueList.get(i).getBodyMeasure().getUnit(), SettingsFragment.getDefaultWeightUnit(getActivity()).toUnit());
                    break;
                case SIZE:
                    normalizedMeasure = UnitConverter.sizeConverter(valueList.get(i).getBodyMeasure().getValue(), valueList.get(i).getBodyMeasure().getUnit(), SettingsFragment.getDefaultSizeUnit(getActivity()));
                    break;
                default:
                    normalizedMeasure = valueList.get(i).getBodyMeasure().getValue();
            }

            Entry value = new Entry((float) DateConverter.nbDays(valueList.get(i).getDate()), normalizedMeasure);
            yVals.add(value);
            if (minBodyMeasure == -1) minBodyMeasure = valueList.get(i).getBodyMeasure().getValue();
            else if (valueList.get(i).getBodyMeasure().getValue() < minBodyMeasure)
                minBodyMeasure = valueList.get(i).getBodyMeasure().getValue();
        }

        mDateGraph.draw(yVals);
    }

    /*  */
    private void FillRecordTable(List<BodyMeasure> valueList) {
        Cursor oldCursor = null;

        if (valueList.isEmpty()) {
            measureList.setAdapter(null);
        } else {
            // ...
            if (measureList.getAdapter() == null) {
                BodyMeasureCursorAdapter mTableAdapter = new BodyMeasureCursorAdapter(getActivity(), mBodyMeasureDb.getCursor(), 0, itemClickDeleteRecord);
                measureList.setAdapter(mTableAdapter);
            } else {
                oldCursor = ((BodyMeasureCursorAdapter) measureList.getAdapter()).swapCursor(mBodyMeasureDb.getCursor());
                if (oldCursor != null)
                    oldCursor.close();
            }
        }
    }

    public String getName() {
        return getArguments().getString("name");
    }

    private void refreshData() {
        View fragmentView = getView();
        if (fragmentView != null) {
            if (getProfile() != null) {
                List<BodyMeasure> valueList = mBodyMeasureDb.getBodyPartMeasuresList(mInitialBodyPart.getId(), getProfile());
                DrawGraph(valueList);
                FillRecordTable(valueList);
            }
        }
    }

    private void showDeleteDialog(final long idToDelete) {

        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    mBodyMeasureDb.deleteMeasure(idToDelete);
                    refreshData();
                    Toast.makeText(getActivity(), getResources().getText(R.string.removedid) + " " + idToDelete, Toast.LENGTH_SHORT)
                            .show();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getResources().getText(R.string.DeleteRecordDialog)).setPositiveButton(getResources().getText(R.string.global_yes), dialogClickListener)
                .setNegativeButton(getResources().getText(R.string.global_no), dialogClickListener).show();
    }

    private void showEditDialog(final long idToEdit) {
        BodyMeasure bodyMeasure = mBodyMeasureDb.getMeasure(idToEdit);

        final Value lastValue = bodyMeasure.getBodyMeasure();

        ValuesEditorDialogbox editorDialogbox = new ValuesEditorDialogbox(getActivity(), bodyMeasure.getDate(), "", new Value[]{lastValue});
        editorDialogbox.setOnDismissListener(dialog -> {
            if (!editorDialogbox.isCancelled()) {
                Date date = DateConverter.localDateStrToDate(editorDialogbox.getDate(), getContext());
                final Value newValue = editorDialogbox.getValues()[0];
                BodyMeasure updatedBodyMeasure = new BodyMeasure(
                        bodyMeasure.getId(),
                        date,
                        bodyMeasure.getBodyPartID(),
                        newValue,
                        bodyMeasure.getProfileID());
                int i = mBodyMeasureDb.updateMeasure(updatedBodyMeasure);
                refreshData();
            }
        });
        editorDialogbox.setOnCancelListener(null);

        editorDialogbox.show();
    }


    private Profile getProfile() {
        return appViMo.getProfile().getValue();
    }

    public Fragment getFragment() {
        return this;
    }

    private void requestForSave(View view) {
        boolean toUpdate = false;

        switch (view.getId()) {
            case R.id.BODYPART_NAME:
                mInitialBodyPart.setCustomName(nameEdit.getText());
                toUpdate = true;
                break;
            case R.id.BODYPART_LOGO:

                mInitialBodyPart.setCustomPicture(mCurrentPhotoPath);
                toUpdate = true;
                break;
        }

        if (toUpdate) {
            mDbBodyPart.update(mInitialBodyPart);
            KToast.infoToast(getActivity(), mInitialBodyPart.getCustomName() + " updated", Gravity.BOTTOM, KToast.LENGTH_SHORT);
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Date date = DateConverter.dateToDate(year, month, dayOfMonth);
        if (editDate != null)
            editDate.setText(DateConverter.dateToLocalDateStr(date, getContext()));
    }

    private Unit getValidUnit(BodyMeasure lastBodyMeasure) {
        UnitType unitType = BodyPartExtensions.getUnitType(mInitialBodyPart.getBodyPartResKey());
        if (lastBodyMeasure != null) {
            if (unitType != lastBodyMeasure.getBodyMeasure().getUnit().getUnitType()) {
                lastBodyMeasure = null;
            }
        }

        Unit unitDef = Unit.UNITLESS;
        if (lastBodyMeasure == null) {
            switch (unitType) {
                case WEIGHT:
                    unitDef = SettingsFragment.getDefaultWeightUnit(getActivity()).toUnit();
                    break;
                case SIZE:
                    unitDef = SettingsFragment.getDefaultSizeUnit(getActivity());
                    break;
                case PERCENTAGE:
                    unitDef = Unit.PERCENTAGE;
                    break;
            }
        } else {
            unitDef = lastBodyMeasure.getBodyMeasure().getUnit();
        }
        return unitDef;
    }

    private Value getValueWithValidUnit(BodyMeasure lastBodyMeasure) {
        Unit unitDef = getValidUnit(lastBodyMeasure);
        Value oldValue = lastBodyMeasure.getBodyMeasure();
        return new Value(oldValue.getValue(), unitDef, oldValue.getId(), oldValue.getLabel());
    }
}
