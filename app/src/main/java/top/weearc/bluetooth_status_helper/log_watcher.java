package top.weearc.bluetooth_status_helper;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class log_watcher extends AppCompatActivity {
    
    TextView log;
    TextView log_time;
    Button btn;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_watcher);{
            setTitle("LOG");
            Bundle bundle = getIntent().getBundleExtra("bundle");
            final String[] information = {bundle.getString("information")};
            final String[] time = {bundle.getString("time")};


            log = (TextView) this.findViewById(R.id.logcat);
            log_time = (TextView) this.findViewById(R.id.logcat_time);
            btn = (Button) this.findViewById(R.id.log_clean);

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    time[0]="\0";
                    information[0] ="\0";

                    log.setText("\0");
                    log_time.setText("\0");
                    Toast.makeText(log_watcher.this,"日志已清除",Toast.LENGTH_SHORT).show();
                }
            });



            log.setText(information[0]);
            log_time.setText(time[0]);


        }

    }
}
