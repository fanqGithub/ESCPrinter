package de.kai_morich.simple_usb_terminal.printer.usb;

import android.app.Activity;

/**
 * Created by fanqi on 2020/3/30.
 * Description:
 */
public class UsbPrinter80MM extends UsbPrinter {

    public UsbPrinter80MM(Activity activity, ICallBack back) {
        super(activity, back);
    }

    @Override
    protected int getLineStringWidth(int textSize) {
        switch (textSize) {
            default:
            case 0:
                return 47;
            case 1:
                return 23;
        }
    }

    @Override
    protected int getLineWidth() {
        return 24;
    }

    @Override
    protected int getDrawableMaxWidth() {
        return 500;
    }
}
