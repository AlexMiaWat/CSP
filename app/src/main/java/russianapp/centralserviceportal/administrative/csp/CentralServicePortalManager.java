package russianapp.centralserviceportal.administrative.csp;

import android.os.AsyncTask;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Response;
import org.asynchttpclient.netty.NettyResponse;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.Arrays;
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
    String token = "", headerDate, ContentType = "text/html; charset=utf-8", content = "";
    String lastVersion = "", exception = "";

    String requestLink;

    public CentralServicePortalManager(MainActivity context, String serviceProvider, String application) {
        this.context = context;
        this.application = application;
        this.serviceProvider = serviceProvider;

        requestLink = serviceProvider + "/" + "CSP/hs/app/connections/firstConnection" + "?application=" + application;

        // device id
        OpenUDID_manager.sync(context);

        asyncHttpClient = asyncHttpClient();
    }

    public void doServiceTask(String action) {
        doTask doTask = new doTask();
        doTask.execute(new String[]{action});
    }

    class doTask extends AsyncTask<String[], String, String> {
        @Override
        protected String doInBackground(String[]... params) {
            String result = "";
            content = ""; exception = "";

            if (Arrays.toString(params[0]).contains("firstConnection")) {
                try {
                    String currentLink = requestLink;

                    Map<String, String> map = IdentificationData.getAppInfo(context);
                    currentLink += IdentificationData.paramsToString(map);

                    map = IdentificationData.getDeviceInfo();
                    currentLink += IdentificationData.paramsToString(map);

                    currentLink += "&comment=" + "Start app action";

                    result = firstConnection(currentLink);
                } catch (Exception e) {
                    exception = e.getMessage();
                    e.printStackTrace();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (content.length() == 0)
                content = "<html><body>" +
                        "<h1>No content! Probably no connection...</h1>" +
                        "<h2>" + exception + "</h2>" +
                        "</body></html>";

            String webData = content;
            webData = webData.replace("#", "%23");
            webData = webData.replace("%", "%25");
            webData = webData.replace("\\", "%27");
            webData = webData.replace("?", "%3f");

            context.webView.loadData(webData, ContentType, "UTF-8");
        }
    }

    public String firstConnection(String requestLink) throws ExecutionException, InterruptedException, ParseException {

        BoundRequestBuilder request = asyncHttpClient.prepareGet(requestLink);

        Future<Response> responseFuture = request.execute();
        NettyResponse nettyResponse = (NettyResponse) responseFuture.get();

        boolean result = (nettyResponse.getStatusCode() == 200);
        headerDate = nettyResponse.getHeader("Date");
        ContentType = nettyResponse.getHeader("Content-Type");

        content = nettyResponse.getResponseBody();
        if (nettyResponse.hasResponseBody() && ContentType.contains("json")) {
            JSONParser parser = new JSONParser();
            org.json.simple.JSONObject item = (org.json.simple.JSONObject) parser.parse(content.trim());

            result = Boolean.parseBoolean(Objects.requireNonNull(item.get("result")).toString());
            String action = Objects.requireNonNull(Objects.requireNonNull(item.get("action"))).toString();
            String serverDate = Objects.requireNonNull(Objects.requireNonNull(item.get("serverDate"))).toString();
            token = Objects.requireNonNull(Objects.requireNonNull(item.get("token"))).toString();
            lastVersion = Objects.requireNonNull(Objects.requireNonNull(item.get("lastVersion"))).toString();
        }

        if (result)
            return "firstConnection | token:" + token + " | lastVersion: " + lastVersion;
        else
            return "firstConnection | error:" + content;
    }

    public void closeConnection() {
        try {
            asyncHttpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
