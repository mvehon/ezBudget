package arh.ezbudget;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Calendar;
import java.util.List;

/**
 * Created by and20_000 on 3/30/2015.
 */
public class StatisticsActivity extends ActionBarActivity{


    private List<ParseObject> parseObjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTitle(R.string.stats);

        // get objects
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Transaction");
        //query.fromLocalDatastore();
        query.addDescendingOrder("transDate");
        query.fromPin("datastore");
        try {
            parseObjects = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        final String[] TIMEFRAME = { getString(R.string.today), getString(R.string.week), getString(R.string.month),
                getString(R.string.halfYear), getString(R.string.year), getString(R.string.allTime),};

        // set timeframe spinner
        Spinner tfSpinner = (Spinner) findViewById(R.id.spinnerTimeFrame);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, TIMEFRAME);

        tfSpinner.setAdapter(adapter);
        tfSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calculateStats(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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

    public void noStatsMessageDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.noTransError);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                onBackPressed();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void calculateStats(int timeFrame)
    {
        Calendar cal = Calendar.getInstance();
        int endDateCode = TransactionOperations.getDateCode(cal);
        int startDateCode = 0;
        int numberOfDays = 0;
        switch(timeFrame)
        {
            // DAY
            case 0:
                cal.add(Calendar.DAY_OF_MONTH, -1);
                break;
            // WEEK
            case 1:
                cal.add(Calendar.DAY_OF_MONTH, -7);
                break;
            // MONTH
            case 2:
                cal.add(Calendar.MONTH, -1);
                break;
            // HALF YEAR
            case 3:
                cal.add(Calendar.MONTH, -6);
                break;
            // YEAR
            case 4:
                cal.add(Calendar.YEAR, -1);
                break;
            // ALL TIME
            case 5:
                cal.setTime(parseObjects.get(parseObjects.size()-1).getDate("transDate"));
                cal.add(Calendar.DAY_OF_MONTH, -1);
                endDateCode = parseObjects.get(0).getInt("dateCode");
                break;
        }
        startDateCode = TransactionOperations.getDateCode(cal);
        long diff = 0;
        if(timeFrame < 5) {
            diff = Calendar.getInstance().getTimeInMillis() - cal.getTimeInMillis();
        }
        else{
            diff = TransactionOperations.getCalendarFromCode(endDateCode).getTimeInMillis() - cal.getTimeInMillis();
        }
        double days = diff/1000/60/60/24;
        numberOfDays = (int)days;
        //int startDateCode = parseObjects.get(parseObjects.size()-1).getInt("dateCode");
        //int numberOfDays = endDateCode - startDateCode;
        if(numberOfDays ==0)
            numberOfDays = 1;

        double[] categoryTotals = new double[Category.SIZE];
        double[] categoryPercents = new double[Category.SIZE];
        double debitTotal = 0;
        double creditTotal = 0;

        // get total spent and earned
        for(ParseObject po : parseObjects)
        {
            int objDate = po.getInt("dateCode");
            if(objDate > startDateCode && objDate <= endDateCode) {
                TransType type = TransType.getType(po.getInt("type"));
                double amount = po.getDouble("amount");
                categoryTotals[po.getInt("category")] += amount;
                if (type == TransType.CREDIT)
                    creditTotal += amount;
                else
                    debitTotal += amount;
            }
        }

        // get percents
        for(int i = 1; i <= Category.getInt(Category.OTHER_DEBIT); i++)
        {
            categoryPercents[i] = 0;
            if(debitTotal != 0)
                categoryPercents[i] = categoryTotals[i]/debitTotal * 100l;
        }
        for(int i = Category.getInt(Category.OTHER_DEBIT) + 1; i < Category.SIZE; i++)
        {
            categoryPercents[i] = 0;
            if(creditTotal != 0)
                categoryPercents[i] = categoryTotals[i]/creditTotal * 100;
        }

        // amounts per length
        double creditPerDay = 0;
        if(creditTotal != 0) {
            creditPerDay = creditTotal / numberOfDays;
        }

        double debitPerDay = 0;
        if(debitTotal != 0) {
            debitPerDay = debitTotal / numberOfDays;
        }

        // display data
        TextView creditTotalTV = (TextView)findViewById(R.id.textViewTotalCredit);
        TextView debitTotalTV = (TextView)findViewById(R.id.textViewTotalDebit);

        TextView creditTotalPerDayTV = (TextView)findViewById(R.id.textViewTotalCreditPerDay);
        TextView debitTotalPerDayTV = (TextView)findViewById(R.id.textViewTotalDebitPerDay);

        String creditTotalStr = String.format("%s%.2f", getString(R.string.currencySign), creditTotal);
        String creditTotalPerDayStr = String.format("%s%.2f/%s", getString(R.string.currencySign), creditPerDay, getString(R.string.day));

        String debitTotalStr = String.format("%s%.2f", getString(R.string.currencySign), debitTotal);
        String debitTotalPerDayStr = String.format("%s%.2f/%s", getString(R.string.currencySign), debitPerDay, getString(R.string.day));

        creditTotalTV.setText(creditTotalStr);
        creditTotalPerDayTV.setText(creditTotalPerDayStr);

        debitTotalTV.setText(debitTotalStr);
        debitTotalPerDayTV.setText(debitTotalPerDayStr);

        if(timeFrame == 0){
            creditTotalPerDayTV.setText("");
            debitTotalPerDayTV.setText("");
        }

        // display category percents
        String[] creditCatStr = new String[Category.NUM_OF_CREDITS];
        String[] debitCatStr = new String[Category.NUM_OF_DEBITS];

        for(int i = 1, j = 0; i < Category.getInt(Category.OTHER_DEBIT)+1; i++, j++)
        {
            debitCatStr[j] = String.format("%s: %.2f", Category.getStr(Category.getCategory(i)), categoryPercents[i]) + "%";
        }
        for(int i = Category.getInt(Category.OTHER_DEBIT)+1, j = 0; i < Category.SIZE; i++, j++)
        {
            creditCatStr[j] = String.format("%s: %.2f", Category.getStr(Category.getCategory(i)), categoryPercents[i]) + "%";
        }

        ListView creditCatLV = (ListView)findViewById(R.id.listViewTotalCredit);
        ListView debitCatLV = (ListView)findViewById(R.id.listViewTotalDebit);

        ArrayAdapter<String> adapterC = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, creditCatStr);

        ArrayAdapter<String> adapterD = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, debitCatStr);

        creditCatLV.setAdapter(adapterC);
        debitCatLV.setAdapter(adapterD);
    }
}
