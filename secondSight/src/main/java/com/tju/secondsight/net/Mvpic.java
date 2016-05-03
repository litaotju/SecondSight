package com.tju.secondsight.net;

import com.tju.secondsight.net.Babe;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class Mvpic{
	/*
     * 获取网址url
     */
	static private final String TAG = "Mvpic"; 
    static public Babe getMeinv(){
    	//api store 网址
    	String httpUrl = "http://apis.baidu.com/txapi/mvtp/meinv";
    	String httpArg = "num=1";
    	String jsonResult = request(httpUrl, httpArg);
    	try {
	    	JSONArray listObj  = new JSONArray(
	    				(new JSONObject(jsonResult)).getString("newslist"));
	    	JSONObject firstObj = listObj.getJSONObject(0);
	    	String title = firstObj.getString("title");
	    	if(title.length() > 10){
	    		title = title.substring(0, 9);
	    	}
	    	return new Babe(listObj.getJSONObject(0).getString("url"), title);
		} catch (Exception e) {
			Log.d(TAG, "Json obj parse funcked.");
			e.printStackTrace();
			return null;
		}
    }
    
    /*
     * 接受url和参数，返回
     */
    static private String request(String httpUrl, String httpArg) {
        BufferedReader reader = null;
        String result = null;
        StringBuffer sbf = new StringBuffer();
        httpUrl = httpUrl + "?" + httpArg;
        try {
            URL url = new URL(httpUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            // 填入apikey到HTTP header
            connection.setRequestProperty("apikey",  "523a5f894b8872d72022a5156bf12606");
            //连接网络的最主要方法
            connection.connect();
            InputStream is = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sbf.append(strRead);
                sbf.append("\r\n");
            }
            reader.close();
            result = sbf.toString();
        } catch (Exception e) {
        	Log.d(TAG, "request fucked");
            e.printStackTrace();
        }
        return result;
    }
}