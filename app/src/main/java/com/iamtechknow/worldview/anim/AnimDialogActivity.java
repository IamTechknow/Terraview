package com.iamtechknow.worldview.anim;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.iamtechknow.worldview.R;

public class AnimDialogActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String ANIM_ARG = "anim";

    private TextView from, to;
    private RadioButton day, month, year;
    private CheckBox loop, saveGIF;

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

        //Get date and set text
        String date = getIntent().getStringExtra(ANIM_ARG);
        from.setText(date);
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
                setResult(RESULT_OK, null);
                finish();
        }
        return true;
    }

    @Override
    public void onClick(View v) {

    }
}
