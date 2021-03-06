package com.example.root.experimentassistant.ThirdLevel;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.root.experimentassistant.R;
import com.example.root.experimentassistant.StaticSetting.StaticConfig;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Date;

public class TakePhotoActivity extends Activity implements OnClickListener{

    private static final int TAKE_PHOTO=1;
    private static final int PICK_PHOTO=2;
    private static final int CANCEL=3;

    private Button btn_take_photo;
    private Button btn_pick_photo;
    private Button btn_cancel;
    private LinearLayout layout;

    private Bundle bundle;

    private String src_path="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);
        btn_take_photo = (Button) this.findViewById(R.id.btn_take_photo);
        btn_pick_photo = (Button) this.findViewById(R.id.btn_pick_photo);
        btn_cancel = (Button) this.findViewById(R.id.btn_cancel);

        bundle=getIntent().getExtras();

        layout=(LinearLayout)findViewById(R.id.pop_layout);

        //添加选择窗口范围监听可以优先获取触点，即不再执行onTouchEvent()函数，点击其他地方时执行onTouchEvent()函数销毁Activity
        layout.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
            }
        });
        //添加按钮监听
        btn_cancel.setOnClickListener(this);
        btn_pick_photo.setOnClickListener(this);
        btn_take_photo.setOnClickListener(this);
    }

    //实现onTouchEvent触屏函数但点击屏幕时销毁本Activity
    @Override
    public boolean onTouchEvent(MotionEvent event){
        finish();
        return true;
    }

    public void onClick(View v) {
        int result_code=RESULT_OK;
        switch (v.getId()) {
            case R.id.btn_take_photo:
                Intent take=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                String name = bundle.getInt("exper_id")+"_"+bundle.getInt("id");
                File file = new File(StaticConfig.IMAGE_STORAGE_URL+name);
                src_path=file.getAbsolutePath();
                take.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                startActivityForResult(take, TAKE_PHOTO);
                break;
            case R.id.btn_pick_photo:
                Intent local=new Intent();
                local.setType("image/*");
                local.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(local, PICK_PHOTO);
                break;
            case R.id.btn_cancel:
                result_code=RESULT_CANCELED;
                break;
            default:
                break;
        }
        Intent result=new Intent();
        bundle.putCharSequence("path",src_path);
        result.putExtras(bundle);
        setResult(result_code,result);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch(requestCode) {
                case TAKE_PHOTO:
                    break;
                case PICK_PHOTO:
                    Uri uri = data.getData();
                    ContentResolver cr = this.getContentResolver();
                    Cursor c = cr.query(uri, null, null, null, null);
                    c.moveToFirst();
                    //这是获取的图片保存在sdcard中的位置
                    src_path = c.getString(c.getColumnIndex("_data"));
                    break;
                default:
                    break;
            }
        }
    }
}