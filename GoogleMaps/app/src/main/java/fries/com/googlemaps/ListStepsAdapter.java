package fries.com.googlemaps;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by tmq on 10/30/15.
 */
public class ListStepsAdapter extends BaseAdapter {
    private ArrayList<Step> listSteps = new ArrayList<>();
    private LayoutInflater lf;
    private Context mContext;

    public ListStepsAdapter(Context context){
        mContext = context;
        lf = LayoutInflater.from(mContext);
    }

    public void setListSteps(ArrayList<Step> list){
        listSteps = list;
    }

    @Override
    public int getCount() {
        return listSteps.size();
    }

    @Override
    public Step getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null){
            view = lf.inflate(R.layout.item_direction, null);
        }
        ImageView img       = (ImageView)   view.findViewById(R.id.imgDirection);
        TextView direction  = (TextView)    view.findViewById(R.id.txtDirectionStep);
        TextView distance   = (TextView)    view.findViewById(R.id.txtDistanceStep);
        TextView duration   = (TextView)    view.findViewById(R.id.txtDurationStep);

        Step step = listSteps.get(position);

        img.setImageResource(getIdImage(step.getManeuver()));
        direction.setText(step.getText());
        distance.setText(step.getDistance());
        duration.setText(step.getDuration());

        return view;
    }


    private static final String FIRST_NAME_IMAGE = "icon_direction_";
    private static int getIdImage(String name){
        int id;
        switch (name){
            case "fork-left":
                id = R.drawable.icon_direction_fork_left;
                break;
            case "fork-right":
                id = R.drawable.icon_direction_fork_right;
                break;
            case "keep-left":
                id = R.drawable.icon_direction_keep_left;
                break;
            case "keep-right":
                id = R.drawable.icon_direction_keep_right;
                break;
            case "merge":
                id = R.drawable.icon_direction_merge;
                break;
            case "noname-left":
                id = R.drawable.icon_direction_noname_left;
                break;
            case "noname-right":
                id = R.drawable.icon_direction_noname_right;
                break;
            case "ramp-left":
                id = R.drawable.icon_direction_ramp_left;
                break;
            case "ramp-right":
                id = R.drawable.icon_direction_ramp_right;
                break;
            case "roundabout-left":
                id = R.drawable.icon_direction_roundabout_left;
                break;
            case "roundabout-right":
                id = R.drawable.icon_direction_roundabout_right;
                break;
            case "start":
                id = R.drawable.icon_direction_start;
                break;
            case "straight":
                id = R.drawable.icon_direction_straight;
                break;
            case "turn-left":
                id = R.drawable.icon_direction_turn_left;
                break;
            case "turn-right":
                id = R.drawable.icon_direction_turn_right;
                break;
            case "turn-slight-left":
                id = R.drawable.icon_direction_turn_slight_left;
                break;
            case "turn-slight-right":
                id = R.drawable.icon_direction_turn_slight_right;
                break;
            case "uturn-left":
                id = R.drawable.icon_direction_uturn_left;
                break;
            case "uturn-right":
                id = R.drawable.icon_direction_uturn_right;
                break;
            default:
                id = android.R.drawable.ic_menu_directions;
                break;
        }

        return id;
    }
}
