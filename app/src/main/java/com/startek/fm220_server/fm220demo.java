package com.startek.fm220_server;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.orhanobut.logger.Logger;
import com.startek.fingerprint.library.FP;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Arrays;


import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static android.content.ContentValues.TAG;
import static java.lang.Integer.parseInt;

///
public class fm220demo extends Activity {

    final int U_LEFT = -41;
    final int U_RIGHT = -42;
    final int U_UP = -43;
    final int U_DOWN = -44;
    final int U_POSITION_CHECK_MASK = 0x00002F00;
    final int U_POSITION_NO_FP = 0x00002000;
    final int U_POSITION_TOO_LOW = 0x00000100;
    final int U_POSITION_TOO_TOP = 0x00000200;
    final int U_POSITION_TOO_RIGHT = 0x00000400;
    final int U_POSITION_TOO_LEFT = 0x00000800;
    final int U_POSITION_TOO_LOW_RIGHT = (U_POSITION_TOO_LOW | U_POSITION_TOO_RIGHT);
    final int U_POSITION_TOO_LOW_LEFT = (U_POSITION_TOO_LOW | U_POSITION_TOO_LEFT);
    final int U_POSITION_TOO_TOP_RIGHT = (U_POSITION_TOO_TOP | U_POSITION_TOO_RIGHT);
    final int U_POSITION_TOO_TOP_LEFT = (U_POSITION_TOO_TOP | U_POSITION_TOO_LEFT);

    final int U_POSITION_OK = 0x00000000;

    final int U_DENSITY_CHECK_MASK = 0x000000E0;
    final int U_DENSITY_TOO_DARK = 0x00000020;
    final int U_DENSITY_TOO_LIGHT = 0x00000040;
    final int U_DENSITY_LITTLE_LIGHT = 0x00000060;
    final int U_DENSITY_AMBIGUOUS = 0x00000080;

    final int U_INSUFFICIENT_FP = -31;
    final int U_NOT_YET = -32;

    final int U_CLASS_A = 65;
    final int U_CLASS_B = 66;
    final int U_CLASS_C = 67;
    final int U_CLASS_D = 68;
    final int U_CLASS_E = 69;
    final int U_CLASS_R = 82;

    /**
     * Called when the activity is first created.
     */
    private TextView theMessage;
    private Button buttonConnect;
    private Button buttonCapture;
    private Button buttonEnroll;
    private Button buttonVerify;
    private Button buttonShow;
    private Button buttonDisC;
    private int connectrtn;
    private int rtn;
    private int rtn2;
    private ImageView myImage;
    ////
    private Button buttonIdentify;
    private EditText UserID;
    private EditText FPID;
    private EditText IP_Text;
    private EditText PORT_Text;
    ////
    //byte[] bMapArray = new byte[1078 + (640 * 480)];
    byte[] bMapArray = new byte[1078 + (640 * 480)];
    byte[] bISOImgArray = new byte[32 + 14  + (264 * 324)];
    private byte[] minu_code1 = new byte[512];
    private byte[] minu_code2 = new byte[512];

    byte[] srno= new byte[16];
    byte[] pak = new byte[16];
    byte[] fwver = new byte[16];
    byte[] Key2= new byte[16];
    byte[] newKey= new byte[16];

    private EventHandler m_eventHandler;
    private Bitmap bMap;

    private int counter = 0;

    private static Context Context;

    //public static final int UPDATE_TEXT_VIEW=0x0001;
////////holing add for usb host
    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";
    private UsbManager manager;
    private PendingIntent mPermissionIntent;
    private UsbDevice d;
    private UsbDeviceConnection conn;
    private UsbInterface usbIf;
    UsbEndpoint epIN;
    UsbEndpoint epOUT;
    UsbEndpoint ep2IN;

