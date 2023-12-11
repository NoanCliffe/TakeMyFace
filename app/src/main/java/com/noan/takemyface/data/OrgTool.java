package com.noan.takemyface.data;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;

public class OrgTool {
    public static class LoginError extends Exception {
        public LoginError() {
            super();
        }

        public LoginError(String message) {
            super(message);
        }
    }
    public static void checkOrgSuccess(String res) throws LoginError {
        JSONObject resjs;
        try {
            resjs = JSONObject.parse(res);
        }catch (JSONException e)
        {
            throw new LoginError("系统错误，请重试");
        }
        checkOrgSuccess(resjs);
    }
    public static void checkOrgSuccess(JSONObject res) throws LoginError {
        if (!res.get("code").equals(0)) {
            throw new LoginError(res.getString("message"));
        }
    }
}
