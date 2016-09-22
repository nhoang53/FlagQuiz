package edu.orangecoastcollege.cs273.nhoang53.flagquiz;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class QuizActivity extends AppCompatActivity {
    // key for reading data from SharedPreferences
    public static final String CHOICES = "pref_numberOfChoices";
    public static final String REGIONS = "pref_regionsToInclude";

    private boolean phoneDevice = true; // used to force portrait mode
    private boolean preferencesChanged = true; // did preferences change???

    /**
     * onCreate generates the appropriate layout to infalte, depending on the
     * screen size. If the device is large or x-large, it will load the content_main.xml
     * (sw700dp-land) which includes both the fragment_quiz.xml and fragment_setting.xml.
     * Otherwise, it just inflates the standard content_main.xml with the fragment_quiz.
     *
     * All defaut reference are set using the preferences.xml file
     * @param savedInstanceState The saved state to restore (not being used)
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // set default values in the app's SharedPreferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // register listener for SharedPreferences changes
        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener((preferenceschangeListener));

        // determine screen size
        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;

        // if device is a tablet, set phone Device to false
        if(screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE ||
                screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE)
            phoneDevice = false; // not a phone-sized device

        // if running on phone-sized device, allow only portrait orientation
        if(phoneDevice)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // get the device's urrent orientation
        int orientation = getResources().getConfiguration().orientation;

        // display the app's menu only in portrait orientation
        if(orientation == Configuration.ORIENTATION_PORTRAIT){
            // inflate the menu
            getMenuInflater().inflate(R.menu.menu_quiz, menu);
            return true;
        }
        else
            return false;
    }

    /**
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*// Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
*/
        Intent preferencesIntent = new Intent(this, SettingActivity.class);
        startActivity(preferencesIntent);
        return super.onOptionsItemSelected(item);
    }

    /**
     * onStart is called after onCreate completes execution.
     * This method will update the number of guess rows to display
     * and the regions to choose flags from, then resets the quiz with
     * the new preferences.
     */
    @Override
    protected void onStart(){
        super.onStart();

        if(preferencesChanged){
            // now that the default preferences have been set,
            // initialize QuizActivityFragment and start the quiz
            QuizActivityFragment quizFragment = (QuizActivityFragment)
                    getSupportFragmentManager().findFragmentById(R.id.quizFragment);
            quizFragment.updateGuessRows(
                    PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.updateRegions(
                    PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.resetQuiz();
            preferencesChanged = false;
            )

        }
    }
}