    ////////////////
    private Button buttonDelete;
    private byte[] piv = new byte[16];
    private byte[] eskey = new byte[256];
   // private byte[] EncryptedMinutiae = new byte[512];
   public static Boolean UI_HTTPS_Enable = true;
    public  static String UI_Srv_IP = "192.168.1.76";
    public  static String UI_Srv_Port = "8444";  //8444 for https,
    public String UI_UserID;
    public String UI_FPID;
    public String UI_Score;
    public String UI_message;
    public int UI_Code;
    public String UI_String;
    /////////////////
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            //call method to set up device communication
                            theMessage.setText("Device is found and try to connect");
                            connectreader();
                        }
                    } else {
                        //      Log.d(TAG, "permission denied for device " + device);
                        theMessage.setText("Device is found");
                    }
                }
            }
        }
    };

    private void connectreader() {
        // TODO Auto-generated method stub

        usbIf = d.getInterface(0);
        Log.d("Device", "Interface:-" + String.valueOf(usbIf.getEndpointCount()));
        Log.d("Device", "Interface Count: " + Integer.toString(d.getInterfaceCount()));

        Log.d("USB", String.valueOf(usbIf.getEndpointCount()));

        //    final UsbEndpoint  usbEndpoint = usbInterface.getEndpoint(0);

        epIN = null;
        epOUT = null;
        ep2IN = null;

        theMessage.setText("num of ep" + usbIf.getEndpointCount());

        epOUT = usbIf.getEndpoint(0);
        epIN = usbIf.getEndpoint(1);
        ep2IN = usbIf.getEndpoint(2);

        //	 theMessage.setText("ep num "+ ep2IN.getEndpointNumber()+"packet size "+ ep2IN.getMaxPacketSize()+"dir "+ep2IN.getDirection());
        //	 theMessage.setText("ep num "+ epIN.getEndpointNumber()+"packet size "+ epIN.getMaxPacketSize()+"dir "+epIN.getDirection());
        theMessage.setText("ep num " + epOUT.getEndpointNumber() + "packet size " + epOUT.getMaxPacketSize() + "dir " + epOUT.getDirection());

        //	 theMessage.setText("manager.hasPermission()");
        if (manager.hasPermission(d) == false) {
            //    	 theMessage.setText("manager.hasPermission() false");
            return;

        }

        conn = manager.openDevice(d);

        if (conn.getFileDescriptor() == -1) {
            Log.d("Device", "Fails to open DeviceConnection");
        } else {

            Log.d("Device", "Opened DeviceConnection" + Integer.toString(conn.getFileDescriptor()));
        }

        if (conn.releaseInterface(usbIf)) {
            Log.d("USB", "Released OK");
        } else {
            Log.d("USB", "Released fails");
        }

        if (conn.claimInterface(usbIf, true)) {
            Log.d("USB", "Claim OK");
        } else {
            Log.d("USB", "Claim fails");
        }
        //     theMessage.setText("EEPROM_read");
        //     byte [] buf= new byte [48];
        //     eeprom_read(0,48,buf);
        theMessage.setText("Device fileDesc" + conn.getFileDescriptor());

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Context = getApplicationContext();
        //SetLibraryPath(Context.getFilesDir().getPath());
        FP.SetFPLibraryPath("/data/data/com.startek.fm220_server/lib/");
        FP.InitialSDK();

        theMessage = (TextView) findViewById(R.id.message);

        theMessage.setText("STARTEK FM220 Android7 SDK build 20180622");

        buttonConnect = (Button) findViewById(R.id.connectB);
        buttonCapture = (Button) findViewById(R.id.captureB);
        buttonEnroll = (Button) findViewById(R.id.enrollB);
        buttonVerify = (Button) findViewById(R.id.verifyB);
        //buttonShow = (Button) findViewById(R.id.showB);
        buttonDisC = (Button) findViewById(R.id.discB);
        myImage = (ImageView) findViewById(R.id.test_image);
        ///
        buttonIdentify = (Button) findViewById(R.id.IdentifyB);
        buttonDelete = (Button) findViewById(R.id.deleteB);
        UserID= (EditText)findViewById(R.id.UserID);
        FPID= (EditText)findViewById(R.id.FPID);
        PORT_Text=(EditText)findViewById(R.id.Port_Text);
        IP_Text = (EditText)findViewById(R.id.IP_Text);

        ///
        //holing reserve for android.hardware.usb test
        //UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        manager = (UsbManager) getSystemService(Context.USB_SERVICE);

        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);


        // check for existing devices
        //PendingIntent mPermissionIntent;
        for (UsbDevice mdevice : manager.getDeviceList().values()) {

            int pid, vid;

            pid = mdevice.getProductId();
            vid = mdevice.getVendorId();

            if (((pid == 0x8220) && (vid == 0x0bca)) || ((pid == 0x8225) && (vid == 0x0bca)) || ((pid == 0x8226) && (vid == 0x0bca))) {
                theMessage.setText("Device PID is found");
                d = mdevice;

                manager.requestPermission(d, mPermissionIntent);

                break;

            }

        }
        /////ori connect here

        //Connect
        buttonConnect.setOnClickListener(new Button.OnClickListener() {
            @Override

            public void onClick(View v) {
              UI_String = IP_Text.getText().toString();

                if(UI_String.equals(""))
                {
                    IP_Text.setText(UI_Srv_IP.toString());
                    Log.d("IVAN", "IIIIIIIIIII");


                }else{
                    UI_Srv_IP = IP_Text.getText().toString();
                    Log.d("IVAN", "gggggggg"+UI_Srv_IP+"ffff");

                }
                UI_String = PORT_Text.getText().toString();
                if(UI_String.equals(""))
                {
                    PORT_Text.setText(UI_Srv_Port.toString());
                    Log.d("IVAN", "pppppppppppp");

                }else{
                    UI_Srv_Port = PORT_Text.getText().toString();
                    Log.d("IVAN", "hhhhhhhh"+UI_Srv_Port+"ffff");
                }
                PORT_Text.postInvalidate();
                IP_Text.postInvalidate();
                Log.d("IVAN", "IP"+UI_Srv_IP+"PORT"+UI_Srv_Port);
                //if(true)
                //return;
                Log.v("Device", "Marcus: Click");
                try {

                    if (conn.getFileDescriptor() == -1) {
                        connectreader();
                        theMessage.setText("try connect without file descripter" + conn.getFileDescriptor());
                        connectrtn = FP.ConnectCaptureDriver(conn, d);
                        Log.d("Device", "Fails to open DeviceConnection");
                    } else {
                        theMessage.setText("try connect with file descripter" + conn.getFileDescriptor());
                        connectrtn = FP.ConnectCaptureDriver(conn, d);
                        Log.d("Device", "Opened DeviceConnection" + Integer.toString(conn.getFileDescriptor()));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


                FP.GetSerialNumber(srno);
                String strSN = new String(srno);
                FP.GetPreAllocatedKey(pak);
                String strPAK = new String(pak);
                FP.GetFWVer(fwver);
                String strFWVer = new String(fwver);
                theMessage.setText("sn: " + strSN + " pak: " + strPAK +" fw ver: " + strFWVer);
            }
        });

        //Capture
        buttonCapture.setOnClickListener(new Button.OnClickListener() {
            @Override

            public void onClick(View v) {

                // Log.v("Device", "Marcus: Click");
                try {

                    if (connectrtn == 0) {
                        m_eventHandler = new EventHandler(Looper.getMainLooper());
//					CaptureThread m_captureThread = new CaptureThread(m_eventHandler);
//					Thread m_capture = new Thread(m_captureThread);
//					m_capture.start();
                        buttonCapture.setEnabled(false);

                        new Thread() {
                            public void run() {
                                super.run();

                                Message msg0 = new Message();
                                msg0.what = PublicData.TEXTVIEW_CAPTURE_PLEASE_PRESS;
                                m_eventHandler.sendMessage(msg0);
								Log.v("Device", "Press your finger");
                                counter++;
                                if ((counter % 15) == 0) {
                                    Log.v("Device", "Start GC");
                                    System.gc();
                                }

                                Log.v("Device", "Marcus: run");
                                // InitialSDK();
                                // Log.v("FP Device", "Marcus: InitialSDK() OK");
                                // PublicData.captureDone=false;
								counter = 0;
                                while ((rtn = FP.Capture()) != 0) {
                                    Message msg2 = new Message();
                                    msg2.what = PublicData.SHOW_PIC;
                                    m_eventHandler.sendMessage(msg2);
                                    Message msg3 = new Message();
                                    msg3.what = PublicData.SHOW_NFIQ;
                                    m_eventHandler.sendMessage(msg3);
                                    if (counter > 50)
                                        break;
                                    counter++;
                                    if (rtn == -2)    //capture fail with abnormal behavior disconnect or device error
                                        break;
                                }
								Log.v("Device", "by Kevin rtn = " + rtn);
								if (rtn == 0)
								{
								    Message msg1 = new Message();
                                    msg1.what = PublicData.TEXTVIEW_SUCCESS;
                                    m_eventHandler.sendMessage(msg1);
                                    Log.v("Device", "Marcus: FP_Capture OK");
                                    // FP.SaveImageBMP("/system/data/fp_image.bmp");
                                    // FP.SaveImageBMP("/data/data/com.startek.fm220_server/fp_image.bmp");
                                    FP.SaveImageBMP("/storage/emulated/0/DCIM/Camera/FP_Capture.bmp");
                                    FP.GetISOImageBuffer((byte)0,(byte)0,bISOImgArray);
                                    Logger.d("ISO img  = " + Arrays.toString(bISOImgArray));

                                    //rtn = FP.GetTemplate(minu_code1);
									//rtn = FP.GetEncryptedTemplate(minu_code1,piv, eskey);
                                    if (rtn == -2)
								    {
								        Message msg2 = new Message();
                                        msg2.what = PublicData.STARTEK_SDK_EXPIRES;
                                        m_eventHandler.sendMessage(msg2);
										return;
								    }
                                    //FP.SaveISOminutia(minu_code1,"/storage/emulated/0/DCIM/Camera/fpcode.ist");
                                    Log.v("Device", "Marcus: FP_SaveImageBMP OK");
							        // try{Thread.sleep(100);}
							        // catch(Exception e){}
                                    Message msg2 = new Message();
                                    msg2.what = PublicData.SHOW_PIC;
                                    m_eventHandler.sendMessage(msg2);
                                    // FP_LedOff();
                                }
								else
								{
								    Message msg1 = new Message();
                                    msg1.what = PublicData.TEXTVIEW_TIMEOUT_CAPTURE_FAIL;
                                    m_eventHandler.sendMessage(msg1);
								}
                            }
                        }.start();
                    } else {
                        theMessage.setText("FP_ConnectCaptureDriver() failed!!");
                        theMessage.postInvalidate();
                        FP.DisconnectCaptureDriver();
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //Enroll ori
        buttonEnroll.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //		led_off();
                if(Check_data_not_empty() == false)
                    return;

                if (connectrtn == 0) {
                    buttonEnroll.setEnabled(false);
                    m_eventHandler = new EventHandler(Looper.getMainLooper());

                    //let thread do main job
                    new Thread() {
                        public void run() {
                            super.run();

                            FP.CreateEnrollHandle();

                            Message msg0 = new Message();
                            msg0.what = PublicData.TEXTVIEW_ENROLL_PLEASE_PRESS;
                            m_eventHandler.sendMessage(msg0);

                            for (int i = 0; i < 6; i++) {
                                //theMessage.setText("Times: "+i);
                                SystemClock.sleep(500);
                                while ((rtn = FP.Capture()) != 0) {
                                    Message msg1 = new Message();
                                    msg1.what = PublicData.TEXTVIEW_PRESS_AGAIN;
                                    m_eventHandler.sendMessage(msg1);
                                    Message msg2 = new Message();
                                    msg2.what = PublicData.SHOW_PIC;
                                    m_eventHandler.sendMessage(msg2);
                                }
								
                               // rtn = FP.GetTemplate(minu_code1);
                                //rtn = FP.GetEncryptedTemplate(minu_code1,piv, eskey);
                                if (rtn == -2)   
								{
								    Message msg2 = new Message();
                                    msg2.what = PublicData.STARTEK_SDK_EXPIRES;
                                    m_eventHandler.sendMessage(msg2);
									return;
								}
                                //rtn = FP.ISOminutiaEnroll(minu_code1, minu_code2);
                                rtn = FP.ISOminutiaEnroll_Encrypted(minu_code1,piv, eskey);

                                while (true) {
                                    rtn2 = FP.CheckBlank();

                                    Message msg2 = new Message();
                                    msg2.what = PublicData.TEXTVIEW_REMOVE_FINGER;
                                    m_eventHandler.sendMessage(msg2);

                                    if (rtn2 != -1)
                                        break;
                                    //theMessage.setText("remove your finger!!!");
                                }

                                if (rtn == U_CLASS_A || rtn == U_CLASS_B) {
                                    //FP.SaveISOminutia(minu_code2, "/system/data/fpcode.dat");
                                    //FP.SaveISOminutia(minu_code2, "/data/data/com.startek.fm220_server/fpcode.dat");
                                    //FP.SaveISOminutia(minu_code2, "/storage/emulated/0/DCIM/Camera/fpcode.dat");
                                    do_Enroll();
                                    SystemClock.sleep(1000);
                                    Message msg3 = new Message();
                                    msg3.what = PublicData.TEXTVIEW_SUCCESS;
                                    m_eventHandler.sendMessage(msg3);

                                    break;
                                } else if (i == 5) {
                                    Message msg4 = new Message();
                                    msg4.what = PublicData.TEXTVIEW_FAILURE;
                                    m_eventHandler.sendMessage(msg4);
                                }
                                //showPic();
                            }

                            FP.DestroyEnrollHandle();
                        }
                    }.start();
                } else {
                    theMessage.setText("FP_ConnectCaptureDriver() failed!!");
                    FP.DisconnectCaptureDriver();
                    return;
                }

            }

        });


        buttonIdentify.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (connectrtn == 0) {
                        m_eventHandler = new EventHandler(Looper.getMainLooper());
                        buttonIdentify.setEnabled(false);
                        UserID.setText("");
                        FPID.setText("");
                        new Thread() {
                            public void run() {
                                Message msg0 = new Message();
                                msg0.what = PublicData.TEXTVIEW_VERIFY_PLEASE_PRESS;
                                m_eventHandler.sendMessage(msg0);
                                Log.v("Device", "Press your finger");
                                counter++;
                                if ((counter % 15) == 0) {
                                    Log.v("Device", "Start GC");
                                    System.gc();
                                }

                                Log.v("Device", "Marcus: run");

                                while ((rtn = FP.Capture()) != 0) {
                                    Message msg2 = new Message();
                                    msg2.what = PublicData.SHOW_PIC;
                                    m_eventHandler.sendMessage(msg2);
                                    //if(counter >20)
                                    //    break;
                                    //counter++;
                                }

                                FP.SaveImageBMP("/storage/emulated/0/DCIM/Camera/fp_Identify_image.bmp");
                                //rtn = FP.GetTemplate(minu_code1);
                                rtn = FP.GetEncryptedTemplate(minu_code1,piv, eskey);

                                if (rtn == -2)
                                {
                                    Message msg2 = new Message();
                                    msg2.what = PublicData.STARTEK_SDK_EXPIRES;
                                    m_eventHandler.sendMessage(msg2);
                                    return;
                                }

                                try {
                                    do_Identify();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                Message msg2 = new Message();
                                msg2.what = PublicData.SHOW_PIC;
                                m_eventHandler.sendMessage(msg2);
                            }
                        }.start();
                    } else {
                        theMessage.setText("FP_ConnectCaptureDriver() failed!!");
                        theMessage.postInvalidate();
                        FP.DisconnectCaptureDriver();
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        buttonVerify.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (connectrtn == 0) {
                        m_eventHandler = new EventHandler(Looper.getMainLooper());
                        buttonVerify.setEnabled(false);
                        UI_UserID = UserID.getText().toString();
                        if(UI_UserID.equals("") ) {
                            theMessage.setText("Please Input UserID ...");
                            buttonVerify.setEnabled(true);
                            return;
                        }
                        //3+FPID.setText("");
                        new Thread() {
                            public void run() {
                                Message msg0 = new Message();
                                msg0.what = PublicData.TEXTVIEW_VERIFY_PLEASE_PRESS;
                                m_eventHandler.sendMessage(msg0);
                                Log.v("Device", "Press your finger");
                                counter++;
                                if ((counter % 15) == 0) {
                                    Log.v("Device", "Start GC");
                                    System.gc();
                                }

                                Log.v("Device", "Marcus: run");

                                while ((rtn = FP.Capture()) != 0) {
                                    Message msg2 = new Message();
                                    msg2.what = PublicData.SHOW_PIC;
                                    m_eventHandler.sendMessage(msg2);
                                    //if(counter >20)
                                    //    break;
                                    //counter++;
                                }

                                FP.SaveImageBMP("/storage/emulated/0/DCIM/Camera/fp_Identify_image.bmp");
                                //rtn = FP.GetTemplate(minu_code1);
                                rtn = FP.GetEncryptedTemplate(minu_code1,piv, eskey);
                                if (rtn == -2)
                                {
                                    Message msg2 = new Message();
                                    msg2.what = PublicData.STARTEK_SDK_EXPIRES;
                                    m_eventHandler.sendMessage(msg2);
                                    return;
                                }

                                try {
                                    do_verify();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                Message msg2 = new Message();
                                msg2.what = PublicData.SHOW_PIC;
                                m_eventHandler.sendMessage(msg2);
                            }
                        }.start();
                    } else {
                        theMessage.setText("FP_ConnectCaptureDriver() failed!!");
                        theMessage.postInvalidate();
                        FP.DisconnectCaptureDriver();
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        buttonDelete.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (connectrtn == 0) {
                        m_eventHandler = new EventHandler(Looper.getMainLooper());
                        if(Check_data_not_empty() == false){
                            theMessage.setText("Please Input UserID ...");
                            buttonVerify.setEnabled(true);
                            return;
                        }
                        buttonDelete.setEnabled(false);
                        UI_FPID = FPID.getText().toString();
                        UI_UserID = UserID.getText().toString();

                        //3+FPID.setText("");
                        new Thread() {
                            public void run() {

                            FP.GetDeleteData(UI_UserID.getBytes(), parseInt(UI_FPID), eskey);
                            String str_EncryptedDeleteData = ByteToHexString(eskey);
                            String json_str = BuildJson_Delete(UI_UserID, str_EncryptedDeleteData);
                            String results = Srv_Delete(json_str);
                                Message msg2 = new Message();
                                msg2.what = PublicData.TEXTVIEW_SUCCESS;
                                m_eventHandler.sendMessage(msg2);
                            }
                        }.start();
                    } else {
                        theMessage.setText("FP_ConnectCaptureDriver() failed!!");
                        theMessage.postInvalidate();
                        FP.DisconnectCaptureDriver();
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        /*
        buttonVerify.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (connectrtn == 0) {
                        m_eventHandler = new EventHandler(Looper.getMainLooper());
                        buttonVerify.setEnabled(false);

                        new Thread() {
                            public void run() {
                                super.run();

                                //if((rtn=FP_LoadISOminutia(minu_code2, "/system/data/fpcode.dat"))==0){
                                //if((rtn=FP_LoadISOminutia(minu_code2, "/data/data/com.startek.fm220_server/fpcode.dat"))==0){
                                if ((rtn = FP.LoadISOminutia(minu_code2, "/storage/emulated/0/DCIM/Camera/fpcode.dat")) == 0) {
                                    Message msg1 = new Message();
                                    msg1.what = PublicData.TEXTVIEW_FILE_EXIST;
                                    m_eventHandler.sendMessage(msg1);
                                    if (connectrtn == 0) {
                                    } else {
                                        FP.DisconnectCaptureDriver();
                                        return;
                                    }

                                    counter++;
                                    if ((counter % 15) == 0) {
                                        Log.v("Device", "Start GC");
                                        System.gc();
                                    }

                                    try {
                                        Thread.sleep(1000);
                                    } catch (Exception e) {
                                    }
                                    Message msg0 = new Message();
                                    msg0.what = PublicData.TEXTVIEW_VERIFY_PLEASE_PRESS;
                                    m_eventHandler.sendMessage(msg0);

                                    counter = 0;
                                    while ((rtn = FP.Capture()) != 0) {
                                        Message msg2 = new Message();
                                        msg2.what = PublicData.SHOW_PIC;
                                        m_eventHandler.sendMessage(msg2);
                                        if(counter >20)
                                            break;
                                        counter++;
                                    }

                                    FP.SaveImageBMP("/storage/emulated/0/DCIM/Camera/fp_Verify_image.bmp");
                                    rtn = FP.GetTemplate(minu_code1);
                                    if (rtn == -2)
                                    {
                                        Message msg2 = new Message();
                                        msg2.what = PublicData.STARTEK_SDK_EXPIRES;
                                        m_eventHandler.sendMessage(msg2);
                                        return;
                                    }
                                    rtn = FP.ISOminutiaMatch360Ex(minu_code1, minu_code2);

                                    if (rtn >= -1) {
                                        Message msg2 = new Message();
                                        msg2 = new Message();
                                        msg2.what = PublicData.TEXTVIEW_SCORE;
                                        m_eventHandler.sendMessage(msg2);

                                        Message msg3 = new Message();
                                        msg3 = new Message();
                                        msg3.what = PublicData.SHOW_PIC;
                                        m_eventHandler.sendMessage(msg3);
                                    }

                                } else {
                                    Message msg4 = new Message();
                                    msg4.what = PublicData.TEXTVIEW_FILE_NOT_EXIST;
                                    m_eventHandler.sendMessage(msg4);
                                    return;
                                }
                            }
                        }.start();
                    } else {
                        theMessage.setText("FP_ConnectCaptureDriver() failed!!");
                        theMessage.postInvalidate();
                        FP.DisconnectCaptureDriver();
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
*/
//        buttonShow.setOnClickListener(new Button.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            }
//        });

        buttonDisC.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                conn.close();
                FP.DisconnectCaptureDriver();
                theMessage.setText("FP_DisconnectCaptureDriver() Succeeded!!");
                theMessage.postInvalidate();

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mUsbReceiver);
    }

    class showPic extends AsyncTask<String, Void, String> {
        //       private ImageView image;
        private Bitmap bMap = null;

        @Override
        protected String doInBackground(String... path) {
            tryGetStream();
            return null;
        }

        protected void onPostExecute(String a) {
            myImage.setImageBitmap(bMap);
            bMap = null;
            System.gc();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
            myImage.postInvalidate();
            Log.v("Device", "Marcus: onProgressUpdate");
        }

        private void tryGetStream() {
            try {
                //buf = FP_GetImageBuffer);
                FP.GetImageBuffer(bMapArray);
                Logger.d(Arrays.toString(Arrays.copyOf(bMapArray, 512)));

                bMap = BitmapFactory.decodeByteArray(bMapArray, 0, bMapArray.length);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PublicData.TEXTVIEW_SUCCESS:
                    buttonCapture.setEnabled(true);
                    buttonEnroll.setEnabled(true);
                    buttonVerify.setEnabled(true);
                    buttonIdentify.setEnabled(true);

                    UserID.setText(UI_UserID);
                    FPID.setText(UI_FPID);
                    UserID.setTextColor(Color.RED);
                    FPID.setTextColor(Color.RED);
                    Log.d("IVAN", "msg ID:"+UserID.getText().toString());

                    String viewText= "Done.";

                    theMessage.setText(viewText);
                    theMessage.postInvalidate();
                    break;
                case PublicData.TEXTVIEW_FAILURE:
                    String str="Failure.. " + UI_message;
                    UserID.setText(UI_UserID);
                    FPID.setText(UI_FPID);
                    UserID.setTextColor(Color.RED);
                    FPID.setTextColor(Color.RED);
                    theMessage.setText(str);
                    theMessage.postInvalidate();
                    buttonEnroll.setEnabled(true);
                    break;
                case PublicData.TEXTVIEW_CAPTURE_PLEASE_PRESS:
                    theMessage.setText("Capture: Press your finger");

                    theMessage.postInvalidate();
                    break;
                case PublicData.TEXTVIEW_ENROLL_PLEASE_PRESS:
                    theMessage.setText("Enroll: Press your finger");
                    theMessage.postInvalidate();
                    break;
                case PublicData.TEXTVIEW_VERIFY_PLEASE_PRESS:
                    theMessage.setText("Press your finger");
                    theMessage.postInvalidate();
                    break;
                case PublicData.TEXTVIEW_SCORE:
                    theMessage.setText("matching score=" + (int) FP.Score());
                    theMessage.postInvalidate();
                    break;
                case PublicData.TEXTVIEW_FILE_EXIST:
                    theMessage.setText("Verify: File exist");
                    theMessage.postInvalidate();
                    break;
                case PublicData.TEXTVIEW_FILE_NOT_EXIST:
                    theMessage.setText("File not exist, please enroll first");
                    theMessage.postInvalidate();
                    buttonVerify.setEnabled(true);
                    break;
                case PublicData.TEXTVIEW_REMOVE_FINGER:
                    theMessage.setText("Please remove your finger");
                    theMessage.postInvalidate();
                    //new showPic().execute("/system/data/fp_image.bmp");
                    new showPic().execute("");

                    break;
                case PublicData.TEXTVIEW_PRESS_AGAIN:
                    theMessage.setText("Please press your finger again");
                    theMessage.postInvalidate();
                    //new showPic().execute("/system/data/fp_image.bmp");
                    new showPic().execute("");
                    break;
				case PublicData.TEXTVIEW_TIMEOUT_CAPTURE_FAIL:
                    theMessage.setText("Timeout or capture fail, please press capture button again");
                    theMessage.postInvalidate();
                    //new showPic().execute("/system/data/fp_image.bmp");
                    new showPic().execute("");
                    break;
				case PublicData.STARTEK_SDK_EXPIRES:
                    theMessage.setText("Startek SDK expires now.\nPlease contact Startek Engineering Inc..");
                    theMessage.postInvalidate();
                    //new showPic().execute("/system/data/fp_image.bmp");
                    new showPic().execute("");
                    break;
                case PublicData.SHOW_PIC:
                    //new showPic().execute("/system/data/fp_image.bmp");
                    new showPic().execute("");
                    buttonCapture.setEnabled(true);
                    buttonEnroll.setEnabled(true);
                    buttonVerify.setEnabled(true);
                    buttonIdentify.setEnabled(true);
                    buttonDelete.setEnabled(true);
                    break;
                case PublicData.SHOW_NFIQ:
                    theMessage.setText("nfiq " + FP.GetNFIQ());
                    theMessage.postInvalidate();
                    break;

            }
            super.handleMessage(msg);
        }
    }

    public static String ByteToHexString ( byte buf[] )
    {

        StringBuffer strbuf = new StringBuffer( buf.length * 2 );
        int i;

        for ( i = 0; i < buf.length; i++ )
        {

            if ( ( ( int ) buf[i] & 0xff ) < 0x10 )

                strbuf.append("0");

            strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
        }

        return strbuf.toString();
    }

    public boolean Check_data_not_empty( ) {

        String UI_User_ID = UserID.getText().toString();
        String UI_FP_Index_str = FPID.getText().toString();

        Log.d("IVAN", "ID " + UI_User_ID+"FP"+UI_FP_Index_str);
        if(UI_User_ID.equals("") || UI_FP_Index_str.equals("") ) {
            theMessage.setText("PInput UserID and FPID ...");
            return false;
        }
        return true;
    }

    public void do_Enroll( )
    {

        String UI_User_ID = UserID.getText().toString();
        String UI_FP_Index_str = FPID.getText().toString();
        int UI_Privilege =1;
        int UI_FP_Index;
        Log.d("IVAN", "ID " + UI_User_ID+"FP"+UI_FP_Index_str);
        if(UI_User_ID.equals("") || UI_FP_Index_str.equals("") ) {
            theMessage.setText("Please Input UserID and FPID ...");
            return;
        }

        UI_FP_Index = parseInt(UI_FP_Index_str);
        String str_EncryptedMinutiae = ByteToHexString(minu_code1);
        String str_EncryptedSessionKey = ByteToHexString(eskey);
        String str_piv = ByteToHexString(piv);

        String json_str = BuildJson_Enroll(str_EncryptedMinutiae, str_EncryptedSessionKey, str_piv, UI_User_ID, UI_FP_Index, UI_Privilege);
        String results = Srv_Enroll(json_str);
    }

    public void do_Identify( ) throws JSONException {

        String str_EncryptedMinutiae = ByteToHexString(minu_code1);
        String str_EncryptedSessionKey = ByteToHexString(eskey);
        String str_piv = ByteToHexString(piv);

        String json_str = BuildJson_Identify(str_EncryptedMinutiae, str_EncryptedSessionKey, str_piv);
        String results = Srv_Identify(json_str);


        UI_message = ParseJsonString(results);
        Log.d("IVAN", "Srv_Identify return string " + results+ "score"+ UI_Score);

        if(UI_Code>40000){
        Message msg1 = new Message();
        msg1.what = PublicData.TEXTVIEW_FAILURE;
        m_eventHandler.sendMessage(msg1);
        }else{
            Message msg4 = new Message();
            msg4.what = PublicData.TEXTVIEW_SUCCESS;
            m_eventHandler.sendMessage(msg4);
        }
    }

    public void do_verify( ) throws JSONException {

        String str_EncryptedMinutiae = ByteToHexString(minu_code1);
        String str_EncryptedSessionKey = ByteToHexString(eskey);
        String str_piv = ByteToHexString(piv);

        String json_str = BuildJson_Verify(str_EncryptedMinutiae, str_EncryptedSessionKey, str_piv, UI_UserID, 0, 1);

        String results = Srv_Verify(json_str);


        UI_message = ParseJsonString(results);
        Log.d("IVAN", "Srv_Verify   return string " + results+ "score"+ UI_Score);


        if(UI_Code>40000){
            Message msg1 = new Message();
            msg1.what = PublicData.TEXTVIEW_FAILURE;
            m_eventHandler.sendMessage(msg1);
        }else{

            Message msg4 = new Message();
            msg4.what = PublicData.TEXTVIEW_SUCCESS;
            m_eventHandler.sendMessage(msg4);
        }
    }

    public static String BuildJson_Enroll(String encMinutiae, String eSkey, String iv, String id, int fp_idx, int privilege)
    {
        //put together as new serialize json string as server need
        json_srv_enroll json_to_srv = new json_srv_enroll();

        //using (var ms = new MemoryStream())
        {

            //assign one json to another json
            json_to_srv.encMinutiae = encMinutiae;
            json_to_srv.eSkey = eSkey;
            json_to_srv.iv = iv;
            json_to_srv.clientUserId = id;
            json_to_srv.fpIndex = fp_idx;
            json_to_srv.privilege = privilege;
        }

        Gson gson = new Gson();
        String ret = gson.toJson(json_to_srv);
        Log.d("IVAN", "BuildJson_Enroll res_str " + ret);
        return ret;
    }

    public static class json_srv_enroll {
        @SerializedName("encMinutiae")
        private String encMinutiae;
        public String get_encMinutiae() { return encMinutiae;}
        public void set_encMinutiae(String data) {this.encMinutiae = data;}

        @SerializedName("eSkey")
        private String eSkey;
        public String get_eSkey() { return eSkey; }
        public void set_eSkey(String data) { this.eSkey = data; }

        @SerializedName("iv")
        private String iv;
        public String get_iv() { return iv; }
        public void set_iv(String data) { this.iv = data; }

        @SerializedName("clientUserId")
        private String clientUserId;
        public String get_clientUserId() { return clientUserId; }
        public void set_clientUserId(String data) { this.clientUserId = data; }

        @SerializedName("fpIndex")
        private int fpIndex;
        public int get_fpIndex() { return fpIndex; }
        public void set_fpIndex(int data) { this.fpIndex = data; }

        @SerializedName("privilege")
        private int privilege;
        public int get_privilege() { return privilege; }
        public void set_privilege(int data) { this.privilege = data; }
    }

    public static String BuildJson_Identify(String encMinutiae, String eSkey, String iv)
    {
        //put together as new serialize json string as server need
        json_srv_identify json_to_srv = new json_srv_identify();
        //String ret_str;
        //using (var ms = new MemoryStream())
        {
            //assign one json to another json
            json_to_srv.encMinutiae = encMinutiae;
            json_to_srv.eSkey = eSkey;
            json_to_srv.iv = iv;

        }
        Gson gson = new Gson();
        String ret = gson.toJson(json_to_srv);

        return ret;
    }

    public static class json_srv_identify {
        @SerializedName("encMinutiae")
        private String encMinutiae;
        public String get_encMinutiae() { return encMinutiae;}
        public void set_encMinutiae(String data) {this.encMinutiae = data;}

        @SerializedName("eSkey")
        private String eSkey;
        public String get_eSkey() { return eSkey; }
        public void set_eSkey(String data) { this.eSkey = data; }

        @SerializedName("iv")
        private String iv;
        public String get_iv() { return iv; }
        public void set_iv(String data_in) { this.iv = data_in; }
    }

    public static class json_srv_delete {
        @SerializedName("clientUserId")
        private String clientUserId;
        public String get_clientUserId() { return clientUserId;}
        public void set_clientUserId(String data) {this.clientUserId = data;}

        @SerializedName("deleteData")
        private String deleteData;
        public String get_deleteData() { return deleteData; }
        public void set_deleteData(String data) { this.deleteData = data; }
    }

    private static String Srv_Identify(String json_string)
    {
        Boolean https_en = UI_HTTPS_Enable;
        String ip = UI_Srv_IP;
        String port = UI_Srv_Port;
        String route = "/redirect/identify";
        Boolean ignore_https_ca = true;


        String ret_str = PostJson2RedirectServer(https_en,ip, port, route, json_string, ignore_https_ca);

        return ret_str;
    }

    public static String BuildJson_Verify(String encMinutiae, String eSkey, String iv, String id, int fp_idx, int privilege)
    {
        return BuildJson_Enroll(encMinutiae, eSkey, iv, id, fp_idx, privilege);
    }
    private static String Srv_Verify(String json_string)
    {
        Boolean https_en = UI_HTTPS_Enable;
        String ip = UI_Srv_IP;
        String port = UI_Srv_Port;
        String route = "/redirect/verify";
        Boolean ignore_https_ca = true;

        String ret_str = PostJson2RedirectServer(https_en,ip, port, route, json_string, ignore_https_ca);

        return ret_str;
    }

    public static String BuildJson_Delete(String clientUserId, String deleteData)
    {
        //put together as new serialize json string as server need
        json_srv_delete json_to_srv = new json_srv_delete();

        {
            //assign one json to another json
            json_to_srv.clientUserId = clientUserId;
            json_to_srv.deleteData = deleteData;
        }
        Gson gson = new Gson();
        String ret = gson.toJson(json_to_srv);

        return ret;
    }

    private static String Srv_Delete(String json_string)
    {
        Boolean https_en = UI_HTTPS_Enable;
        String ip = UI_Srv_IP;
        String port = UI_Srv_Port;
        String route = "/redirect/delete";
        Boolean ignore_https_ca = true;

        String ret_str = PostJson2RedirectServer(https_en,ip, port, route, json_string, ignore_https_ca);

        return ret_str;
    }

    private static String Srv_Enroll(String json_string)
    {
        Boolean https_en = UI_HTTPS_Enable;
        String ip = UI_Srv_IP;
        String port = UI_Srv_Port;
        String route = "/redirect/enroll";
        Boolean ignore_https_ca = true;

        String ret_str = PostJson2RedirectServer(https_en,ip, port, route, json_string, ignore_https_ca);

        return ret_str;
    }

    private static String PostJson2RedirectServer(boolean https_en,String SrvIp, String port, String route, String json_string, Boolean Ignore_CA)
    {
        String protocol = "";
        String ret_str = "";


        if(https_en == true)
        {
            protocol = "https://" + SrvIp + ":" + port;
        }
        else
        {
            protocol = "http://"+ SrvIp + ":" + port;
        }

        //HttpURLConnection connection = null;
        DataOutputStream wr;
        InputStream is;
        try
        {
            URL url = new URL(protocol + route);
            if(https_en == true)
            {
                if(Ignore_CA == true)   //if need to ignore CA (ex. self signed CA for HTTPS)
                {
                    TrustManager[] trustAllCerts = new TrustManager[] {
                            new X509TrustManager() {
                                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                    return new X509Certificate[0];
                                }
                                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                                }
                                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                                }
                            }
                    };

                    SSLContext sc = SSLContext.getInstance("SSL");
                    sc.init(null, trustAllCerts, new java.security.SecureRandom());
                    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

                    HostnameVerifier allHostsValid = new HostnameVerifier()
                    {
                        public boolean verify(String hostname, SSLSession session)
                        {
                            return true;
                        };
                    };

                    // Install the all-trusting host verifier
                    HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

                }


                HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type","application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Content-Length", Integer.toString(json_string.length())); //?
                connection.setRequestProperty("User-agent","myapp");
                connection.setConnectTimeout(120000);
                connection.setReadTimeout(120000);
                connection.setUseCaches(false);
                connection.setDoOutput(true);
                connection.setDoInput(true);

                //Write out
                wr = new DataOutputStream (connection.getOutputStream ());
                wr.writeBytes (json_string);
                wr.flush ();
                wr.close ();
                is = connection.getInputStream();
            }
            else
            {
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type","application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Content-Length", Integer.toString(json_string.length())); //?
                connection.setRequestProperty("User-agent","myapp");
                connection.setConnectTimeout(120000);
                connection.setReadTimeout(120000);
                connection.setUseCaches(false);
                connection.setDoOutput(true);
                connection.setDoInput(true);
                //Write out
                wr = new DataOutputStream (connection.getOutputStream ());
                wr.writeBytes (json_string);
                wr.flush ();
                wr.close ();
                is = connection.getInputStream();
            }
            /*
            wr.writeBytes (json_string);
            wr.flush ();
            wr.close ();

            */
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while((line = rd.readLine()) != null)
            {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            ret_str = response.toString();
            //return response.toString();


        }
        catch(SocketTimeoutException e)
        {
            System.out.println("TIMEOUT: " + e);
        }
        catch(Exception e)
        {
            System.out.println("ERROR: " + e);
        }
        finally
        {
            //if(connection != null)
            {
                //connection.disconnect();

            }
        }
        Log.d("IVAN", "res_str " + ret_str);
        return ret_str;
    }

    public String ParseJsonString (String message) throws JSONException {

        
        JSONObject json = new JSONObject(message);
        int code = json.getInt("code");
        String userID;
        String fpIndex;
        String score;
        String msg = json.getString("message");
        Log.d("IVAN","code  "+code);

       // UI_UserID="0";
        UI_FPID="0";
        UI_Code = code;
        switch(code)
        {
            case 20003:
            case 20004:
                JSONObject jsonObj = json.getJSONObject("data");
                if(code == 20004){
                  userID = jsonObj.getString("clientUserId");
                  UI_UserID = userID;
                }
                fpIndex = jsonObj.getString("fpIndex");
                score = jsonObj.getString("score");
                UI_Score = score;
                if(parseInt(score) <2000)
                    UI_UserID = "NO User";

                UI_FPID = fpIndex;

                Log.d(TAG,"code  "+code);
                Log.d(TAG,"score  "+UI_Score);
                Log.d(TAG,"fpIndex  "+UI_FPID);
                Log.d(TAG,"userID "+UI_UserID);
                break;




        }
        if(code >40000) {
            UI_FPID="0";
            UI_UserID="0";
            Message msg4 = new Message();
            msg4.what = PublicData.TEXTVIEW_FAILURE;
            m_eventHandler.sendMessage(msg4);
        }
        return msg;
    }

}
