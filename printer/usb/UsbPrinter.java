package de.kai_morich.simple_usb_terminal.printer.usb;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;

import static de.kai_morich.simple_usb_terminal.printer.usb.PrinterUtil.decodeBitmap;
import static de.kai_morich.simple_usb_terminal.printer.usb.PrinterUtil.scalingBitmap;

/**
 * Created by fanqi on 2020/3/30.
 * Description:
 */
public abstract class UsbPrinter {

    private static final String TAG = UsbPrinter.class.getCanonicalName();

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private static String mCharsetName = "GBK";

    private static UsbManager usbManager;
    /**
     * 满足的设备
     */
    private static UsbDevice myUsbDevice;
    /**
     * usb接口
     */
    private static UsbInterface usbInterface;
    /**
     * 块输出端点
     */
    private static UsbEndpoint epBulkOut;
    private static UsbEndpoint epBulkIn;
    /**
     * 控制端点
     */
    private static UsbEndpoint epControl;
    /**
     * 中断端点
     */
    private static UsbEndpoint epIntEndpointOut;
    private static UsbEndpoint epIntEndpointIn;
    /**
     * 连接
     */
    private static UsbDeviceConnection myDeviceConnection;

    private Activity mActivity;

    private ICallBack callBack;

    @SuppressLint("NewApi")
    public UsbPrinter(Activity activity, ICallBack back) {
        this.mActivity = activity;
        this.callBack = back;
        init();
    }

