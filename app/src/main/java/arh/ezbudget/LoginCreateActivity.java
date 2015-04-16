package arh.ezbudget;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import java.util.List;

/**
 * Created by and20_000 on 3/22/2015.
 */
public class LoginCreateActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_create);
        setTitle(R.string.loginTitle);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.logout) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // stops the users from leaving without log in in
    @Override
    public void onBackPressed() {
        errorMessageDialog(R.string.loginCreateError);
    }

    // displays error dialog with msg and sets view to red
    private void errorMessageRed(final View view, int msg)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                view.setBackgroundColor(Color.RED);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    // displays error dialog with msg
    private void errorMessageDialog(int msg)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    // sets user, loads user transactions, calls return to main
    public void setUser()
    {
        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Transaction");
        //query.addDescendingOrder("dateCode");
        query.whereEqualTo("userID", currentUser.getObjectId());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                if (e == null) {
                    //Log.d("score", "Retrieved " + parseObjects.size() + " scores");
                    for (ParseObject po : parseObjects)
                    {
                        try {
                            po.pin("datastore");
                        } catch (com.parse.ParseException e1) {
                            e1.printStackTrace();
                        }
                    }
                    returnToMain();
                } else {
                    //Log.d("score", "Error: " + e.getMessage());
                    errorMessageDialog(R.string.unknownError);
                }
            }
        });
    }

    // returns to caller
    public void returnToMain()
    {
        int mode = Activity.MODE_PRIVATE;
        SharedPreferences mySharedPreferences = getSharedPreferences("EZbudget_preferences", mode);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.commit();
        finish();
    }

    // creates user after field validation, calls set user
    public void onCreatePressed(View view)
    {
        final EditText name = (EditText)findViewById(R.id.editTextName);
        final EditText email = (EditText)findViewById(R.id.editTextEmail);
        final EditText pass = (EditText)findViewById(R.id.editTextPassword);
        final EditText repass = (EditText)findViewById(R.id.editTextRepeatPassword);

        // verify input is present
        if(name.getText().toString().equals(""))
        {
            errorMessageRed(name, R.string.noNameError);
        }
        else if(email.getText().toString().equals(""))
        {
            errorMessageRed(email, R.string.noEmailError);
        }
        else if(pass.getText().toString().equals(""))
        {
            errorMessageRed(pass, R.string.noPasswordError);
        }
        else if(repass.getText().toString().equals(""))
        {
            errorMessageRed(repass, R.string.noRepasswordError);
        }
        else if(!repass.getText().toString().equals(pass.getText().toString()))
        {
            errorMessageRed(repass, R.string.passwordMismatchError);
        }
        else {
            // create user
            ParseUser user = new ParseUser();
            user.setUsername(name.getText().toString());
            user.setPassword(pass.getText().toString());
            user.setEmail(email.getText().toString());

            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(com.parse.ParseException e) {
                    if (e == null) {
                        // Hooray! Let them use the app now.
                        setUser();
                    } else {
                        // Sign up didn't succeed. Look at the ParseException
                        // to figure out what went wrong
                        int code = e.getCode();
                        switch (code) {
                            case 100: errorMessageDialog(R.string.noInternetError);break;// unable to connect
                            case 125: errorMessageRed(email, R.string.invalidEmailError);break;// invalid email
                            case 202: errorMessageRed(name, R.string.nameTakenError);break;// name taken
                            case 203: errorMessageRed(email, R.string.emailTakenError);break;// email taken
                        }
                    }
                }
            });
        }
    }

    // logs in in user if credentials are valid, calls set user
    public void onLoginPressed(View view)
    {
        final EditText name = (EditText)findViewById(R.id.editTextName);
        final EditText pass = (EditText)findViewById(R.id.editTextPassword);

        if(name.getText().toString().equals(""))
        {
            errorMessageRed(name, R.string.noEmailError);
        }
        else if(pass.getText().toString().equals(""))
        {
            errorMessageRed(pass, R.string.noPasswordError);
        }
        else
        {
            ParseUser.logInInBackground(name.getText().toString(), pass.getText().toString(), new LogInCallback() {
                @Override
                public void done(ParseUser parseUser, com.parse.ParseException e) {
                    if (parseUser != null) {
                        // Hooray! The user is logged in.
                        setUser();
                    } else {
                        // Signup failed. Look at the ParseException to see what happened.
                        if(e.getCode() == 101)//object not found
                        {
                            errorMessageDialog(R.string.invalidCredentialsError);
                        }
                    }
                }
            });
        }
    }
}
