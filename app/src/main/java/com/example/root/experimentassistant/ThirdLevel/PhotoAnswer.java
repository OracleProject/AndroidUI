package com.example.root.experimentassistant.ThirdLevel;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import com.example.root.experimentassistant.ViewModel.*;

import com.example.root.experimentassistant.Internet.CookieUnits;
import com.example.root.experimentassistant.Internet.ExperimentHttpClient;
import com.example.root.experimentassistant.Model.User;
import com.example.root.experimentassistant.R;
import com.example.root.experimentassistant.StaticSetting.StaticConfig;
import com.example.root.experimentassistant.ViewModel.ImageItem;
import com.example.root.experimentassistant.ViewModel.Question;
import com.example.root.experimentassistant.ViewModel.ViewCourse;
import com.example.root.experimentassistant.util.Bimp;
import com.facebook.common.file.FileUtils;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

import cz.msebera.android.httpclient.Header;

public class PhotoAnswer extends AppCompatActivity {
    private GridView photoGridView;

    private GridAdapter gridAdapter;

    private PopupWindow popupWindow;

    public static Bitmap bitmap;

    private ImageView back;

    private EditText answerText;

    private View parentView;

    private TextView upload;

    private TextView questionText;

    private Dialog loading_dialog;

    private int questionId;

    private Question question;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentView=getLayoutInflater().inflate(R.layout.activity_photo_answer, null);
        setContentView(parentView);

        bitmap= BitmapFactory.decodeResource(getResources(),R.mipmap.add);
        photoGridView=(GridView)findViewById(R.id.selectGridview);
        back=(ImageView)findViewById(R.id.Cancle);
        answerText=(EditText)findViewById(R.id.answerText);
        upload=(TextView)findViewById(R.id.answerUpload);
        questionText=(TextView)findViewById(R.id.question);
        questionId=getIntent().getExtras().getInt("questionId");
        question=User.getInstance().getExperiment().getQuestions().get(questionId);

