package arh.ezbudget;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import static arh.ezbudget.R.layout.activity_add_transaction;

/**
 * Created by Matthew on 4/15/2015.
 */
public class EditUser extends Activity {
    boolean pressedonce = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        final EditText username = (EditText) findViewById(R.id.username);
        final EditText pw = (EditText) findViewById(R.id.pw);
        final EditText email = (EditText) findViewById(R.id.email);

        Button delete = (Button) findViewById(R.id.delete);
        Button save = (Button) findViewById(R.id.save);

        final ParseUser currentUser = ParseUser.getCurrentUser();
        username.setText(currentUser.getUsername());
        email.setText(currentUser.getEmail());


        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pressedonce) {
                    Toast.makeText(getBaseContext(),
                            "This will permanently delete your account, press again if you are sure you want to delete your account.",
                            Toast.LENGTH_LONG).show();
                } else {
                    //Delete account and kill app here
                    Toast.makeText(getBaseContext(),
                            "Your account will be deleted.",
                            Toast.LENGTH_LONG).show();
                    currentUser.deleteInBackground();

                }
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username.getText();
                currentUser.setUsername(username.getText().toString());
                currentUser.setEmail(email.getText().toString());

                if (pw.getText().length() > 0) {
                    currentUser.setPassword(pw.getText().toString());
                }

                currentUser.saveInBackground();
                Toast.makeText(getBaseContext(),
                        "Your changes have been saved.",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });

    }
}
