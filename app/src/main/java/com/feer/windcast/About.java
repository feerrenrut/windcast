package com.feer.windcast;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class About extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setupActionBar();

        TextView emailSupportTextView = (TextView) findViewById(R.id.email_support_textview);
        emailSupportTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchEmailSupportIntent();
            }
        });

        ImageView bomImage = (ImageView) findViewById(R.id.bom_attrib_imageView);
        bomImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchBOMWebsite();
            }
        });
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void launchBOMWebsite() {
        //launch website
        String urlString = "http://www.bom.gov.au/data-access/3rd-party-attribution.shtml";
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
        startActivity(browserIntent);
    }

    private void launchEmailSupportIntent() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "FeerSoft@gmail.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "WindCast Support");
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }
}
