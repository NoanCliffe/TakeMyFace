package com.noan.takemyface.data;

import android.util.Log;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.noan.takemyface.data.model.LoggedInUser;
import okhttp3.*;

import java.io.IOException;

import static com.noan.takemyface.data.OrgTool.checkOrgSuccess;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {





    public Result<LoggedInUser> login(String username, String password,boolean publicNetwork) {
        try {
            Log.i("Login info", "Login Start");
            CookieStore cookies = new CookieStore();
            OkHttpClient httpClient = new OkHttpClient.Builder().cookieJar(cookies.getCookieJar()).build();

            String ifaceLoginUrl = "https://org.xjtu.edu.cn/openplatform/oauth/authorize?response_type=code&client_id=1543&redirect_uri=http%3A%2F%2Ficlassface.xjtu.edu.cn%2Foauth%2Fcallback%2Fxjtu&scope=user_info&state=1234&responseType=code&appId=1543&redirectUri=https%3A%2F%2Ficlassface.xjtu.edu.cn%2Foauth%2Fcallback%2Fxjtu";
            Request getSessionIdReq = new Request.Builder()
                    .url(ifaceLoginUrl).get().build();
            Call getSessionID = httpClient.newCall(getSessionIdReq);
            getSessionID.execute().close();
            JSONObject loginData = JSON.parseObject("{'loginType': 1,'jcaptchaCode': ''}");
            loginData.put("username", username);
            loginData.put("pwd", KeyCrypto.orgPasswordEncrypt(password));
            RequestBody loginBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), loginData.toJSONString());
            Request loginReq = new Request.Builder()
                    .url("https://org.xjtu.edu.cn/openplatform/g/admin/login")
                    .post(loginBody).build();
            Response loginRes = httpClient.newCall(loginReq).execute();
            JSONObject loginResJson = JSON.parseObject(loginRes.body().string());
            loginRes.close();
            checkOrgSuccess(loginResJson);
            cookies.addCookie("org.xjtu.edu.cn", "open_Platform_User", loginResJson.getJSONObject("data").getString("tokenKey"));

            Request getInfoReq = new Request.Builder().url("https://org.xjtu.edu.cn/openplatform/g/admin/getUserIdentity?memberId=" + loginResJson.getJSONObject("data").getJSONObject("orgInfo").getString("memberId")).get().build();
            Response info = httpClient.newCall(getInfoReq).execute();
            JSONObject userInfo = JSON.parseObject(info.body().string());
            info.close();
            checkOrgSuccess(userInfo);
            String sno = userInfo.getJSONArray("data").getJSONObject(0).getString("personNo");
            Request getRedirectUrl = new Request.Builder().url("https://org.xjtu.edu.cn/openplatform/oauth/auth/getRedirectUrl?userType=1&personNo=" + sno).get().build();
            Response redirectUrl = httpClient.newCall(getRedirectUrl).execute();
            JSONObject redirectUrlResJs = JSON.parseObject(redirectUrl.body().string());
            redirectUrl.close();
            if(publicNetwork)
            {
                String sessionToken="";
                JSONObject tokenRes = new JSONObject();
                tokenRes.put("type","org");
                tokenRes.put("sno",sno);
                tokenRes.put("token",loginResJson.getJSONObject("data").getString("tokenKey"));
                LoggedInUser User =
                        new LoggedInUser(
                                KeyCrypto.encryptToken(tokenRes.toString()),
                                sno);
                return new Result.Success<>(User);
            }


            checkOrgSuccess(redirectUrlResJs);
            String ifaceURL = redirectUrlResJs.getString("data");
            Request getSessionReq = new Request.Builder().url(ifaceURL).get().build();
            httpClient.newCall(getSessionReq).execute().close();
            String sessionToken = cookies.getCookieValue("iclassface.xjtu.edu.cn", "session");
            JSONObject tokenRes = new JSONObject();
            tokenRes.put("type","iface");
            tokenRes.put("token",sessionToken);
            LoggedInUser User =
                    new LoggedInUser(
                            KeyCrypto.encryptToken(tokenRes.toString()),
                            sno);
            return new Result.Success<>(User);
        } catch (IOException e) {
            return new Result.Error(new IOException("登录失败，网络错误", e));
        } catch (OrgTool.LoginError e) {
            return new Result.Error(e);
        } catch (JSONException e)
        {
            return new Result.Error(new OrgTool.LoginError("系统错误，请重试"));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}