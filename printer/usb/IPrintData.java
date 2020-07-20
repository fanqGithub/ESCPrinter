package de.kai_morich.simple_usb_terminal.printer.usb;

import java.util.List;

/**
 * Created by fanqi on 2020/4/28.
 * Description:
 */
public interface IPrintData {
    List<byte[]> prepareDataBeforeSend();
}
