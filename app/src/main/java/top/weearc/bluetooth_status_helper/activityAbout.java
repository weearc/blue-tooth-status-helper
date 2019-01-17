package top.weearc.bluetooth_status_helper;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class activityAbout extends AppCompatActivity {

    TextView textView;
    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setTitle("About");

        textView = (TextView) this.findViewById(R.id.mAbout);
        textView.setText("蓝牙助手的出现是因为手机ROM巨坑的设计，虽然看起来功能很简单但是却意外的难写。在此感谢杨瑞龙老师的指导以及CSDN及博客园上大量的项目源码和分析可供参考。\n\n学到了很多东西，谢谢！\n\n\n\n\n\n重庆大学网络信息协会  齐彬烨");

        btn = (Button) this.findViewById(R.id.mgithub);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://github.com/weearc/");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);

            }
        });
    }
}
