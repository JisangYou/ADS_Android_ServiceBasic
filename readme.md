# ADS04 Android

## 수업 내용

- service를 학습
- notification 학습

## Code Review


1. MainActivity

```java
public class MainActivity extends AppCompatActivity {
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intent = new Intent(this, MyService.class);
    }

    // 서비스 시작
    public void start(View view) {
        intent.setAction("START");
        startService(intent);
    }

    // 서비스 종료
    public void stop(View view) {
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
            service = ((MyService.CustomBinder) iBinder).getService();
            Toast.makeText(MainActivity.this, "total=" + service.getTotal(), Toast.LENGTH_SHORT).show();
        }

        // 서비스가 중단되거나 연결이 도중에 끊겼을 때 발생한다
        // 예) 정상적으로 stop이 호출되고, onDestroy가 발생하면 호출되지 않습니다.
        @Override
        public void onServiceDisconnected(ComponentName componentName) {

            isService = false;
        }
    };

    public void bind(View view) {

        bindService(intent, con, Context.BIND_AUTO_CREATE);
    }

    public void unbind(View view) {
        if (isService) {
            unbindService(con);
        }

    }

    public void startForeground(View view) {

    }

    public void stopForeground(View view) {

    }

}
```

2. Myservice

```Java
public class MyService extends Service {
    public MyService() {
    }

    // 컴포넌트는 바인더를 통해 서비스에 접근할 수 있다
    class CustomBinder extends Binder {
        public CustomBinder() {

        }

        public MyService getService() {
            // 서비스 객체를 리턴
            return MyService.this;
        }
    }

    // 외부로부터 데이터를 전달하려면 바인더를 사용
    // Binder 객체는 IBinder 인터페이스 상속구현 객체
    IBinder binder = new CustomBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("MyService", "========onBind()");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("MyService", "========onUnbind()");
        return super.onUnbind(intent);
    }

    public int getTotal() {
        return total;
    }

    private int total = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {// 서비스가 호출 될때 마다 실행
        if (intent != null) {
            String action = intent.getAction();
            switch (action) {
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

    private void setNotification(String cmd) {
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
                                                    // 만약 component자체가 activiy면 getActivity로 할 수 있음.
        // PendingIntent 생성시 마지막에 들어가는 Flag 값
        /* 출처 : http://aroundck.tistory.com/2134
        FLAG_CANCEL_CURRENT : 이전에 생성한 PendingIntent 는 취소하고 새롭게 만든다.
        FLAG_NO_CREATE : 이미 생성된 PendingIntent 가 없다면 null 을 return 한다. 생성된 녀석이 있다면 그 PendingIntent 를 반환한다. 즉 재사용 전용이다.
        FLAG_ONE_SHOT : 이 flag 로 생성한 PendingIntent 는 일회용이다.
        FLAG_UPDATE_CURRENT : 이미 생성된 PendingIntent 가 존재하면 해당 Intent 의 Extra Data 만 변경한다.
        */

        // 노티피케이션에 들어가는 버튼을 만드는 명령
        int iconId = android.R.drawable.ic_media_pause;
        if (cmd.equals("START"))
            iconId = android.R.drawable.ic_media_play;
        String btnTitle = cmd;

        NotificationCompat.Action pauseAction
                = new NotificationCompat.Action.Builder(iconId, btnTitle, pendingIntent).build();
        builder.addAction(pauseAction);

        Notification notification = builder.build();
        startForeground(FLAG, notification);// 안드로이드의 메모리 관리 정책에 의해 서비스는 도중에 종료될 수 있습니다. 이를 방지하기 위해서는 서비스가 foreground에서 실행
        
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MyService", "========onCreate()");
    }

    @Override
    public void onDestroy() {

        stopForeground(true); // 포그라운드 상태에서 해제된다. 서비스는 유지

        super.onDestroy();
        Log.d("MyService", "========onDestroy()");
    }
}
```


## 보충설명

### service란?

