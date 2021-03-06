package org.messic.android.messiccore.datamodel.dao;

import java.util.ArrayList;
import java.util.List;

import org.messic.android.messiccore.datamodel.MDMMessicServerInstance;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class DAOServerInstance
    extends DAO
{

    public DAOServerInstance()
    {
        super( MDMMessicServerInstance.TABLE_NAME, MDMMessicServerInstance.getColumns() );
    }

    public void create()
    {
        getDatabase().execSQL( MDMMessicServerInstance.TABLE_CREATE );
    }

    public List<MDMMessicServerInstance> getAll()
    {
        open();
        List<MDMMessicServerInstance> result = new ArrayList<MDMMessicServerInstance>();
        Cursor cursor = super._getAll();
        while ( !cursor.isAfterLast() )
        {
            MDMMessicServerInstance msi = new MDMMessicServerInstance( cursor );
            result.add( msi );
            cursor.moveToNext();
        }

        cursor.close();
        close();
        return result;
    }

    public MDMMessicServerInstance get( int sid )
    {
        MDMMessicServerInstance msi = null;
        open();
        Cursor cursor = super._get( sid );
        if ( cursor.getCount() > 0 )
        {
            msi = new MDMMessicServerInstance( cursor );
        }
        cursor.close();
        close();
        return msi;
    }

    public void remove( MDMMessicServerInstance instance )
    {
        open();
        super._delete( instance.lsid );
        close();
    }

    public MDMMessicServerInstance save( MDMMessicServerInstance instance )
    {
        open();
        ContentValues cv = new ContentValues();
        cv.put( MDMMessicServerInstance.COLUMN_IP, instance.ip );
        cv.put( MDMMessicServerInstance.COLUMN_NAME, instance.name );
        cv.put( MDMMessicServerInstance.COLUMN_PORT, instance.port );
        cv.put( MDMMessicServerInstance.COLUMN_SECURED, DAO.getBoolean( instance.secured ) );
        cv.put( MDMMessicServerInstance.COLUMN_VERSION, instance.version );
        cv.put( MDMMessicServerInstance.COLUMN_LAST_USER, instance.lastUser );
        cv.put( MDMMessicServerInstance.COLUMN_LAST_PASSWORD, instance.lastPassword );

        Cursor c = null;
        if ( instance.lsid > 0 )
        {
            cv.put( MDMMessicServerInstance.COLUMN_SID, instance.lsid );
            c = super._update( cv, instance.lsid );
        }
        else
        {
            c = super._save( cv );
        }

        c.moveToFirst();
        MDMMessicServerInstance msi = new MDMMessicServerInstance( c );
        c.close();
        close();
        return msi;
    }
}
