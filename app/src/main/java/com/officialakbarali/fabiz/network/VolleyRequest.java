package com.officialakbarali.fabiz.network;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.Map;

public class VolleyRequest extends StringRequest {
   // private static final String PRE_LOGIN_URL = "https://aliakbar9697.000webhostapp.com/";
    private static final String PRE_LOGIN_URL = "http://efabiz.com/app/";
    private Map<String, String> parameters;

    public VolleyRequest(String PASSED_URL, Map<String, String> passedParameters, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.POST, PRE_LOGIN_URL + PASSED_URL, listener, errorListener);
        parameters = passedParameters;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return parameters;
    }
}
