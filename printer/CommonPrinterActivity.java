package de.kai_morich.simple_usb_terminal.printer;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import de.kai_morich.simple_usb_terminal.ImageUtil;
import de.kai_morich.simple_usb_terminal.R;
import de.kai_morich.simple_usb_terminal.printer.usb.ESC;
import de.kai_morich.simple_usb_terminal.printer.usb.ICallBack;
import de.kai_morich.simple_usb_terminal.printer.usb.IPrintData;
import de.kai_morich.simple_usb_terminal.printer.usb.PrinterUtil;
import de.kai_morich.simple_usb_terminal.printer.usb.UsbPrinter;
import de.kai_morich.simple_usb_terminal.printer.usb.UsbPrinter58MM;
import de.kai_morich.simple_usb_terminal.printer.usb.UsbPrinterManager;
import de.kai_morich.simple_usb_terminal.proxy.Speakable;
import de.kai_morich.simple_usb_terminal.proxy.dynamicproxy.Animal;
import de.kai_morich.simple_usb_terminal.proxy.dynamicproxy.StrengthenAnimalHandler;
import de.kai_morich.simple_usb_terminal.proxy.staticproxy.Human;
import de.kai_morich.simple_usb_terminal.proxy.staticproxy.HumanProxy;

public class CommonPrinterActivity extends AppCompatActivity {

    public static final String SD_ROOT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();

    public static final String CASHIER_ROOT_DIR = SD_ROOT_DIR + "/cashier";

    public static final String PIC_PATH = CASHIER_ROOT_DIR + "/pic/";

    private static final String TAG = CommonPrinterActivity.class.getCanonicalName();

    //    private UsbPrinter commonPrinter;
    private Bitmap bitmapCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_printer);
//        commonPrinter = new UsbPrinter58MM(this, iCallBack);
        UsbPrinterManager.getInstance().initPrinter(this, UsbPrinterManager.USB_PRINTER_58);

        //静态代理的使用
        Human human = new Human();
        HumanProxy proxy = new HumanProxy(human);
        proxy.speak();

        //JDK动态代理使用
        Animal animal = new Animal();
        StrengthenAnimalHandler animalHandler = new StrengthenAnimalHandler(animal);
        Speakable proxyInstance = (Speakable) Proxy.newProxyInstance(animal.getClass().getClassLoader(), animal.getClass().getInterfaces(), animalHandler);
        proxyInstance.eat();

        bitmapCode = ImageUtil.getBitMp(PIC_PATH + "cashier_ticket_code.png");


    }

    public void print(View view) {

        UsbPrinterManager.getInstance().printData(new PrintOrderData());

//        commonPrinter.reset();
//        commonPrinter.textOutLn("打印机ESC");
//        commonPrinter.feedLine();
//        commonPrinter.reset();
//        commonPrinter.setAlignCenter();
//        commonPrinter.setFontSize(1);
//        commonPrinter.textOut("黄焖鸡米饭");
//        commonPrinter.feedLine();
//        commonPrinter.feedLine();
//        commonPrinter.reset();
//        commonPrinter.textOutInOneLine("商品", "数量", "价格\n");
//        commonPrinter.printDivide();
//        commonPrinter.setBarCodeHeight((byte) 200);
//        commonPrinter.printBarCodeWithHRI("6333040322001", (byte) 2);
//        commonPrinter.feedLine();
//        commonPrinter.feedLine();
//        commonPrinter.printBitmap(bitmapCode,240);
//
//        commonPrinter.partialCut();
    }

    private ICallBack iCallBack = new ICallBack() {
        @Override
        public void onDeviceConnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CommonPrinterActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onPrintSuccess() {
            Log.d(TAG, "onPrintSuccess: ");
        }

        @Override
        public void onPrintError(String err) {

        }

        @Override
        public void onDeviceDisConnected() {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class PrintOrderData implements IPrintData {

        @Override
        public List<byte[]> prepareDataBeforeSend() {
            List<byte[]> list = new ArrayList<byte[]>();
            list.add(ESC.Align_Center());
            list.add(PrinterUtil.strTobytes("宝地广场店"));
            list.add(ESC.RESET());
            list.add(PrinterUtil.strTobytes("时间：2020-4-28 11:20"));
            list.add(ESC.Feed_LINE());
            list.add(ESC.Feed_LINE());
            list.add(ESC.feedPaperCut());
            return list;
        }
    }
}
