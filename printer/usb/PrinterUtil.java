package de.kai_morich.simple_usb_terminal.printer.usb;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fanqi on 2020/3/30.
 * Description:
 */
public class PrinterUtil {

    private static String[] binaryArray = {"0000", "0001", "0010", "0011",
            "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011",
            "1100", "1101", "1110", "1111"};

    private static String hexStr = "0123456789ABCDEF";

    private static String mCharsetName = "GBK";

    public static final int CN_FONTA = 0;
    public static final int CN_FONTB = 1;
    public static final int EN_FONTA = 2;
    public static final int EN_FONTB = 3;
    public static final int EN_FONTC = 4;

    public static int grayPixle(int pixel) {
        int red = (pixel & 0x00ff0000) >> 16;//获取r分量
        int green = (pixel & 0x0000ff00) >> 8;//获取g分量
        int blue = pixel & 0x000000ff;//获取b分量
        return (int) (red * 0.3f + green * 0.59f + blue * 0.11f);//加权平均法进行灰度化
    }


    public static Bitmap resizeImage(Bitmap bitmap, int requestWidth) {
        //将位图宽度规范化为8的整数倍
        int legalWidth = (requestWidth + 7) / 8 * 8;
        int height = (int) (legalWidth * bitmap.getHeight() / (float) bitmap.getWidth());
        return Bitmap.createScaledBitmap(bitmap, legalWidth, height, true);
    }

    public static byte[] transImgData(Bitmap bitmap) {
        int width = bitmap.getWidth() / 8;
        int height = bitmap.getHeight();
        byte[] cmd = new byte[width * height + 4 + 4];
        cmd[0] = 29;
        cmd[1] = 118;
        cmd[2] = 48;
        cmd[3] = 0;
        //计算xL
        cmd[4] = (byte) (width % 256);
        //计算xH
        cmd[5] = (byte) (width / 256);
        //计算yL
        cmd[6] = (byte) (height % 256);
        //计算yH
        cmd[7] = (byte) (height / 256);

        int index = 8;
        int temp = 0;
        int part[] = new int[8];
        for (int j = 0; j < bitmap.getHeight(); j++) {
            for (int i = 0; i < bitmap.getWidth(); i += 8) {
                //横向每8个像素点组成一个字节。
                for (int k = 0; k < 8; k++) {
                    int pixel = bitmap.getPixel(i + k, j);
                    int grayPixle = PrinterUtil.grayPixle(pixel);
                    if (grayPixle > 128) {
                        //灰度值大于128位   白色 为第k位0不打印
                        part[k] = 0;
                    } else {
                        part[k] = 1;
                    }
                }

                //128千万不要写成2^7，^是异或操作符
                temp = part[0] * 128 +
                        part[1] * 64 +
                        part[2] * 32 +
                        part[3] * 16 +
                        part[4] * 8 +
                        part[5] * 4 +
                        part[6] * 2 +
                        part[7] * 1;
                cmd[index++] = (byte) temp;
            }
        }
        return cmd;
    }