        init();
    }

    private void init(){
        //初始化Btmp
        if(Bimp.tempSelectBitmap.size()!=0){
            Bimp.tempSelectBitmap.clear();
        }
        //已经回答过
        Log.d("question","type "+question.getAnswer_type()+" isAnswer "+(question.isAnswered()?"answered":"no answered"));
        if(question.isAnswered()&&question.getAnswer_type()==Question.PHOTOQUESTION){
            Bimp.tempSelectBitmap=((photoQuestion)question).getBitmap();
            answerText.setText(question.getAnswer());
        }

        //初始化问题
        questionText.setText(question.getQuestion());

        //初始化弹出菜单
        popupWindow=new PopupWindow(this);
        View view = getLayoutInflater().inflate(R.layout.activity_take_photo,null);
        final LinearLayout layout=(LinearLayout)view.findViewById(R.id.pop_layout);

        popupWindow.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setContentView(view);

        Button takePhoto=(Button)view.findViewById(R.id.btn_take_photo);
        Button pickPhoto=(Button)view.findViewById(R.id.btn_pick_photo);
        Button cancle=(Button)view.findViewById(R.id.btn_cancel);

        //拍照
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent take=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(take,0);
                popupWindow.dismiss();
            }
        });
        // 从相册中取
        pickPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PhotoAnswer.this,AlbumActivity.class));
                overridePendingTransition(R.anim.trans_in, R.anim.trans_out);
                layout.clearAnimation();
                popupWindow.dismiss();
            }
        });
        //取消
        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.startAnimation(AnimationUtils.loadAnimation(PhotoAnswer.this,R.anim.trans_out));

                new Handler().postDelayed(new Runnable(){

                    public void run() {
                        popupWindow.dismiss();
                        layout.clearAnimation();
                    }

                }, 300);
            }
        });

        //返回按钮
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Bimp.tempSelectBitmap.clear();
            }
        });

        //提交按钮
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PhotoAnswer.this,"update press",Toast.LENGTH_SHORT);
                Log.d("update","press");
                Upload();
            }
        });

        //图片栏初始化
        gridAdapter=new GridAdapter(this);
        photoGridView.setAdapter(gridAdapter);
        photoGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position == Bimp.tempSelectBitmap.size()) {
                        layout.startAnimation(AnimationUtils.loadAnimation(PhotoAnswer.this,R.anim.trans_in));
                        popupWindow.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
                    } else {
                        Intent intent = new Intent(PhotoAnswer.this,
                                PhotoPreviewActivity.class);
                        intent.putExtra("position", position);
                        startActivity(intent);
                    }

            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (Bimp.tempSelectBitmap.size() < 9 && resultCode == RESULT_OK) {
                    Bitmap bm = (Bitmap) data.getExtras().get("data");

                    ImageItem takePhoto = new ImageItem();
                    takePhoto.setBitmap(bm);
                    Bimp.tempSelectBitmap.add(takePhoto);
                }
                break;
        }
    }

    private void Upload(){
        loading_dialog= StaticConfig.createLoadingDialog(PhotoAnswer.this,"上传中...");
        String url = "api/experiments/" + User.getInstance().getExperiment().getId() + "/submitStep";

        Map<String, String> heads = new HashMap<>();
        heads.put("token", User.getInstance().getToken());

        RequestParams params=new RequestParams();
        params.put("answer",answerText.getText().toString());
        params.put("ImageCount",Bimp.tempSelectBitmap.size());
        params.put("index",questionId+1);

        try{
            for (int i = 0; i < Bimp.tempSelectBitmap.size(); i++) {
                Bitmap bm = Bimp.tempSelectBitmap.get(i).getBitmap();
                File file = new File(getFilesDir(), "tmpPhoto"+i+".jpg");
                BufferedOutputStream bos;
                bos = new BufferedOutputStream(new FileOutputStream(file));
                bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                bos.flush();
                bos.close();
                params.put("images["+i+"]",file);
            }
        }catch(Exception e){
            e.printStackTrace();
            Log.d("upload","Exception");
        }

        Log.d("update","begin");
        ExperimentHttpClient.getInstance().postMultipart(url, params, heads, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("fileUpdate","success "+statusCode);
                StaticConfig.closeDialog(loading_dialog);

                question.setAnswered(true);
                question.setAnswer(answerText.getText().toString());
                Log.d("question after","id "+questionId+"question"+" type "+question.getAnswer_type()+" isAnswer "+(question.isAnswered()?"answered":"no answered"));
                if(question.getAnswer_type()==Question.PHOTOQUESTION){
                    ArrayList<ImageItem> tmp=new ArrayList<ImageItem>();
                    tmp.addAll(Bimp.tempSelectBitmap);
                    ((photoQuestion)question).setBitmap(tmp);
                }

                Intent res=new Intent();
                res.putExtra("questionId",questionId);
                setResult(RESULT_OK,res);
                finish();
                Bimp.tempSelectBitmap.clear();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                String message = errorResponse.optString("message");
                if (message.isEmpty()) {
                    message = "网络故障，请稍候再试";
                }

                Toast.makeText(PhotoAnswer.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    class GridAdapter extends BaseAdapter{
        private Context mContext = null;

        private int selectedPosition=-1;

        public GridAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            if(Bimp.tempSelectBitmap.size() == 9){
                return 9;
            }
            return (Bimp.tempSelectBitmap.size() + 1);
        }

        @Override
        public ViewCourse getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return  position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.photo_grid_item,
                        parent, false);
                holder = new ViewHolder();
                holder.image = (ImageView) convertView
                        .findViewById(R.id.item_grida_image);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (position ==Bimp.tempSelectBitmap.size()) {
                holder.image.setImageBitmap(BitmapFactory.decodeResource(
                        getResources(), R.mipmap.add));
                if (position == 9) {
                    holder.image.setVisibility(View.GONE);
                }
            } else {
                holder.image.setImageBitmap(Bimp.tempSelectBitmap.get(position).getBitmap());
            }

            return convertView;
        }

        public void setSelectedPosition(int position) {
            selectedPosition = position;
        }

        public int getSelectedPosition() {
            return selectedPosition;
        }

        private class ViewHolder {
            public ImageView image;
        }

        Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        gridAdapter.notifyDataSetChanged();
                        break;
                }
                super.handleMessage(msg);
            }
        };

        public void loading() {
            new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        if(Bimp.max>Bimp.tempSelectBitmap.size()){
                            Bimp.max=Bimp.tempSelectBitmap.size();
                        }
                        if (Bimp.max == Bimp.tempSelectBitmap.size()) {
                            Message message = new Message();
                            message.what = 1;
                            handler.sendMessage(message);
                            break;
                        } else {
                            Bimp.max += 1;
                            Message message = new Message();
                            message.what = 1;
                            handler.sendMessage(message);
                        }
                    }
                }
            }).start();
        }
    }

    protected void onRestart() {
        gridAdapter.loading();
        super.onRestart();
    }

}
