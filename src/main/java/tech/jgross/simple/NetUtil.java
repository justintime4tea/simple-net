/*
 *
 * Copyright (C) 2017 JGross Tech
 * Author: Justin Gross <justin@jgross.tech>
 *
 * This file is part of simple-net.
 *
 * simple-net is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * simple-net is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with simple-net.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package tech.jgross.simple;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Simple network utility for common network tasks
 */
public class NetUtil {

    private NetUtil () {}

    /**
     * Discover the public facing IP address of the connected device
     *
     * @return Public IP address
     * @throws IOException if an I/O exception occurs
     * @throws ParseException if JSON used cannot be parsed
     */
    public static InetAddress getPublicIP() throws IOException, ParseException {
        return getPublicIP(IPLookupService.IPIFY);
     }

    /**
     * Discover the public facing IP address of the connected device using
     * the provided {@link IPLookupService}
     *
     * @param lookupService is a supported public IP address lookup service
     * @return InetAddress representation of the public IP address
     * @throws IOException if an I/O exception occurs
     * @throws ParseException if JSON used cannot be parsed
     */
    public static InetAddress getPublicIP(IPLookupService lookupService) throws IOException, ParseException {

        String response;
        JSONParser parser = new JSONParser();
        JSONObject jsonResponse;

        switch (lookupService) {
            case IPIFY:
                response = simpleHttpGet(getLookupURL(lookupService));
                jsonResponse = (JSONObject) parser.parse(response);
                return InetAddress.getByName(jsonResponse.get("ip").toString());
            case WTFISMYIP:
                response = simpleHttpGet(getLookupURL(lookupService));
                jsonResponse = (JSONObject) parser.parse(response);
                return InetAddress.getByName(jsonResponse.get("YourFuckingIPAddress").toString());
            case IPINFO_DOT_IO:
                response = simpleHttpGet(getLookupURL(lookupService));
                jsonResponse = (JSONObject) parser.parse(response);
                return InetAddress.getByName(jsonResponse.get("ip").toString());
            case TRACKIP:
                response = simpleHttpGet(getLookupURL(lookupService));
                jsonResponse = (JSONObject) parser.parse(response);
                return InetAddress.getByName(jsonResponse.get("IP").toString());
            case ICANHAZIP:
                return InetAddress.getByName(simpleHttpGet(getLookupURL(lookupService)));
            default:
                return getPublicIP(IPLookupService.IPIFY);
        }
    }

    /**
     * Issue a simple GET request to a {@link URL} end-point
     *
     * @param url of the remote resource to issue an Http request upon
     * @return a string representing the response from a simple http GET request
     * @throws IOException if an I/O exception occurs
     */
    public static String simpleHttpGet(URL url) throws IOException {
        return simpleHttpGet(url, UserAgent.FIREFOX_LINUX_X64, 4000, 4000);
    }

    /**
     * Issue a simple GET request to a {@link URL} end-point using chosen {@link UserAgent}
     *
     * @param ua UserAgent to use for URLConnection
     * @param url of the remote resource to issue an Http request upon
     * @return a string representing the response from a simple http GET request
     * @throws IOException if an I/O exception occurs
     */
    public static String simpleHttpGet(URL url, UserAgent ua) throws IOException {
        return simpleHttpGet(url, ua, 4000, 4000);
    }

    /**
     * Issue a simple GET request to a {@ink URL} end-point using a chosen {@link UserAgent} while
     * setting both connection and read timeouts.
     *
     * @see URLConnection#setReadTimeout(int)
     * @see URLConnection#setConnectTimeout(int)
     *
     * @param url of the remote resource to issue an Http request upon
     * @param ua UserAgent to use for URLConnection
     * @param connectionTimeout used to set connection timeout
     * @param readTimeout used to set readTimeout
     * @return a string representing the response from a simple http GET request
     * @throws IOException if an I/O exception occurs
     */
    public static String simpleHttpGet(URL url, UserAgent ua, int connectionTimeout, int readTimeout) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(connectionTimeout);
        connection.setReadTimeout(readTimeout);
        connection.setRequestProperty("User-Agent", getUserAgent(ua));
        ArrayList<String> resultLines = new ArrayList<>();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        bufferedReader.lines().forEachOrdered(resultLines::add);

