package com.project.group.projectga;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.*;

public class MainActivity extends AppCompatActivity {

    String message; // input used for the test, this is saved to the database by the save button
    String result;  // string retrieved from the database, this is displayed by the load button
    DatabaseReference DbReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // reference to "message" location in the database
        DbReference = FirebaseDatabase.getInstance().getReference("message");

        // Read from the database when "message" location is changed
        DbReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                result = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
    }

    /**
     * Called when the user taps the Test button
     */
    public void saveButton(View view) {
        // save the contents of the editText field to the "message" location of the database
        EditText editText = (EditText) findViewById(R.id.editText);
        message = editText.getText().toString();

        DbReference.setValue(message);
    }

    public void loadButton(View view) {
        // display the contents of the "message" location in the textView
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(result);
    }
}