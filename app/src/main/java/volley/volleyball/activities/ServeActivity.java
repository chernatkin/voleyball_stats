package volley.volleyball.activities;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import volley.volleyball.database.GamesDatabaseHelper;
import volley.volleyball.R;
import volley.volleyball.ResultType;
import volley.volleyball.ServeType;


public class ServeActivity extends Activity {

    public static final String GAME_ID_PARAM_NAME = "game_id";

    SectionsPagerAdapter mSectionsPagerAdapter;

    ViewPager mViewPager;

    GamesDatabaseHelper db;

    GamesDatabaseHelper.GameEntry gameEntry;

    List<GamesDatabaseHelper.TeamMemberEntry> members;

    long teamMemberId = -1;
    ServeType serveType;
    ResultType resultType;
    int setNumber = 1;
    String stageName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serve);
        final ActionBar bar = getActionBar();
        if(bar != null){
            bar.setIcon(R.mipmap.activity_monitor);
        }

        final long gameId = getIntent().getLongExtra(GAME_ID_PARAM_NAME, -1);
        if(gameId == -1){
            finish();
        }

        db = new GamesDatabaseHelper(this);
        gameEntry = db.fetchGame(gameId);
        members = Collections.unmodifiableList(db.fetchGameMembers(gameId));
        db.close();

        db = new GamesDatabaseHelper(this);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.actions_pager);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(final int position) {
                switch (position){
                    case 0:
                        setStageName(getString(R.string.action_players_title));
                        break;
                    case 1:
                        setStageName(getString(R.string.action_type_title));
                        break;
                    case 2:
                        setStageName(getString(R.string.action_result_title));
                        break;
                    default:
                        break;
                }
                setActionBarTitle(setNumber, stageName);
            }
        });
        mViewPager.setAdapter(mSectionsPagerAdapter);

        setActionBarTitle(setNumber, getString(R.string.action_players_title));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_serve, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        if (id == R.id.action_first_set) {
            setSetNumber(1);
        }
        else if (id == R.id.action_second_set) {
            setSetNumber(2);
        }
        else if (id == R.id.action_third_set) {
            setSetNumber(3);
        }
        else if (id == R.id.action_fourth_set) {
            setSetNumber(4);
        }
        else if (id == R.id.action_fifth_set) {
            setSetNumber(5);
        }
        else if (id == R.id.action_finish_write) {
            finish();
        }
        else if (id == R.id.action_revert_previous) {
            final int count = db.deleteLastGameActivity(gameEntry.getId());
            if(count > 0){
                final Toast toast = Toast.makeText(this, getString(R.string.last_record_deleted_toast), Toast.LENGTH_SHORT);
                toast.show();
            }
            return true;
        }
        else {
            return super.onContextItemSelected(item);
        }

        return true;
    }

    private void setSetNumber(final int number){
        this.setNumber = number;
        setActionBarTitle(setNumber, stageName);
    }

    private void setStageName(final String name){
        this.stageName = name;
        setActionBarTitle(setNumber, stageName);
    }

    private void setActionBarTitle(final int setNumber, final String stageName){
        final ActionBar bar = getActionBar();
        if(bar == null){
            return;
        }
        bar.setTitle(getString(R.string.action_set_number_title) + setNumber + "   " + stageName);
    }

    public void onServe(View view){
        onAction(ServeType.SERVE);
    }

    public void onAttack(View view){
        onAction(ServeType.ATTACK);
    }

    public void onBlock(View view){
        onAction(ServeType.BLOCK);
    }

    public void onPass(View view){
        onAction(ServeType.PASS);
    }

    public void onDig(View view){
        onAction(ServeType.DIG);
    }

    public void onAction(ServeType type){
        serveType = type;
        mViewPager.setCurrentItem(2, false);
    }

    public void onTeamMemberClick(final long memberId){
        this.teamMemberId = memberId;
        mViewPager.setCurrentItem(1, false);
    }

    public void onSuccessResult(View view){
        onResult(ResultType.SUCCESS);
    }

    public void onFailResult(View view){
        onResult(ResultType.FAIL);
    }

    public void onRegularResult(View view){
        onResult(ResultType.REGULAR);
    }

    public void onResult(final ResultType type){
        resultType = type;
        final int msg;
        if(teamMemberId == -1){
            mViewPager.setCurrentItem(0, false);
            msg = R.string.action_player_selection_fail_msg;
        }
        else if(serveType == null){
            mViewPager.setCurrentItem(1, false);
            msg = R.string.action_type_selection_fail_msg;
        }
        else{
            db.putGameActivity(gameEntry.getId(), teamMemberId, new Date(), setNumber, serveType, type);
            mViewPager.setCurrentItem(0, false);
            msg = R.string.action_save_success_msg;
        }

        final Toast toast = Toast.makeText(this, getString(msg), Toast.LENGTH_SHORT);
        toast.show();

        serveType = null;
        teamMemberId = -1;
        resultType = null;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(final int position) {
            switch (position){
                case 0:
                    return ServeMembersFragment.newInstance();
                case 1:
                    return ServeTypeFragment.newInstance();
                case 2:
                    return ServeResultFragment.newInstance();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }


    }

    public static class ServeMembersFragment extends Fragment {

        public static ServeMembersFragment newInstance() {
            return new ServeMembersFragment();
        }

        public ServeMembersFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_serve_members, container, false);

            final ListView listView = (ListView)rootView.findViewById(R.id.serve_members_list_view);
            final ArrayAdapter<GamesDatabaseHelper.TeamMemberEntry> adapter = new ArrayAdapter<GamesDatabaseHelper.TeamMemberEntry>(getActivity(), R.layout.serve_members_list_item, ((ServeActivity)getActivity()).members);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ((ServeActivity)getActivity()).onTeamMemberClick(adapter.getItem(position).getId());
                }
            });

            return rootView;
        }
    }

    public static class ServeTypeFragment extends Fragment {

        public static ServeTypeFragment newInstance() {
            return new ServeTypeFragment();
        }

        public ServeTypeFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_serve_type, container, false);
        }
    }

    public static class ServeResultFragment extends Fragment {

        public static ServeResultFragment newInstance() {
            return new ServeResultFragment();
        }

        public ServeResultFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_serve_result, container, false);
        }
    }
}
