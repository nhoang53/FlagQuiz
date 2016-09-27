package edu.orangecoastcollege.cs273.nhoang53.flagquiz;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.LogRecord;

/**
 * A placeholder fragment containing a simple view.
 */
public class QuizActivityFragment extends Fragment {
    // String uesed when logging errormessages
    private static final String TAG = "FlagQuiz Activity";
    private static final int FLAGS_IN_QUIZ = 10;

    private List<String> fileNameList; // flag file names
    private List<String> quizCountriesList; // countries in current quiz
    private Set<String> regionsSet; // world regions in current quiz
    private String correctAnswer; // correct country for the current flag
    private int totalGuesses; // number of guesses made
    private int correctAnswers; // number of correct guesses
    private int guessRows; // number of rows displaying guess Buttons
    private SecureRandom random; // used to randomize the quiz
    private Handler handler; // used to delay loading next flag

    private LinearLayout quizLinearLayout; // layout that contains the quiz
    private TextView questionNumberTextView; // shows current question #
    private ImageView flagImageView; // displays a flag
    private LinearLayout[] guessLinearLayouts; // rows of answer Buttons
    private TextView answerTextView; // displays correct answer;


    /**
     * Configures the QuizActivityFragment when its View is created.
     * @param inflater the layout inflater
     * @param container The view group contrain in which the fragment resides
     * @param savedInstanceState Any saved state to restore in this fragment
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);

        fileNameList = new ArrayList<>();
        quizCountriesList = new ArrayList<>();
        random = new SecureRandom();
        handler = new Handler();

        // get references to GUI components
        quizLinearLayout =
                (LinearLayout) view.findViewById(R.id.quizLinearLayout);
        questionNumberTextView = (TextView) view.findViewById(R.id.questionNumberTextView);
        flagImageView = (ImageView) view.findViewById(R.id.flagImageView);
        guessLinearLayouts = new LinearLayout[4];
        guessLinearLayouts[0] = (LinearLayout) view.findViewById(R.id.row1LinearLayout);
        guessLinearLayouts[1] = (LinearLayout) view.findViewById(R.id.row2LinearLayout);
        guessLinearLayouts[2] = (LinearLayout) view.findViewById(R.id.row3LinearLayout);
        guessLinearLayouts[3] = (LinearLayout) view.findViewById(R.id.row4LinearLayout);
        answerTextView = (TextView) view.findViewById(R.id.answerTextView);

        // configure listeners for the guess Buttons
        for(LinearLayout row : guessLinearLayouts) {
            for (int column = 0; column < row.getChildCount(); column++) {
                Button button = (Button) row.getChildAt(column);
                button.setOnClickListener(guessButtonListener);
            }
        }

        // set questionNumberTextView's text
        questionNumberTextView.setText(getString(R.string.question, 1, FLAGS_IN_QUIZ));

        return view; // return the fragment's view for display
        //return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    /**
     * updateGuessRow is called from QuizActivity when the app is launched and each time the
     * user changes the number of guess buttons to display with each flag.
     * @param sharedPreferences The shared preferences from preferences.xml
     */
    public void updateGuessRows(SharedPreferences sharedPreferences){
        // get the number of guess buttons that should be displayed
        String choices = sharedPreferences.getString(QuizActivity.CHOICES, null);
        guessRows = Integer.parseInt(choices) / 2;

        // hide all guess button LinearLayouts
        for(LinearLayout layout : guessLinearLayouts)
            layout.setVisibility(View.GONE);

        // display appropriate guess button LinearLayouts
        for(int row = 0; row < guessRows; row++)
            guessLinearLayouts[row].setVisibility(View.VISIBLE);
    }

    /**
     * Update the world regions for the quiz based on values in SharedPreferences
     * @param sharedPreferences the shared preferences defind in preferences.xml
     */
    public void updateRegions(SharedPreferences sharedPreferences){
        regionsSet = sharedPreferences.getStringSet(QuizActivity.REGIONS, null);
    }

    /**
     * Configure and start up a new quiz based on the settings.
     */
    public void resetQuiz(){
        // use AssetManager to get image file names for aenabled regions
        AssetManager assets = getActivity().getAssets();
        fileNameList.clear(); // empty list of image file names

        try{
            // loop through each region
            for(String region : regionsSet){
                // get a list of all flag image
                String[] paths = assets.list(region);
                System.out.println("")

                for(String path : paths)
                    fileNameList.add(path.replace(".png", ""));
            }
        }
        catch (IOException exception){
            Log.e(TAG, "Error loading image file names", exception);
        }

        correctAnswers = 0; // reset the number of correct answers made
        totalGuesses = 0; // reset the total number of guesses the user made
        quizCountriesList.clear(); // clear prior list of quiz countries

        int flagCounter = 1;
        int numberOfFlags = fileNameList.size();

        // add FLAGS_IN_QUIZ random file names to the quizCountriesList
        while(flagCounter <= FLAGS_IN_QUIZ){
            int randomIndex = random.nextInt(numberOfFlags);

            // get the random file name
            String fileName = fileNameList.get(randomIndex);

            // If the region is enabled and it hans't already been chosen
            if(!quizCountriesList.contains(fileName)){
                quizCountriesList.add(fileName); // add the file to the list
                ++flagCounter;
            }
        }

        loadNextFlag(); // start the quiz by loading the first flag
    }

