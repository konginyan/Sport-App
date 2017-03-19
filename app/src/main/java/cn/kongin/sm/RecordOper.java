package cn.kongin.sm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class RecordOper {
    private RDBHelper dbHelper;

    public RecordOper(Context context){
        dbHelper=new RDBHelper(context);
    }

    public int insert(Record record){
        //打开连接，写入数据
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(Record.KEY_step,record.step);
        values.put(Record.KEY_grade,record.grade);
        values.put(Record.KEY_date,record.date);
        //
        long record_Id=db.insert(Record.TABLE,null,values);
        db.close();
        return (int)record_Id;
    }

    public void delete(int record_Id){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        db.delete(Record.TABLE,Record.KEY_ID+"=?", new String[]{String.valueOf(record_Id)});
        db.close();
    }

    public void update(Record record) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Record.KEY_step,record.step);
        values.put(Record.KEY_grade,record.grade);
        values.put(Record.KEY_date,record.date);

        db.update(Record.TABLE, values, Record.KEY_ID + "=?", new String[]{String.valueOf(record.ID)});
        db.close();
    }

    public ArrayList<HashMap<String, String>> getList(){
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        String selectQuery="SELECT "+
                Record.KEY_ID+","+
                Record.KEY_step+","+
                Record.KEY_grade+","+
                Record.KEY_date+" FROM "+Record.TABLE;
        ArrayList<HashMap<String,String>> recordList=new ArrayList<HashMap<String, String>>();
        Cursor cursor=db.rawQuery(selectQuery,null);

        if(cursor.moveToFirst()){
            do{
                HashMap<String,String> record=new HashMap<String,String>();
                record.put("id",cursor.getString(cursor.getColumnIndex(Record.KEY_ID)));
                record.put("step",cursor.getString(cursor.getColumnIndex(Record.KEY_step)));
                record.put("grade",cursor.getString(cursor.getColumnIndex(Record.KEY_grade)));
                record.put("date",cursor.getString(cursor.getColumnIndex(Record.KEY_date)));
                recordList.add(record);
            }while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return recordList;
    }

    public ArrayList<HashMap<String, String>> getList(String date){
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        String selectQuery="SELECT "+
                Record.KEY_ID+","+
                Record.KEY_step+","+
                Record.KEY_grade+","+
                Record.KEY_date+
                " FROM "+Record.TABLE+
                " WHERE "+ Record.KEY_date+"=\""+date+"\"";
        ArrayList<HashMap<String,String>> recordList=new ArrayList<HashMap<String, String>>();
        Cursor cursor=db.rawQuery(selectQuery,null);

        if(cursor.moveToFirst()){
            do{
                HashMap<String,String> record=new HashMap<String,String>();
                record.put("id",cursor.getString(cursor.getColumnIndex(Record.KEY_ID)));
                record.put("step",cursor.getString(cursor.getColumnIndex(Record.KEY_step)));
                record.put("grade",cursor.getString(cursor.getColumnIndex(Record.KEY_grade)));
                record.put("date",cursor.getString(cursor.getColumnIndex(Record.KEY_date)));
                recordList.add(record);
            }while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return recordList;
    }
}
