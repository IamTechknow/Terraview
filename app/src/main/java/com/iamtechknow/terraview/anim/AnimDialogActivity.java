package com.iamtechknow.terraview.anim;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.iamtechknow.terraview.R;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.util.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;

public class AnimDialogActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
    public static final String ANIM_ARG = "anim", START_EXTRA = "start", END_EXTRA = "end", INTERVAL_EXTRA = "year",
                               LOOP_EXTRA = "loop", SAVE_EXTRA = "save", SPEED_EXTRA = "speed", LAYER_EXTRA = "layer";
    private static final String URL_BASE = "https://worldview.earthdata.nasa.gov/?p=geographic&", URL_AB = "ab=on", URL_L = "l=",
                                URL_AS = "as=", URL_AE = "ae=", URL_AV = "av=", URL_AL = "al=", TEXT = "text/plain";
    public static final int DAY = 0, MONTH = 1, YEAR = 2, WEEK = 3, SPEED_OFFSET = 1, DEFAULT_SPEED = 30;

    private enum DateState {
        NONE, START, END
    }

    private TextView start, end;
    private RadioButton day, month, year, week;
    private CheckBox loop, saveGIF;
    private DatePickerDialog mDateDialog;
    private SeekBar seekBar;

    //Save the text view that was selected
    private DateState dateState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anim);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        start = (TextView) findViewById(R.id.start_date);
        end = (TextView) findViewById(R.id.end_date);
        start.setOnClickListener(this);
        end.setOnClickListener(this);

        day = (RadioButton) findViewById(R.id.day_button);
        month = (RadioButton) findViewById(R.id.month_button);
        year = (RadioButton) findViewById(R.id.year_button);
        week = (RadioButton) findViewById(R.id.week_button);

        loop = (CheckBox) findViewById(R.id.loop_checkbox);
        saveGIF = (CheckBox) findViewById(R.id.save_checkbox);

        seekBar = (SeekBar) findViewById(R.id.anim_speed);

        Calendar c = Calendar.getInstance();
        Utils.getCalendarMidnightTime(c);
        mDateDialog = new DatePickerDialog(this, this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        //Restore prior settings, or get date and set text
        initDialog(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.anim, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.anim_start:
                if(dialogStateOK()) {
                    setResult(RESULT_OK, getResult());
                    finish();
                }
                break;
            case R.id.anim_url:
                if(dialogStateOK())
                    shareURL();
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.start_date:
                dateState = DateState.START;
                break;

            default:
                dateState = DateState.END;
        }
        mDateDialog.show();
    }

    /**
     * Get the date selected and set the text view of the date previously pressed.
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, dayOfMonth);

        if(dateState == DateState.START)
            start.setText(Utils.parseDateForDialog(c.getTime()));
        else
            end.setText(Utils.parseDateForDialog(c.getTime()));

        dateState = DateState.NONE;
    }

    private void initDialog(Intent extras) {
        if(extras.getStringExtra(START_EXTRA) != null) {
            start.setText(extras.getStringExtra(START_EXTRA));
            end.setText(extras.getStringExtra(END_EXTRA));
            seekBar.setProgress(extras.getIntExtra(SPEED_EXTRA, DEFAULT_SPEED - SPEED_OFFSET) - SPEED_OFFSET);
            loop.setChecked(extras.getBooleanExtra(LOOP_EXTRA, false));

            switch(extras.getIntExtra(INTERVAL_EXTRA, DAY)) {
                case DAY:
                    day.setChecked(true);
                    break;

                case MONTH:
                    month.setChecked(true);
                    break;

                case WEEK:
                    week.setChecked(true);
                    break;

                default:
                    year.setChecked(true);
            }
        } else
            start.setText(extras.getStringExtra(ANIM_ARG));
    }

    /**
     * Get the current states of the dialog's UI elements and save them end the intent bundle.
     * @return intent end be sent as the activity result
     */
    private Intent getResult() {
        int interval = day.isChecked() ? DAY : month.isChecked() ? MONTH : YEAR;
        String startStr = Utils.parseDate(Utils.parseDialogDate(start.getText().toString()));
        String endStr = Utils.parseDate(Utils.parseDialogDate(end.getText().toString()));

        Intent result = new Intent();
        result.putExtra(START_EXTRA, startStr)
            .putExtra(END_EXTRA, endStr)
            .putExtra(LOOP_EXTRA, loop.isChecked())
            .putExtra(SAVE_EXTRA, saveGIF.isChecked())
            .putExtra(INTERVAL_EXTRA, interval)
            .putExtra(SPEED_EXTRA, seekBar.getProgress() + SPEED_OFFSET); //1 - 30 FPS

        return result;
    }

    /**
     * Check the dialog's UI elements end see if an animation may be created and if not warn the user
     * @return Whether the dialog is incomplete
     */
    private boolean dialogStateOK() {
        if(end.getText().length() == 0) { //end date never touched
            warnUserIncomplete();
            return false;
        } else if(areDatesNotInOrder(start.getText().toString(), end.getText().toString())) { //same dates
            warnUserAboutDates();
            return false;
        }

        return true;
    }

    private boolean areDatesNotInOrder(String start, String end) {
        Date startDate = Utils.parseDialogDate(start), endDate = Utils.parseDialogDate(end);
        return startDate.compareTo(endDate) >= 0;
    }

    /**
     * Warn the user the start and end date fields can't be empty.
     */
    private void warnUserIncomplete() {
        Snackbar.make(findViewById(R.id.thelayout), R.string.anim_warning, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Warn the user the start and end date fields can't be the same
     */
    private void warnUserAboutDates() {
        Snackbar.make(findViewById(R.id.thelayout), R.string.anim_warning_before, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Create an intent chooser to allow the user to send the URL or to copy to the clipboard.
     * Note: Animations are not supported on the mobile web interface at this time.
     */
    private void shareURL() {
        String startStr = Utils.parseDate(Utils.parseDialogDate(start.getText().toString())),
        endStr = Utils.parseDate(Utils.parseDialogDate(end.getText().toString())),
        result = encodeURL(getIntent().getParcelableArrayListExtra(LAYER_EXTRA), startStr, endStr, seekBar.getProgress() + SPEED_OFFSET, loop.isChecked());

        Intent intent = new Intent(Intent.ACTION_SEND)
            .setType(TEXT)
            .putExtra(Intent.EXTRA_TEXT, result);
        startActivity(Intent.createChooser(intent, getString(R.string.share_url)));
    }

    /**
     * Encode Worldview URL for the current animation settings
     */
    private String encodeURL(ArrayList<Layer> layers, String start, String end, int speed, boolean loop) {
        StringBuilder result = new StringBuilder(URL_BASE).append(URL_L);
        for(Layer l : layers)
            result.append(l.getIdentifier()).append(',');
        result.deleteCharAt(result.length() - 1).append('&'); //delete trailing comma

        return result + URL_AB + '&' +
                URL_AS + start + '&' + URL_AE + end + '&' +
                URL_AV + Math.min(speed, 10) + '&' + URL_AL + loop;
    }
}
