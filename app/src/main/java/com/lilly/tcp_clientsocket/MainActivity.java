package com.lilly.tcp_clientsocket;

import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    public TextView Toptext;
    public TextView datatext;
    public TextView byText;
    public Button StartButton;
    public Button StopButton;
    public Button ConnButton;
    public Button DiconButton;
    public Button IsconButton;
    public Button Lilly;
    private Socket socket;
    // fixme: TAG
    String TAG = "socketTest";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ConnButton = findViewById(R.id.button1);
        StartButton = findViewById(R.id.button2);
        StopButton = findViewById(R.id.button3);
        DiconButton = findViewById(R.id.button4);
        IsconButton = findViewById(R.id.button5);
        Lilly = findViewById(R.id.button6);
        final EditText ipNumber = findViewById(R.id.ipText);



        Log.i(TAG, "Application createad");

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }


        ConnButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Connect 시도", Toast.LENGTH_SHORT).show();
                String addr = ipNumber.getText().toString().trim();
                ConnectThread thread = new ConnectThread(addr);

                //키보드 자동 내리기
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(ipNumber.getWindowToken(), 0);

                thread.start();


            }
        });

        // fixme: 버튼 ClickListener
        StartButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartThread sthread = new StartThread();
                StartButton.setEnabled(false);
                StopButton.setEnabled(true);

                sthread.start();

            }
        });
        StopButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                StopThread spthread = new StopThread();
                StartButton.setEnabled(true);
                StopButton.setEnabled(false);
                spthread.start();
            }
        });
        DiconButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    socket.close();
                    Toast.makeText(getApplicationContext(), "DisConnect", Toast.LENGTH_SHORT).show();
                    DiconButton.setEnabled(false);
                    ConnButton.setEnabled(true);
                    StartButton.setEnabled(false);
                    StopButton.setEnabled(false);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "DisConnect 실패", Toast.LENGTH_SHORT).show();
                }
            }
        });
        IsconButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean iscon = socket.isClosed();
                InetAddress addr = socket.getInetAddress();
                String tmp = addr.getHostAddress();
                if(!iscon){
                    Toast.makeText(getApplicationContext(), tmp + " 연결 중", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getApplicationContext(), "연결이 안 되어 있습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Lilly.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), " Lilly is Cute.\n Lilly is working hard.", Toast.LENGTH_SHORT).show();
            }
        });



    }




    // fixme: Start 버튼 클릭 시 데이터 송/수신.
    class StartThread extends Thread{

        int bytes;
        String Dtmp;
        int dlen;

        public StartThread(){

            datatext = findViewById(R.id.recvByte);
            byText = findViewById(R.id.ByteText);
        }


        public String byteArrayToHex(byte[] a) {
            StringBuilder sb = new StringBuilder();
            for(final byte b: a)
                sb.append(String.format("%02x ", b&0xff));
            return sb.toString();
        }

        public void run(){

            // 데이터 송신
            try {

                String OutData = "AT+START\n";
                byte[] data = OutData.getBytes();
                OutputStream output = socket.getOutputStream();
                output.write(data);
                Log.d(TAG, "AT+START\\n COMMAND 송신");

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG,"데이터 송신 오류");
            }

            // 데이터 수신
            try {
                Log.d(TAG, "데이터 수신 준비");

                //TODO:수신 데이터(프로토콜) 처리

                while (true) {
                    byte[] buffer = new byte[1024];

                    InputStream input = socket.getInputStream();

                    bytes = input.read(buffer);
                    Log.d(TAG, "byte = " + bytes);

                    //바이트 헥사(String)로 바꿔서 Dtmp String에 저장.
                    Dtmp = byteArrayToHex(buffer);
                    Dtmp = Dtmp.substring(0,bytes*3);
                    Log.d(TAG, Dtmp);


                    //프로토콜 나누기
                    String[] DSplit = Dtmp.split("a5 5a"); // sync(2byte) 0xA5, 0x5A
                    Dtmp = "";
                    for(int i=1;i<DSplit.length-1;i++){ // 제일 처음과 끝은 잘림. 데이터 버린다.
                        Dtmp = Dtmp + DSplit[i] + "\n";
                    }
                    dlen =  DSplit.length- 2;


                    runOnUiThread(new Runnable() {
                        public void run() {
                            datatext.setText(Dtmp);
                            byText.setText("데이터 " + dlen + "개");
                        }
                    });

                }
            }catch(IOException e){
                e.printStackTrace();
                Log.e(TAG,"수신 에러");
            }


        }

    }

    // fixme: Stop 버튼 클릭 시 데이터 송신.
    class StopThread extends Thread{


        public StopThread(){
        }

        public void run(){

            // 데이터 송신
            try {

                String OutData = "AT+STOP\n";
                byte[] data = OutData.getBytes();
                OutputStream output = socket.getOutputStream();
                output.write(data);
                Log.d(TAG, "AT+STOP\\n COMMAND 송신");

            } catch (IOException e) {
                e.printStackTrace();
            }



        }

    }
    // fixme: Socket Connect.
    class ConnectThread extends Thread {
        String hostname;

        public ConnectThread(String addr) {
            hostname = addr;
        }

        public void run() {
            try { //클라이언트 소켓 생성

                int port = 35000;
                socket = new Socket(hostname, port);
                Log.d(TAG, "Socket 생성, 연결.");

                Toptext = findViewById(R.id.text1);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        InetAddress addr = socket.getInetAddress();
                        String tmp = addr.getHostAddress();
                        Toptext.setText(tmp + " 연결 완료");
                        Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();

                        DiconButton.setEnabled(true);
                        ConnButton.setEnabled(false);
                        StartButton.setEnabled(true);
                    }
                });




            } catch (UnknownHostException uhe) { // 소켓 생성 시 전달되는 호스트(www.unknown-host.com)의 IP를 식별할 수 없음.

                Log.e(TAG, " 생성 Error : 호스트의 IP 주소를 식별할 수 없음.(잘못된 주소 값 또는 호스트 이름 사용)");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error : 호스트의 IP 주소를 식별할 수 없음.(잘못된 주소 값 또는 호스트 이름 사용)", Toast.LENGTH_SHORT).show();
                        Toptext.setText("Error : 호스트의 IP 주소를 식별할 수 없음.(잘못된 주소 값 또는 호스트 이름 사용)");
                    }
                });

            } catch (IOException ioe) { // 소켓 생성 과정에서 I/O 에러 발생.

                Log.e(TAG, " 생성 Error : 네트워크 응답 없음");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error : 네트워크 응답 없음", Toast.LENGTH_SHORT).show();
                        Toptext.setText("네트워크 연결 오류");
                    }
                });


            } catch (SecurityException se) { // security manager에서 허용되지 않은 기능 수행.

                Log.e(TAG, " 생성 Error : 보안(Security) 위반에 대해 보안 관리자(Security Manager)에 의해 발생. (프록시(proxy) 접속 거부, 허용되지 않은 함수 호출)");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error : 보안(Security) 위반에 대해 보안 관리자(Security Manager)에 의해 발생. (프록시(proxy) 접속 거부, 허용되지 않은 함수 호출)", Toast.LENGTH_SHORT).show();
                        Toptext.setText("Error : 보안(Security) 위반에 대해 보안 관리자(Security Manager)에 의해 발생. (프록시(proxy) 접속 거부, 허용되지 않은 함수 호출)");
                    }
                });


            } catch (IllegalArgumentException le) { // 소켓 생성 시 전달되는 포트 번호(65536)이 허용 범위(0~65535)를 벗어남.

                Log.e(TAG, " 생성 Error : 메서드에 잘못된 파라미터가 전달되는 경우 발생.(0~65535 범위 밖의 포트 번호 사용, null 프록시(proxy) 전달)");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), " Error : 메서드에 잘못된 파라미터가 전달되는 경우 발생.(0~65535 범위 밖의 포트 번호 사용, null 프록시(proxy) 전달)", Toast.LENGTH_SHORT).show();
                        Toptext.setText("Error : 메서드에 잘못된 파라미터가 전달되는 경우 발생.(0~65535 범위 밖의 포트 번호 사용, null 프록시(proxy) 전달)");
                    }
                });


            }




        }
    }




    @Override
    protected void onStop() {  //앱 종료시
        super.onStop();
        try {
            socket.close(); //소켓을 닫는다.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

