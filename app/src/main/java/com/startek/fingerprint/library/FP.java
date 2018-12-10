package com.startek.fingerprint.library;

import android.annotation.TargetApi;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.os.Build;
import android.os.Environment;
import android.text.format.DateFormat;

import com.orhanobut.logger.Logger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import android.util.Log;

public class FP {
    private static UsbModule usbModule;
    private static FPSensorPara fpSensorPara;
    public static byte[] bMapArray= new byte[1078+(264*324)];

    public static void SetFPLibraryPath(String filepath) {
        // TODO o FPNative.SetFPLibraryPath(filepath);
    }

    public static void InitialSDK() {
        // do nothing in jni
        NativeApi.init();
    }

    public static int ConnectCaptureDriver(UsbDeviceConnection connection, UsbDevice device) {
        if (usbModule == null) {
            usbModule = new UsbModule();
        }
        if (fpSensorPara == null) {
            fpSensorPara = new FPSensorPara();
        }
        usbModule.connect(connection, device);
        NativeApi.setFileDescriptor(usbModule.fileDescriptor);
        byte[] data=usbModule.eepromRead();
        NativeApi.setEEPROM(data);
        fpSensorPara.setParaFromEEPROM(data);
        usbModule.setSensorReg(fpSensorPara);
        Log.d("FP","AEC "+fpSensorPara.m_AEC + "AGC " + fpSensorPara.m_AGC);

        //setlowspeed here
        //usbModule.lowSpeed(fpSensorPara);

        return 0;
    }

    public static void DisconnectCaptureDriver() {
        if (usbModule != null) {
            usbModule.disconnect();
            usbModule = null;
        }
    }

    public static int Capture() {
        int rtn;
        if (usbModule != null) {
            rtn=usbModule.snap();
            NativeApi.getImageBuffer(bMapArray);
            FPNative.FP_UpdateImgBufBMP(bMapArray);
            return rtn;
        }
        return -1;
    }

    public static int CheckBlank() {
        int rtn;
        if (usbModule != null) {
            rtn=usbModule.snap();
            NativeApi.getImageBuffer(bMapArray);
            FPNative.FP_UpdateImgBufBMP(bMapArray);
            return rtn;
        }
       return -1;
     /* if (usbModule != null) {
            usbModule.snap();
        }
        return NativeApi.checkBlank();*/

    }

    public static void SaveImageBMP(String filepath) {
        // TODO o FPNative.FP_SaveImageBMP(filepath);
        // FPNative.FP_SaveImageBMP(filepath);
		NativeApi.SaveImageBMP(filepath);
    }

    public static int CreateEnrollHandle() {
        // TODO FPNative.FP_CreateEnrollHandle();
        return FPNative.FP_CreateEnrollHandle();
    }

    public static int GetTemplate(byte[] m1) {
        // TODO FPNative.FP_GetTemplate(m1);
        return FPNative.FP_GetTemplate(m1);
    }

    public static int GetEncryptedTemplate(byte[] m1, byte[] piv, byte[] eskey) {
        // TODO FPNative.FP_GetEncryptedTemplate(m1);
        return FPNative.FP_GetEncryptedTemplate(m1, piv, eskey);
    }

    public static int ISOminutiaEnroll(byte[] m1, byte[] m2) {
        // TODO FPNative.ISOminutiaEnroll(m1, m2);
        return FPNative.FP_ISOminutiaEnroll(m1, m2);
    }

    public static int ISOminutiaEnroll_Encrypted(byte[] m1, byte[] piv, byte[] eskey) {
        // TODO FPNative.ISOminutiaEnroll_Encrypted(m1, m2);
        return FPNative.FP_ISOminutiaEnroll_Encrypted( m1,  piv,  eskey);
    } //FP_ISOminutiaEnroll_Encrypted

    public static void SaveISOminutia(byte[] m2, String filepath) {
        // TODO FPNative.FP_SaveISOminutia(m2, filepath);
        FPNative.FP_SaveISOminutia(m2, filepath);
    }

    public static void DestroyEnrollHandle() {
        // TODO FPNative.FP_DestroyEnrollHandle();
        FPNative.FP_DestroyEnrollHandle();
    }

    public static int LoadISOminutia(byte[] m2, String filepath) {
        // TODO FPNative.FP_LoadISOminutia(m2, filepath);
        return FPNative.FP_LoadISOminutia(m2, filepath);
    }

    public static int ISOminutiaMatchEx(byte[] m1, byte[] m2) {
        // TODO FPNative.FP_ISOminutiaMatchEx(m1, m2);
        return FPNative.FP_ISOminutiaMatchEx(m1, m2);
    }

    public static int ISOminutiaMatch180Ex(byte[] m1, byte[] m2) {
        // TODO FPNative.FP_ISOminutiaMatch180Ex(m1, m2);
        return FPNative.FP_ISOminutiaMatch180Ex(m1, m2);
    }

