package de.kai_morich.simple_usb_terminal.printer.usb;

/**
 * Created by fanqi on 2020/3/30.
 * Description:
 */
public interface ICallBack {

    void onDeviceConnected();

    void onPrintSuccess();

    void onPrintError(String err);

    void onDeviceDisConnected();
}