        return String.join("", resultLines);
    }

    /**
     * Get the public IP address of current device using an {@link IPLookupService}
     *
     * @param lookupService a supported IPLookupService
     * @return a string representing the URL of a public IP lookup service
     */
    public static URL getLookupURL(IPLookupService lookupService) throws MalformedURLException {
        switch (lookupService) {
            case IPIFY:
                return new URL("https://api.ipify.org/?format=json");
            case WTFISMYIP:
                return new URL("https://wtfismyip.com/json");
            case IPINFO_DOT_IO:
                return new URL("https://ipinfo.io/json");
            case ICANHAZIP:
                return new URL("https://icanhazip.com");
            case TRACKIP:
                return new URL("https://www.trackip.net/ip?json");
            default:
                return getLookupURL(IPLookupService.IPIFY);
        }
    }

    /**
     * Get the Http response format of corresponding {@link IPLookupService}
     *
     * @param lookupService is the IP lookup service
     * @return the format which is used in the response from the provided IP lookup service
     */
    public static ResponseFormat getServiceResponseFormat(IPLookupService lookupService) {
        switch (lookupService) {
            case IPIFY:
                return ResponseFormat.JSON;
            case WTFISMYIP:
                return ResponseFormat.JSON;
            case IPINFO_DOT_IO:
                return ResponseFormat.JSON;
            case ICANHAZIP:
                return ResponseFormat.TEXT;
            case TRACKIP:
                return ResponseFormat.JSON;
            default:
                return ResponseFormat.JSON;
        }
    }

    /**
     * Provides a common {@link UserAgent} string
     *
     * @param ua UserAgent being requested
     * @return the requested UserAgent as a string
     */
    public static String getUserAgent(UserAgent ua) {
        switch (ua) {
            case CHROME_LINUX_X64:
                return "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";
            case CHROME_WIN10_X64:
                return "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";
            case CHROME_WIN7_X64:
                return "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";
            case CHROME_MACOSX:
                return "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";
            case FIREFOX_LINUX_X64:
                return "Mozilla/5.0 (Windows NT 6.1; rv:48.0) Gecko/20100101 Firefox/48.0";
            case FIREFOX_WIN10_X64:
                return "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:51.0) Gecko/20100101 Firefox/51.0";
            case FIREFOX_WIN7_X64:
                return "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:51.0) Gecko/20100101 Firefox/51.0";
            case FIREFOX_MACOSX:
                return "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.12; rv:51.0) Gecko/20100101 Firefox/51.0";
            case IE11_WIN10_X64:
                return "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko";
            case IE11_WIN8_X64:
                return "Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; rv:11.0) like Gecko";
            case IE11_WIN7_X64:
                return "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko";
            case IE9_WINVISTA:
                return "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.0; Trident/5.0; Trident/5.0)";
            case SAFARI_MACOSX:
                return "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_3) AppleWebKit/602.4.8 (KHTML, like Gecko) Version/10.0.3 Safari/602.4.8";
            case SAFARI_IOS:
                return "Mozilla/5.0 (iPad; CPU OS 10_2_1 like Mac OS X) AppleWebKit/602.4.6 (KHTML, like Gecko) Version/10.0 Mobile/14D27 Safari/602.1";
            default:
                return getUserAgent(UserAgent.FIREFOX_LINUX_X64);
        }
    }

    /**
     * Supported Public IP lookup services
     */
    public enum IPLookupService {
        IPIFY, WTFISMYIP, IPINFO_DOT_IO, ICANHAZIP, TRACKIP
    }

    /**
     * Type of response from an Http request
     */
    public enum ResponseFormat {
        JSON, HTML, TEXT, XML
    }

    /**
     * Recent user agents for major operating systems
     */
    public enum UserAgent {
        CHROME_LINUX_X64, CHROME_WIN10_X64, CHROME_WIN7_X64, CHROME_MACOSX,
        FIREFOX_LINUX_X64, FIREFOX_WIN10_X64, FIREFOX_WIN7_X64, FIREFOX_MACOSX,
        IE11_WIN10_X64, IE11_WIN8_X64, IE11_WIN7_X64, IE9_WINVISTA, SAFARI_MACOSX, SAFARI_IOS
    }

}
