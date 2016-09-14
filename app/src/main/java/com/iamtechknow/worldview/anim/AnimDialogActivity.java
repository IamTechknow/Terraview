package com.iamtechknow.worldview.anim;

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
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.iamtechknow.worldview.R;
import com.iamtechknow.worldview.util.Utils;

import java.util.Calendar;

public class AnimDialogActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
    public static final String ANIM_ARG = "anim", FROM_EXTRA = "day", TO_EXTRA = "month", INTERVAL_EXTRA = "year",
                               LOOP_EXTRA = "loop", SAVE_EXTRA = "save";
    public static final int DAY = 0, MONTH = 1, YEAR = 2;

    private enum DateState {
        NONE, FROM, TO
    }

    private TextView from, to;
    private RadioButton day, month, year;
    private CheckBox loop, saveGIF;
    private DatePickerDialog mDateDialog;

    //Save the text view that was selected
    private DateState dateState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anim);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        from = (TextView) findViewById(R.id.from_date);
        to = (TextView) findViewById(R.id.to_date);
        from.setOnClickListener(this);
        to.setOnClickListener(this);

        day = (RadioButton) findViewById(R.id.day_button);
        month = (RadioButton) findViewById(R.id.month_button);
        year = (RadioButton) findViewById(R.id.year_button);

        loop = (CheckBox) findViewById(R.id.loop_checkbox);
        saveGIF = (CheckBox) findViewById(R.id.save_checkbox);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        mDateDialog = new DatePickerDialog(this, this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        //Get date and set text
        String date = getIntent().getStringExtra(ANIM_ARG);
        from.setText(String.format("%s   ", date));
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
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.from_date:
                dateState = DateState.FROM;
                break;

            default:
                dateState = DateState.TO;
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

        if(dateState == DateState.FROM)
            from.setText(String.format("%s   ", Utils.parseDate(c.getTime())));
        else
            to.setText(String.format("%s   ", Utils.parseDate(c.getTime())));

        dateState = DateState.NONE;
    }

    /**
     * Get the current states of the dialog's UI elements and save them to the intent bundle.
     * @return intent to be sent as the activity result
     */
    private Intent getResult() {
        int interval = day.isChecked() ? DAY : month.isChecked() ? MONTH : YEAR;

        Intent result = new Intent();
        result.putExtra(FROM_EXTRA, from.getText())
            .putExtra(TO_EXTRA, to.getText())
            .putExtra(LOOP_EXTRA, loop.isChecked())
            .putExtra(SAVE_EXTRA, saveGIF.isChecked())
            .putExtra(INTERVAL_EXTRA, interval);

        return result;
    }

    /**
     * Check the dialog's UI elements to see if an animation may be created and if not warn the user
     * @return Whether the dialog is incomplete
     */
    private boolean dialogStateOK() {
        if(to.getText().length() == 0) { //to date never touched
            warnUserIncomplete();
            return false;
        } else if(day.isChecked() && from.getText().toString().equals(to.getText().toString())) { //same dates
            warnUserSameDates();
            return false;
        }

        return true;
    }

    /**
     * Warn the user the from and to date fields can't be empty.
     */
    private void warnUserIncomplete() {
        Snackbar.make(findViewById(R.id.thelayout), R.string.anim_warning, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Warn the user the from and to date fields can't be the same
     */
    private void warnUserSameDates() {
        Snackbar.make(findViewById(R.id.thelayout), R.string.anim_warning_same, Snackbar.LENGTH_LONG).show();
    }
}
