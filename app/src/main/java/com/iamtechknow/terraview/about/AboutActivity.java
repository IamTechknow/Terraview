package com.iamtechknow.terraview.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.iamtechknow.terraview.BuildConfig;
import com.iamtechknow.terraview.R;
import com.iamtechknow.terraview.util.Utils;

/**
 * Supports the user interface meant to show various information about the app.
 */
public class AboutActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String GITHUB_REPO = "http://github.com/IamTechknow/Terraview";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.about_faq).setOnClickListener(this);
        findViewById(R.id.about_main).setOnClickListener(this);
        findViewById(R.id.about_feedback).setOnClickListener(this);
        findViewById(R.id.about_github).setOnClickListener(this);

        TextView body = (TextView) findViewById(R.id.about_ver);
        body.setText(Html.fromHtml(getString(R.string.about_ver, BuildConfig.VERSION_NAME)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Account for android.R.id.home
        finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.about_faq:
                Utils.showWebPage(this, getString(R.string.faq), getString(R.string.faq_title));
                break;
            case R.id.about_main:
                Utils.showAbout(this);
                break;
            case R.id.about_feedback:
                openEmail();
                break;
            case R.id.about_github:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_REPO)));
        }
    }

    private void openEmail() {
        //Send an intent to start Gmail, by using the appropriate intent action and extras.
        Intent feedback = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getString(R.string.email), null))
                .putExtra(Intent.EXTRA_EMAIL, new String[] {getString(R.string.email)})
                .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
        startActivity(feedback);
    }
}
