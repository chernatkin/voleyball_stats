package volley.volleyball.adapters;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import volley.volleyball.database.GamesDatabaseHelper;
import volley.volleyball.R;

public class GameListAdapter extends ObjectArrayAdapter<GamesDatabaseHelper.GameEntry> {

    public GameListAdapter(Context context, int textViewResourceId, List<GamesDatabaseHelper.GameEntry> objects) {
        super(context, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = super.getView(position, convertView, parent);
        final int color = getBackgroundColor(position);
        setBackgroundColor(convertView, color);

        final TextView dateView = (TextView)convertView.findViewById(R.id.games_list_item_date);
        setBackgroundColor(dateView, color);
        final TextView firstTeamView = (TextView)convertView.findViewById(R.id.games_list_item_first_team);
        setBackgroundColor(firstTeamView, color);
        final TextView secondTeamView = (TextView)convertView.findViewById(R.id.games_list_item_second_team);
        setBackgroundColor(secondTeamView, color);

        dateView.setText(DATE_FORMAT.format(getItem(position).getCreationDate()));
        firstTeamView.setText(getItem(position).getFirstTeam());
        secondTeamView.setText(getItem(position).getSecondTeam());
        return convertView;
    }
}
