package russianapp.centralserviceportal.administrative.csp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.codec.digest.DigestUtils;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Response;
import org.asynchttpclient.netty.NettyResponse;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import russianapp.centralserviceportal.administrative.MainActivity;

import static org.asynchttpclient.Dsl.asyncHttpClient;

public class CentralServicePortalManager {

    MainActivity context;
    AsyncHttpClient asyncHttpClient;
    String serviceProvider, application;
    String token = "", cspToken = "", headerDate, ContentType = "text/html; charset=utf-8", content = "";
    String lastVersion = "", exception = "";

    Map<String, String> mapAppInfo = new HashMap<>();
    Map<String, String> mapDeviceInfo = new HashMap<>();

    String requestLink;

    public CentralServicePortalManager(MainActivity context, String serviceProvider, String application) {
        this.context = context;
        this.application = application;
        this.serviceProvider = serviceProvider;

        requestLink = serviceProvider + "/" + "CSP/hs/app/connections/";

        // device id
        OpenUDID_manager.sync(context);

        asyncHttpClient = asyncHttpClient();
    }

    public void doServiceTask(String action) {
        doTask doTask = new doTask();
        doTask.execute(action);
    }

    String firstConnection(String requestLink) throws ExecutionException, InterruptedException, ParseException {

        BoundRequestBuilder request = asyncHttpClient.prepareGet(requestLink);
        request.addHeader("cspToken", DigestUtils.md5Hex(cspToken));

        Future<Response> responseFuture = request.execute();
        NettyResponse nettyResponse = (NettyResponse) responseFuture.get();

        boolean result = (nettyResponse.getStatusCode() == 200);
        headerDate = nettyResponse.getHeader("Date");
        ContentType = nettyResponse.getHeader("Content-Type");

        content = nettyResponse.getResponseBody();
        if (nettyResponse.hasResponseBody() && ContentType.contains("json")) {
            JSONParser parser = new JSONParser();
            org.json.simple.JSONObject item = (org.json.simple.JSONObject) parser.parse(content);

            result = Boolean.parseBoolean(Objects.requireNonNull(item.get("result")).toString());
            String action = Objects.requireNonNull(Objects.requireNonNull(item.get("action"))).toString();
            String serverDate = Objects.requireNonNull(Objects.requireNonNull(item.get("serverDate"))).toString();
            token = Objects.requireNonNull(Objects.requireNonNull(item.get("token"))).toString();
            lastVersion = Objects.requireNonNull(Objects.requireNonNull(item.get("lastVersion"))).toString();
        }

        if (result) return "firstConnection | token:" + token + " | lastVersion: " + lastVersion;
        else return "firstConnection | error:" + content;
    }

    String getData(String requestLink) throws ExecutionException, InterruptedException, ParseException {

        BoundRequestBuilder request = asyncHttpClient.prepareGet(requestLink);
        request.addHeader("cspToken", DigestUtils.md5Hex(cspToken));

        Future<Response> responseFuture = request.execute();
        NettyResponse nettyResponse = (NettyResponse) responseFuture.get();

        boolean result = (nettyResponse.getStatusCode() == 200);
        headerDate = nettyResponse.getHeader("Date");
        ContentType = nettyResponse.getHeader("Content-Type");

        content = nettyResponse.getResponseBody();
        if (nettyResponse.hasResponseBody() && ContentType.contains("json")) {
            JSONParser parser = new JSONParser();
            org.json.simple.JSONObject item = (org.json.simple.JSONObject) parser.parse(content);

            result = Boolean.parseBoolean(Objects.requireNonNull(item.get("result")).toString());
            token = Objects.requireNonNull(Objects.requireNonNull(item.get("token"))).toString();
        }

        if (result) return "getData | token:" + token + " | lastVersion: " + lastVersion;
        else return "getData | error:" + content;
    }

