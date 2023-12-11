package com.noan.takemyface.data;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CookieStore {
    private ConcurrentHashMap<String, List<Cookie>> cookieStore;



    private final CookieJar cookieJar;
    public CookieJar getCookieJar() {
        return cookieJar;
    }
    CookieStore() {
        cookieStore = new ConcurrentHashMap<>();
        cookieJar = new CookieJar() {
            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                cookieStore.put(url.host(), cookies);
            }


            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                List<Cookie> cookies = cookieStore.get(url.host());
                return cookies != null ? cookies : new ArrayList<Cookie>();
            }
        };
    }


    public void addCookie(String host, String name, String value) {
        Cookie newCookie = new Cookie.Builder().name(name).value(value).domain(host).build();
        List<Cookie> cookies = cookieStore.get(host);
        List<Cookie> newCookies;
        if (cookies != null) {
            newCookies = new ArrayList<Cookie>(cookies);
        } else {
            newCookies = new ArrayList<Cookie>();
        }
        newCookies.add(newCookie);
        cookieStore.put(host, newCookies);
    }
    public String getCookieValue(String host,String key)
    {
        List<Cookie> cookies = cookieStore.get(host);
        if(cookies!=null) {
            for (Cookie i : cookies) {
                if (i.name().equals(key)) {
                    return i.value();
                }
            }
        }
        return null;
    }
}
