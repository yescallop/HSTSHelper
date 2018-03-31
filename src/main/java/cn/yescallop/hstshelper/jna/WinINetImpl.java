package cn.yescallop.hstshelper.jna;

import cn.yescallop.hstshelper.jna.WinINet.INTERNET_PER_CONN_OPTION.ByReference;
import cn.yescallop.hstshelper.jna.WinINet.INTERNET_PER_CONN_OPTION_LIST;
import com.sun.jna.Pointer;

/**
 * See https://github.com/monkeyWie/proxyee-down/blob/master/common/src/main/java/lee/study/down/jna/WinInetImpl.java
 */

public class WinINetImpl {

    public static INTERNET_PER_CONN_OPTION_LIST buildOptionList(int size) {
        INTERNET_PER_CONN_OPTION_LIST list = new INTERNET_PER_CONN_OPTION_LIST();
        list.dwSize = list.size();
        list.pszConnection = null;
        list.dwOptionCount = size;
        list.pOptions = new ByReference();
        return list;
    }

    public static boolean refreshOptions(INTERNET_PER_CONN_OPTION_LIST list) {
        return WinINet.INSTANCE.InternetSetOption(Pointer.NULL, WinINet.INTERNET_OPTION_PER_CONNECTION_OPTION, list, list.size()) &&
                WinINet.INSTANCE.InternetSetOption(Pointer.NULL, WinINet.INTERNET_OPTION_PROXY_SETTINGS_CHANGED, Pointer.NULL, 0) &&
                WinINet.INSTANCE.InternetSetOption(Pointer.NULL, WinINet.INTERNET_OPTION_REFRESH, Pointer.NULL, 0);
    }
}