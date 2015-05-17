package volley.volleyball;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

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

        playerView.setText(getItem(position).getPlayerName());
        successView.setText(StringUtils.toString(getItem(position).getResults().get(ResultType.SUCCESS)));
        failView.setText(StringUtils.toString(getItem(position).getResults().get(ResultType.FAIL)));
        return convertView;
    }
}
