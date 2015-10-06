package ru.vif2ne.backend.account;

import android.accounts.Account;
import android.os.Parcel;

/**
 * Created by serg on 08.09.15.
 */
public class Vif2neAccount extends Account {
    public static final String TYPE = "ru.vif2ne";

    public static final String TOKEN_FULL_ACCESS = TYPE+".TOKEN_FULL_ACCESS";

    public Vif2neAccount(String name) {
        super(name, TYPE);
    }

    public Vif2neAccount(Parcel in) {
        super(in);
    }
}