    /**
     * 初始化打印机(获取usb打印设备,连接打印机,获取USB连接权限)
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void init() {
        //创建usbManager
        usbManager = (UsbManager) mActivity.getSystemService(Context.USB_SERVICE);
        enumeraterDevices(mActivity);
        getDeviceInterface();
        assignEndpoint();
        openDevice();
    }

    /**
     * 枚举设备
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static void enumeraterDevices(Activity activity) {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();

//            //这里是根据vid pid获取对应的设备。
//            if (device.getVendorId() == 4070 && device.getProductId() == 33054) {
//                // 获取USBDevice
//                myUsbDevice = device;
//                PendingIntent pi = PendingIntent.getBroadcast(activity, 0,
//                        new Intent(ACTION_USB_PERMISSION), 0);
//                usbManager.requestPermission(myUsbDevice, pi);
//            }
            Log.d(TAG, "enumeraterDevices: device=" + device.toString());
            //方式二
            int deviceClass = device.getDeviceClass();
            if (deviceClass == 0) {
                judgePrinter(device);
            } else {
                if (device.getProductName().toLowerCase().contains("printer")) {
                    judgePrinter(device);
                }
            }
        }
    }

    private static void judgePrinter(UsbDevice device) {
        UsbInterface usbI = device.getInterface(0);
        Log.d(TAG, "enumeraterDevices: usbI=" + usbI.toString());
        if (usbI.getInterfaceClass() == 7) {
            Log.d(TAG, "enumeraterDevices: 找到有打印机=" + device.toString());
            myUsbDevice = device;
        }
    }


    /**
     * 获取设备的接口
     */
    private static void getDeviceInterface() {
        if (myUsbDevice != null) {
            Log.d(TAG, "interfaceCounts : " + myUsbDevice.getInterfaceCount());
            try {
                usbInterface = myUsbDevice.getInterface(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d(TAG, "getDeviceInterface:成功获得设备接口:" + usbInterface.getId());
        }
    }

    /**
     * 分配端点，IN | OUT，即输入输出
     */
    private static void assignEndpoint() {
        if (usbInterface != null) {
            for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
                UsbEndpoint ep = usbInterface.getEndpoint(i);
                switch (ep.getType()) {
                    //块
                    case UsbConstants.USB_ENDPOINT_XFER_BULK:
                        //输出
                        if (UsbConstants.USB_DIR_OUT == ep.getDirection()) {
                            epBulkOut = ep;
                            Log.d(TAG, "assignEndpoint:Find the BulkEndpointOut," + "index:" + i + "," + "使用端点号：" + epBulkOut.getEndpointNumber());
                        } else {
                            epBulkIn = ep;
                            Log.d(TAG, "Find the BulkEndpointIn:" + "index:" + i + "," + "使用端点号：" + epBulkIn.getEndpointNumber());
                        }
                        break;
                    //控制
                    case UsbConstants.USB_ENDPOINT_XFER_CONTROL:
                        epControl = ep;
                        Log.d(TAG, "find the ControlEndPoint:" + "index:" + i + "," + epControl.getEndpointNumber());
                        break;
                    //中断
                    case UsbConstants.USB_ENDPOINT_XFER_INT:
                        //输出
                        if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                            epIntEndpointOut = ep;
                            Log.d(TAG, "find the InterruptEndpointOut:" + "index:" + i + "," + epIntEndpointOut.getEndpointNumber());
                        }
                        if (ep.getDirection() == UsbConstants.USB_DIR_IN) {
                            epIntEndpointIn = ep;
                            Log.d(TAG, "find the InterruptEndpointIn:" + "index:" + i + "," + epIntEndpointIn.getEndpointNumber());
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * 连接设备
     */
    private void openDevice() {
        //接口是否为null
        if (usbInterface != null) {
            // 在open前判断是否有连接权限；对于连接权限可以静态分配，也可以动态分配权限
            UsbDeviceConnection conn = null;
            if (usbManager.hasPermission(myUsbDevice)) {
                //有权限，那么打开
                conn = usbManager.openDevice(myUsbDevice);
            }
            if (null == conn) {
                Log.d(TAG, "openDevice: 不能连接到设备");
                if (callBack != null) {
                    callBack.onDeviceDisConnected();
                }
                return;
            }
            //打开设备
            if (conn.claimInterface(usbInterface, true)) {
                myDeviceConnection = conn;
                if (myDeviceConnection != null) {
                    Log.d(TAG, "open设备成功！");
                    if (callBack != null) {
                        callBack.onDeviceConnected();
                    }
//                    final String mySerial = myDeviceConnection.getSerial();
//                    Log.d(TAG, "设备serial number：" + mySerial);
                }
            } else {
                Log.d(TAG, "无法打开连接通道。");
                if (callBack != null) {
                    callBack.onDeviceDisConnected();
                }
                conn.close();
            }
        }
    }

    /**
     * 分割线
     */
    public void printDivide() {
        int length = getLineWidth();
        String line = "";
        while (length > 0) {
            line += "- ";
            length--;
        }
        this.textOutLn(line);
    }

    /**
     * 平分输出
     *
     * @param texts
     */
    public void textOutInOneLine(String... texts) {
        int lineLength = getLineStringWidth(0);
        int allTextWidth = 0;
        for (String txt : texts) {
            allTextWidth += PrinterUtil.getStringWidth(txt);
        }
        int needEmpty = (lineLength - allTextWidth % lineLength) / 2;
        String empty = "";
        while (needEmpty > 0) {
            empty += " ";
            needEmpty--;
        }
        StringBuilder sb = new StringBuilder();
        for (String txt : texts) {
            sb.append(txt + empty);
        }
        String finalPrint = sb.substring(0, sb.length() - 1);
        this.textOutLn(finalPrint);
    }

    public void textOutLn(final String txt) {
        this.textOut(txt + "\r\n");
    }

    public void textOut(final String txt) {
        try {
            this.outWrite(txt);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void feedLine() {
        this.write(ESC.Feed_LINE());
    }

    /**
     * 0-7
     *
     * @param size
     */
    public void setFontSize(int size) {
        this.write(ESC.fontSizeSetBig(size));
    }

    public void setAlignLeft() {
        this.write(ESC.Align_Left());
    }

    public void setAlignCenter() {
        this.write(ESC.Align_Center());
    }

    public void setAlignRight() {
        this.write(ESC.Align_Right());
    }

    public void underLine(boolean cn, int dot) {
        this.write(ESC.underLine(cn, dot));
    }

    public void reset() {
        this.write(ESC.RESET());
    }

    public void fullCut() {
        this.write(ESC.feedPaperCut());
    }

    public void partialCut() {
        this.write(ESC.feedPaperCutPartial());
    }

    public void selectFont(int fontType) {
        switch (fontType) {
            case PrinterUtil.CN_FONTA:
                this.write(ESC.selectCNFontA());
                break;
            case PrinterUtil.CN_FONTB:
                this.write(ESC.selectCNFontB());
                break;
            case PrinterUtil.EN_FONTA:
                this.write(ESC.selectFontA());
                break;
            case PrinterUtil.EN_FONTB:
                this.write(ESC.selectFontB());
                break;
            case PrinterUtil.EN_FONTC:
                this.write(ESC.selectFontC());
                break;
            default:
                this.write(ESC.selectCNFontA());
                break;
        }
    }

    private boolean checkReady() {
        boolean ready = false;
        if (myUsbDevice != null && myDeviceConnection != null) {
            ready = true;
        }
        return ready;
    }

    private int outWrite(String outStr) throws UnsupportedEncodingException {
        byte[] buffer = outStr.getBytes(mCharsetName);
        return write(buffer);
    }

    public int write(byte[] data) {
        if (checkReady()) {
            int result = myDeviceConnection.bulkTransfer(epBulkOut, data, data.length, 0);
            if (result == data.length) {
                if (callBack != null) {
                    callBack.onPrintSuccess();
                }
            } else {
                if (callBack != null) {
                    callBack.onPrintError("print err");
                }
            }
            return result;
        } else {
            Log.d(TAG, "write: device not ready");
            if (callBack != null) {
                callBack.onPrintError("print err");
            }
            return -255;
        }
    }

    /**
     * 使用光栅位图的打印方式
     */
    public void printBitmap(Bitmap bitmap, int requestWidth) {
        checkReady();
        // GS v 0 m xL xH yL yH d1...dk
        if (bitmap == null) {
            return;
        }
        //规范化位图宽高
        bitmap = PrinterUtil.resizeImage(bitmap, requestWidth);
        byte[] cmd = PrinterUtil.transImgData(bitmap);
        this.write(cmd);
    }

    /**
     * 打印 Drawable 图片
     *
     * @param res Resources
     * @param id  资源ID
     * @throws IOException 异常
     */
    @SuppressWarnings("unused")
    @Deprecated
    public void printDrawable(Resources res, int id) throws IOException {
        int maxWidth = getDrawableMaxWidth();
        Bitmap image = scalingBitmap(res, id, maxWidth);
        if (image == null) {
            return;
        }
        byte[] command = decodeBitmap(image, maxWidth);
        image.recycle();
        if (command != null) {
            write(command);
        }
    }

    public void printBarCode(String barcodeStr) {
        //一般 13位长条码 m可以=2
        byte m = 2;
        this.write(ESC.print_bar_code(m, barcodeStr));
    }

    /**
     * 0≤n≤255
     * n 如下表:
     * 0: 不打印 HRI
     * 1: HRI 在条码下方
     * 2: HRI 在条码上方
     * 3: HRI 在条码上方和下方
     *
     * @param barcodeStr
     * @param HIR
     */
    public void printBarCodeWithHRI(String barcodeStr, byte HIR) {
        //一般 13位长条码 m可以=2
        byte m = 2;
        this.write(ESC.barcodeHRI(HIR));
        this.write(ESC.print_bar_code(m, barcodeStr));
    }

    public void setHorizontalPosition(byte n) {
        this.write(ESC.set_horizontal_position(n));
    }

    public void setBarCodeHeight(byte height) {
        this.write(ESC.barcode_height(height));
    }


    /**
     * 根据字号的大小，获取每行能输出的字节长度。
     *
     * @param textSize
     * @return
     */
    protected abstract int getLineStringWidth(int textSize);

    /**
     * 获取每行能打印"-"的字节总长度
     *
     * @return
     */
    protected abstract int getLineWidth();

    protected abstract int getDrawableMaxWidth();

}
