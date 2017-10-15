# ServiceBasic
## Service란?

- 서비스는 액티비티(Activity), 방송수신자(BroadcastReceiver), 콘텐츠제공자(ContentProvider)와 함께 어플리케이션을 구성하는 4대 컴포넌트 중의 하나
- 서비스는 액티비티와는 달리 사용자 인터페이스를 가지지않고 백그라운드에서 실행되며 한 번 시작된 서비스는 사용자가 다른앱으로 이동해도 계속해서 실행이 유지
- 서비스를 이용하면 프로세스간 통신(IPC : interprocess communication) 기능도 구현

## 종류

시작 타입의 서비스(Started Service)

- startService()를 호출해서 서비스를 시작하면 시작타입의 서비스
- 이러한 서비스는 한 번 시작되면 백그라운드에서 무한정 실행되지만 보통은 서비스에서 해야할 작업이 끝나면 스스로 종료하는 경우가 대부분
- 이러한 형태의 서비스는 호출자에게 결과값을 반환할 수 없으며 파일 다운로드나 음악재생 등에 사용됨

연결타입의 서비스(Bound Service)

- bindService()를 호출해서 서비스를 시작하면 연결타입의 서비스가 됨.
- 연결타입의 서비스는 클라이언트-서버와 같이 동작하는 것이 특징이죠. 즉 액티비티는 서비스에게 어떤 요청을 전송하고 서비스는 결과값을 반환해주는 형태.
- 이러한 형태의 서비스는 액티비티와 연결되어 있는 동안에만 실행되고 액티비티가 사라지면 서비스도 소멸 또한 하나의 서비스에 다수의 액티비티가 연결될 수도 있음.


## 특이사항

- 서비스도 애플리케이션의 구성요소이므로, 시스템에서 관리
- 따라서 새로 만든 서비스는 항상 메니페스트 파일에 등록해줘야 함.

### 예제 코드
#### MainActivity

```Java
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
        intent.setAction("START");
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
```

- 메인에서 서비스로 인텐트를 통해 메시지를 전달
- 플래그 값을 이용해 서비스를 제어

#### MyService
```Java
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
        if(intent != null){
            String action = intent.getAction();
            switch(action){
                case "START":
                    setNotification("PAUSE");
                    break;
                case "PAUSE":
                    setNotification("START");
                    break;
                case "DELETE":
                    stopForeground(true);
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    // 포어그라운드 서비스하기
    // 포어그라운드 서비스 번호
    public static final int FLAG = 17465;

    private void setNotification(String cmd){
        // 포어그라운드 서비스에서 보여질 노티바 만들기
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher) //최상단 스테이터스 바에 나타나는 아이콘
                .setContentTitle("음악제목")
                .setContentText("가수명");
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        builder.setLargeIcon(icon); // 노티바에 나타나는 큰 아이콘
        // icon.release 필요

        // 노티바 전체를 클릭했을 때 발생하는 액션처리
        Intent deleteIntent = new Intent(getBaseContext(), MyService.class);
        deleteIntent.setAction("DELETE"); // <- intent.getAction에서 취하는 명령어
        PendingIntent mainIntent = PendingIntent.getService(getBaseContext(), 1, deleteIntent, 0);
        builder.setContentIntent(mainIntent);

        /*
           노티에 나타나는 버튼 처리
         */
        // 클릭을 했을때 noti를 멈추는 명령어를 서비스에서 다시 받아서 처리
        Intent pauseIntent = new Intent(getBaseContext(), MyService.class);
        pauseIntent.setAction(cmd); // <- intent.getAction에서 취하는 명령어
        PendingIntent pendingIntent = PendingIntent.getService(getBaseContext(), 1, pauseIntent, PendingIntent.FLAG_CANCEL_CURRENT);


        // 노티피케이션에 들어가는 버튼을 만드는 명령
        int iconId = android.R.drawable.ic_media_pause;
        if(cmd.equals("START"))
            iconId = android.R.drawable.ic_media_play;
        String btnTitle = cmd;

        NotificationCompat.Action pauseAction
                = new NotificationCompat.Action.Builder(iconId, btnTitle, pendingIntent).build();
        builder.addAction(pauseAction);

        Notification notification = builder.build();
        startForeground(FLAG, notification);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MyService","========onCreate()");
    }

    @Override
    public void onDestroy() {

        stopForeground(true); // 포그라운드 상태에서 해제된다. 서비스는 유지

        super.onDestroy();
        Log.d("MyService","========onDestroy()");
    }
}
```

- 'PendingIntent'는 위임된 인텐트라고 해석할 수 있음.
- 노티바를 삭제, 중지 할때 다른 PendingIntent를 줌. 이에 대한 제어는 다음과 같은 명령어들로 함.
-    PendingIntent 생성시 마지막에 들어가는 Flag 값
    /* 출처 : http://aroundck.tistory.com/2134
    FLAG_CANCEL_CURRENT : 이전에 생성한 PendingIntent 는 취소하고 새롭게 만든다.
    FLAG_NO_CREATE : 이미 생성된 PendingIntent 가 없다면 null 을 return 한다. 생성된 녀석이 있다면 그 PendingIntent 를 반환한다. 즉 재사용 전용이다.
    FLAG_ONE_SHOT : 이 flag 로 생성한 PendingIntent 는 일회용이다.
    FLAG_UPDATE_CURRENT : 이미 생성된 PendingIntent 가 존재하면 해당 Intent 의 Extra Data 만 변경한다.

- 서비스 개념 보충 필요!
