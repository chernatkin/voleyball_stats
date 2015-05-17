package volley.volleyball;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


public class GamesListActivity extends Activity{

    private GamesDatabaseHelper dbHelper;

    private static final int EDIT_GAME_ITEM_ID = 1;

    private static final int OPEN_STATS_ITEM_ID = 2;

    GameListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games_list);
        final ActionBar bar = getActionBar();
        if(bar != null){
            bar.setIcon(R.mipmap.volleyball_ball);
            bar.setTitle("Games list");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        dbHelper = new GamesDatabaseHelper(this);

        final ListView listView = (ListView) findViewById(R.id.games_list_view);
        adapter = new GameListAdapter(this, R.layout.games_list_item, dbHelper.fetchAllGames());
        listView.setAdapter(adapter);
        registerForContextMenu(listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                goToTeamsActivity(adapter.getItem(position).getId());
            }
        });
    }

    private void goToTeamsActivity(final long gameId){
        final Intent intent = new Intent(this, TeamsActivity.class);
        if(gameId != -1) {
            intent.putExtra(TeamsActivity.GAME_ID_PARAM_NAME, gameId);
        }
        startActivity(intent);
    }

    private void goToStatsActivity(final long gameId){
        final Intent intent = new Intent(this, PlayerServeStatsActivity.class);
        intent.putExtra(PlayerServeStatsActivity.GAME_ID_PARAM_NAME, gameId);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        dbHelper.close();
        dbHelper = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_games_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_create_new_game) {
            goToTeamsActivity(-1);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.games_list_view) {
            menu.add(Menu.NONE, EDIT_GAME_ITEM_ID, 1, "Edit game");
            menu.add(Menu.NONE, OPEN_STATS_ITEM_ID, 1, "Open statistics");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final GamesDatabaseHelper.GameEntry entry = adapter.getItem(acmi.position);

        switch (item.getItemId()){
            case EDIT_GAME_ITEM_ID:
                goToTeamsActivity(entry.getId());
                return true;
            case OPEN_STATS_ITEM_ID:
                goToStatsActivity(entry.getId());
                return true;
        }
        return super.onContextItemSelected(item);
    }

    public void onCreateNewClick(View view){
        goToTeamsActivity(-1);
    }
}
