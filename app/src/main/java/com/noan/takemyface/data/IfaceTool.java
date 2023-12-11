package com.noan.takemyface.data;

import android.util.Log;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;

import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;

public class IfaceTool {
    public class SignSuccessRes {
        public String getLocation() {
            return location;
        }

        public String getSignName() {
            return signName;
        }

        private String location;
        private String signName;

        SignSuccessRes(String location, String signName) {
            this.location = location;
            this.signName = signName;
        }
    }

    private CookieStore cookieStore = new CookieStore();

    public void setToken(String token) {
        this.token = token;
    }

    String token = "";



    public Result<String> login(String token) {
        //this.token = token;
        try {
            OkHttpClient httpClient = new OkHttpClient.Builder().followRedirects(false).cookieJar(cookieStore.getCookieJar()).build();
            try {
                JSONObject tokenJs = JSONObject.parse(KeyCrypto.decryptToken(token));
                if(tokenJs.getString("type").equals("org"))
                {
                    cookieStore.addCookie("org.xjtu.edu.cn","cur_appId_","zANcarpyJuM=");
                    cookieStore.addCookie("org.xjtu.edu.cn","open_Platform_User",tokenJs.getString("token"));

                    Request testTokenReq = new Request.Builder().url("https://org.xjtu.edu.cn/openplatform/oauth/auth/getRedirectUrl?userType=1&personNo="+tokenJs.getString("sno")).get().build();
                    Response redirUrlRes = httpClient.newCall(testTokenReq).execute();
                    String resStr=redirUrlRes.body().string();
                    redirUrlRes.close();
                    OrgTool.checkOrgSuccess(resStr);
                    JSONObject resJs = JSONObject.parse(resStr);
                    String ifaceURL = resJs.getString("data");
                    Request getSessionReq = new Request.Builder().url(ifaceURL).get().build();
                    httpClient.newCall(getSessionReq).execute().close();

                    this.token = cookieStore.getCookieValue("iclassface.xjtu.edu.cn", "session");
                    if (this.token==null)
                    {
                        throw new OrgTool.LoginError("Token已过期，请重新生成");
                    }
                }
                else if(tokenJs.getString("type").equals("iface")){
                    this.token=tokenJs.getString("token");
                    cookieStore.addCookie("iclassface.xjtu.edu.cn", "session", this.token);
                }
            } catch (IllegalBlockSizeException | IllegalArgumentException e) {
                throw new OrgTool.LoginError("Token无效，请检查");
            } catch (JSONException e) {
                throw new OrgTool.LoginError("读取Token失败，请重新生成");
            }




            Request testTokenReq = new Request.Builder().url("https://iclassface.xjtu.edu.cn/face/detect").get().build();
            Response res = httpClient.newCall(testTokenReq).execute();
            int statusCode = res.code();
            res.close();
            if (statusCode != 200) {
                Log.w("session check", "invalid session token:" + token);
                throw new OrgTool.LoginError("Token已过期，请重新生成");
            }
            return new Result.Success<String>(cookieStore.getCookieValue("iclassface.xjtu.edu.cn","session"));
        } catch (IOException e) {
            return new Result.Error(new IOException("登录失败，网络错误", e));
        } catch (
                OrgTool.LoginError e) {
            return new Result.Error(e);
        }
    }

    public Result<String> getLocation() {
        try {
            OkHttpClient httpClient = new OkHttpClient.Builder().followRedirects(false).cookieJar(cookieStore.getCookieJar()).build();
            cookieStore.addCookie("iclassface.xjtu.edu.cn", "session", token);
            Request getLocationReq = new Request.Builder().url("https://iclassface.xjtu.edu.cn/wifi/location").get().build();
            Response getLocationRes = httpClient.newCall(getLocationReq).execute();
            String location = JSONObject.parseObject(getLocationRes.body().string()).getString("location");
            if (location.equals("未标定位置"))
            {
                throw new OrgTool.LoginError("无法获取有效位置，请确认连接校园无线网后再签到!");
            }
            getLocationRes.close();
            return new Result.Success<String>(location);
        } catch (IOException e) {
            return new Result.Error(new IOException("请求位置失败，网络错误", e));
        } catch (OrgTool.LoginError e) {
            return new Result.Error(e);
        }
    }

    public Result<SignSuccessRes> signWithFace(String faceBase64) {
        try {
            OkHttpClient httpClient = new OkHttpClient.Builder().followRedirects(false).cookieJar(cookieStore.getCookieJar()).build();
            cookieStore.addCookie("iclassface.xjtu.edu.cn", "session", token);
            RequestBody signBody = new FormBody.Builder().add("base64", faceBase64).build();
            Request signReq = new Request.Builder().url("https://iclassface.xjtu.edu.cn/face/detect").post(signBody).build();
            Response signRes = httpClient.newCall(signReq).execute();
            if (signRes.code() != 200) {
                throw new OrgTool.LoginError("");
            }
            JSONObject sign = JSONObject.parseObject(signRes.body().string());
            signRes.close();
            if(!sign.get("code").equals(0))
                throw new OrgTool.LoginError(sign.getString("msg"));
            JSONObject signInfo = sign.getJSONObject("data");
            return new Result.Success<>(new SignSuccessRes(signInfo.getString("location"),signInfo.getString("username")));
        } catch (IOException e) {
            return new Result.Error(new IOException("签到失败，网络错误", e));
        } catch (OrgTool.LoginError e) {
            return new Result.Error(e);
        }
    }

    public void logout() {
        logout(this.token);
    }

    public void logout(String token) {
        new Thread(() -> {

            OkHttpClient httpClient = new OkHttpClient.Builder().followRedirects(false).cookieJar(cookieStore.getCookieJar()).build();
            cookieStore.addCookie("iclassface.xjtu.edu.cn", "session", token);
            Request logoutReq = new Request.Builder().url("https://iclassface.xjtu.edu.cn/oauth/logout/xjtu").get().build();
            try {
                httpClient.newCall(logoutReq).execute().close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
