package arh.ezbudget;

/**
 * Created by and20_000 on 3/21/2015.
 */
public enum Interval {
    ONCE(0), DAILY(1), WEEKLY(2), BIWEEKLY(3), MONTHLY(4), BIMONTHLY(5), QUARTERLY(6), TWICE_ANNUALLY(7), ANNUALLY(8), ERROR(9);

    private final int code;
    private Interval(int code)
    {
        this.code = code;
    }
    public static int getInt(Interval i)
    {
        return i.code;
    }
    public static Interval getInterval(int i)
    {
        switch(i)
        {
            case 0: return ONCE;
            case 1: return DAILY;
            case 2: return WEEKLY;
            case 3: return BIWEEKLY;
            case 4: return MONTHLY;
            case 5: return BIMONTHLY;
            case 6: return QUARTERLY;
            case 7: return TWICE_ANNUALLY;
            case 8: return ANNUALLY;
        }
        return ERROR;
    }
}
