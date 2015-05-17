package volley.volleyball.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import volley.volleyball.database.GamesDatabaseHelper;
import volley.volleyball.adapters.PlayerStatsAdapter;
import volley.volleyball.R;
import volley.volleyball.ServeType;


public class PlayerServeStatsActivity extends Activity {

    public static final String GAME_ID_PARAM_NAME = "game_id";

    SectionsPagerAdapter mSectionsPagerAdapter;

    ViewPager mViewPager;

    long gameId;

    List<GamesDatabaseHelper.PlayerStatsEntry> stats;

    ServeType serveType = ServeType.SERVE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_serve_stats);
        final ActionBar bar = getActionBar();
        if(bar != null){
            bar.setIcon(R.mipmap.stats);
        }

        gameId = getIntent().getLongExtra(GAME_ID_PARAM_NAME, -1);
        if(gameId == -1){
            finish();
        }

        final GamesDatabaseHelper dbHelper = new GamesDatabaseHelper(this);
        stats = Collections.unmodifiableList(dbHelper.fetchPlayerStats(gameId));
        dbHelper.close();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(final int position) {
                setCurrentServeType(ServeType.valueOf(position));
            }
        });
        mViewPager.setAdapter(mSectionsPagerAdapter);
        setCurrentServeType(serveType);
    }

    private void setCurrentServeType(final ServeType serveType){
        getActionBar().setTitle(serveType.name() + "   Statistics");
    }

    private List<GamesDatabaseHelper.PlayerStatsEntry> filterStats(final ServeType serveType){
        final List<GamesDatabaseHelper.PlayerStatsEntry> filtered = new ArrayList<GamesDatabaseHelper.PlayerStatsEntry>();

        for(GamesDatabaseHelper.PlayerStatsEntry entry : stats){
            if(entry.getServeType().equals(serveType)){
                filtered.add(entry);
            }
        }
        return filtered;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return StatTableFragment.newInstance(ServeType.valueOf(position));
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }

    public static class StatTableFragment extends Fragment {

        private static final String SERVE_TYPE_ARG = "serve_type";

        public static StatTableFragment newInstance(ServeType serveType) {
            final StatTableFragment fragment = new StatTableFragment();
            final Bundle args = new Bundle();
            args.putString(SERVE_TYPE_ARG, serveType.name());
            fragment.setArguments(args);
            return fragment;
        }

        public StatTableFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_player_serve_stats, container, false);

            final ServeType serveType = ServeType.valueOf(getArguments().getString(SERVE_TYPE_ARG));

            final ListView listView = (ListView)rootView.findViewById(R.id.player_serve_stats_list_view);
            final LinearLayout headerView = (LinearLayout)inflater.inflate(R.layout.player_serve_stats_list_item, null);
            setChildBackground(headerView, Color.GRAY);
            listView.addHeaderView(headerView);

            final PlayerServeStatsActivity activity = (PlayerServeStatsActivity)getActivity();
            final PlayerStatsAdapter adapter = new PlayerStatsAdapter(activity, R.layout.player_serve_stats_list_item, activity.filterStats(serveType));
            listView.setAdapter(adapter);

            return rootView;
        }

        private void setChildBackground(final LinearLayout view, final int color){
            for(int i = 0; i < view.getChildCount(); i++){
                view.getChildAt(i).setBackgroundColor(color);
            }
        }
    }

}