- service는 Activity처럼 사용자와 상호작용하는 컴포넌트가 아니고, Background에서 동작하는 컴포넌트
- 서비스는 액티비티(Activity), 방송수신자(BroadcastReceiver), 콘텐츠제공자(ContentProvider)와 함께 어플리케이션을 구성하는 4대 컴포넌트 중의 하나
- 서비스는 액티비티와는 달리 사용자 인터페이스를 가지지않고 백그라운드에서 실행되며 한 번 시작된 서비스는 사용자가 다른앱으로 이동해도 계속해서 실행이 유지
- 서비스를 이용하면 프로세스간 통신(IPC : interprocess communication) 기능도 구현

### service는 왜 필요한가?

- Activity가 종료되어 있는 상태에서도 동작하기 위해서 만들어진 컴포넌트
- 음악 App 같은 경우에 노래를 틀고 App을 종료해도 노래가 계속 나옴, 네비게이션 같은 경우 역시도 경로지정을 하면, app을 종료해도 길 찾기가 계속 됨.
- 만약 Service가 실행되고 있는 상태라면 안드로이드 OS에서는 해당 Process를 죽이지 않도록 방지하고 관리
- 그렇게 때문에 메모리 부족이나 특별한 경우를 제외하고는 Background 동작을 수행하도록 설계

### service 특징

- 서비스는 프로세스간 메모리를 공유하거나, 원하는 함수를 호출할 수 있도록 하는 IPC, RPC를 지원
- 서비스도 애플리케이션의 구성요소이므로, 시스템에서 관리
- 따라서 새로 만든 서비스는 항상 메니페스트 파일에 등록해줘야 함.

### service 사용방법

- Service에는 두가지 있음.
- 1. Started Service를 이용하는 방법
- 2. Bound Service를 이용하는 방법

#### 1. StartService()

