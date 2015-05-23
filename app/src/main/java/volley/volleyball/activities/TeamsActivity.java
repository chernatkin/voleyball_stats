package volley.volleyball.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import volley.volleyball.database.GamesDatabaseHelper;
import volley.volleyball.R;
import volley.volleyball.StringUtils;


public class TeamsActivity extends Activity {

    public static final String GAME_ID_PARAM_NAME = "game_id";

    private GamesDatabaseHelper dbHelper;

    private long gameId = -1;

    GamesDatabaseHelper.GameEntry gameEntry;

    List<GamesDatabaseHelper.TeamMemberEntry> teamMemders = new ArrayList<GamesDatabaseHelper.TeamMemberEntry>(16);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teams);
        final ActionBar bar = getActionBar();
        if(bar != null){
            bar.setIcon(R.mipmap.gtk_edit);
        }

        dbHelper = new GamesDatabaseHelper(this);

        gameId = getIntent().getLongExtra(GAME_ID_PARAM_NAME, -1);
        final Calendar creationDate = new GregorianCalendar();
        if(gameId != -1){
            gameEntry = dbHelper.fetchGame(gameId);
            teamMemders = dbHelper.fetchGameMembers(gameId);
            creationDate.setTime(gameEntry.getCreationDate());

            ((EditText)findViewById(R.id.new_game_first_team_input)).setText(gameEntry.getFirstTeam());
            ((EditText)findViewById(R.id.new_game_second_team_input)).setText(gameEntry.getSecondTeam());
        }

        fillTeamMemberEntries(teamMemders, (LinearLayout)findViewById(R.id.edit_game_team_members_list));

        final DatePicker datePicker = (DatePicker)findViewById(R.id.new_game_date_picker);
        datePicker.updateDate(creationDate.get(Calendar.YEAR), creationDate.get(Calendar.MONTH), creationDate.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_teams, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();

        if (id == R.id.action_return_to_list) {
            finish();
        }
        else if (id == R.id.action_open_stats){
            goToStatsActivity(gameId);
        }
        else if (id == R.id.action_write_game_results){
            goToServeActivity(gameId);
        }
        else {
            return super.onOptionsItemSelected(item);
        }

        return true;
    }

    public void onSaveButtonClick(View view){

        final LinearLayout inputList = (LinearLayout)findViewById(R.id.edit_game_team_members_list);

        if(!isNamesUnique(inputList)){
            final Toast toast = Toast.makeText(this, getString(R.string.players_duplicates_fail_msg), Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        final DatePicker datePicker = (DatePicker)findViewById(R.id.new_game_date_picker);
        final Date creationDate = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth()).getTime();

        final String firstTeam = ((EditText)findViewById(R.id.new_game_first_team_input)).getText().toString();
        final String secondTeam = ((EditText)findViewById(R.id.new_game_second_team_input)).getText().toString();

        gameId = dbHelper.putNewGame(gameId, creationDate, firstTeam, secondTeam);

        for(int i = 0; i < inputList.getChildCount(); i++){
            final EditText input = (EditText)inputList.getChildAt(i);
            final String member = StringUtils.toString(input.getText());

            final GamesDatabaseHelper.TeamMemberEntry entry = (GamesDatabaseHelper.TeamMemberEntry)input.getTag();
            entry.setFullName(member);

            dbHelper.putNewGameMember(gameId, entry);
        }

        goToServeActivity(gameId);
    }

    private boolean isNamesUnique(final LinearLayout inputList){

        final Set<String> names = new HashSet<String>(16);
        for(int i = 0; i < inputList.getChildCount(); i++){
            final String name = StringUtils.toString(((EditText)inputList.getChildAt(i)).getText());
            if(!StringUtils.isEmpty(name) && !names.add(name)){
                return false;
            }
        }
        return true;
    }

    private void fillTeamMemberEntries(final List<GamesDatabaseHelper.TeamMemberEntry> teamMemders, final LinearLayout inputList){

        for(int i = 0; i < inputList.getChildCount(); i++){
            final EditText input = (EditText)inputList.getChildAt(i);
            if(i < teamMemders.size()){
                input.setText(teamMemders.get(i).getFullName());
                input.setTag(teamMemders.get(i));
            }
            else{
                input.setTag(new GamesDatabaseHelper.TeamMemberEntry());
            }
        }
    }

    private void goToServeActivity(final long gameId){
        final Intent intent = new Intent(this, ServeActivity.class);
        intent.putExtra(ServeActivity.GAME_ID_PARAM_NAME, gameId);
        startActivity(intent);
        finish();
    }

    private void goToStatsActivity(final long gameId){
        final Intent intent = new Intent(this, PlayerServeStatsActivity.class);
        intent.putExtra(PlayerServeStatsActivity.GAME_ID_PARAM_NAME, gameId);
        startActivity(intent);
        finish();
    }
}
