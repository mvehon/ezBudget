package arh.ezbudget;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import java.util.Calendar;

import static arh.ezbudget.R.layout.activity_add_transaction;


public class AddTransactionActivity extends ActionBarActivity {

    private TransType type;
    private Boolean editMode;
    private ParseObject editObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_add_transaction);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // set title label and populate spinners

        final String[] CATEGORIES_DEBITS = { getString(R.string.selectCat),
                getString(R.string.bill), getString(R.string.groceries), getString(R.string.alcohol),
                getString(R.string.tobacco), getString(R.string.medical), getString(R.string.dining),
                getString(R.string.entertainment), getString(R.string.hobby), getString(R.string.other)};

        final String[] CATEGORIES_CREDITS = { getString(R.string.selectCat),getString(R.string.paycheck),
                getString(R.string.grant), getString(R.string.gift), getString(R.string.annuity),
                getString(R.string.other)};

        final String[] RECURRING = { getString(R.string.notRecurring), getString(R.string.daily),
                getString(R.string.weekly), getString(R.string.biweekly), getString(R.string.monthly),
                getString(R.string.bimonthly), getString(R.string.quarterly), getString(R.string.twiceAnnually),
                getString(R.string.annually)};

        Intent caller = getIntent();
        type = (TransType)caller.getSerializableExtra("Type");
        Spinner catSpinner = (Spinner)findViewById(R.id.spinnerCategory);
        switch(type)
        {
            case CREDIT:
                setTitle(R.string.add_credit);
                ArrayAdapter<String> adapterC = new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_item, CATEGORIES_CREDITS);
                catSpinner.setAdapter(adapterC);
                break;
            case DEBIT:
                setTitle(R.string.add_debit);
                ArrayAdapter<String> adapterD = new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_item, CATEGORIES_DEBITS);
                catSpinner.setAdapter(adapterD);
                break;
        }

        Spinner recSpinner = (Spinner)findViewById(R.id.spinnerRecurring);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, RECURRING);
        recSpinner.setAdapter(adapter);

        editMode = caller.getBooleanExtra("editMode", false);
        if(editMode)
        {
            editObject = TransactionOperations.getEditObject();
            setTitle(R.string.editTrans);

            EditText desc = (EditText)findViewById(R.id.editTextDesc);
            EditText amt = (EditText)findViewById(R.id.editTextAmount);
            DatePicker dp = (DatePicker)findViewById(R.id.datePicker);
            Button btn = (Button)findViewById(R.id.buttonCreateTrans);

            int cat = editObject.getInt("category");
            if(type == TransType.CREDIT)
                cat -= Category.getInt(Category.OTHER_DEBIT);
            catSpinner.setSelection(cat);
            recSpinner.setSelection(editObject.getInt("interval"));
            desc.setText(editObject.getString("desc"));
            amt.setText(Double.toString(editObject.getDouble("amount")));
            Calendar cal = TransactionOperations.getCalendarFromCode(editObject.getInt("dateCode"));
            dp.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            btn.setText(R.string.edit);
        }
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
        if (id == R.id.edituser) {
            startActivity(new Intent(AddTransactionActivity.this, EditUser.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    public void onCreateTransPressed(View view)
    {
        final Spinner cat = (Spinner)findViewById(R.id.spinnerCategory);
        final Spinner rec = (Spinner)findViewById(R.id.spinnerRecurring);
        final EditText desc = (EditText)findViewById(R.id.editTextDesc);
        final EditText amt = (EditText)findViewById(R.id.editTextAmount);
        final DatePicker dp = (DatePicker)findViewById(R.id.datePicker);

        // valid input
        if(cat.getSelectedItemPosition() == 0)
        {
            errorMessageRed(cat, R.string.catError);
        }
        else if(amt.getText().toString().equals(""))
        {
            errorMessageRed(amt, R.string.amtError);
        }
        else
        {

            int category = cat.getSelectedItemPosition();
            if(type == TransType.CREDIT)
                category += Category.getInt(Category.OTHER_DEBIT);
            Interval interval = Interval.getInterval(rec.getSelectedItemPosition());

            double amount = Double.parseDouble(amt.getText().toString());
            // date
            Calendar calStart = Calendar.getInstance();

            calStart.set(dp.getYear(),dp.getMonth(), dp.getDayOfMonth());

            Calendar calEnd = (Calendar)calStart.clone();


            int dateCode = TransactionOperations.getDateCode(calStart);
            int nextDateCode = TransactionOperations.getNextOccurrenceDateCode(calEnd, interval);

            Boolean isRecurring = false;
            if(interval != Interval.ONCE)
            {
                isRecurring = true;
            }
            // parse object
            ParseObject parseTrans;
            if(editMode)
                parseTrans = editObject;
            else
                parseTrans = new ParseObject("Transaction");
            parseTrans.put("type", TransType.getInt(type));
            parseTrans.put("desc", desc.getText().toString());
            parseTrans.put("amount", amount);
            parseTrans.put("category", category);
            parseTrans.put("interval", Interval.getInt(interval));
            parseTrans.put("dateCode", dateCode);
            parseTrans.put("nextDateCode", nextDateCode);
            parseTrans.put("isRecurring", isRecurring);
            parseTrans.put("userID", ParseUser.getCurrentUser().getObjectId());

            parseTrans.put("transDate", calStart.getTime());

            parseTrans.saveEventually();
            try {
                parseTrans.pin("datastore");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            setResult(RESULT_OK, null);
            finish();
        }
    }
}
