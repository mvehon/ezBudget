package arh.ezbudget;

import android.app.Activity;
import android.content.Context;

/**
 * Created by and20_000 on 3/21/2015.
 */
public enum Category {
    ERROR(0, "Error"), BILL(1, "Bill"), GROCERIES(2, "Groceries"), ALCOHOL(3, "Alcohol"),
    TOBACCO(4, "Tobacco"), MEDICAL(5, "Medical"), DINNING(6, "Dinning"), ENTERTAINMENT(7, "Entertainment"),
    HOBBY(8, "Hobby"), OTHER_DEBIT(9, "Other"), PAYCHECK(10, "Paycheck"), GRANT(11, "Grant"), GIFT(12, "Gift"),
    ANNUITY(13, "Annuity"), OTHER_CREDIT(14, "Other");

    public static final int SIZE = 15;
    public static final int NUM_OF_DEBITS = 9;
    public static final int NUM_OF_CREDITS = 5;


    private final int code;
    private final String str;
    private Category(int code, String str)
    {
        this.code = code;
        this.str = str;
    }
    public static int getInt(Category c)
    {
        return c.code;
    }
    public static String getStr(Category c) { return c.str; }


    public static Category getCategory(int i)
    {
        switch(i)
        {
            case 1: return BILL;
            case 2: return GROCERIES;
            case 3: return ALCOHOL;
            case 4: return TOBACCO;
            case 5: return MEDICAL;
            case 6: return DINNING;
            case 7: return ENTERTAINMENT;
            case 8: return HOBBY;
            case 9: return OTHER_DEBIT;
            case 10: return PAYCHECK;
            case 11: return GRANT;
            case 12: return GIFT;
            case 13: return ANNUITY;
            case 14: return OTHER_CREDIT;
        }
        return ERROR;
    }
}
