package com.nuvolect.securesuite.data;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.nuvolect.securesuite.data.SqlCipher.ATab;
import com.nuvolect.securesuite.data.SqlCipher.GTTab;
import com.nuvolect.securesuite.license.LicensePersist;
import com.nuvolect.securesuite.main.CConst;

import net.sqlcipher.Cursor;
import net.sqlcipher.DatabaseUtils;
import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class MyAccounts {

    static SQLiteDatabase account_db = SqlCipher.account_db;
    static String ACCOUNT_TABLE = SqlCipher.ACCOUNT_TABLE;
    static String GROUP_TITLE_TABLE = SqlCipher.GROUP_TITLE_TABLE;
    static String ACCOUNT_DATA_TABLE = SqlCipher.ACCOUNT_DATA_TABLE;
    static String[] ACCOUNT_DATA_COLUMNS = SqlCipher.ACCOUNT_DATA_COLUMNS;
    static String[] GROUP_TITLE_COLUMNS = SqlCipher.GROUP_TITLE_COLUMNS;

    private static Activity m_act;

    public static void setMasterAccount(Activity act) {

        m_act = act;

        final List<String> stringMenu = new ArrayList<String>();

        Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}$", Pattern.CASE_INSENSITIVE);

        Account[] myAccounts = AccountManager.get(m_act).getAccounts();

        for (Account myAccount : myAccounts) {

            if (myAccount.type.contentEquals("com.google") && EMAIL_PATTERN.matcher(myAccount.name).matches()){

                stringMenu.add(myAccount.name.toLowerCase(Locale.US));
            }
        }

        if( stringMenu.size() == 1){

            // There is only one account, so use it
            LicensePersist.setLicenseAccount(m_act, stringMenu.get(0));
            m_act.recreate();
            return;
        }

        final CharSequence[] items = stringMenu.toArray(new CharSequence[stringMenu.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(m_act);
        builder.setTitle(
                "Greetings! Please select an account to host your license")
                .setCancelable(false)
                .setIcon(CConst.SMALL_ICON)
                .setItems( items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        String myAccount = stringMenu.get(which);
                        LicensePersist.setLicenseAccount(m_act, myAccount);

                        m_act.recreate();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

        /**
         * Return a cursor for all contact records in an account.
         * Projection is just contact_id.
         * @param account
         * @return Cursor
         */
        public static Cursor getAccountCursor(String account) {

            String[] projection = {ATab.contact_id.toString()};
            String where = ATab.account_name+"=?";
            String[] args = new String[]{ account };

            Cursor c = account_db.query(ACCOUNT_TABLE, projection, where, args, null, null, null);

            return c;
        }
    //TODO mirror search from Web app.  App uses DTab, web app uses ATab
    /**
     * Return a cursor for all contact records in an account
     * matching the search string on display_name.
     * Projection is _id, contact_id, display_name, starred.
     * @param account
     * @return Cursor
     */
    public static Cursor getAccountCursor(String account, String search) {

        String[] projection = {
                ATab._id.toString(),
                ATab.contact_id.toString(),
                ATab.display_name.toString(),
                ATab.starred.toString(),
                ATab.last_name.toString(),
        };
        String where1= ATab.account_name+"=?";
        String where2= ATab.display_name+" LIKE ?";
        String where = DatabaseUtils.concatenateWhere( where1, where2);
        String[] args = new String[]{ account, "%" + search + "%"};

        Cursor c = account_db.query(ACCOUNT_TABLE, projection, where, args, null, null,
                ATab.display_name.toString() + " ASC");

        return c;
    }

        /**
         * Return a cursor for all contact records in an account that are starred,
         * matching the search string on display_name.
         * Projection is _id, contact_id, display_name, starred.
         * @param account
         * @return Cursor
         */
        public static Cursor getAccountStarredCursor(String account, String search) {

            String[] projection = {
                    ATab._id.toString(),
                    ATab.contact_id.toString(),
                    ATab.display_name.toString(),
                    ATab.starred.toString(),
                    ATab.last_name.toString(),
                    };
            String where1= ATab.account_name+"=?";
            String where2= ATab.display_name+" LIKE ?";
            String where = DatabaseUtils.concatenateWhere( where1, where2);
            String where3= ATab.starred+"=?";
            where = DatabaseUtils.concatenateWhere( where, where3);
            String[] args = new String[]{ account, "%" + search + "%","1"};

            Cursor c = account_db.query( ACCOUNT_TABLE, projection, where, args, null, null,
                    ATab.display_name.toString()+" ASC");

            return c;
        }

        /**
         * Return the account of a contact_id.  Throw an exception
         * if something other than a single contact records is found.
         * @param contact_id
         * @return
         */
        public static String getAccount(long contact_id) {

            String[] projection = { ATab.account_name.toString() };
            String where = ATab.contact_id+"=?";
            String[] args = new String[]{ String.valueOf( contact_id)};

            Cursor c = account_db.query(ACCOUNT_TABLE, projection, where, args, null, null, null);

            String account = "";

            int count = c.getCount();
            if( count == 1)
                account = c.getString( 0 );// only item in projection
            else{
                throw new RuntimeException("getAccount, get should only find one record");
            }

            return account;
        }

        /**
         * Get the unique list of accounts among all contacts
         * @return String [] list of accounts
         */
        public static String[] getAccounts() {

            boolean distinctValues = false;
            String [] columns = { ATab.account_name.toString() };
            String uniqueValuesHaving = GTTab.account_name.toString();
            List<String> accountNames = new ArrayList<String>();

            Cursor c = account_db.query(
                    distinctValues,  // distinct
                    GROUP_TITLE_TABLE,   // table
                    columns,         // columns
                    null,            // selection
                    null,            // selectionArgs
                    uniqueValuesHaving, // groupBy
                    null,            // having
                    null, // ATab.account_name.toString()+" ASC", // orderBy
                    null); // limit

            while( c.moveToNext()){

                accountNames.add(c.getString( 0 ));
            }
            c.close();

            return accountNames.toArray(new String[accountNames.size()]);
        }

        /**
         * Get the first account
         * @return String
         */
        public static String getFirstAccount(){

            String[] accounts = getAccounts();
            if( accounts.length == 0)
                return "";
            else
                return accounts[0];
        }

        public static int numberOfAccounts(){

            String[] accounts = getAccounts();
            return accounts.length;
        }

        /**
         * Delete all contacts and groups of an account.
         * @param account_to_delete
         */
        public static void deleteAccount(Context ctx, String account_to_delete) {

            long[] contactIds = SqlCipher.getContactIds(account_to_delete);
            for( long contact_id : contactIds)
                SqlCipher.deleteContact(ctx, contact_id, true);

            int[] groupIds = MyGroups.getGroupIds(account_to_delete);
            for( int group_id : groupIds)
                MyGroups.deleteGroupForce(ctx, group_id, true);

            // Reset all in-memory group data
            MyGroups.loadGroupMemory();
        }

    /**
     * Get number of contacts in an account
     * @param account
     * @return
     */
    public static int getContactCount(String account) {

        Cursor c = getAccountCursor(account);
        int count = c.getCount();
        c.close();
        return count;
    }

    /**
     * Get the number of starred contacts in an account
     * @param account
     * @return
     */
    public static int getStarredCount(String account) {

        Cursor c = getAccountStarredCursor(account, "");
        int count = c.getCount();
        c.close();
        return count;
    }
}