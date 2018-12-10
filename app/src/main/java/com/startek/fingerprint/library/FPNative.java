package com.startek.fingerprint.library;

public class FPNative {
    static {
        System.loadLibrary("startek_jni");
    }

    static native void SetFPLibraryPath(String filepath);

    static native void InitialSDK();

    //static native int FP_ConnectCaptureDriver(int number);

    //static native void FP_DisconnectCaptureDriver();

    //static native int FP_Capture();

    //static native int FP_CheckBlank();

    static native void FP_SaveImageBMP(String filepath);

    static native int FP_CreateEnrollHandle();

    static native int FP_GetTemplate(byte[] m1);    //done

    static native int FP_GetEncryptedTemplate(byte[] ecode, byte[] iv, byte[] eskey);

    static native int FP_ISOminutiaEnroll(byte[] m1, byte[] m2);

    static native int FP_ISOminutiaEnroll_Encrypted(byte[] ecode, byte[] iv, byte[] eskey);

    static native void FP_SaveISOminutia(byte[] m2, String filepath);   //done

    static native void FP_DestroyEnrollHandle();

    static native int FP_LoadISOminutia(byte[] m2, String filepath);    //done

    static native int FP_ISOminutiaMatchEx(byte[] m1, byte[] m2);

    static native int FP_ISOminutiaMatch180Ex(byte[] m1, byte[] m2);

    static native int FP_ISOminutiaMatch360Ex(byte[] m1, byte[] m2);    //done

    static native int Score();  //done

    //static native void FP_GetImageBuffer(byte[] bmpBuffer);

    static native int FP_GetImageWidth();

    static native int FP_GetImageHeight();

    //static native int FP_LedOff();

    static native int FP_GetNFIQ(); //done

    static native void FP_GetISOImageBuffer(byte ImgCompAlgo,byte FpPos,byte[] isobuf);

    static native int FP_UpdateImgBufBMP(byte[] bmp);

    static native void FP_AES128_ECB_encrypt(byte[] In,byte[] Key,byte[] Out);

    static native void FP_AES128_ECB_decrypt(byte[] In,byte[] Key,byte[] Out);

    static native void FP_SetPublicKey(byte[] Key,int len);

    static native void FP_SetSessionKey(byte[] Key);

    static native void FP_GetDeleteData(byte[] user,int index,byte[] del_data);
}
