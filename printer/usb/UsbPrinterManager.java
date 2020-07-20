package de.kai_morich.simple_usb_terminal.printer.usb;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

/**
 * Created by fanqi on 2020/4/28.
 * Description:
 */
public class UsbPrinterManager {

    public static final int USB_PRINTER_58 = 0;
    public static final int USB_PRINTER_80 = 1;

    private boolean isInitialized = false;
    private UsbPrinter mPrinter;
    private static final String TAG = UsbPrinterManager.class.getCanonicalName();

    private UsbPrinterManager() {
    }

    private static class PrinterHolder {
        private static final UsbPrinterManager USB_PRINTER_MANAGER = new UsbPrinterManager();
    }

    public static UsbPrinterManager getInstance() {
        return PrinterHolder.USB_PRINTER_MANAGER;
    }

    public void initPrinter(Activity activity, int PRINTER_TYPE) {
        if (mPrinter == null) {
            switch (PRINTER_TYPE) {
                case USB_PRINTER_58:
                    mPrinter = new UsbPrinter58MM(activity, iCallBack);
                    break;
                case USB_PRINTER_80:
                    mPrinter = new UsbPrinter80MM(activity, iCallBack);
                    break;
                default:
                    break;
            }
        }
    }

    private ICallBack iCallBack = new ICallBack() {
        @Override
        public void onDeviceConnected() {
            isInitialized = true;
            Log.d(TAG, "onDeviceConnected: ");
        }

        @Override
        public void onPrintSuccess() {
            Log.d(TAG, "onPrintSuccess: ");
        }

        @Override
        public void onPrintError(String err) {
            Log.e(TAG, "onPrintError: ");
        }

        @Override
        public void onDeviceDisConnected() {
            isInitialized = false;
            Log.d(TAG, "onDeviceDisConnected: ");
        }
    };

    public void printData(IPrintData printData) {
        if (!isInitialized) {
            Log.d(TAG, "printData: 打印机未初始化");
            return;
        }
        List<byte[]> list = printData.prepareDataBeforeSend();
        if (list != null) {
            new PrintTask(list, mPrinter).execute();
        } else {
            Log.d(TAG, "printData: 打印数据为空，或未准备好");
        }
    }

    private static class PrintTask extends AsyncTask<Void, Integer, String> {

        private List<byte[]> printData;
        private UsbPrinter printer;

        public PrintTask(List<byte[]> data, UsbPrinter p) {
            this.printData = data;
            this.printer = p;
        }

        @Override
        protected String doInBackground(Void... voids) {
            if (printer != null) {
                for (int i = 0; i < printData.size(); ++i) {
                    printer.write((byte[]) printData.get(i));
                }
            }
            return null;
        }
    }

}
