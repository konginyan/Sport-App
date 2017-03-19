package cn.kongin.sm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RDBHelper extends SQLiteOpenHelper {
    //数据库版本号
    private static final int DATABASE_VERSION=4;

    //数据库名称
    private static final String DATABASE_NAME="record.db";

    public RDBHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE="CREATE TABLE "+ Record.TABLE+"("
                +Record.KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT ,"
                +Record.KEY_step+" INTEGER, "
                +Record.KEY_grade+" INTEGER, "
                +Record.KEY_date+" TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //如果旧表存在，删除，所以数据将会消失
        db.execSQL("DROP TABLE IF EXISTS "+ Record.TABLE);

        //再次创建表
        onCreate(db);
    }
}
