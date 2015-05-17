package volley.volleyball.adapters;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import volley.volleyball.StringUtils;
import volley.volleyball.database.GamesDatabaseHelper;
import volley.volleyball.R;

public class GameListAdapter extends ObjectArrayAdapter<GamesDatabaseHelper.GameEntry> {

    protected final List<GamesDatabaseHelper.GameEntry> allObjects;

    protected final GameListFilter filter = new GameListFilter();

    public GameListAdapter(Context context, int textViewResourceId, List<GamesDatabaseHelper.GameEntry> objects) {
        super(context, textViewResourceId, objects);
        this.allObjects = Collections.unmodifiableList(new ArrayList<GamesDatabaseHelper.GameEntry>(objects));
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

    @Override
    public Filter getFilter() {
        return filter;
    }

    private class GameListFilter extends Filter {
        @Override
        protected FilterResults performFiltering(final CharSequence filterString) {
            final FilterResults results = new FilterResults();

            final List<GamesDatabaseHelper.GameEntry> visible = new ArrayList<GamesDatabaseHelper.GameEntry>(allObjects.size());

            if (filterString == null || filterString.length() == 0){
                visible.addAll(allObjects);
            }
            else {
                final String filterStringLower = filterString.toString().toLowerCase();
                for (GamesDatabaseHelper.GameEntry entry : allObjects){
                    if(StringUtils.toString(entry).toLowerCase().contains(filterStringLower)){
                        visible.add(entry);
                    }
                }
            }

            results.values = visible;
            results.count = visible.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            GameListAdapter.this.clear();
            GameListAdapter.this.addAll((List<GamesDatabaseHelper.GameEntry>) results.values);
        }
    }
}