    String putData(String requestLink) throws ExecutionException, InterruptedException, ParseException {

        BoundRequestBuilder request = asyncHttpClient.prepareGet(requestLink);
        request.addHeader("cspToken", DigestUtils.md5Hex(cspToken));
        request.addHeader("Content-Type", "application/json; charset=UTF-8");

        String postingData = generatePostingData();
        request.setBody(postingData);

        Future<Response> responseFuture = request.execute();
        NettyResponse nettyResponse = (NettyResponse) responseFuture.get();

        boolean result = (nettyResponse.getStatusCode() == 200);
        headerDate = nettyResponse.getHeader("Date");
        ContentType = nettyResponse.getHeader("Content-Type");

        content = nettyResponse.getResponseBody();
        if (nettyResponse.hasResponseBody() && ContentType.contains("json")) {
            JSONParser parser = new JSONParser();
            org.json.simple.JSONObject item = (org.json.simple.JSONObject) parser.parse(content);

            result = Boolean.parseBoolean(Objects.requireNonNull(item.get("result")).toString());
            token = Objects.requireNonNull(Objects.requireNonNull(item.get("token"))).toString();
        }

        if (result) return "getData | token:" + token + " | lastVersion: " + lastVersion;
        else return "getData | error:" + content;
    }

    String getCspToken(Map<String, String> map0, Map<String, String> map) {

        String result = "";
        result += map0.get("serverHour");
        result += map.get("sdk");
        result += map.get("release");
        result += map.get("brand");
        result += map.get("device");
        result += map.get("model");
        result += map.get("manufacturer");
        result += map.get("product");
        result += map.get("board");
        result += map.get("display");
        result += map.get("hardware");
        result += map.get("host");
        result += map.get("id");
        return result;
    }