    /**
     * After user guesses a flag correctly, load the next flag.
     */
    private  void loadNextFlag(){
        // get file name of the next flag and remove it from the list
        String nextImage = quizCountriesList.remove(0); // remove first name (regionName-countryName)
        correctAnswer = nextImage; // update the correct answer
        answerTextView.setText(""); // clear answerTextView

        // display current question number
        questionNumberTextView.setText(getString(R.string.question, (correctAnswers + 1), FLAGS_IN_QUIZ));

        // extract the region from the next image's name ex.Africa-Algeria
        // String.indexOf() will return the index of first occurrence within this string or -1
        String region = nextImage.substring(0, nextImage.indexOf('-'));

        // use AssetManager to load next image from assets folder
        AssetManager assets = getActivity().getAssets();

        // get an InputStream to the asset representing the next flag
        // and try to use the InputStream
        try(InputStream stream = assets.open(region + "/" + nextImage + ".png")){
            // load the asset as a Drawable and display on the flagImageView
            Drawable flag = Drawable.createFromStream(stream, nextImage);
            flagImageView.setImageDrawable(flag);
        }
        catch (IOException exception){
            Log.e(TAG, "Error loading" + nextImage, exception);
        }

        Collections.shuffle(fileNameList); // shuffle file names

        // put the correct answer at the end of fileNameList
        int correct = fileNameList.indexOf(correctAnswer);
        fileNameList.add(fileNameList.remove(correct)); // ???

        // add 2, 4, 6, or 8 guess Buttons based on the value of guessRows
        for(int row = 0; row < guessRows; row++){
            // place Buttons in currentTableRow
            for(int column = 0; column < guessLinearLayouts[row].getChildCount(); column++){
                // get reference to Button to configure
                Button newGuessButton = (Button) guessLinearLayouts[row].getChildAt(column);
                newGuessButton.setEnabled(true);

                // get country name and set it as newGuessbutton's text
                String fileName = fileNameList.get((row * 2) + column);
                newGuessButton.setText(getCountryName(fileName));
            }
        }

        // randomly replace one Button with the correct answer
        int row = random.nextInt(guessRows); // pick random row
        int column = random.nextInt(2); // pick random column
        LinearLayout randomRow = guessLinearLayouts[row]; // get the row
        String countryName = getCountryName(correctAnswer);
        ((Button) randomRow.getChildAt(column)).setText(countryName);
    }

    /**
     * Cutting the country name out of the name file
     * @param fileName
     * @return string country  name
     */
    private String getCountryName(String fileName){
        return fileName.substring(fileName.indexOf('-') + 1).replace('-', ' ');
    }

    /**
     * Called when a guess button is clicked. this listener is uesd for all buttons
     * in the flag quiz
     */
    private View.OnClickListener guessButtonListener = new View.OnClickListener() {
        /**
         * Occur when guess button is clicked
         * @param v
         */
        @Override
        public void onClick(View v) {
            Button guessButton = ((Button) v);
            String guess = guessButton.getText().toString();
            String answer = getCountryName(correctAnswer);
            ++totalGuesses; // increment number of guesses that the user has made

            if(guess.equals(answer)){ // if the guess is correct
                ++correctAnswers; // increment the number of correct answers

                // display correct answer in green text
                answerTextView.setText(answer + "!");
                answerTextView.setTextColor(
                        getResources().getColor(R.color.correct_answer,
                                getContext().getTheme()));

                disableButtons(); // disable all guess Buttons

                loadNextFlag();

                // if the user has correctly identified FLAGS_IN_QUIZ flags
                if(correctAnswers == FLAGS_IN_QUIZ){
                    // DialogFragment to display quiz stats and start new quiz
                    DialogFragment quizResults = new DialogFragment(){
                        // create an AlertDialog and return it
                        @Override
                        public Dialog onCreateDialog(Bundle bundle){
                            AlertDialog.Builder builder =
                                    new AlertDialog.Builder(getActivity());
                            builder.setMessage(getString(R.string.results,
                                    totalGuesses, (1000 / (double) totalGuesses)));

                            // "Reset Quiz" Button
                            builder.setPositiveButton(R.string.reset_quiz,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            resetQuiz();
                                        }
                                    }
                            );

                            return builder.create(); // return the AlerDialog
                        }
                    };
                }
            }
            else{
                // display "Incorrect!" in red
                answerTextView.setText(R.string.incorrect_answer);
                answerTextView.setTextColor(getResources().getColor(
                        R.color.incorrect_answer, getContext().getTheme()));
                guessButton.setEnabled(false); // disable incorrect answer
            }
        }
    };

    // utility method that disables all answer Buttons
    private void disableButtons() {
        for (int row = 0; row < guessRows; row++) {
            LinearLayout guessRow = guessLinearLayouts[row];
            for (int i = 0; i < guessRow.getChildCount(); i++)
                guessRow.getChildAt(i).setEnabled(false);
        }
    }

}
