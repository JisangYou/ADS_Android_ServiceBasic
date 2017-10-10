package orgs.androidtown.servicebasic;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intent = new Intent(this, MyService.class);
    }
    // 서비스 시작
    public void start(View view){
        startService(intent);
    }
    // 서비스 종료
    public void stop(View view){
        stopService(intent);
        isService = false;
    }

    boolean isService = false;
    //Service를 담아두는 변수
    MyService service;
    // 서비스와의 연결 통로
    ServiceConnection con = new ServiceConnection() {
        // 서비스와 연결되는 순간 호출되는 함수
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            isService = true;
            service = ((MyService.CustomBinder)iBinder).getService();
            Toast.makeText(MainActivity.this, "total="+service.getTotal(),Toast.LENGTH_SHORT).show();
        }
        // 서비스가 중단되거나 연결이 도중에 끊겼을 때 발생한다
        // 예) 정상적으로 stop이 호출되고, onDestroy가 발생하면 호출되지 않습니다.
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isService = false;
        }
    };

    public void bind(View view){
        bindService(intent, con, Context.BIND_AUTO_CREATE);
    }
    public void unbind(View view){
        if(isService){
            unbindService(con);
        }

    }

    public void startForeground(View view){

    }

    public void stopForeground(View view){

    }

}