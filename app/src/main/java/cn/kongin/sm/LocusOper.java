package cn.kongin.sm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class LocusOper {
    private DBHelper dbHelper;

    public LocusOper(Context context){
        dbHelper=new DBHelper(context);
    }

    public int insert(Locus locus){
        //打开连接，写入数据
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(Locus.KEY_start,locus.start);
        values.put(Locus.KEY_end,locus.end);
        values.put(Locus.KEY_length,locus.length);
        values.put(Locus.KEY_speed,locus.speed);
        values.put(Locus.KEY_date,locus.date);
        values.put(Locus.KEY_grade,locus.grade);
        //
        long locus_Id=db.insert(Locus.TABLE,null,values);
        db.close();
        return (int)locus_Id;
    }

    public void delete(int locus_Id){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        db.delete(Locus.TABLE,Locus.KEY_ID+"=?", new String[]{String.valueOf(locus_Id)});
        db.close();
    }

    public void update(Locus locus) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Locus.KEY_start, locus.start);
        values.put(Locus.KEY_end, locus.end);
        values.put(Locus.KEY_length, locus.length);
        values.put(Locus.KEY_speed, locus.speed);
        values.put(Locus.KEY_date,locus.date);
        values.put(Locus.KEY_grade,locus.grade);

        db.update(Locus.TABLE, values, Locus.KEY_ID + "=?", new String[]{String.valueOf(locus.ID)});
        db.close();
    }

    public ArrayList<HashMap<String, String>> getList(){
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        String selectQuery="SELECT "+
                Locus.KEY_ID+","+
                Locus.KEY_start+","+
                Locus.KEY_end+","+
                Locus.KEY_length+","+
                Locus.KEY_speed+","+
                Locus.KEY_date+","+
                Locus.KEY_grade+" FROM "+Locus.TABLE;
        ArrayList<HashMap<String,String>> locusList=new ArrayList<HashMap<String, String>>();
        Cursor cursor=db.rawQuery(selectQuery,null);

        if(cursor.moveToFirst()){
            do{
                HashMap<String,String> locus=new HashMap<String,String>();
                locus.put("id",cursor.getString(cursor.getColumnIndex(Locus.KEY_ID)));
                locus.put("start",cursor.getString(cursor.getColumnIndex(Locus.KEY_start)));
                locus.put("end",cursor.getString(cursor.getColumnIndex(Locus.KEY_end)));
                locus.put("length",cursor.getString(cursor.getColumnIndex(Locus.KEY_length)));
                locus.put("speed",cursor.getString(cursor.getColumnIndex(Locus.KEY_speed)));
                locus.put("date",cursor.getString(cursor.getColumnIndex(Locus.KEY_date)));
                locus.put("grade",cursor.getString(cursor.getColumnIndex(Locus.KEY_grade)));
                locusList.add(0,locus);//倒叙
            }while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return locusList;
    }

    public ArrayList<HashMap<String, String>> getList(String date){
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        String selectQuery="SELECT "+
                Locus.KEY_ID+","+
                Locus.KEY_start+","+
                Locus.KEY_end+","+
                Locus.KEY_length+","+
                Locus.KEY_speed+","+
                Locus.KEY_date+","+
                Locus.KEY_grade+
                " FROM "+Locus.TABLE+
                " WHERE "+Locus.KEY_date+"=\""+date+"\"";
        ArrayList<HashMap<String,String>> locusList=new ArrayList<HashMap<String, String>>();
        Cursor cursor=db.rawQuery(selectQuery,null);

        if(cursor.moveToFirst()){
            do{
                HashMap<String,String> locus=new HashMap<String,String>();
                locus.put("id",cursor.getString(cursor.getColumnIndex(Locus.KEY_ID)));
                locus.put("start",cursor.getString(cursor.getColumnIndex(Locus.KEY_start)));
                locus.put("end",cursor.getString(cursor.getColumnIndex(Locus.KEY_end)));
                locus.put("length",cursor.getString(cursor.getColumnIndex(Locus.KEY_length)));
                locus.put("speed",cursor.getString(cursor.getColumnIndex(Locus.KEY_speed)));
                locus.put("date",cursor.getString(cursor.getColumnIndex(Locus.KEY_date)));
                locus.put("grade",cursor.getString(cursor.getColumnIndex(Locus.KEY_grade)));
                locusList.add(locus);
            }while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return locusList;
    }
}