    public static int ISOminutiaMatch360Ex(byte[] m1, byte[] m2) {
        // TODO FPNative.FP_ISOminutiaMatch360Ex(m1, m2);
        return FPNative.FP_ISOminutiaMatch360Ex(m1, m2);
    }

    public static int Score() {
        // TODO FPNative.Score();
        return FPNative.Score();
    }

    public static void GetImageBuffer(byte[] bmpBuffer) {
        NativeApi.getImageBuffer(bmpBuffer);

//        System.arraycopy(data, 0, bmpBuffer, 0, data.length - 1);

//        byte[] imageData = NativeApi.receiveImage();
//        System.arraycopy(imageData, 0, bmpBuffer, 0, imageData.length - 1);
    }

    public static void GetISOImageBuffer(byte ImgCompAlgo, byte FpPos, byte[] isoImgBuf) {
        FPNative.FP_GetISOImageBuffer(ImgCompAlgo, FpPos, isoImgBuf);

    }

    public static void GetDeleteData(byte[] user,int index,byte[] del_data) {
        FPNative.FP_GetDeleteData( user, index,del_data);

    }

    public static int GetImageWidth() {
        return NativeApi.getImageWidth();
    }

    public static int GetImageHeight() {
        return NativeApi.getImageHeight();
    }

    public static int LedOn() {
        if (usbModule != null) {
            return usbModule.ledOn();
        }
        return -1;
    }

    public static int LedOff() {
        if (usbModule != null) {
            return usbModule.ledOff();
        }
        return -1;
    }

    public static int GetNFIQ() {
        return FPNative.FP_GetNFIQ();
    }

    // ----

    public static int GetResolution() {
        return 500;
    }

    public static int GetGrayLevel() {
        return 256;
    }

    public static float GetImageH() {
        return 15.44f;
    }

    public static float GetImageV() {
        return 17.475f;
    }

    public static String GetCompAlgo() {
        return "WSQ";
    }

    public static String GetCompRatio() {
        return "10:1";
    }

    public static String GetEncodeType() {
        return "WSQ Encodding";
    }

    public static String GetCertification() {
        return "PIV";
    }

    public static int ConnectCaptureDriver2(UsbDeviceConnection connection, UsbDevice device) {
        return ConnectCaptureDriver(connection, device);
    }

    //add get serial number
    public static int GetSerialNumber(byte[] sn) {
        if (usbModule != null) {
            byte[] snData = usbModule.eepromReadLen(48, 9);
            //byte[] snData =usbModule.eepromRead();
            System.arraycopy(snData, 0, sn, 0, snData.length - 1);
            return 0;
        }
        return -1;
    }

    //add get pre-allocated key
    public static int GetPreAllocatedKey(byte[] pak) {
        if (usbModule != null) {
            byte[] pakData = usbModule.eepromReadLen(64, 8);
            System.arraycopy(pakData, 0, pak, 0, 8);
            return 0;
        }
        return -1;
    }

    public static int SetSerialNumber(byte[] sn) {
        int r;
        if (usbModule != null) {
            r = usbModule.eepromWriteLen(48,9,sn);
            return r;
        }
        return -1;
    }

    public static int SetPreAllocatedKey(byte[] pak) {
        int r;
        if (usbModule != null) {
            r = usbModule.eepromWriteLen(64,8,pak);
            return r;
        }
        return -1;
    }

    //add get fw version
    public static int GetFWVer(byte[] fwver) {

        byte[] fwkData = usbModule.getfwVer();
        System.arraycopy(fwkData, 0, fwver, 0, 9);
        return 0;
    }