![startService](http://cfile10.uf.tistory.com/image/250B8A3B56D846C33149C9)
>> startService()를 호출해서 서비스를 시작하면 시작타입의 서비스

- StartService는 백스라운드에서 동작을 하지만 기본 어플리케이션 즉 프로세스안에서 동작, 그리고 프로세스 안 다른 컴포넌드들과 유기적으로 통신
- 특정 서비스를 동작시켜 두는 데 목적을 둔 형태, 서비스를 동작시킨 컴포넌트는 서비스를 중단하는 것 이외에 어떤 제어도 할 수없음.
- 이러한 서비스는 한 번 시작되면 백그라운드에서 무한정 실행되지만 보통은 서비스에서 해야할 작업이 끝나면 스스로 종료하는 경우가 대부분
- 이러한 형태의 서비스는 호출자에게 결과값을 반환할 수 없으며 파일 다운로드나 음악재생 등에 사용됨

#### 2. BindService()

![BindService](http://cfile24.uf.tistory.com/image/22498C3B56D846C50210D4)

>> bindService()를 호출해서 서비스를 시작하면 연결타입의 서비스가 됨.
- 연결타입의 서비스는 클라이언트-서버와 같이 동작하는 것이 특징이죠. 즉 액티비티는 서비스에게 어떤 요청을 전송하고 서비스는 결과값을 반환해주는 형태.
- 이러한 형태의 서비스는 액티비티와 연결되어 있는 동안에만 실행되고 액티비티가 사라지면 서비스도 소멸 또한 하나의 서비스에 다수의 액티비티가 연결될 수도 있음
- 서비스는 프로세스 내에서 다른 컴포넌트들과 서로 유기적으로 통신하는것 뿐만 아니라 다른 앱 즉 다른 프로세스와도 Data 공유 및 통신
- 대표적인 예로 다른 어플리케이션에서 어떠한 신호가 발생하였을 때 서비스가 반응하여 동작.
- 외부 라이브러리를 사용하는 것과 유사함.

### service 생명주기

![서비스 생명주기](http://cfile23.uf.tistory.com/image/1752B4484F79B1AA02F5C4)
![서비스 생명주기2](http://cfile23.uf.tistory.com/image/23510544544E61DE1E4A92)

- 주요 콜백 메소드
```Java
onStartCommand()
다른 컴포넌트(액티비티 등)가 startService()를 호출함으로써 서비스 실행을 요청할 때, 시스템이 호출해주는 콜백 메소드입니다. 이 메소드가 실행되면 서비스는 started 상태가 되며, 후면(background)에서 작업을 수행합니다. 만약 이 메소드를 구현한다면, 서비스가 할 작업을 모두 마쳤을 때 서비스를 중지하기 위해 stopSelf()나 stopService()를 호출하는 부분도 구현해야 합니다. (만약 서비스를 바인드만 해서 사용한다면, 이 메소드를 구현하지 않아도 됩니다.)

onBind()
다른 컴포넌트가 (RPC를 하기 위해) bindService()를 호출함으로써 서비스를 바인드할 때, 시스템이 호출해주는 콜백 메소드입니다. 이 메소드에서는, 다른 컴포넌트가 서비스의 메소드들을 사용할 수 있도록 IBinder라는 인터페이스를 리턴해줘야 합니다. 다른 컴포넌트에 의해 바인드 되어 사용될 서비스라면 이 콜백 메소드를 반드시 구현해야 하지만, 바인드 되는 서비스가 아니라면 구현할 필요가 없습니다(기본적으로는 IBinder가 null로 리턴됩니다).

onCreate()
서비스가 생성될 때 호출되는 콜백 메소드이며, 여기에서는 (액티비티의 onCreate()와 마찬가지로) 서비스가 살아있는 동안 사용할 멤버 변수들을 셋팅하는 일을 합니다. 이 메소드는 onStartCommand()나 onBind()가 호출되기 전에 호출되며, 서비스가 실행되고 있는 중이라면 호출되지 않습니다.

onDestroy()
서비스가 더이상 사용되지 않아 종료될 때 호출되는 콜백 메소드입니다. 이 메소드는 서비스의 생명주기에서 가장 마지막에 호출되는 콜백 메소드이기 때문에, 여기에서는 서비스에서 사용하던 리소스들(쓰레드, 등록된 리스너, 리시버 등)을 모두 정리해줘야(clean up) 합니다. 

만약 어떤 컴포넌트가 startService()를 호출하여 서비스를 실행했다면(이 경우 서비스의 onStartCommand()가 호출됩니다), 그 서비스는 스스로 stopSelf()를 호출하거나 다른 컴포넌트가 stopService()를 호출할 때까지 started 상태로 유지됩니다. 

만약 어떤 컴포넌트가 bindService()를 호출하여 서비스를 생성했다면(이 경우 서비스의 onStartCommand()가 호출되지 않습니다), 그 서비스는 바인드되어 있는 동안에만 실행됩니다. 만약 서비스를 바인드한 컴포넌트들이 모두 언바인드(unbind)한다면, 시스템은 그 서비스를 종료시킵니다.

- 출처: http://android-kr.tistory.com/9 [안드로이드-KR]

```

- Service는 기본적으로 프로세스가 종료되더라도 다시 살아는 구조로 되어있음.
- 종료된 Service는 onCreate() -> onStartCommand()순으로 주기를 타는데 살아있을 때 또 부르면 onCreate()를 거치지 않고 onStartCommand()로 바로 옴
- onStartCommand()는 3가지 리턴타입이 있음.

![3가지리턴타입](http://cfile21.uf.tistory.com/image/2767773356D84A020B6FA0)

### 유의점

![유의점](http://cfile9.uf.tistory.com/image/1138C84F502A1CD42BF7E1)

- 모든 컴포넌트들이 Main Thread 안에 실행된다는 점 입니다. 
- Main Thread는 앞서 Thread의 예제에서 살펴봤듯이, UI 작업을 처리해주는 Thread
- Service 역시 Main Thread에서 관리 Thread 작업이 필요한 경우, 별도의 작업 Thread를 생성해서 관리해줘야 함. 

### notification

#### 기본 method

![notiBasic](http://cfile29.uf.tistory.com/image/2576B64358968283132099)

- +추가옵션

```java
1. setLargeIcon : 큰그림
2. setSmallIcon : 큰그림 밑에 작은그림
3. setTicker : 알람 발생시 잠깐 나오는 텍스트 (테스트 해보니까 가상 머신에서는 안나오고 실제 디바이스에서는 나오네요)
4. setContetnTitle : 제목
5. setContentText : 내용
6. setWen : 알람 시간 (miliSecond 단위로 넣어주면 내부적으로 자동으로 파싱합니다)
7. setDefaults : 알람발생시 진동, 사운드등을 설정 (사운드, 진동 둘다 설정할수도 있고 한개 또는 설정하지 않을 수도있음)
8. setContentIntent : 알람을 눌렀을 때 실행할작업 인텐트를 설정합니다.
9. setAutoCancel : 알람 터치시 자동으로 삭제할 것인지 설정합니다.
10. setNumber : 확인하지 않은 알림 갯수를 설정합니다. (999가 초과되면 999+로 나타냅니다.
```

#### PendingIntent

- 수행시킬 작업 및 인텐트(실행의도)와 및 그것을 수행하는 주체를 지정하기 위한 정보를 명시 할 수 있는 기능의 클래스
- 컴포넌트에서 다른 컴포넌트에게 작업을 요청하는 인텐트를 사전에 생성시키고 만든다는 점과 "특정 시점"에 자신이 아닌 다른 컴포넌트들이 펜딩인텐트를 사용하여 다른 컴포넌트에게 작업을 요청시키는 데 사용된다는 점이 차이점
- pendingIntent를 사용하는 대표적인 예 

```Java
- 사용자가 Notification을 통해 특정한 동작을 할 때, 실행되는 인텐트를 생성함 (NotificationManager가 인텐트를 실행)
- 사용자가 AppWidget을 통해 특정한 동작을 할 때,  실행되는 인텐트를 생성함 (홈 스크린이 인텐트를 실행)
- 미래의 특정 시점에 실행되는 인텐트를 선언함 (안드로이드의 AlarmManager가 인텐트를 실행
```
- 사용법 
```Java
PendingIntent pi = PendingIntent.getActivity()(or getService())(Context context, int requestCode,
            @NonNull Intent intent, @Flags int flags)
```

#### Noti sending

- 예제 코드
``` java
NotificationManager NM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
NM.notify(0, mBuilder.build());
//  상기 이미지 mBuilder 참고
```


### 출처

- 출처 : http://cocomo.tistory.com/418 [Cocomo Coding]
- 출처: http://arabiannight.tistory.com/entry/안드로이드Android-Service-사용법 [아라비안나이트]
- 출처: http://twinw.tistory.com/49 [흰고래의꿈]
- 출처: 이것이 안드로이드다[박성근의 안드로이드 앱 프로그래밍]
- 출처 : http://www.joshi.co.kr/index.php?document_srl=13912&mid=board_QBES95
- 출처: http://techlog.gurucat.net/80 [하얀쿠아의 개발 이야기]
- 출처: http://android-kr.tistory.com/9 [안드로이드-KR]

## TODO

- service 관련 예제 만들어보기.
- service와 Thread의 차이 및 원리 학습 [service와Thread](https://medium.com/@joongwon/android-service%EC%99%80-thread%EC%9D%98-%EC%B0%A8%EC%9D%B4-a9175016450) 


## Retrospect

- 서비스는 액티비티처럼 눈에 보이는 것이 아니기에 개념이 굉장히 어려웠으나, 예시를 보면서 공부를 하니 한결 나았다. 
- 아직도 조금 이해가 안되는 부분이 있으므로, 좀더 복습을 해야하는 부분

## Output

- 생략