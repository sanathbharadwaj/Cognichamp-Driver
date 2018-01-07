package com.anekvurna.pingme;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

public class ViewProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        ParseQuery query = new ParseQuery("Profile");
        query.fromLocalDatastore();
        query.setLimit(1);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                ParseObject object = objects.get(0);
                getTextView(R.id.view_name).setText(object.getString("name"));
                getTextView(R.id.view_address).setText(object.getString("address"));
                getTextView(R.id.view_mobile).setText(object.getString("mobile"));
                getTextView(R.id.view_email).setText(object.getString("email"));
                getTextView(R.id.view_alternate).setText(object.getString("alternateMobile"));
                getTextView(R.id.view_landline).setText(object.getString("landline"));
                getTextView(R.id.view_alpha).setText(object.getString("alphanumeric"));

            }
        });
    }

    public TextView getTextView(int id) {
        return (TextView) findViewById(id);
    }
}
