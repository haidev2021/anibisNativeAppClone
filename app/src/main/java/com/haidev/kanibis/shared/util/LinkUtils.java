package com.haidev.kanibis.shared.util;


import android.text.TextUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class LinkUtils {

    private static ArrayList<String> _liveAPIs = new ArrayList<>();
    private static String[] _serverApiUrls = new String[] {};
    private static String[] _serverApiTitles = new String[] {};

    public static void init(String[] serverApiUrls, String[] serverApiTitles) {
        _serverApiUrls = serverApiUrls;
        _serverApiTitles = serverApiTitles;
        for (int i = 0; i < _serverApiTitles.length; i++)
            if(_serverApiTitles[i].toLowerCase().contains("live"))
                _liveAPIs.add(_serverApiUrls[i]);
    }

    public static boolean isAnibisLink(String url) {
        return !TextUtils.isEmpty(url);
    }

    public static String resolveServerUrl(String url) {
        return url;
    }

    public static boolean isWebUrl(String url) {
        if (TextUtils.isEmpty(url))
            return false;

        if (url.startsWith("http://") || url.startsWith("https://")) {
            try {
                URL uri = new URL(url);
                return true;
            } catch (MalformedURLException ex) {
                return false;
            }
        }

        return false;
    }
}
