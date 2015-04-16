package arh.ezbudget;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends ActionBarActivity {

    // global data

    // the following items are used for the 2 list views and 1 context menu
    private int currentlySelectedListView;
    private List<ParseObject> creditsList;
    private List<ParseObject> debitsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        int mode = Activity.MODE_PRIVATE;
        Parse.initialize(this, "66TVMD7RDWRKyQgWilP4HyDG4tbmFFpuSTECsqzL", "VXPCxy4t9tQHOlZcwmgkf5mckk1lhw6Iy1WTuJhM");

        SharedPreferences mySharedPreferences = getSharedPreferences("EZbudget_preferences", mode);
        Boolean isLoggedIn = mySharedPreferences.getBoolean("isLoggedIn", false);
        if(!isLoggedIn)
        {
            Intent loginCreateIntent = new Intent(this, LoginCreateActivity.class);
            startActivityForResult(loginCreateIntent, 0);
        }
        else
            loadDataIntoListViews();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        loadDataIntoListViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        else if(id == R.id.statistics){
            return true;
        }
        if (id == R.id.edituser) {
            startActivity(new Intent(MainActivity.this, EditUser.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // from screen button; sets type add calls add transaction()
    public void onDebitPressed(View view)
    {
        addTransaction(TransType.DEBIT);
    }

    // from screen button; sets type add calls add transaction()
    public void onCreditPressed(View view)
    {
        addTransaction(TransType.CREDIT);
    }

    // jumps to add transaction activity
    public void addTransaction(TransType type)
    {
        Intent addTransactionIntent = new Intent(this, AddTransactionActivity.class);
        addTransactionIntent.putExtra("Type", type);
        addTransactionIntent.putExtra("editMode", false);
        startActivityForResult(addTransactionIntent, 0);
    }

    // from action bar menu; logs out user, clears local data store; jumps to login/create activity
    public void onLogoutPressed(MenuItem item) {
        // log out on device
        int mode = Activity.MODE_PRIVATE;
        SharedPreferences mySharedPreferences = getSharedPreferences("EZbudget_preferences", mode);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.commit();
        // log out on parse
        ParseUser.getCurrentUser().logOut();
        try {
            ParseObject.unpinAll("datastore");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // go to login activity
        Intent loginCreateIntent = new Intent(this, LoginCreateActivity.class);
        startActivityForResult(loginCreateIntent, 0);
    }

    // from action bar menu; jumps to statistics activity
    public void onStatisticsPressed(MenuItem item)
    {
        Intent statisticsIntent = new Intent(this, StatisticsActivity.class);
        startActivity(statisticsIntent);
    }

    // calls find list function with both types: credit and debit
    public void loadDataIntoListViews()
    {
        findListByType(TransType.CREDIT);
        findListByType(TransType.DEBIT);
    }

    // queries the local datastore for transactions of type and calls populate list view
    private void findListByType(final TransType type)
    {
        TransactionOperations.createOccurrences();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Transaction");
        //query.fromLocalDatastore();
        query.fromPin("datastore");
        query.addDescendingOrder("transDate");
        query.whereEqualTo("type", TransType.getInt(type));
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                if (e == null) {
                    //Log.d("score", "Retrieved " + parseObjects.size() + " scores");

                    populateListView(parseObjects, type);
                } else {
                    //Log.d("score", "Error: " + e.getMessage());
                }
            }
        });
    }

    // populates list views from local data store
    private void populateListView(List<ParseObject> ls, TransType type)
    {
        ListView listView;
        if(type == TransType.CREDIT) {
            listView = (ListView) findViewById(R.id.listViewCredit);
            creditsList = ls;
        }
        else {
            listView = (ListView) findViewById(R.id.listViewDebit);
            debitsList = ls;
        }

        List<Map<String, String>> data = new ArrayList<>();
        for (ParseObject po : ls)
        {
            Category cat = Category.getCategory(po.getInt("category"));
            String catStr = Category.getStr(cat);
            String desc = po.getString("desc");
            double amount = po.getDouble("amount");
            String amountStr = String.format("%s%.2f", getString(R.string.currencySign), amount);
            Map<String, String> datum = new HashMap<>(2);
            datum.put("title", catStr + ": " + amountStr);
            datum.put("subtitle", desc);
            datum.put("object_id", po.getObjectId());
            data.add(datum);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, data,
                android.R.layout.simple_list_item_2,
                new String[] {"title", "subtitle"},
                new int[] {android.R.id.text1,
                        android.R.id.text2});
        listView.setAdapter(adapter);
        registerForContextMenu(listView);
    }

    // creates debit/credit context menu and sets global values for list
    // containing object to be editted/deleted
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.trans_list_menu, menu);

        // get list id
        try {
            ListView selectedListView = (ListView)v;
            currentlySelectedListView = selectedListView.getId();
            //selectedListViewAdapter = (ArrayAdapter<Course>)selectedListView.getAdapter();
        } catch(ClassCastException e) {
        }
    }

    // context menu that appears from long tapping credit/debit list item
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // get selected object
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ParseObject currentPo = null;
        if(currentlySelectedListView == R.id.listViewCredit)
        {
            currentPo = creditsList.get(info.position);
        }
        else if(currentlySelectedListView == R.id.listViewDebit)
        {
            currentPo = debitsList.get(info.position);
        }
        else
            return false;

        // perform action
        switch (item.getItemId()) {
            case R.id.menu_edit:
                editTransaction(currentPo);
                return true;
            case R.id.menu_delete:
                deleteTransaction(currentPo);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    // sets edit mode and goes to create transaction activity
    // uses singleton to pass object to edit
    public void editTransaction(ParseObject po)
    {
        TransType type = TransType.getType(po.getInt("type"));
        Intent addTransactionIntent = new Intent(this, AddTransactionActivity.class);
        addTransactionIntent.putExtra("Type", type);
        addTransactionIntent.putExtra("editMode", true);
        TransactionOperations.setEditObject(po);
        startActivityForResult(addTransactionIntent, 0);
    }

    // removes objects and deletes from parse
    public void deleteTransaction(final ParseObject po)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.deleteTransConfirm);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked YES button
                final TransType type = TransType.getType(po.getInt("type"));
                try {
                    po.unpin("datastore");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                po.deleteEventually();
                findListByType(type);
            }
        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked NO button

            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
