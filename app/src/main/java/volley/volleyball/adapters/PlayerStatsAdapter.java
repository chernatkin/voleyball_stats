package volley.volleyball.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import volley.volleyball.database.GamesDatabaseHelper;
import volley.volleyball.R;
import volley.volleyball.ResultType;
import volley.volleyball.StringUtils;

public class PlayerStatsAdapter extends ObjectArrayAdapter<GamesDatabaseHelper.PlayerStatsEntry> {

    public PlayerStatsAdapter(Context context, int textViewResourceId, List<GamesDatabaseHelper.PlayerStatsEntry> objects) {
        super(context, textViewResourceId, objects);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = super.getView(position, convertView, parent);
        final int color = getBackgroundColor(position);

        final TextView playerView = (TextView)convertView.findViewById(R.id.serve_stats_player_name);
        setBackgroundColor(playerView, color);
        final TextView successView = (TextView)convertView.findViewById(R.id.serve_stats_success_count);
        setBackgroundColor(successView, color);
        final TextView failView = (TextView)convertView.findViewById(R.id.serve_stats_fail_count);
        setBackgroundColor(failView, color);
        final TextView regularView = (TextView)convertView.findViewById(R.id.serve_stats_regular_count);
        setBackgroundColor(regularView, color);

        playerView.setText(getItem(position).getPlayerName());
        successView.setText(StringUtils.toString(getItem(position).getResults().get(ResultType.SUCCESS)));
        failView.setText(StringUtils.toString(getItem(position).getResults().get(ResultType.FAIL)));
        regularView.setText(StringUtils.toString(getItem(position).getResults().get(ResultType.REGULAR)));
        return convertView;
    }
}
