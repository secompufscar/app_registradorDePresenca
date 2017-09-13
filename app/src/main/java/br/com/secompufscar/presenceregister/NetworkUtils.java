package br.com.secompufscar.presenceregister;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import br.com.secompufscar.presenceregister.data.DataBase;
import br.com.secompufscar.presenceregister.data.Presenca;


public class NetworkUtils {
    public static boolean CONNECTED = false;

    public static final String BASE_URL = "https://secompufscar.com.br/";
    public static final String API_PATH = BASE_URL + "api/";
    public static final String POST_PATH = "https://beta2.secompufscar.com.br/area-administrativa/api/registrar-presenca/";

    public static URL buildUrl(String path) {

        Uri builtUri = Uri.parse(path).buildUpon().build();

        URL url = null;

        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static boolean updateConnectionState(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);

        //For WiFi Check
        boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .isConnectedOrConnecting();

        boolean is3g = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                .isConnectedOrConnecting();

        CONNECTED = isWifi || is3g;

        return CONNECTED;
    }

    @Nullable
    public static String getResponseFromHttpUrl(URL url, Context context) throws IOException {
        if (updateConnectionState(context)) {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                InputStream in = urlConnection.getInputStream();

                Scanner scanner = new Scanner(in);
                scanner.useDelimiter("\\A");

                boolean hasInput = scanner.hasNext();
                if (hasInput) {
                    return scanner.next();
                } else {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            } finally {
                urlConnection.disconnect();
            }
        } else {
            return "";
        }
    }

    public static boolean hostIsAvailable(Context context) {
        if (updateConnectionState(context)) {
            Runtime runtime = Runtime.getRuntime();
            try {
                Process ipProcess = runtime.exec("/system/bin/ping -c 1 " + BASE_URL);
                int exitValue = ipProcess.waitFor();
                return (exitValue == 0);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    static class PostResponse {
        public int status_code;
        public String message;

        public PostResponse() {
            status_code = 0;
            message = null;
        }
    }

    public static PostResponse postPresenca(Context context, Presenca presenca) {
        PostResponse responsePost = new PostResponse();
        if (updateConnectionState(context)) {
            String charset = "UTF-8";
            OutputStream output = null;

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("id_atividade", presenca.getIdAtividade());
                jsonObject.accumulate("id_inscricao", presenca.getIdParticipante());
                jsonObject.accumulate("timestamp", presenca.getHorario());
                jsonObject.accumulate("api_key", context.getResources().getString(R.string.API_KEY));

                String query = jsonObject.toString();

                URL post_url = buildUrl(POST_PATH);

                if (post_url != null) {
                    HttpURLConnection connection = (HttpURLConnection) post_url.openConnection();

                    connection.setDoOutput(true); // Triggers POST.
                    connection.setRequestProperty("Accept-Charset", charset);
                    connection.setRequestProperty("Content-Type", "application/json");

//                    connection.setConnectTimeout(2000);

                    output = connection.getOutputStream();
                    output.write(query.getBytes(charset));

                    responsePost.status_code = connection.getResponseCode();

                    if (responsePost.status_code == 200) {
                        InputStream response = connection.getInputStream();
                        Scanner scanner = new Scanner(response);
                        responsePost.message = scanner.useDelimiter("\\A").next();
                    }
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                Log.d("teste timeoutexception", e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("teste ioexception", e.toString());
            } catch (JSONException e) {
                Log.d("teste jsonexception", e.toString());
            } finally {
                try {
                    if (output != null)
                        output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return responsePost;
    }

    public static ArrayList<Presenca> postAllPresencas(Context context) {
        if (updateConnectionState(context)) {
            ArrayList<Presenca> presencas = DataBase.getDB().getAllEntries();
            ArrayList<Presenca> erros = new ArrayList<>();
            for (Presenca presenca : presencas) {
                Log.d("teste presenca", presenca.toString());
                PostResponse response = postPresenca(context, presenca);
                Log.d("teste satuscode", String.valueOf(response.status_code));

                if (response.status_code == 200) {
                    DataBase.getDB().deleteEntry(presenca.getId());
                } else if (response.status_code == 404){
                    erros.add(presenca);
                    DataBase.getDB().deleteEntry(presenca.getId());
                }
            }
            return erros;
        } else {
            return null;
        }
    }
}