    void getLastPackage(String packageName) {

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.parse("market://details?id=" + packageName);
        intent.setData(uri);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            uri = Uri.parse("https://play.google.com/store/apps/details?id=" + packageName);
            intent.setData(uri);
            context.startActivity(intent);
        }
    }

    String addParamsToHttpString() {
        String currentLink = "";

        mapAppInfo = IdentificationData.getAppInfo(context);
        currentLink += IdentificationData.paramsToString(mapAppInfo);

        mapDeviceInfo = IdentificationData.getDeviceInfo();
        currentLink += IdentificationData.paramsToString(mapDeviceInfo);

        cspToken = getCspToken(mapAppInfo, mapDeviceInfo);

        return currentLink;
    }

    String generatePostingData() {
        return "{ \"object\" : \"material\"" +
                "\"data\" :  [" +
                "{ \"table\" : \"Ads\"" +
                "  \"id\" : \"456123789\"" +
                "  \"user\" : \"\"" +
                "  \"itemDescription\" : \"Сеть быстрого питания Lanzhou набирает курьеров. Со своим авто!\"" +
                "  \"itemPreview\" : \"Курьер по доставке еды 100 000 - 150 000 тг./За месяц\"" +
                "  \"itemTitle\" : \"Курьер по доставке еды\"" +
                "  \"itemLink\" : \"https://www.olx.kz/obyavlenie/kurer-po-dostavke-edy-IDhW0oQ.html#b0b3087493\"" +
                "  \"price\" : \"100 000 - 150 000 тг./За месяц\"" +
                "  \"phone\" : \"+77020008600\"" +
                "  \"date\" : \"2020-06-01T08:31:00Z\"" +
                "  \"smallImage\" : \"nothing\"" +
                "  \"fullImage\" : \"nothing\"" +
                "  \"Category_id\" : \"4.1\"" +
                "  \"source\" : \"olx.kz\"" +
                "  \"enabled\" : \"true\"" +
                "  \"favorites\" : \"false\"" +
                "  \"favoriteDate\" : \"2020-06-01T10:31:10Z\"" +
                "}," +
                "{ \"table\" : \"Ads\"" +
                "  \"id\" : \"485123789\"" +
                "  \"user\" : \"\"" +
                "  \"itemDescription\" : \"Джинсовые, плотные шорты. Состояние хорошее. Размер s. В талии 34 см.\"" +
                "  \"itemPreview\" : \"Продам шорты 3 000 тг.\"" +
                "  \"itemTitle\" : \"Продам шорты\"" +
                "  \"itemLink\" : \"https://www.olx.kz/obyavlenie/prodam-shorty-IDiM7XD.html#2fc84d4b81\"" +
                "  \"price\" : \"3 000 тг.\"" +
                "  \"phone\" : \"+77020008600\"" +
                "  \"date\" : \"2020-06-01T08:31:00Z" +
                "\"" +
                "  \"smallImage\" : \"https://frankfurt.apollo.olxcdn.com/v1/files/u1un74y2sirt2-KZ/image;s=644x461;r=180\"" +
                "  \"fullImage\" : \"https://frankfurt.apollo.olxcdn.com/v1/files/u1un74y2sirt2-KZ/image;s=1000x700;r=180\"" +
                "  \"Category_id\" : \"5.3\"" +
                "  \"source\" : \"olx.kz\"" +
                "  \"enabled\" : \"true\"" +
                "  \"favorites\" : \"true\"" +
                "  \"favoriteDate\" : \"0001-01-01T00:00:00Z\"" +
                "}," +
                "]" +
                "}";
    }

    public void closeConnection() {
        try {
            asyncHttpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class doTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String action = params[0];
            content = "";
            exception = "";
            double startTime = System.nanoTime();

            Snackbar snackbar = Snackbar.make(context.navigationView, String.format("%s processing...", action), Snackbar.LENGTH_INDEFINITE);
            snackbar.show();

            if (action.contains("firstConnection")) {
                try {
                    String currentLink = requestLink + "firstConnection?application=" + application;
                    currentLink += addParamsToHttpString();
                    currentLink += "&comment=" + "Start app action";

                    String result = firstConnection(currentLink);

                    // Update app
                    if (Integer.parseInt(lastVersion) != Integer.parseInt(Objects.requireNonNull(mapAppInfo.get("appVersion"))))
                        Snackbar.make(context.navigationView, "Please update app to the latest version on Google Play Market", Snackbar.LENGTH_LONG)
                                .setAction("Update now", v -> {
                                    getLastPackage(mapAppInfo.get("packageName"));
                                }).show();
                } catch (Exception e) {
                    exception = e.getMessage();
                    e.printStackTrace();
                }
            }

            if (action.contains("getData")) {
                try {
                    String currentLink = requestLink + "getData?application=" + application;
                    currentLink += addParamsToHttpString();
                    currentLink += "&token=" + token;
                    currentLink += "&table=" + "Ads";
                    currentLink += "&comment=" + "getting data from csp action";

                    String result = getData(currentLink);
                } catch (Exception e) {
                    exception = e.getMessage();
                    e.printStackTrace();
                }
            }

            if (action.contains("putData")) {
                try {
                    String currentLink = requestLink + "putData?application=" + application;
                    currentLink += addParamsToHttpString();
                    currentLink += "&token=" + token;
                    currentLink += "&comment=" + "putting data to csp from app";

                    String result = putData(currentLink);
                } catch (Exception e) {
                    exception = e.getMessage();
                    e.printStackTrace();
                }
            }

            snackbar.dismiss();
            double endTime = System.nanoTime();
            double duration = (endTime - startTime) / 1000000 / 1000;
            DecimalFormat df = new DecimalFormat("#.####");
            return String.format("%s processing time: %s seconds", action, df.format(duration));
        }

        @Override
        protected void onPostExecute(String result) {
            context.timer.setText(result);

            if (content.length() == 0) {
                ContentType = "text/html; charset=utf-8";
                content = "<html><body>" +
                        "<h1>No content! Probably no connection...</h1>" +
                        "<h2>" + exception + "</h2>" +
                        "</body></html>";
            }

            if (exception.length() != 0) {
                ContentType = "text/html; charset=utf-8";
                content = "<html><body>" +
                        "<h1>Error! Probably bad answer format...</h1>" +
                        "<h2>" + exception + "</h2>" +
                        "<a>" + content + "</a>" +
                        "</body></html>";
            }

            String webData = content;
            webData = webData.replace("#", "%23");
            webData = webData.replace("%", "%25");
            webData = webData.replace("\\", "%27");
            webData = webData.replace("?", "%3f");

            context.webView.loadData(webData, ContentType, "UTF-8");
        }
    }
}
