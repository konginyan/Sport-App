package cn.kongin.sm;

public class Locus {
    public static final String TABLE="Locus";

    //表的各域名
    public static final String KEY_ID="id";
    public static final String KEY_start="start";
    public static final String KEY_end="end";
    public static final String KEY_length="length";
    public static final String KEY_speed="speed";
    public static final String KEY_date="date";
    public static final String KEY_grade="grade";

    //属性
    public int ID;
    public String start;
    public String end;
    public String length;
    public String speed;
    public String date;
    public int grade;

    public Locus(String st, String et, String l, String s, String d, int g){
        start = st;
        end = et;
        length = l;
        speed = s;
        date = d;
        grade = g;
    }
}
