package user.example.com.tozandatacollectapp.CameraRemote;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import user.example.com.tozandatacollectapp.R;

public class Device{
    String endPointUrl;
    HttpSendJSON sendJson;

    public Device(String endPointUrl){
        this.endPointUrl = endPointUrl;
        sendJson = new HttpSendJSON(endPointUrl);
    }


    public boolean startRecMode(){
        return isSuccess("startRecMode");
    }


    public String getEvent(){
        String responce = sendJson.post("getEvent", "false");
        return responce;
    }


    public String takePicture(){

        String responce = sendJson.post("actTakePicture");

        if(responce.contains("error") && responce.contains("40403")){

            responce = sendJson.post("awaitTakePicture");

        }

        return getUrl(responce);
    }


    public String startLiveview(){

        String responce = sendJson.post("startLiveview");

        return getUrl(responce);
    }


    public boolean stopLiveview(){
        return isSuccess("stopLiveview");
    }


    public boolean setFocusMode(String focusMode){
        String responce = sendJson.post("setFocusMode", "\"" + focusMode + "\"");
        return responce.contains("\"result\"");
    }


    public HashMap<String, Boolean> getAvailableFocusMode(){
        String responce = sendJson.post("getAvailableFocusMode");

        HashMap<String, Boolean> value = new HashMap<>();
        if(hasResult(responce)){
            Pattern result = Pattern.compile("\\[.*\\]");
            Matcher matcher = result.matcher(responce);
            if(matcher.find()){
                String resultStr = matcher.group();
                resultStr = resultStr.substring(1, resultStr.length() - 1);
                matcher = result.matcher(resultStr);
                if(matcher.find()){
                    String listStr = matcher.group();
                    String current = resultStr.replace(listStr, "").replaceAll("[,\"]", "");
                    System.out.println("current:" + current);
                    listStr = listStr.trim().substring(1, listStr.length() - 1);
                    for(String part : listStr.split(",")){
                        String mode = part.substring(part.indexOf("\""), part.lastIndexOf("\""));
                        value.put(mode, mode.equals(current));
                    }
                    System.out.println("value:" + value.toString());
                }
            }
        }
        return value;
    }


    public String getUrl(String responce){
        System.out.println("getUrl:" + responce);
        Pattern element = Pattern.compile("http:\\/\\/[^\"]+");
        Matcher matcher = element.matcher(responce);

        if(matcher.find())
            return matcher.group();
        return null;
    }


    public boolean hasResult(String responce){
        return responce.contains("\"result\"");
    }


    public boolean isSuccess(String method){
        String responce = sendJson.post(method);
        return hasResult(responce);
    }

    public static List<String> downloadHtml(String url,String charset){
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        ArrayList<String> lineList = new ArrayList<String>();
        try {
            //Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.anan-nct.ac.jp", 8080));
            URLConnection conn = new URL(url).openConnection();
            is = conn.getInputStream();
            isr = new InputStreamReader(is,charset);
            br = new BufferedReader(isr);

            String line = null;
            while((line = br.readLine()) != null) {
                lineList.add(line);
            }
            return lineList;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }finally {
            try {
                br.close();
            }catch(Exception e) {
            }
            try {
                isr.close();
            }catch(Exception e) {
            }
            try {
                is.close();
            }catch(Exception e) {
            }
        }
    }

    public static String getEndpointUrl(String srcUrl){

        String src = "";
        for(String s : downloadHtml(srcUrl, "UTF-8")){
            src += s;
        }

        XMLParser parser = new XMLParser(src);

        boolean loop = true;
        String endPointUrl = null;

        while(parser.hasNextData() && loop){
            int state = parser.nextData();
            if(state == XMLParser.STATE_TAG_OPEN){
                if(parser.getTagName().equals("av:X_ScalarWebAPI_ServiceType")){
                    while(parser.nextData() != XMLParser.STATE_TEXT);
                    String api = parser.getStr();
                    if(api.equals("camera")){
                        while(parser.nextData() != XMLParser.STATE_TEXT);
                        endPointUrl = parser.getStr() + "/" + api;
                        System.out.println("endpoint:" + endPointUrl == null ? "null" : endPointUrl);
                        loop = false;
                    }
                }
            }
        }
        return endPointUrl;
    }

    public boolean isIDLE(){
        String event = getEvent();
        if(event == null) return false;
        return event.contains("\"IDLE\"");
    }

    public static void sleep(int msec){
        System.out.println("wait:" + msec);
        try{
            Thread.sleep(5000);
        }catch(InterruptedException e){

        }
        System.out.println("restart");
    }

    public interface OnLoadFinishCallBack {
        void onLoadFinish(Bitmap bmp);
    }
    public static File getImg(final String inputData, final Context context, final OnLoadFinishCallBack callBack) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(inputData);

                    HttpURLConnection conn =
                            (HttpURLConnection) url.openConnection();
                    conn.setAllowUserInteraction(false);
                    conn.setInstanceFollowRedirects(true);
                    conn.setRequestMethod("GET");
                    conn.connect();

                    int httpStatusCode = conn.getResponseCode();

                    if(httpStatusCode != HttpURLConnection.HTTP_OK){
                        throw new Exception();
                    }

                    File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), context.getString(R.string.app_name));
                    if(!dir.exists()) dir.mkdirs();

                    File out = new File(dir, System.currentTimeMillis() + ".jpeg");

                    // Input Stream
                    DataInputStream dataInStream
                            = new DataInputStream(
                            conn.getInputStream());

                    Bitmap bitmap = null;
                    bitmap = BitmapFactory.decodeStream(dataInStream);

                    if(callBack != null)
                        callBack.onLoadFinish(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        return null;
    }


}