    // ----
/*
    private static void byteToFile(byte[] data) {

        try {
            long current = System.currentTimeMillis();

            File logFile = new File(Environment.getExternalStorageDirectory(),
                    FP.class.getSimpleName() + "-" +
                            DateFormat.format("yyyy-MM-dd", current) + ".log");
            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(logFile))) {
                out.write(data);

            } catch (Exception e) {
                Logger.e(e.getMessage(), e);
            }
        } catch (Exception e) {
            Logger.e(e.getMessage(), e);
        }
    }
*/
    public static class UsbModule {
        private UsbDeviceConnection connection;
        private int fileDescriptor;
        private UsbEndpoint endpointIn1;
        private UsbEndpoint endpointOut1;
        private UsbEndpoint endpointIn2;
        private UsbEndpoint endpointOut2;

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private void connect(UsbDeviceConnection connection, UsbDevice device) {
            this.connection = connection;
            this.fileDescriptor = connection.getFileDescriptor();

            Logger.d("fileDescriptor = " + fileDescriptor);

            UsbInterface usbInterface = device.getInterface(0);

            connection.claimInterface(usbInterface, true);
            //connection.setInterface(usbInterface);
            NativeApi.setInterface(connection.getFileDescriptor());

            for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
                UsbEndpoint endpoint = usbInterface.getEndpoint(i);
                Logger.d("endpoint = " + endpoint.toString());

                Logger.d("endpoint.getEndpointNumber = " + endpoint.getEndpointNumber());
                Logger.d("endpoint.getDirection = " + endpoint.getDirection());

                if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                    if (endpoint.getEndpointNumber() == 1) {
                        this.endpointOut1 = endpoint;
                    } else if (endpoint.getEndpointNumber() == 2) {
                        this.endpointOut2 = endpoint;
                    }
                } else if (endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                    if (endpoint.getEndpointNumber() == 1) {
                        this.endpointIn1 = endpoint;
                    } else if (endpoint.getEndpointNumber() == 2) {
                        this.endpointIn2 = endpoint;
                    }
                }
            }
        }

        private void disconnect() {
            if (this.connection != null) {
                this.connection.close();
                this.connection = null;
            }

            this.endpointIn1 = null;
            this.endpointOut1 = null;
            this.endpointIn2 = null;
            this.endpointOut2 = null;
        }

        private int ledOn() {
            byte[] data = new byte[16];
            data[0] = 0x00;
            data[1] = 0x07;
            data[2] = 0x07;

            int result = this.connection.bulkTransfer(
                    this.endpointOut1, data, 3, 500);

            return result < 0 ? result : 0;
        }

        private int ledOff() {
            byte[] data = new byte[16];
            data[0] = 0x00;
            data[1] = 0x07;
            data[2] = 0x00;

            int result = this.connection.bulkTransfer(
                    this.endpointOut1, data, 3, 500);

            return result < 0 ? result : 0;
        }


        private int snap() {
            this.ledOn();
//            this.lowSpeed();
            this.clearEp2Buffer();
            this.setCifStart();

            int result = NativeApi.capture();

            this.ledOff();
            return result;
        }

        private int setCifStart() {
            byte[] data = new byte[16];
            data[0] = 0x00;
            data[1] = 0x0a;

            int result = this.connection.bulkTransfer(
                    this.endpointOut1, data, 3, 1500);

            Logger.e("data length = " + result);
            Logger.e("data = " + Arrays.toString(data));

            return result < 0 ? result : 0;
        }

        private int clearEp2Buffer() {
            int result = 0;
            byte[] data = new byte[512];

            while (result == 0) {
                result = this.connection.bulkTransfer(
                        this.endpointIn2, data, data.length, 50);
            }

            if (result < 0) {
                Logger.e("clearEp2Buffer result = " + result);

                return result;
            }

            return 0;
        }

        private int regWriteCmd(byte cmd, byte address, byte value) {
            byte[] data = new byte[16];
            int rtn;
            data[0]=cmd;
            data[1]=address;
            data[2]=value;

            rtn = this.connection.bulkTransfer(
                    this.endpointOut1,data,3,500);

            return rtn;
        }

        private void  setSensorReg(FPSensorPara fpSenPara){
            int tmp=0;
            regWriteCmd((byte) 0x5d, (byte) 0x35, (byte) 0x00);
            regWriteCmd((byte) 0x5d, (byte) 0x80, (byte) fpSenPara.m_AGC);
            tmp=fpSenPara.m_AEC*2;
            regWriteCmd((byte) 0x5d, (byte) 0x09, (byte) (tmp/256));
            regWriteCmd((byte) 0x5d, (byte) 0x80, (byte) (tmp%256));

        }

        private void lowSpeed(FPSensorPara fpPara) {
            byte[] data = new byte[16];
            data[0] = 0x60;
            data[1] = 0x04;

            // low speed
            int lowSpeed = this.connection.bulkTransfer(
                    this.endpointOut1, data, 2, 500);

            Logger.e("lowSpeed = " + lowSpeed);
            Logger.e("data = " + Arrays.toString(data));
            // ----
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //setSensorReg(fpPara);
            int tmp=0;

            regWriteCmd((byte) 0x5d, (byte) 0x35, (byte) 0x00);
            regWriteCmd((byte) 0x5d, (byte) 0x80, (byte) fpPara.m_AGC);
            tmp=fpPara.m_AEC*2/6;
            regWriteCmd((byte) 0x5d, (byte) 0x09, (byte) (tmp/256));
            regWriteCmd((byte) 0x5d, (byte) 0x80, (byte) (tmp%256));

/*
            data[0] = 0x5d;
            data[1] = 0x35;
            data[2] = 0x00;

            this.connection.bulkTransfer(
                    this.endpointOut1, data, 0, 3, 500);

            data[0] = 0x5d;
            data[1] = (byte) 0x80;
            data[2] = 0x0a;// data[2] = (byte) 0x1c;

            this.connection.bulkTransfer(
                    this.endpointOut1, data, 0, 3, 500);

            data[0] = 0x5d;
            data[1] = 0x09;
            data[2] = 0x00;

            this.connection.bulkTransfer(
                    this.endpointOut1, data, 0, 3, 500);

            data[0] = 0x5d;
            data[1] = (byte) 0x80;
            data[2] = (byte) 0x30;// data[2] = (byte) 0xb4;

            this.connection.bulkTransfer(
                    this.endpointOut1, data, 0, 3, 500);
*/
        }

        private byte[] getfwVer() {
            byte[] data = new byte[9];
            data[0] = 0x00;
            data[1] = 0x06;

            this.connection.bulkTransfer(this.endpointOut1, data, 2, 500);
            this.connection.bulkTransfer(this.endpointIn1, data, 9, 1000);

            Logger.e("fw_ver = " + Arrays.toString(data));
            return data;
        }

        private static final int EEPROM_START_ADDRESS = (int) 0x1C00;

        private byte[] eepromRead() {
            byte[] buffer = new byte[64];

            for (int i = 0; i < 48; i++) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                byte[] data = new byte[16];
                data[0] = 0x51;
                data[1] = (byte) ((EEPROM_START_ADDRESS + i) >> 8);
                data[2] = (byte) ((EEPROM_START_ADDRESS + i) & 0xff);

                this.connection.bulkTransfer(this.endpointOut1, data, 3, 500);

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                this.connection.bulkTransfer(this.endpointIn1, data, 1, 1000);

                buffer[i] = data[0];
            }

            Logger.e("eepromRead = " + Arrays.toString(buffer));
            return buffer;
        }

        private byte[] eepromReadLen(int offset,int len) {
            byte[] buffer = new byte[len];

            for (int i = 0; i < len; i++) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                byte[] data = new byte[16];
                data[0] = 0x51;
                data[1] = (byte) ((EEPROM_START_ADDRESS + i+offset) >> 8);
                data[2] = (byte) ((EEPROM_START_ADDRESS + i+offset) & 0xff);

                this.connection.bulkTransfer(this.endpointOut1, data, 3, 500);

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                this.connection.bulkTransfer(this.endpointIn1, data, 1, 1000);

                buffer[i] = data[0];
            }

            Logger.e("eepromRead = " + Arrays.toString(buffer));
            return buffer;
        }

        private int eepromWriteLenCMD(byte cmd, int offset,int len, byte[] buf) {
            byte[] buffer = new byte[len];

            for (int i = 0; i < len; i++) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                byte[] data = new byte[16];
                data[0] = cmd;
                data[1] = (byte) ((EEPROM_START_ADDRESS + i+offset) >> 8);
                data[2] = (byte) ((EEPROM_START_ADDRESS + i+offset) & 0xff);
                data[3] = buf[i];

                this.connection.bulkTransfer(this.endpointOut1, data, 4, 500);

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return 0;
        }

        private int eepromWriteLen(int offset,int len, byte[] buf) {
            //To Do
            /*
                            int r;
                            unsigned char data[32];
                            r=EEPROM_write_cmd(0x51,offset,len,buf);
                            r=EEPROM_read(offset,len,data);
                            r=strncmp(buf,data,len);
                            LOGD("EEPROM_write 0x51 cmp r %d %s %s\n", r,buf,data);
                            if(r==0)
                                return 0;
                            r=EEPROM_write_cmd(0x55,offset,len,buf);
                            r=EEPROM_read(offset,len,data);
                            r=strncmp(buf,data,len);
                            LOGD("EEPROM_write 0x55 cmp r %d %s %s\n", r,buf,data);
                            if(r==0)
                                return 0;
                            else
                                return -1;
            * */
            int r;
            boolean result;
            r=eepromWriteLenCMD( (byte) 0x51, offset,len,buf);
            byte [] data= eepromReadLen(offset,len);
            result=Arrays.equals(buf,data);
            Logger.e("eepromWrite 0x51 = " + Arrays.toString(buf) + "2nd str " + Arrays.toString(data));
            if(result==true)
                return 0;
            r=eepromWriteLenCMD( (byte) 0x55, offset,len,buf);
            byte [] data2= eepromReadLen(offset,len);
            result=Arrays.equals(buf,data2);
            Logger.e("eepromWrite 0x55 = " + Arrays.toString(buf) + "2nd str " + Arrays.toString(data));
            if(result==true)
                return 0;
            else
                return -1;



        }

    }


    public static class FPSensorPara{
        public byte m_AGC;
        public byte m_AEC;

        void setParaFromEEPROM(byte [] data){
            int tmp;
            m_AGC=data[16];
            m_AEC=data[19];

        }

    }

}
