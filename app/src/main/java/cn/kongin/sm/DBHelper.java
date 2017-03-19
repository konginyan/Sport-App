package cn.kongin.sm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{
    //数据库版本号
    private static final int DATABASE_VERSION=4;

    //数据库名称
    private static final String DATABASE_NAME="locus.db";

    public DBHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE="CREATE TABLE "+ Locus.TABLE+"("
                +Locus.KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT ,"
                +Locus.KEY_start+" TEXT, "
                +Locus.KEY_end+" TEXT, "
                +Locus.KEY_length+" TEXT, "
                +Locus.KEY_speed+" TEXT, "
                +Locus.KEY_date+" TEXT, "
                +Locus.KEY_grade+" TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //如果旧表存在，删除，所以数据将会消失
        db.execSQL("DROP TABLE IF EXISTS "+ Locus.TABLE);

        //再次创建表
        onCreate(db);
    }
}
