package com.feer.windcast;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


public class About extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        TextView emailSupportTextView = (TextView) findViewById(R.id.email_support_textview);

        emailSupportTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchEmailSupportIntent();
            }
        });
    }

    private void launchEmailSupportIntent() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "FeerSoft@gmail.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "WindCast Support");
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }
}
