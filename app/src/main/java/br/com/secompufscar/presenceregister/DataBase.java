package br.com.secompufscar.presenceregister;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Italo on 20/03/2016.
 */
public class DataBase extends SQLiteOpenHelper {
    public static final String DATABASE_NAME="Secomp.db";
    public static final int DATABASE_VERSION=1;
    public static final String TABLE="presenca";
    public static final String PARTICIPANTE_ID="participante_ID";
    public static final String EVENTO_ID="evento_ID";
    public static final String HORARIO="horario_presenca";
    public static final String ID="ID";
    public DataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                        PARTICIPANTE_ID + " TEXT, " +
                        EVENTO_ID + " TEXT, " + HORARIO + " TEXT) "
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,  int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }
    public boolean insertPresenca(String participanteID, String eventoID,String horario) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(EVENTO_ID, eventoID);
        contentValues.put(HORARIO,horario);
        contentValues.put(PARTICIPANTE_ID, participanteID);
        db.insert(TABLE, null, contentValues);
        return true;
    }
    public Cursor getAllEntries() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery( "SELECT * FROM " + TABLE, null );
    }
    public void deleteEntry(int id){
        SQLiteDatabase db= getWritableDatabase();
        db.delete(TABLE,ID+" = '"+String.valueOf(id)+"'",null);
    }

}