    public static int getStringWidth(String str) {
        int width = 0;
        for (char c : str.toCharArray()) {
            width += isChinese(c) ? 2 : 1;
        }
        return width;
    }

    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION;
    }


    /**
     * 缩放图片
     *
     * @param drawable 图片
     * @param maxWidth 最大宽
     * @return 缩放后的图片
     */
    public static Bitmap scalingDrawable(Drawable drawable, int maxWidth) {
        if (drawable == null || drawable.getIntrinsicWidth() == 0
                || drawable.getIntrinsicHeight() == 0) {
            return null;
        }
        final int width = drawable.getIntrinsicWidth();
        final int height = drawable.getIntrinsicHeight();
        try {
            Bitmap image = Bitmap.createBitmap(width, height,
                    drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                            : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(image);
            drawable.setBounds(0, 0, width, height);
            drawable.draw(canvas);
            // 精确缩放
            if (maxWidth <= 0 || width <= maxWidth) {
                return image;
            }
            final float scale = maxWidth / (float) width;
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            Bitmap resizeImage = Bitmap.createBitmap(image, 0, 0, width, height, matrix, true);
            image.recycle();
            return resizeImage;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    /**
     * 缩放图片
     *
     * @param res      资源
     * @param id       ID
     * @param maxWidth 最大宽
     * @return 缩放后的图片
     */
    public static Bitmap scalingBitmap(Resources res, int id, int maxWidth) {
        if (res == null) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;// 设置只量取宽高
        BitmapFactory.decodeResource(res, id, options);// 量取宽高
        options.inJustDecodeBounds = false;
        // 粗略缩放
        if (maxWidth > 0 && options.outWidth > maxWidth) {
            // 超过限定宽
            double ratio = options.outWidth / (double) maxWidth;// 计算缩放比
            int sampleSize = (int) Math.floor(ratio);// 向下取整，保证缩放后不会低于最大宽高
            if (sampleSize > 1) {
                options.inSampleSize = sampleSize;// 设置缩放比，原图的几分之一
            }
        }
        try {
            Bitmap image = BitmapFactory.decodeResource(res, id, options);
            final int width = image.getWidth();
            final int height = image.getHeight();
            // 精确缩放
            if (maxWidth <= 0 || width <= maxWidth) {
                return image;
            }
            final float scale = maxWidth / (float) width;
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            Bitmap resizeImage = Bitmap.createBitmap(image, 0, 0, width, height, matrix, true);
            image.recycle();
            return resizeImage;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    public static byte[] decodeBitmap(Bitmap image, int parting) {
        ArrayList<byte[]> data = decodeBitmapToDataList(image, parting);
        int len = 0;
        for (byte[] srcArray : data) {
            len += srcArray.length;
        }
        byte[] destArray = new byte[len];
        int destLen = 0;
        for (byte[] srcArray : data) {
            System.arraycopy(srcArray, 0, destArray, destLen, srcArray.length);
            destLen += srcArray.length;
        }
        return destArray;
    }

    public static ArrayList<byte[]> decodeBitmapToDataList(Bitmap image, int parting) {
        if (parting <= 0 || parting > 255) {
            parting = 255;
        }
        if (image == null) {
            return null;
        }

        final int width = image.getWidth();
        final int height = image.getHeight();
        if (width <= 0 || height <= 0) {
            return null;
        }
        if (width > 2040) {
            // 8位9针，宽度限制2040像素（但一般纸张都没法打印那么宽，但并不影响打印）
            final float scale = 2040 / (float) width;
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            Bitmap resizeImage;
            try {
                resizeImage = Bitmap.createBitmap(image, 0, 0, width, height, matrix, true);
            } catch (OutOfMemoryError e) {
                return null;
            }
            ArrayList<byte[]> data = decodeBitmapToDataList(resizeImage, parting);
            resizeImage.recycle();
            return data;
        }

        // 宽命令
        String widthHexString = Integer.toHexString(width % 8 == 0 ? width / 8 : (width / 8 + 1));
        if (widthHexString.length() > 2) {
            // 超过2040像素才会到达这里
            return null;
        } else if (widthHexString.length() == 1) {
            widthHexString = "0" + widthHexString;
        }
        widthHexString += "00";

        // 每行字节数(除以8，不足补0)
        String zeroStr = "";
        int zeroCount = width % 8;
        if (zeroCount > 0) {
            for (int i = 0; i < (8 - zeroCount); i++) {
                zeroStr += "0";
            }
        }
        ArrayList<String> commandList = new ArrayList<>();
        // 高度每parting像素进行一次分割
        int time = height % parting == 0 ? height / parting : (height / parting + 1);// 循环打印次数
        for (int t = 0; t < time; t++) {
            int partHeight = t == time - 1 ? height % parting : parting;// 分段高度

            // 高命令
            String heightHexString = Integer.toHexString(partHeight);
            if (heightHexString.length() > 2) {
                // 超过255像素才会到达这里
                return null;
            } else if (heightHexString.length() == 1) {
                heightHexString = "0" + heightHexString;
            }
            heightHexString += "00";

            // 宽高指令
            String commandHexString = "1D763000";
            commandList.add(commandHexString + widthHexString + heightHexString);

            ArrayList<String> list = new ArrayList<>(); //binaryString list
            StringBuilder sb = new StringBuilder();
            // 像素二值化，非黑即白
            for (int i = 0; i < partHeight; i++) {
                sb.delete(0, sb.length());
                for (int j = 0; j < width; j++) {
                    // 实际在图片中的高度
                    int startHeight = t * parting + i;
                    //得到当前像素的值
                    int color = image.getPixel(j, startHeight);
                    int red, green, blue;
                    if (image.hasAlpha()) {
                        //得到alpha通道的值
                        int alpha = Color.alpha(color);
                        //得到图像的像素RGB的值
                        red = Color.red(color);
                        green = Color.green(color);
                        blue = Color.blue(color);
                        final float offset = alpha / 255.0f;
                        // 根据透明度将白色与原色叠加
                        red = 0xFF + (int) Math.ceil((red - 0xFF) * offset);
                        green = 0xFF + (int) Math.ceil((green - 0xFF) * offset);
                        blue = 0xFF + (int) Math.ceil((blue - 0xFF) * offset);
                    } else {
                        //得到图像的像素RGB的值
                        red = Color.red(color);
                        green = Color.green(color);
                        blue = Color.blue(color);
                    }
                    // 接近白色改为白色。其余黑色
                    if (red > 160 && green > 160 && blue > 160) {
                        sb.append("0");
                    } else {
                        sb.append("1");
                    }
                }
                // 每一行结束时，补充剩余的0
                if (zeroCount > 0) {
                    sb.append(zeroStr);
                }
                list.add(sb.toString());
            }
            // binaryStr每8位调用一次转换方法，再拼合
            ArrayList<String> bmpHexList = new ArrayList<>();
            for (String binaryStr : list) {
                sb.delete(0, sb.length());
                for (int i = 0; i < binaryStr.length(); i += 8) {
                    String str = binaryStr.substring(i, i + 8);
                    // 2进制转成16进制
                    String hexString = binaryStrToHexString(str);
                    sb.append(hexString);
                }
                bmpHexList.add(sb.toString());
            }

            // 数据指令
            commandList.addAll(bmpHexList);
        }
        ArrayList<byte[]> data = new ArrayList<>();
        for (String hexStr : commandList) {
            data.add(hexStringToBytes(hexStr));
        }
        return data;
    }


    /**
     * 2进制转成16进制
     *
     * @param binaryStr 2进制串
     * @return 16进制串
     */
    @SuppressWarnings("unused")
    public static String binaryStrToHexString(String binaryStr) {
        String hex = "";
        String f4 = binaryStr.substring(0, 4);
        String b4 = binaryStr.substring(4, 8);
        for (int i = 0; i < binaryArray.length; i++) {
            if (f4.equals(binaryArray[i])) {
                hex += hexStr.substring(i, i + 1);
            }
        }
        for (int i = 0; i < binaryArray.length; i++) {
            if (b4.equals(binaryArray[i])) {
                hex += hexStr.substring(i, i + 1);
            }
        }
        return hex;
    }

    /**
     * 16进制指令list转换为byte[]指令
     *
     * @param list 指令集
     * @return byte[]指令
     */
    @SuppressWarnings("unused")
    public static byte[] hexListToByte(List<String> list) {
        ArrayList<byte[]> commandList = new ArrayList<>();
        for (String hexStr : list) {
            commandList.add(hexStringToBytes(hexStr));
        }
        int len = 0;
        for (byte[] srcArray : commandList) {
            len += srcArray.length;
        }
        byte[] destArray = new byte[len];
        int destLen = 0;
        for (byte[] srcArray : commandList) {
            System.arraycopy(srcArray, 0, destArray, destLen, srcArray.length);
            destLen += srcArray.length;
        }
        return destArray;
    }

    /**
     * 16进制串转byte数组
     *
     * @param hexString 16进制串
     * @return byte数组
     */
    @SuppressWarnings("unused")
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    public static byte[] strTobytes(String outStr) {
        try {
            return outStr.getBytes(mCharsetName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 16进制char 转 byte
     *
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) hexStr.indexOf(c);
    }

}
