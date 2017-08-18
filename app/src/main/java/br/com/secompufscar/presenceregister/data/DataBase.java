package br.com.secompufscar.presenceregister.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DataBase extends SQLiteOpenHelper {

    private static DataBase db;


    public static final String DATABASE_NAME = "secompPresencas.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE = "presenca";
    public static final String PARTICIPANTE_ID = "participante_ID";
    public static final String ATIVIDADE_ID = "ATIVIDADE_ID";
    public static final String HORARIO = "horario_presenca";
    public static final String ID = "ID";

    public DataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static void setInstance(Context context) {

        if (db == null) {
            db = new DataBase(context.getApplicationContext());
        }
    }

    public static synchronized DataBase getDB() {
        return db;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                PARTICIPANTE_ID + " TEXT, " +
                ATIVIDADE_ID + " TEXT, " + HORARIO + " TEXT) "
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public boolean insertPresenca(Presenca presenca) {

        SQLiteDatabase db = getWritableDatabase();
        boolean status;

        ContentValues contentValues = new ContentValues();
        contentValues.put(ATIVIDADE_ID, presenca.getIdAtividade());
        contentValues.put(HORARIO, presenca.getHorario());
        contentValues.put(PARTICIPANTE_ID, presenca.getIdParticipante());

        try {
            db.insertOrThrow(TABLE, null, contentValues);
            status = true;
        } catch (SQLiteException e) {
            status = false;
        }

        db.close();
        return status;
    }

    public ArrayList<Presenca> getAllEntries() {
        ArrayList<Presenca> presencas = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE, null);
        db.close();

        cursor.moveToFirst();

        do {
            String id_atividade = cursor.getString(cursor.getColumnIndex(DataBase.ATIVIDADE_ID));
            String id_participante = cursor.getString(cursor.getColumnIndex(DataBase.PARTICIPANTE_ID));
            String horario = cursor.getString(cursor.getColumnIndex(DataBase.HORARIO));
            String id_presenca = cursor.getString(cursor.getColumnIndex(DataBase.ID));

            Presenca presenca = new Presenca(id_presenca, id_participante, id_atividade, horario);

            presencas.add(presenca);
        } while (cursor.moveToNext());

        return presencas;
    }

    public int getCountPresencas(){
        String countQuery = "SELECT  * FROM " + TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    public void deleteEntry(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE, ID + " = '" + String.valueOf(id) + "'", null);
        db.close();
    }

}