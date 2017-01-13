package com.nuvolect.securesuite.data;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.provider.ContactsContract;

public class ImportUtil {

    /**
     * Get the RawContacts data summary for each account
     * @return
     */
    public static Bundle generateCloudSummary(Context ctx){

        Bundle bundle = new Bundle();

        String selectionA = ContactsContract.RawContacts.DELETED + " != 1";
        String selectionB = ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY + " <> ''";
        String selection = DatabaseUtils.concatenateWhere( selectionA, selectionB);

        HashMap<String, Integer> rawContactsMap = new HashMap<String, Integer>();

        Cursor c = ctx.getContentResolver().query(
                ContactsContract.RawContacts.CONTENT_URI,
                new String[]{
                        ContactsContract.RawContacts.ACCOUNT_NAME,
                        ContactsContract.RawContacts.CONTACT_ID, },
                        selection, null, null );

        //Tally all of the raw contacts by account

        while( c.moveToNext()){

            String account = c.getString( 0 );
            // Build contact data sums for each account
            if( rawContactsMap.containsKey(account)){

                Integer rawContactTotal = rawContactsMap.get(account);
                rawContactsMap.put(account, ++rawContactTotal);

            }else
                rawContactsMap.put(account, 1);
        }
        c.close();

        final CharSequence[] accountList = new CharSequence[ rawContactsMap.size()];
        final int[] countList = new int[ rawContactsMap.size()];
        int i=0;

        for( Map.Entry<String, Integer> anAccount : rawContactsMap.entrySet()){

            String currentAccount = anAccount.getKey().toLowerCase(Locale.US);
            int rawContactsThisAccount = anAccount.getValue();

            accountList[ i ] = currentAccount+"\n"+rawContactsThisAccount+" contacts";
            countList[ i++] = rawContactsThisAccount;
        }

        bundle.putCharSequenceArray("accountList", accountList);
        bundle.putIntArray("countList", countList);

        return bundle;
    }
}