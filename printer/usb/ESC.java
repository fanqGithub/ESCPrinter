package de.kai_morich.simple_usb_terminal.printer.usb;

/**
 * Created by fanqi on 2020/3/30.
 * Description:
 */
public class ESC {

    //换码
    public static final byte ESC = 27;

    //文本分隔符
    public static final byte FS = 28;

    //组分隔符
    public static final byte GS = 29;

    //打印并换行（水平定位）
    public static final byte LF = 10;

    /**
     * 重置/初始化
     *
     * @return
     */
    public static byte[] RESET() {
        byte[] result = new byte[2];
        result[0] = ESC;
        result[1] = 64;
        return result;
    }

    /**
     * 走纸
     *
     * @return
     */
    public static byte[] Feed_LINE() {
        byte[] result = new byte[1];
        result[0] = LF;
        return result;
    }

    /**
     * 左对齐
     * ESC a n
     *
     * @return bytes for this command
     */
    public static byte[] Align_Left() {
        byte[] result = new byte[3];
        result[0] = ESC;
        result[1] = 97;
        result[2] = 0;
        return result;
    }

    /**
     * 居中对齐
     * ESC a n
     *
     * @return bytes for this command
     */
    public static byte[] Align_Center() {
        byte[] result = new byte[3];
        result[0] = ESC;
        result[1] = 97;
        result[2] = 1;
        return result;
    }

    /**
     * 右对齐
     * ESC a n
     *
     * @return bytes for this command
     */
    public static byte[] Align_Right() {
        byte[] result = new byte[3];
        result[0] = ESC;
        result[1] = 97;
        result[2] = 2;
        return result;
    }


    /**
     * 下划线
     * ESC - n/FS - n
     *
     * @param cn  是否为中文
     * @param dot 线宽 （0表示关闭）
     * @return bytes for this command
     */
    public static byte[] underLine(boolean cn, int dot) {
        byte[] result = new byte[3];
        result[0] = cn ? FS : ESC;
        result[1] = 45;
        switch (dot) {
            default:
            case 0:
                result[2] = 0;
                break;
            case 1:
                result[2] = 1;
                break;
            case 2:
                result[2] = 2;
                break;
        }
        return result;
    }

    /**
     * 开启着重强调(加粗)
     * ESC E n
     *
     * @return bytes for this command
     */
    @SuppressWarnings("unused")
    public static byte[] emphasizedOn() {
        byte[] result = new byte[3];
        result[0] = ESC;
        result[1] = 69;
        result[2] = 0xF;
        return result;
    }

    /**
     * 关闭着重强调(加粗)
     * ESC E n
     *
     * @return bytes for this command
     */
    @SuppressWarnings("unused")
    public static byte[] emphasizedOff() {
        byte[] result = new byte[3];
        result[0] = ESC;
        result[1] = 69;
        result[2] = 0;
        return result;
    }

    /**
     * 打印并走纸n行
     * Prints the data in the print buffer and feeds n lines
     * ESC d n
     *
     * @param n lines
     * @return bytes for this command
     */
    @SuppressWarnings("unused")
    public static byte[] printAndFeedLines(byte n) {
        byte[] result = new byte[3];
        result[0] = ESC;
        result[1] = 100;
        result[2] = n;
        return result;
    }


    /**
     * 字体变大为标准的n倍
     *
     * @param num 倍数
     * @return bytes for this command
     */
    @SuppressWarnings("unused")
    public static byte[] fontSizeSetBig(int num) {
        byte realSize = 0;
        switch (num) {
            case 0:
                realSize = 0;
                break;
            case 1:
                realSize = 17;
                break;
            case 2:
                realSize = 34;
                break;
            case 3:
                realSize = 51;
                break;
            case 4:
                realSize = 68;
                break;
            case 5:
                realSize = 85;
                break;
            case 6:
                realSize = 102;
                break;
            case 7:
                realSize = 119;
                break;
        }
        byte[] result = new byte[3];
        result[0] = GS;
        result[1] = 33;
        result[2] = realSize;
        return result;
    }


