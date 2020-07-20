package de.kai_morich.simple_usb_terminal.printer.usb;

import android.app.Activity;

/**
 * Created by fanqi on 2020/3/30.
 * Description:
 */
public class UsbPrinter58MM extends UsbPrinter {

    public UsbPrinter58MM(Activity activity, ICallBack back) {
        super(activity, back);
    }

    @Override
    protected int getLineStringWidth(int textSize) {
        switch (textSize) {
            default:
            case 0:
                return 31;
            case 1:
                return 15;
        }
    }

    @Override
    protected int getLineWidth() {
        return 16;
    }

    @Override
    protected int getDrawableMaxWidth() {
        return 380;
    }
}
