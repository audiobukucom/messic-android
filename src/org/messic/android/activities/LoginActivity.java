/*
 * Copyright (C) 2013
 *
 *  This file is part of Messic.
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.messic.android.activities;

import org.messic.android.R;
import org.messic.android.controllers.Configuration;
import org.messic.android.controllers.LoginController;
import org.messic.android.datamodel.MDMMessicServerInstance;
import org.messic.android.util.MessicPreferences;
import org.messic.android.util.UtilDownloadService;
import org.messic.android.util.UtilMusicPlayer;
import org.messic.android.util.UtilNetwork;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity
    extends Activity
{
    private LoginController controller = new LoginController();

    private void showLoginOnline( boolean show )
    {
        findViewById( R.id.login_online_status ).setVisibility( ( show ? View.VISIBLE : View.GONE ) );
        findViewById( R.id.login_online_thostname ).setVisibility( ( show ? View.VISIBLE : View.GONE ) );
        findViewById( R.id.login_online_thostname_ip ).setVisibility( ( show ? View.VISIBLE : View.GONE ) );
        findViewById( R.id.login_online_layout ).setVisibility( ( show ? View.VISIBLE : View.GONE ) );
        if ( show )
        {
            fillOnline();
        }
    }

    private void showSearchOnline( boolean show )
    {
        View vs = findViewById( R.id.login_searchonline );
        vs.setVisibility( ( show ? View.VISIBLE : View.GONE ) );
        if ( show )
        {
            vs.setOnClickListener( new View.OnClickListener()
            {
                public void onClick( View v )
                {
                    Intent ssa = new Intent( LoginActivity.this, SearchMessicServiceActivity.class );
                    LoginActivity.this.startActivity( ssa );
                    finish();
                }
            } );
        }
    }

    private void showLoginOffline( boolean show )
    {
        View v = findViewById( R.id.login_boffline );
        v.setVisibility( ( show ? View.VISIBLE : View.GONE ) );
        if ( show )
        {
            v.setOnClickListener( new View.OnClickListener()
            {
                public void onClick( View v )
                {
                    Configuration.setOffline( true );
                    Intent ssa = new Intent( LoginActivity.this, BaseActivity.class );
                    LoginActivity.this.startActivity( ssa );
                }
            } );
        }
    }

    private void layoutScreen()
    {
        boolean flagShowOnline = false;
        boolean flagShowOffline = false;

        // 1. is there a last messic server used?
        MDMMessicServerInstance prefferedServer = Configuration.getLastMessicServerUsed( this );
        if ( prefferedServer != null && prefferedServer.ip.trim().length() > 0 )
        {
            // we show the online login against this last server
            showSearchOnline( false );
            showLoginOnline( true );
            flagShowOnline = true;

            // 1.1 Is this last server used online?
            UtilNetwork.MessicServerStatusListener mssl = new UtilNetwork.MessicServerStatusListener()
            {
                public void setResponse( boolean reachable, boolean running )
                {
                    if ( reachable && running )
                    {
                        final View vStatus = findViewById( R.id.login_online_status );
                        final TextView vStatusOffline = (TextView) findViewById( R.id.login_offline_description );
                        vStatus.post( new Runnable()
                        {
                            public void run()
                            {
                                vStatus.setBackgroundResource( R.drawable.ic_check_circle_green_24dp );
                                vStatusOffline.setText( getString( R.string.login_explanation ) );
                            }
                        } );
                    }
                    else
                    {
                        final View vStatus = findViewById( R.id.login_online_status );
                        final TextView vStatusOffline = (TextView) findViewById( R.id.login_offline_description );

                        vStatus.post( new Runnable()
                        {
                            public void run()
                            {
                                vStatus.setBackgroundResource( R.drawable.ic_highlight_off_red_24dp );
                                vStatusOffline.setText( getString( R.string.login_server_offline ) );
                            }
                        } );
                    }
                }
            };
            UtilNetwork.checkMessicServerUpAndRunning( prefferedServer, mssl );
        }
        else
        {
            showLoginOnline( false );
            showSearchOnline( true );
        }

        // 2. is there offline music available?
        if ( LoginController.checkEmptyDatabase( this.getApplicationContext() ) )
        {
            // yes! no offline option to play music
            showLoginOffline( false );
        }
        else
        {
            // no!, let's show the offline button
            showLoginOffline( true );
            flagShowOffline = true;
        }

        if ( !flagShowOffline && !flagShowOnline )
        {
            // then the user should first go to search a valid messic service
            Intent ssa = new Intent( LoginActivity.this, SearchMessicServiceActivity.class );
            LoginActivity.this.startActivity( ssa );
            finish();
        }
    }

    /**
     * set the behaviour and content of the online fields
     */
    private void fillOnline()
    {
        MessicPreferences p = new MessicPreferences( this );
        MDMMessicServerInstance instance = Configuration.getCurrentMessicService();
        ( (TextView) findViewById( R.id.login_online_thostname ) ).setText( instance.name );
        ( (TextView) findViewById( R.id.login_online_thostname_ip ) ).setText( instance.ip );
        ( (CheckBox) findViewById( R.id.login_online_cbremember ) ).setChecked( p.getRemember() );
        TextView tlogin = ( (TextView) findViewById( R.id.login_online_tusername ) );
        final TextView tpass = ( (TextView) findViewById( R.id.login_online_tpassword ) );
        if ( p.getRemember() )
        {
            String user = Configuration.getLastMessicUser();
            tlogin.setText( ( user != null ? user : "" ) );
            String password = Configuration.getLastMessicPassword();
            tpass.setText( ( password != null ? password : "" ) );
        }

        tlogin.setOnEditorActionListener( new TextView.OnEditorActionListener()
        {
            public boolean onEditorAction( TextView v, int actionId, KeyEvent event )
            {
                if ( actionId == EditorInfo.IME_ACTION_NEXT )
                {
                    tpass.requestFocus();
                    return true;
                }
                return false;
            }
        } );
        tpass.setOnEditorActionListener( new TextView.OnEditorActionListener()
        {
            public boolean onEditorAction( TextView v, int actionId, KeyEvent event )
            {
                if ( actionId == EditorInfo.IME_ACTION_GO )
                {
                    login();
                    return true;
                }
                return false;
            }
        } );

        View bsearchServer = (View) findViewById( R.id.login_online_status );
        bsearchServer.setOnClickListener( new View.OnClickListener()
        {
            public void onClick( View v )
            {
                Intent ssa = new Intent( LoginActivity.this, SearchMessicServiceActivity.class );
                LoginActivity.this.startActivity( ssa );
                finish();
            }
        } );

        Button loginaction = (Button) findViewById( R.id.login_online_bloginaction );
        loginaction.setOnClickListener( new View.OnClickListener()
        {
            public void onClick( View v )
            {
                login();
            }
        } );
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if ( Configuration.isFirstTime( this ) )
        {
            Configuration.setFirstTime( false );
            Intent ssa = new Intent( LoginActivity.this, WelcomeActivity.class );
            LoginActivity.this.startActivity( ssa );
        }
        else
        {

            // put the layout considering the situation
            layoutScreen();
        }
    }

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_login );

        UtilMusicPlayer.startMessicMusicService( this );
        UtilDownloadService.startDownloadService( this );

    }

    /**
     * perform login process
     */
    private void login()
    {
        String username = ( (TextView) findViewById( R.id.login_online_tusername ) ).getText().toString();
        String password = ( (TextView) findViewById( R.id.login_online_tpassword ) ).getText().toString();
        boolean remember = ( (CheckBox) findViewById( R.id.login_online_cbremember ) ).isChecked();
        try
        {
            ProgressDialog dialog =
                ProgressDialog.show( LoginActivity.this, getString( R.string.login_title ),
                                     getString( R.string.login_message ), true );
            controller.login( LoginActivity.this, remember, username, password, dialog );
        }
        catch ( Exception e )
        {
            Toast.makeText( LoginActivity.this, "Error while trying to login", Toast.LENGTH_LONG ).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.login_activity_actions, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if ( id == R.id.action_settings )
        {
            return true;
        }
        else if ( id == R.id.action_scan )
        {
            Intent ssa = new Intent( LoginActivity.this, SearchMessicServiceActivity.class );
            LoginActivity.this.startActivity( ssa );
            finish();
            return true;
        }
        return super.onOptionsItemSelected( item );
    }

}