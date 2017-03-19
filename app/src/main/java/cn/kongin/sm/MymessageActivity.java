package cn.kongin.sm;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MymessageActivity extends AppCompatActivity {

    String fileName;// 图片名字
    private Bitmap head;
    CircleImageView cv;
    ListView perlist;
    SimpleAdapter adapter;
    List<Map<String,Object>> viewList;
    String[] listTitle;
    String[] listContent;

    SharedPreferences mySharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mymessage);
        setlist();
        cv = (CircleImageView)findViewById(R.id.PhotoSelect);
        cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoDialog();
            }
        });
        fileName = "head.jpg";
        mySharedPreferences = getSharedPreferences("test", 0);
        boolean headerExist = mySharedPreferences.getBoolean("HeaderExist",false);
        if(headerExist) initHeadPic();
        editor = mySharedPreferences.edit();
        editor.putBoolean("firstUser",false);
        editor.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        savePersonalData();
    }

    /**
     * 个人信息列表
     */
    private void setlist() {
        viewList =new ArrayList<Map<String,Object>>();
        listTitle = new String[]{"昵称","性别","出生年月","身高","体重"};
        listContent = new String[]{"未设置","未设置","未设置","未设置","未设置"};
        loadPersonalData();

        for(int i=0;i<listTitle.length;i++){
            Map<String,Object>map=new HashMap<String,Object>();
            map.put("title",listTitle[i]);
            map.put("content",listContent[i]);
            viewList.add(map);
        }

        perlist = (ListView)findViewById(R.id.perlist);
        adapter = new SimpleAdapter(MymessageActivity.this,
                viewList,
                R.layout.data_line,
                new String[]{"title","content"},
                new int[]{R.id.title,R.id.content}
        );
        perlist.setAdapter(adapter);
        perlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        nameDialog();
                        break;
                    case 1:
                        sexDialog();
                        break;
                    case 2:
                        birthdayDialog();
                        break;
                    case 3:
                        heightDialog();
                        break;
                    case 4:
                        weightDialog();
                        break;
                }
            }
        });
    }

    private void nameDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.app_icon);
        builder.setTitle("请输入姓名");
        //创建一个EditText对象设置为对话框中显示的View对象
        final EditText ed = new EditText(MymessageActivity.this);
        builder.setView(ed);
        //用户选好要选的选项后，点击确定按钮
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!ed.getText().toString().equals("")){
                    listContent[0] = ed.getText().toString();
                    Map<String,Object>map=new HashMap<String,Object>();
                    map.put("title","昵称");
                    map.put("content",listContent[0]);
                    viewList.set(0,map);
                    adapter.notifyDataSetChanged();
                }
                else Toast.makeText(getApplicationContext(),"名字不能为空",Toast.LENGTH_SHORT).show();
            }
        });
        // 取消选择
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private void sexDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请选择您的性别");
        //定义单选的选项
        final String[] items = new String[]{
                "男",
                "女",
                "外星人"
        };
        //设置单选选项
        //arg1：表示默认选中哪一项，-1表示没有默认选中项
        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            //点击任何一个单选选项都会触发这个侦听方法执行
            //arg1：点击的是哪一个选项
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listContent[1] = items[which];
                Map<String,Object>map=new HashMap<String,Object>();
                map.put("title","性别");
                map.put("content",listContent[1]);
                viewList.set(1,map);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        // 设置取消按钮，用户可以决定不选
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        //直接调用builder的show方法同样可以显示对话框，其内部也是先创建对话框对象，然后调用对话框的show()
        builder.show();
    }

    private void birthdayDialog(){
        new DatePickerDialog(MymessageActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String tmonth = String.valueOf(month+1);
                String tday = String.valueOf(dayOfMonth);
                if(month<9){
                    tmonth = "0" + String.valueOf(month+1);
                }
                if(dayOfMonth<10){
                    tday = "0" + String.valueOf(dayOfMonth);
                }
                listContent[2] = String.valueOf(year)+"/"+tmonth+"/"+tday;
                Map<String,Object>map=new HashMap<String,Object>();
                map.put("title","出生年月");
                map.put("content",listContent[2]);
                viewList.set(2,map);
                adapter.notifyDataSetChanged();
            }
        },getbirthyear(),
                getbirthmonth()-1,
                getbirthday()).show();
    }

    private void heightDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.app_icon);
        builder.setTitle("选择你的身高");
        //定义列表中的选项
        final String[] items = new String[202];
        java.text.DecimalFormat df = new java.text.DecimalFormat("#0.00");
        items[0] = "手动输入";
        for(int i=1;i<items.length;i++){
            items[i] = df.format(0.99+i*0.01) + "米";
        }

        //设置列表选项
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0){
                    extraDialog(3);
                }
                else {
                    listContent[3] = items[which];
                    Map<String,Object>map=new HashMap<String,Object>();
                    map.put("title","身高");
                    map.put("content",listContent[3]);
                    viewList.set(3,map);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        // 取消选择
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    private void weightDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.app_icon);
        builder.setTitle("选择你的体重");
        //定义列表中的选项
        final String[] items = new String[127];
        items[0] = "手动输入";
        for(int i=1;i<items.length;i++){
            items[i] = 34 +i + "公斤";
        }

        //设置列表选项
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0){
                    extraDialog(4);
                }
                else {
                    listContent[4] = items[which];
                    Map<String,Object>map=new HashMap<String,Object>();
                    map.put("title","体重");
                    map.put("content",listContent[4]);
                    viewList.set(4,map);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        // 取消选择
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    private void extraDialog(final int type){
        final String unit;
        final String title;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.app_icon);
        switch (type){
            case 3:builder.setTitle("请输入身高（单位：米）");
                unit = "米";
                title = "身高";
                break;
            case 4:builder.setTitle("请输入体重（单位：公斤）");
                unit = "公斤";
                title = "体重";
                break;
            default:unit = "";
                title = "";
                break;
        }
        //创建一个EditText对象设置为对话框中显示的View对象
        final EditText ed = new EditText(MymessageActivity.this);
        builder.setView(ed);
        //用户选好要选的选项后，点击确定按钮
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(ed.getText().toString().matches("[0-9]+(\\.[0-9]+)?")){
                    listContent[type] = ed.getText().toString() + unit;
                    Map<String,Object>map=new HashMap<String,Object>();
                    map.put("title",title);
                    map.put("content",listContent[type]);
                    viewList.set(type,map);
                    adapter.notifyDataSetChanged();
                }
                else Toast.makeText(getApplicationContext(),"输入格式错误",Toast.LENGTH_SHORT).show();
            }
        });
        // 取消选择
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private void savePersonalData(){
        mySharedPreferences= getSharedPreferences("test", 0);
        editor = mySharedPreferences.edit();
        editor.putString("pName",listContent[0]);
        editor.putString("pSex",listContent[1]);
        editor.putString("pBirth",listContent[2]);
        editor.putString("pHeight",listContent[3]);
        editor.putString("pWeight",listContent[4]);
        editor.commit();
    }

    private void loadPersonalData(){
        mySharedPreferences= getSharedPreferences("test", 0);
        listContent[0] = mySharedPreferences.getString("pName","未设置");
        listContent[1] = mySharedPreferences.getString("pSex","未设置");
        listContent[2] = mySharedPreferences.getString("pBirth","未设置");
        listContent[3] = mySharedPreferences.getString("pHeight","未设置");
        listContent[4] = mySharedPreferences.getString("pWeight","未设置");
    }

    private int getbirthyear(){
        if(listContent[2].equals("未设置")){
            return 1995;
        }
        else return Integer.parseInt(listContent[2].substring(0,4));
    }
    private int getbirthmonth(){
        if(listContent[2].equals("未设置")){
            return 10;
        }
        return Integer.parseInt(listContent[2].substring(5,7));
    }
    private int getbirthday(){
        if(listContent[2].equals("未设置")){
            return 10;
        }
        return Integer.parseInt(listContent[2].substring(8));
    }

    private void photoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle("选择方式");
        //定义列表中的选项
        final String[] items = {"相册","照相机"};

        //设置列表选项
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0: Intent intent1 = new Intent(Intent.ACTION_PICK, null);
                        intent1.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                        startActivityForResult(intent1, 1);
                        dialog.dismiss();
                        break;
                    case 1:Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent2.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "head.jpg")));
                        startActivityForResult(intent2, 2);// 采用ForResult打开
                        dialog.dismiss();
                        break;
                }
            }
        });
        // 取消选择
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    cropPhoto(data.getData());// 裁剪图片
                }

                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    File temp = new File(Environment.getExternalStorageDirectory() + "/head.jpg");
                    cropPhoto(Uri.fromFile(temp));// 裁剪图片
                }

                break;
            case 3:
                if (data != null) {
                    Bundle extras = data.getExtras();
                    head = extras.getParcelable("data");
                    if (head != null) {
                        /**
                         * 上传服务器代码
                         */
                        setPicToView(head);// 保存在SD卡中
                        cv.setImageBitmap(head);// 用ImageView显示出来
                    }
                }
                break;
            default:
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void cropPhoto(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 3);
    }

    private void setPicToView(Bitmap mBitmap) {
        FileOutputStream fout = null;
        try {
            fout = openFileOutput(fileName, MODE_PRIVATE);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fout);// 把数据写入文件
            mySharedPreferences = getSharedPreferences("test", 0);
            editor = mySharedPreferences.edit();
            editor.putBoolean("HeaderExist",true);
            editor.commit();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                // 关闭流
                fout.flush();
                fout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initHeadPic(){
        FileInputStream fin = null;
        try {
            fin = openFileInput(fileName);
            Bitmap bt = BitmapFactory.decodeStream(fin);// 从SD卡中找头像，转换成Bitmap
            if (bt != null) {
                @SuppressWarnings("deprecation")
                Drawable drawable = new BitmapDrawable(bt);// 转换成drawable
                cv.setImageDrawable(drawable);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                // 关闭流
                fin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
