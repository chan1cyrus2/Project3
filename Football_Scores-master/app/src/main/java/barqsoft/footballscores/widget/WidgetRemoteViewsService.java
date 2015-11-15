package barqsoft.footballscores.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * Created by chan1cyrus2 on 11/14/2015.
 */
public class WidgetRemoteViewsService extends RemoteViewsService{
    public static final String LOG_TAG = WidgetRemoteViewsService.class.getSimpleName();
    private static final String[] FOOTBALL_COLUMNS = {
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.MATCH_ID
    };

    static final int INDEX_DATE = 0;
    static final int INDEX_TIME = 1;
    static final int INDEX_HOME = 2;
    static final int INDEX_AWAY = 3;
    static final int INDEX_HOME_GOALS = 4;
    static final int INDEX_AWAY_GOALS = 5;
    static final int INDEX_MATCH_ID = 6;

    public static final String EXTRA_ID = "barqsoft.footballscores.EXTRA_ID";
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                Log.v(LOG_TAG, "onDataSetChanged called");
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                String[] dateArgs = new String[1];
                //Take Today scores for the widget only
                Date formatDate = new Date(System.currentTimeMillis());
                SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                dateArgs[0] = mformat.format(formatDate);
                data = getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(),
                        FOOTBALL_COLUMNS,
                        null,
                        dateArgs,
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_list_item);

                String date = data.getString(INDEX_DATE);
                String time = data.getString(INDEX_TIME);
                String home = data.getString(INDEX_HOME);
                String away = data.getString(INDEX_AWAY);
                int homeGoal = data.getInt(INDEX_HOME_GOALS);
                int awayGoal = data.getInt(INDEX_AWAY_GOALS);
                int matchID = data.getInt(INDEX_MATCH_ID);

                views.setTextViewText(R.id.home_name, home);
                views.setTextViewText(R.id.away_name, away);
                views.setTextViewText(R.id.date_textview, time);
                views.setTextViewText(R.id.score_textview, Utilies.getScores(homeGoal, awayGoal));

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    final Intent fillInIntent = new Intent();
                    fillInIntent.putExtra(EXTRA_ID, matchID);
                    views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                }
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
