package com.nuvolect.securesuite.main;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.nuvolect.securesuite.R;
import com.nuvolect.securesuite.nfc.NfcSession;
import com.nuvolect.securesuite.util.ActionBarUtil;
import com.nuvolect.securesuite.util.AppTheme;
import com.nuvolect.securesuite.util.LogUtil;
import com.nuvolect.securesuite.util.PermissionManager;
import com.nuvolect.securesuite.util.WorkerCommand;

import java.util.Locale;

public class SettingsActivity extends Activity {

    private static final boolean DEBUG = LogUtil.DEBUG;
    Activity m_act;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if(DEBUG)LogUtil.log("SettingsFragmentActivity onCreate");

        m_act = this;

        if( savedInstanceState == null)
            AppTheme.activateTheme(m_act);
        setContentView(R.layout.settings_preference);

        if( savedInstanceState == null){

            ActionBar actionBar = getActionBar();
            ActionBarUtil.setDisplayShowTitleEnabled(actionBar, false);
            ActionBarUtil.setDisplayHomeAsUpEnabled(actionBar, true);

            AppTheme.applyActionBarTheme(m_act, actionBar);
        }

        SettingsFragment.startSettingsFragment(m_act, R.id.settings_fragmment_container);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.settings_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                m_act.finish();
                return true;

            case R.id.menu_refresh:
                SettingsFragment.startSettingsFragment( m_act, R.id.settings_fragmment_container);
                WorkerCommand.queStartIncSync(m_act);
                return true;

            case R.id.menu_password_generator:{

                PasswordFragment f = PasswordFragment.newInstance(m_act);
                f.start();
                break;
            }
            case R.id.menu_pairing_mode:{

                SettingsFragment.PairingModeDialogFragment frag = new SettingsFragment.PairingModeDialogFragment();
                frag.show(getFragmentManager(), "pairing_mode_dialog");
                break;
            }
            case R.id.menu_whats_new:{
                String url = CConst.BLOG_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            }
            case R.id.menu_help:{
                String url = "http://www.nuvolect.com/securesuite_help";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            }
            case R.id.menu_donate: {

                String url = "http://www.nuvolect.com/donate/#securesuite";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            }
            case R.id.menu_developer_feedback:{

                int appVersion = 0;
                try {
                    appVersion = m_act.getPackageManager().getPackageInfo(
                            m_act.getPackageName(), 0).versionCode;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"team@nuvolect.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "SecureSuite Feedback, App Version: "+appVersion);
                i.putExtra(Intent.EXTRA_TEXT   , "Please share your thoughts or ask a question.");

                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(m_act, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Return the index of the current theme, an integer starting from 1.
     * @param ctx
     * @return integer, current theme
     */
    public static int getThemeNumber(Context ctx){

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        int number = Integer.parseInt(pref.getString(CConst.THEME_SETTINGS, "2"));

        return number;
    }

    public static void setTheme(Context ctx, String themeString){

        int themeNumber = AppTheme.getThemeNumber(themeString);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        pref.edit().putString( CConst.THEME_SETTINGS, String.valueOf(themeNumber)).apply();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        LogUtil.log(LogUtil.LogType.SETTINGS_ACTIVITY, "onNewIntent");

        NfcSession.getInstance().handleIntentInBackground(intent);
    }

    /**
     * Used by the PermissionManager to refresh when user makes changes.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        PermissionManager.getInstance(m_act).refresh();
    }

    public static boolean getNotifyIncomingCall(Context ctx) {
        SharedPreferences settings;
        settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        return settings.getBoolean(CConst.NOTIFY_INCOMING_CALL, true);
    }

    public static boolean getInternationalNotation(Context ctx) {
        SharedPreferences settings;
        settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        return settings.getBoolean(CConst.INTERNATIONAL_NOTATION, false);
    }

    public static String getCountryCode(Context ctx) {
        SharedPreferences settings;
        settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        String cc = ctx.getResources().getConfiguration().locale.toString();
        if( cc.length() != 2)
            cc = "US";
        return settings.getString(CConst.COUNTRY_CODE, cc).toUpperCase(Locale.US);
    }
}