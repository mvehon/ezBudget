package arh.ezbudget;

/**
 * Created by and20_000 on 3/21/2015.
 */
public enum TransType {
    DEBIT(0), CREDIT(1), ERROR(3);

    private final int code;
    private TransType(int code)
    {
        this.code = code;
    }
    public static int getInt(TransType t)
    {
        return t.code;
    }
    public static TransType getType(int i)
    {
        switch(i)
        {
            case 0: return DEBIT;
            case 1: return CREDIT;
        }
        return ERROR;
    }
}
