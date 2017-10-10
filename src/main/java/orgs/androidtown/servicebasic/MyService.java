package orgs.androidtown.servicebasic;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;


public class MyService extends Service {
    public MyService() {
    }

    // 컴포넌트는 바인더를 통해 서비스에 접근할 수 있다
    class CustomBinder extends Binder {
        public CustomBinder(){

        }
        public MyService getService(){
            return MyService.this;
        }
    }

    IBinder binder = new CustomBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("MyService","========onBind()");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("MyService","========onUnbind()");
        return super.onUnbind(intent);
    }

    public int getTotal(){
        return total;
    }

    private int total = 0;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground();
        Log.d("MyService","========onStartCommand()");
        for(int i=0 ; i<1000 ; i++){
            total += i;
            System.out.println("서비스에서 동작중입니다."+i);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    // 포어그라운드 서비스하기
    // 포어그라운드 서비스 번호
    public static final int FLAG = 17465;
    private void startForeground(){
        // 포어그라운드 서비스에서 보여질 노티바 만들기
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        Notification notification = builder.setSmallIcon(R.mipmap.ic_launcher) // 아이콘
                .setContentTitle("노티 타이틀")
                .setContentText("노티 내용")
                .build();

        startForeground(FLAG, notification);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MyService","========onCreate()");
    }

    @Override
    public void onDestroy() {
        //stopForeground(true);
        super.onDestroy();
        Log.d("MyService","========onDestroy()");
    }
}
