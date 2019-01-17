package top.weearc.bluetooth_status_helper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.midi.MidiDeviceService;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {

    // 重命名Bluetooth adapter
    private BluetoothAdapter bltAdapter;
    String information = "日志记录开始..."+"\n";
    String additional_inform="";
    String time="";
    private TextView log;
    private TextView log_time;
    public Button goTO_log;
    private Switch mswitch;
    int timeout=300;//google所允许的设备最大超时时间（可被发现时间）
    EditText editText;
    Button cs_btn;
    Button scan_btn;
    private long firstTime = 0;//设定用户按下返回键初次的时间，并在下面改写返回方法
    Button mhelp;
    public static final  String TAG="MainActivity";
    Button mCancel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        log = (TextView)findViewById(R.id.logcat);
        log_time =(TextView)findViewById(R.id.logcat_time);
        goTO_log = (Button)this.findViewById(R.id.goTO_log);
        mswitch = (Switch) this.findViewById(R.id._switch);
        editText =(EditText) this.findViewById(R.id.editText);
        cs_btn = (Button) this.findViewById(R.id.changeStatus);
        scan_btn = (Button) this.findViewById(R.id._scan);
        mhelp = (Button) this.findViewById(R.id.mhelp);
        mCancel = (Button) this.findViewById(R.id.mCancel);

        setTitle("蓝牙助手");
        {
            if (savedInstanceState == null ){
                bltAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

                //查看设备是否支持蓝牙功能
                if (bltAdapter != null ){

                    //查看蓝牙是否开启
                    if (bltAdapter.isEnabled()){
                        //若已经开启蓝牙则弹出一个提示已经开启
                        String notification = "蓝牙已经开启";
                        additional_inform="蓝牙开启";
                        information=logcat(information,additional_inform);
                        Toast.makeText(MainActivity.this,notification,Toast.LENGTH_SHORT).show();
                    }else   {
                        //若未开启则弹出一个窗口请求开启权限
                        Intent enablebtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enablebtIntent, Constants.REQUEST_ENABLE_BT);
                    }
                }else {
                    //当设备不支持蓝牙功能的时候点击按钮退出程序
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("注意");
                    builder.setMessage("您的设备很可能不支持蓝牙功能");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    builder.show();
                }
            }

        }
        //蓝牙开关功能
        mswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    Intent enablebtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enablebtIntent, Constants.REQUEST_ENABLE_BT);
                    Toast.makeText(MainActivity.this,"蓝牙开启",Toast.LENGTH_SHORT).show();
                    additional_inform="蓝牙开启";
                    information=logcat(information,additional_inform);
                } else {
                    bltAdapter.disable();
                    Toast.makeText(MainActivity.this,"蓝牙关闭",Toast.LENGTH_SHORT).show();
                    additional_inform="蓝牙关闭";
                    information=logcat(information,additional_inform);
                }
            }
        });

        //查看日志功能
        goTO_log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent log = new Intent(MainActivity.this,log_watcher.class);
                Bundle bundle = new Bundle();
                bundle.putString("information",information);
                bundle.putString("time",time);
                log.putExtra("bundle",bundle);
                startActivity(log);
            }
        });

        //可被发现状态时间

        cs_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String _timeout = editText.getText().toString();

                if(TextUtils.isEmpty(editText.getText()) ){//判断输入是否为空

                    Intent dis_intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    dis_intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                    startActivity(dis_intent);
                    Toast.makeText(MainActivity.this,"设备参照默认最大时间:300秒设置可被发现",Toast.LENGTH_LONG).show();
                    additional_inform="设备可被发现，时间"+timeout+"秒";
                    information=logcat(information,additional_inform);

                } else {
                    timeout =Integer.parseInt(_timeout);
                    //若输入值不为空，采用安卓本身方法进行广播
                    if (timeout <= 300 && timeout != 0){

                        Intent dis_intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        dis_intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, timeout);
                        startActivity(dis_intent);
                        Toast.makeText(MainActivity.this,"设备目前已经可被发现，时间为"+timeout+"秒",Toast.LENGTH_LONG).show();
                        additional_inform="设备可被发现，时间"+timeout+"秒";
                        information=logcat(information,additional_inform);

                    } if (timeout > 300){
                        //若时间长于300秒，采用反射方式调用系统蓝牙可被发现的开关以达到延时目的
                        setDiscoverableTimeout(timeout);
                        Toast.makeText(MainActivity.this,"设备目前已经可被发现，时间为"+timeout+"秒",Toast.LENGTH_LONG).show();
                        additional_inform="设备可被发现，时间"+timeout+"秒";
                        information=logcat(information,additional_inform);

                    } if (timeout == 0){
                        Intent dis_intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        dis_intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
                        startActivity(dis_intent);
                        timeout=120;
                        Toast.makeText(MainActivity.this,"输入值为0。设备目前已经可被发现，默认时间为"+timeout+"秒",Toast.LENGTH_LONG).show();
                        additional_inform="设备可被发现，时间"+timeout+"秒";
                        information=logcat(information,additional_inform);
                    }
                }
            }
        });

        //蓝牙广播以及扫描入口
        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bltAdapter.isEnabled()){
                    bltAdapter.enable();
                }

                if (bltAdapter.isDiscovering()) {
                    bltAdapter.cancelDiscovery();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {//利用睡眠阻隔系统重启扫描服务，并处理异常
                        e.printStackTrace();
                    }
                    bltAdapter.startDiscovery();
                }else{
                    bltAdapter.startDiscovery();
                }
                Toast.makeText(MainActivity.this,"正在进行扫描...",Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(bltAdapter.ACTION_REQUEST_ENABLE);
                startActivity(intent);
                //扫描到了任一蓝牙设备
                if(BluetoothDevice.ACTION_FOUND.equals(intent.getAction()))
                {
                    Log.v(TAG, "### BT BluetoothDevice.ACTION_FOUND ##");
                    BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(btDevice != null){

                        Log.v(TAG , "Name : " + btDevice.getName() + " Address: " + btDevice.getAddress());
                        Toast.makeText(MainActivity.this,"Name:"+btDevice.getName()+"Address:"+btDevice.getAddress(),Toast.LENGTH_LONG).show();

                    }
                    else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction()))
                    {
                        Log.v(TAG, "### BT ACTION_BOND_STATE_CHANGED ##");
                        int cur_bond_state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
                        int previous_bond_state = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_NONE);
                        Log.v(TAG, "### cur_bond_state ##" + cur_bond_state + " ~~ previous_bond_state" + previous_bond_state);
                    }
                }


                additional_inform="设备进行扫描";
                information=logcat(information,additional_inform);
            }
        });

        //帮助信息以及关于
        mhelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder mhelp = new AlertDialog.Builder(MainActivity.this);
                mhelp.setTitle("帮助");
                mhelp.setMessage("\n        ｸﾞｯ!(๑•̀ㅂ•́)و✧\n");
                mhelp.setPositiveButton("前往帮助页面", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = Uri.parse("https://blog.weearc.top/2018/12/20/bluetooth-status-helper/");//要跳转的网址
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);

                    }
                });
                mhelp.setNeutralButton("返回", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog alertDialog = mhelp.show();
                        alertDialog.dismiss();
                    }
                });
                mhelp.setNegativeButton("关于", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent about = new Intent(MainActivity.this,activityAbout.class);
                        startActivity(about);
                    }
                });
                mhelp.show();

            }
        });
        //点击取消扫描和状态改变
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bltAdapter.isDiscovering()) {
                    bltAdapter.cancelDiscovery();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {//利用睡眠阻隔系统重启扫描服务，并处理异常
                        e.printStackTrace();
                    }
                }
                Toast.makeText(MainActivity.this,"已取消",Toast.LENGTH_SHORT).show();
            }
        });


    }

    //双击返回退出
    @Override
    public void onBackPressed() {
        long secondTime = System.currentTimeMillis();
        if (secondTime - firstTime > 2000) {
            Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            firstTime = secondTime;
        } else{
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode, data);
        switch (requestCode){
            case Constants.REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK){
                    additional_inform="蓝牙开启";
                    information=logcat(information,additional_inform);
                    AlertDialog.Builder builder_welcome = new AlertDialog.Builder(MainActivity.this);
                    builder_welcome.setTitle("");
                    builder_welcome.setMessage("感谢使用蓝牙助手！\n 这款工具面向部分ROM没有提供查看蓝牙状态功能的机型\n 如：HTC，LG\n 如果您的手机ROM拥有类似功能，您仍可尝试使用。");
                    builder_welcome.setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    });
                    builder_welcome.show();
                    Toast.makeText(MainActivity.this,"蓝牙已开启",Toast.LENGTH_SHORT).show();

                }else {
                    Toast.makeText(MainActivity.this,"蓝牙助手需要蓝牙权限才可以正常运行",Toast.LENGTH_SHORT).show();
                    finish();
                }
                default:super.onActivityResult(requestCode,resultCode,data);
        }

    }


    private String logcat(String information, String additional_inform){
        information=information+additional_inform+'\n';
        additional_inform="\0";
        //log.setText(information);
        get_log_time();
        return information;
    }

    private String get_log_time(){
        SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss");
        java.util.Date date=new java.util.Date();
        time=time+"\n"+sdf.format(date);
        //log_time.setText(time+"\n");
        return time;
    }
    //设定蓝牙超时时间
    public void setDiscoverableTimeout(int timeout) {
        BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
        try {
            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
            setDiscoverableTimeout.setAccessible(true);
            Method setScanMode =BluetoothAdapter.class.getMethod("setScanMode", int.class,int.class);
            setScanMode.setAccessible(true);
            setDiscoverableTimeout.invoke(adapter, timeout);
            setScanMode.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE,timeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}