package cn.yescallop.hstshelper;

import cn.yescallop.hstshelper.jna.WinINet;
import cn.yescallop.hstshelper.jna.WinINet.INTERNET_PER_CONN_OPTION;
import cn.yescallop.hstshelper.jna.WinINet.INTERNET_PER_CONN_OPTION_LIST;
import cn.yescallop.hstshelper.jna.WinINetImpl;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.log4j.Logger;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Scallop Ye
 */
public class HSTSHelper {

    private static final Logger LOGGER = Logger.getLogger(HSTSHelper.class);

    public static void main(String[] args) {
        System.out.println("Starting HSTSHelper\nThis program is licensed under Press enter to stop the program\n");

        Pattern[] rules;
        int ignoredCount = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader("rules.txt"));
            List<Pattern> list = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    try {
                        list.add(Pattern.compile(line));
                    } catch (PatternSyntaxException e) {
                        ignoredCount++;
                    }
                }
            }
            rules = list.toArray(new Pattern[list.size()]);
        } catch (IOException e) {
            LOGGER.error("Failed to load rules!");
            System.exit(1);
            return;
        }
        String message = "Loaded " + rules.length + " rule(s)";
        if (ignoredCount != 0) {
            message += ", " + ignoredCount + " ignored";
        }
        LOGGER.info(message);

        final Logger redirectorLogger = Logger.getLogger("Redirector");

        HttpProxyServer server = DefaultHttpProxyServer.bootstrap()
                .withPort(1084)
                .withFiltersSource(new HttpFiltersSourceAdapter() {
                    public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                        return new HttpFiltersAdapter(originalRequest) {
                            @Override
                            public HttpResponse clientToProxyRequest(HttpObject httpObject) {
                                if (originalRequest.method() == HttpMethod.CONNECT)
                                    return null;
                                String uri = originalRequest.uri();
                                String uriCut = originalRequest.uri().substring(7);
                                for (Pattern rule : rules) {
                                    if (rule.matcher(uriCut).find()) {
                                        HttpHeaders headers = new DefaultHttpHeaders()
                                                .add("Location", "https://" + uriCut);
                                        redirectorLogger.info("Redirected: " + uri);
                                        return new DefaultHttpResponse(
                                                originalRequest.protocolVersion(),
                                                HttpResponseStatus.FOUND,
                                                headers
                                        );
                                    }
                                }
                                return null;
                            }
                        };
                    }
                })
                .start();
        enableProxy();
        Runtime.getRuntime().addShutdownHook(new Thread(HSTSHelper::disableProxy, "main-shutdown-hook"));
        try {
            System.in.read();
        } catch (IOException e) {
            //ignored
        }
        server.stop();
    }

    private static void enableProxy() {
        INTERNET_PER_CONN_OPTION_LIST list = WinINetImpl.buildOptionList(2);
        INTERNET_PER_CONN_OPTION[] pOptions = (INTERNET_PER_CONN_OPTION[]) list.pOptions
                .toArray(list.dwOptionCount);

        // Set flags.
        pOptions[0].dwOption = WinINet.INTERNET_PER_CONN_FLAGS;
        pOptions[0].Value.dwValue = WinINet.PROXY_TYPE_PROXY;
        pOptions[0].Value.setType(int.class);

        // Set proxy name.
        pOptions[1].dwOption = WinINet.INTERNET_PER_CONN_PROXY_SERVER;
        pOptions[1].Value.pszValue = "127.0.0.1:1084";
        pOptions[1].Value.setType(String.class);

        if (WinINetImpl.refreshOptions(list)) {
            LOGGER.info("Proxy set to system default");
        } else {
            LOGGER.warn("Failed to set proxy to system default!");
        }
    }

    private static void disableProxy() {
        INTERNET_PER_CONN_OPTION_LIST list = WinINetImpl.buildOptionList(1);
        INTERNET_PER_CONN_OPTION[] pOptions = (INTERNET_PER_CONN_OPTION[]) list.pOptions
                .toArray(list.dwOptionCount);
        // Set flags.
        pOptions[0].dwOption = WinINet.INTERNET_PER_CONN_FLAGS;
        pOptions[0].Value.dwValue = WinINet.PROXY_TYPE_DIRECT;
        pOptions[0].Value.setType(int.class);

        if (WinINetImpl.refreshOptions(list)) {
            LOGGER.info("Proxy disabled");
        } else {
            LOGGER.warn("Failed to disable proxy!");
        }
    }
}
