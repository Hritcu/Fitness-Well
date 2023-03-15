package com.fitnesswell.bodymeasures;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fitnesswell.DAO.Profile;
import com.fitnesswell.DAO.bodymeasures.BodyMeasure;
import com.fitnesswell.DAO.bodymeasures.BodyPart;
import com.fitnesswell.DAO.bodymeasures.DAOBodyMeasure;
import com.fitnesswell.R;
import com.fitnesswell.graph.MiniDateGraph;
import com.fitnesswell.utils.DateConverter;
import com.fitnesswell.utils.ImageUtil;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

public class BodyPartListAdapter extends ArrayAdapter<BodyPart> implements View.OnClickListener {

    Context mContext;
    private Profile mProfile = null;

    public BodyPartListAdapter(ArrayList<BodyPart> data, Context context) {
        super(context, R.layout.bodypart_row, data);
        this.mContext = context;
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BodyPart dataModel = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.bodypart_row, parent, false);
            viewHolder.txtID = convertView.findViewById(R.id.LIST_BODYPART_ID);
            viewHolder.txtName = convertView.findViewById(R.id.LIST_BODYPART);
            viewHolder.txtLastMeasure = convertView.findViewById(R.id.LIST_BODYPART_LASTRECORD);
            viewHolder.logo = convertView.findViewById(R.id.LIST_BODYPART_LOGO);
            viewHolder.miniGraph = new MiniDateGraph(getContext(), convertView.findViewById(R.id.LIST_BODYPART_MINIGRAPH), "");

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.txtID.setText(String.valueOf(dataModel.getId()));
        viewHolder.txtName.setText(dataModel.getName(getContext()));

        if (dataModel.getLastMeasure() != null)
            viewHolder.txtLastMeasure.setText(String.valueOf(dataModel.getLastMeasure().getBodyMeasure().getValue()));
        else
            viewHolder.txtLastMeasure.setText("-");
        if (!dataModel.getCustomPicture().isEmpty()) {
            ImageUtil.setPic(viewHolder.logo, dataModel.getCustomPicture());
        } else {
            if (dataModel.getBodyPartResKey() != -1)
                viewHolder.logo.setImageDrawable(dataModel.getPicture(getContext()));
            else
                viewHolder.logo.setImageDrawable(null); // Remove the image, Custom is not managed yet
        }

        convertView.post(() -> {
                    DAOBodyMeasure mDbBodyMeasure = new DAOBodyMeasure(getContext());

                    List<BodyMeasure> valueList = mDbBodyMeasure.getBodyPartMeasuresListTop4(dataModel.getId(), getProfile());

                    if (valueList != null) {
                        if (valueList.size() < 1) {
                            viewHolder.miniGraph.getChart().clear();
                        } else {
                            ArrayList<Entry> yVals = new ArrayList<>();

                            if (valueList.size() > 0) {
                                for (int i = valueList.size() - 1; i >= 0; i--) {
                                    Entry value = new Entry((float) DateConverter.nbDays(valueList.get(i).getDate()), valueList.get(i).getBodyMeasure().getValue());
                                    yVals.add(value);
                                }

                                viewHolder.miniGraph.draw(yVals);
                            }
                        }
                    }
                }
        );

        return convertView;
    }

    private Profile getProfile() {
        return mProfile;
    }

    public void setProfile(Profile profileID) {
        mProfile = profileID;
    }

    private static class ViewHolder {
        TextView txtID;
        TextView txtName;
        TextView txtLastMeasure;
        ImageView logo;
        MiniDateGraph miniGraph;
    }
}
