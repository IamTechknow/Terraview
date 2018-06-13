package com.iamtechknow.terraview.anim;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
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
    public static final String ANIM_ARG = "anim", LAYER_EXTRA = "layer";
    private static final String URL_BASE = "https://worldview.earthdata.nasa.gov/?p=geographic&", URL_AB = "ab=on", URL_L = "l=",
                                URL_AS = "as=", URL_AE = "ae=", URL_AV = "av=", URL_AL = "al=", URL_INTERVAL = "z=", TEXT = "text/plain";
    public static final int DAY = 1, MONTH = 2, YEAR = 3, SPEED_OFFSET = 1;

    private enum DateState {
        NONE, START, END
    }

    private TextView start, end;
    private RadioButton day, month;
    private CheckBox loop;
    private DatePickerDialog mDateDialog;
    private SeekBar seekBar;

    //Save the text view that was selected
    private DateState dateState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anim);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        start = findViewById(R.id.start_date);
        end = findViewById(R.id.end_date);
        start.setOnClickListener(this);
        end.setOnClickListener(this);

        day = findViewById(R.id.day_button);
        month = findViewById(R.id.month_button);
        loop = findViewById(R.id.loop_checkbox);
        seekBar = findViewById(R.id.anim_speed);

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
                if(dialogStateOK())
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getURL())));
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
        start.setText(extras.getStringExtra(ANIM_ARG));
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
        Intent intent = new Intent(Intent.ACTION_SEND)
            .setType(TEXT)
            .putExtra(Intent.EXTRA_TEXT, getURL());
        startActivity(Intent.createChooser(intent, getString(R.string.share_url)));
    }

    private String getURL() {
        String startStr = Utils.parseDate(Utils.parseDialogDate(start.getText().toString())),
                endStr = Utils.parseDate(Utils.parseDialogDate(end.getText().toString()));
        return encodeURL(getIntent().getParcelableArrayListExtra(LAYER_EXTRA), startStr, endStr, seekBar.getProgress() + SPEED_OFFSET, loop.isChecked());
    }

    /**
     * Encode Worldview URL for the current animation settings
     */
    private String encodeURL(ArrayList<Layer> layers, String start, String end, int speed, boolean loop) {
        StringBuilder result = new StringBuilder(URL_BASE).append(URL_L);
        for(Layer l : layers)
            result.append(l.getIdentifier()).append(',');
        result.deleteCharAt(result.length() - 1).append('&'); //delete trailing comma

        return result + URL_INTERVAL + getInterval() + '&' + URL_AB + '&' +
                URL_AS + start + '&' + URL_AE + end + '&' +
                URL_AV + Math.min(speed, 10) + '&' + URL_AL + loop;
    }

    private int getInterval() {
        if(day.isChecked())
            return DAY;
        else if(month.isChecked())
            return MONTH;
        else //No need to explicitly check
            return YEAR;
    }
}