    /**
     * 进纸切割(全切)
     * Feeds paper to ( cutting position + n x vertical motion unit )
     * and executes a full cut ( cuts the paper completely )
     *
     * @return bytes for this command
     */
    @SuppressWarnings("unused")
    public static byte[] feedPaperCut() {
        byte[] result = new byte[4];
        result[0] = GS;
        result[1] = 86;
        result[2] = 65;
        result[3] = 0;
        return result;
    }

    /**
     * 进纸切割（留部分，半切）
     * Feeds paper to ( cutting position + n x vertical motion unit )
     * and executes a partial cut ( one point left uncut )
     *
     * @return bytes for this command
     */
    @SuppressWarnings("unused")
    public static byte[] feedPaperCutPartial() {
        byte[] result = new byte[4];
        result[0] = GS;
        result[1] = 86;
        result[2] = 66;
        result[3] = 0;
        return result;
    }


    /**
     * Select Font A
     * ESC M n
     *
     * @return bytes for this command
     */
    @SuppressWarnings("unused")
    public static byte[] selectFontA() {
        byte[] result = new byte[3];
        result[0] = ESC;
        result[1] = 77;
        result[2] = 0;
        return result;
    }

    /**
     * Select Font B
     * ESC M n
     *
     * @return bytes for this command
     */
    @SuppressWarnings("unused")
    public static byte[] selectFontB() {
        byte[] result = new byte[3];
        result[0] = ESC;
        result[1] = 77;
        result[2] = 1;
        return result;
    }

    /**
     * Select Font C ( some printers don't have font C )
     * ESC M n
     *
     * @return bytes for this command
     */
    @SuppressWarnings("unused")
    public static byte[] selectFontC() {
        byte[] result = new byte[3];
        result[0] = ESC;
        result[1] = 77;
        result[2] = 2;
        return result;
    }

    /**
     * Select Font A
     * FS ! n
     *
     * @return bytes for this command
     */
    @SuppressWarnings("unused")
    public static byte[] selectCNFontA() {
        byte[] result = new byte[3];
        result[0] = FS;
        result[1] = 33;
        result[2] = 0;
        return result;
    }

    /**
     * Select Font B
     * FS ! n
     *
     * @return bytes for this command
     */
    @SuppressWarnings("unused")
    public static byte[] selectCNFontB() {
        byte[] result = new byte[3];
        result[0] = FS;
        result[1] = 33;
        result[2] = 1;
        return result;
    }


    /**
     * 水平方向向右移动col列
     *
     * @param col
     * @return
     */
    public static byte[] set_horizontal_position(byte col) {
        byte[] result = new byte[4];
        result[0] = ESC;
        result[1] = 68;
        result[2] = col;
        result[3] = 0;
        return result;
    }

    /**
     * 打印条码
     *
     * @param m             格式1: 0≤m≤10 格式 2: 65 ≤ m ≤ 75
     * @param barcode2print
     * @return
     */
    public static byte[] print_bar_code(byte m, String barcode2print) {
        byte[] barcodeBytes = barcode2print.getBytes();
        byte[] result = new byte[3 + barcodeBytes.length + 1];
        result[0] = GS;
        result[1] = 107;
        result[2] = m;
        int idx = 3;
        for (byte b : barcodeBytes) {
            result[idx] = b;
            idx++;
        }
        result[idx] = 0;
        return result;
    }

    //1 ≤ n ≤ 255
    //n 为条码垂直方向的点数 默认值:50
    public static byte[] barcode_height(byte n) {
        byte[] result = new byte[3];
        result[0] = GS;
        result[1] = 104;
        result[2] = n;
        return result;
    }

    public static byte[] barcodeHRI(byte n) {
        byte[] result = new byte[3];
        result[0] = GS;
        result[1] = 72;
        result[2] = n;
        return result;
    }

}
