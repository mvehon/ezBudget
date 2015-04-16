package arh.ezbudget;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by and20_000 on 3/25/2015.
 */
public class TransactionOperations {

    /*private static TransactionOperations ourInstance = new TransactionOperations();

    public static TransactionOperations getInstance() {
        return ourInstance;
    }

    private TransactionOperations() {
    }*/

    private static ParseObject editObject;

    public static void setEditObject(ParseObject po){editObject = po;}
    public static ParseObject getEditObject(){return editObject;}

    private static int currTimeCode;

    private static Date objectDate;

    public static int getNextOccurrenceDateCode(Calendar cal, Interval interval)
    {
        switch(interval)
        {
            case ONCE:
                return 0;
            case DAILY:
                cal.add(cal.DATE, 1);
                break;
            case WEEKLY:
                cal.add(cal.DATE, 7);
                break;
            case BIWEEKLY:
                cal.add(cal.DATE, 14);
                break;
            case MONTHLY:
                cal.add(cal.MONTH, 1);
                break;
            case QUARTERLY:
                cal.add(cal.MONTH, 3);
                break;
            case BIMONTHLY:
                cal.add(cal.MONTH, 2);
                break;
            case TWICE_ANNUALLY:
                cal.add(cal.MONTH, 6);
                break;
            case ANNUALLY:
                cal.add(cal.YEAR, 1);
                break;
        }
        return getDateCode(cal);
    }

    // returns date code of calendar
    public static int getDateCode(Calendar cal)
    {
        int dateCode = 10000 * cal.get(Calendar.YEAR) + 100 * cal.get(Calendar.MONTH) + cal.get(Calendar.DAY_OF_MONTH);
        return dateCode;
    }

    // returns calendar object initialized with date code parameter
    public static Calendar getCalendarFromCode(int code)
    {
        String codeStr = Integer.toString(code);
        String yearStr = codeStr.substring(0,4);
        String monthStr = codeStr.substring(4,6);
        String dayStr = codeStr.substring(6,8);

        int year = Integer.valueOf(yearStr);
        int month = Integer.valueOf(monthStr);
        int day = Integer.valueOf(dayStr);

        Calendar cal = Calendar.getInstance();

        cal.set(year, month, day);

        return cal;
    }

    // creates new transactions of recurring items
    public static void createOccurrences()
    {
        // get current date
        Calendar cal = Calendar.getInstance();
        objectDate = cal.getTime();
        currTimeCode = getDateCode(cal);

        // make query
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Transaction");
        query.fromLocalDatastore();
        query.whereEqualTo("isRecurring", true);
        List<ParseObject> parseObjects = null;
        try {
            parseObjects = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // look through objects and create recurrences
        for(ParseObject po : parseObjects)
        {
            if(po.getInt("nextDateCode") <= currTimeCode)
            {
                createParseObject(po);
                //parseObjects.add(po);
            }
        }
    }

    private static void recurrsiveOcurrenceAdder(ParseObject po)
    {
        if(po.getInt("nextDateCode") <= currTimeCode)
        {
            createParseObject(po);
        }
    }

    private static void createParseObject(ParseObject po)
    {
        po.put("isRecurring", false);
        po.saveEventually();
        ParseObject newPo = new ParseObject("Transaction");
        newPo.put("desc", po.getString("desc"));
        newPo.put("amount", po.getDouble("amount"));
        newPo.put("type", po.getInt("type"));
        newPo.put("category", po.getInt("category"));
        newPo.put("dateCode", po.getInt("nextDateCode"));
        newPo.put("interval", po.getInt("interval"));
        newPo.put("userID", ParseUser.getCurrentUser().getObjectId());
        newPo.put("isRecurring", true);

        Calendar cal = getCalendarFromCode(po.getInt("nextDateCode"));

        objectDate = cal.getTime();
        newPo.put("transDate", objectDate);

        Interval interval = Interval.getInterval(po.getInt("interval"));

        int nextDateCode = getNextOccurrenceDateCode(cal, interval);

        newPo.put("nextDateCode", nextDateCode);

        newPo.saveEventually();
        try {
            newPo.pin("datastore");
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        recurrsiveOcurrenceAdder(newPo);
    }
}
