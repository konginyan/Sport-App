package cn.kongin.sm;

public class Record {
    public static final String TABLE="record";

    //表的各域名
    public static final String KEY_ID="id";
    public static final String KEY_step="step";
    public static final String KEY_grade="grade";
    public static final String KEY_date="date";


    //属性
    public int ID;
    public int step;
    public int grade;
    public String date;

    public Record(int s, int g, String d){
        step = s;
        grade = g;
        date = d;
    }
}
