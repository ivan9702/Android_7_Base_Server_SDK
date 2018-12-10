package com.startek.fingerprint.library;

public class NativeApi {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("fpapi");
    }

    public static native void init();

    public static native void unInit();

    public static native byte[] setFileDescriptor(int fileDescriptor);

    public static native void setEEPROM(byte[] data);

    public static native byte[] receiveImage();

    public static native byte[] snap(int fileDescriptor, int endpointAddress);

    public static native int capture();

    public static native int checkBlank();

    public static native int getNFIQ();

    public static native int getImageWidth();

    public static native int getImageHeight();

    public static native void getImageBuffer(byte[] data);

    public static native void setInterface(int interface_id);

	public static native void SaveImageBMP(String filepath);

	//

}
