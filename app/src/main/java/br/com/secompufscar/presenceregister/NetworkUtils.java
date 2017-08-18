package br.com.secompufscar.presenceregister;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Scanner;

import br.com.secompufscar.presenceregister.data.DataBase;
import br.com.secompufscar.presenceregister.data.Presenca;


public class NetworkUtils {
    public static boolean CONNECTED = false;

    public static String LICENSE_KEY;
    public static final String BASE_URL = "https://secompufscar.com.br/";
    public static final String API_PATH = "api/";
    public static final String POST_PATH = API_PATH;

    public static void inicializeNetworkUtils(String licenseKey){
        LICENSE_KEY = licenseKey;
    }

    public static URL buildUrl(String path) {

        Uri builtUri = Uri.parse(BASE_URL + path).buildUpon().build();

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
                return null;
            } finally {
                urlConnection.disconnect();
            }
        } else {
            return null;
        }
    }

    public static boolean hostIsAvailable(Context context) {

        if(updateConnectionState(context)){
            Log.d("teste","Conectado");

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

    public static String postPresenca(Context context, Presenca presenca) {

        if (updateConnectionState(context)) {
            String charset = "UTF-8";
            String query;
            String responseBody = null;
            OutputStream output = null;

            try {

                query = String.format("id_participante=%s&id_atividade=%s&horario=%s",
                        URLEncoder.encode(presenca.getIdParticipante(), charset),
                        URLEncoder.encode(presenca.getIdAtividade(), charset),
                        URLEncoder.encode(presenca.getHorario(), charset));

                Log.d("TESTE query", query);

                URL post_url = buildUrl(POST_PATH);

                if (post_url != null) {
                    HttpURLConnection connection = (HttpURLConnection) post_url.openConnection();

                    connection.setDoOutput(true); // Triggers POST.
                    connection.setRequestProperty("Accept-Charset", charset);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
                    connection.setConnectTimeout(2000);

                    output = connection.getOutputStream();
                    output.write(query.getBytes(charset));

                    InputStream response = connection.getInputStream();
                    Scanner scanner = new Scanner(response);
                    responseBody = scanner.useDelimiter("\\A").next();

                    Log.d("httpconnection", responseBody);

                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (SocketTimeoutException e) {
                Log.d("httpconnectionesocket", e.getMessage());
                responseBody = "timeOutException";
            } catch (IOException e) {
                Log.d("httpconnectionerror3", e.getMessage());
                responseBody = "timeOutException";
            } finally {
                try {
                    if (output != null)
                        output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return responseBody;
        } else {
            return null;
        }
    }

    public static boolean postAllPresencas(Context context) {
        if (updateConnectionState(context)) {

            ArrayList<Presenca> presencas = DataBase.getDB().getAllEntries();
            for (Presenca presenca : presencas) {
                postPresenca(context, presenca);
                // TODO: Verificar se deu certo e excluir;
                DataBase.getDB().deleteEntry(presenca.getId());
            }

            //TODO: Tratar isso
//        uploadPD = ProgressDialog.show(context, "Sincronizando registros de presenças", "");
//        uploadPD.setMax(cursor.getCount());
//        uploadPD.setProgress(0);
            return true;

        } else {
            return false;
        }

    }
}
