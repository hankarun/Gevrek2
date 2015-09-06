package com.hankarun.gevrek.helpers;

import android.os.AsyncTask;
import android.util.Log;

import com.hankarun.gevrek.interfaces.AsyncResponse;

import org.apache.commons.net.nntp.Article;
import org.apache.commons.net.nntp.NNTPClient;
import org.apache.commons.net.nntp.NewsgroupInfo;
import org.apache.commons.net.nntp.SimpleNNTPHeader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class NNTPHelper {
    public AsyncResponse asyncResponse;
    String username;
    String password;

    public NNTPHelper(String _name, String _pass){
        username = _name;
        password = _pass;
    }

    public void deleteArticle(String articleNumber,String sender, String mGroup, String mFrom){
        new NNTPConnect1().execute(articleNumber,sender,mGroup,mFrom);
    }

    class NNTPConnect1 extends AsyncTask<String, Boolean, Boolean> {

        protected Boolean doInBackground(String... urls) {
            NNTPClient client = new NNTPClient();


            try {
                client.setSocketFactory(new MySSLSocketFactory());
                client.connect("news.ceng.metu.edu.tr",563);
                if(!client.authenticate(username, password)) {
                    Log.d("test","false");
                    return false;

                }

                //Retrive article.
                NewsgroupInfo group = new NewsgroupInfo();
                client.selectNewsgroup(urls[2], group);
                Reader reader = client.retrieveArticleInfo(Integer.decode(urls[0]));
                int data = reader.read();
                String datas = "";
                while(data != -1){
                    datas += (char) data;
                    data = reader.read();
                }
                Pattern pattern = Pattern.compile("\\<(.*?)\\>");
                Matcher matcher = pattern.matcher(datas);
                matcher.find();
                String articleid = "<" + matcher.group(1) + ">";
                SimpleNNTPHeader header1;
                header1 = new SimpleNNTPHeader(urls[3], "cancel " + articleid);
                header1.addNewsgroup(urls[2]);
                header1.addHeaderField("Control", "cancel " + articleid);
                header1.addHeaderField("Sender", urls[1]);
                    if (client.isAllowedToPost()) {
                        Writer writer = client.postArticle();

                        if (writer != null) {
                            writer.write(header1.toString());
                            writer.close();
                            if (client.completePendingCommand())
                             return true;
                        }
                    }
                } catch (Exception e) {
                    Log.d("nntp", e.getMessage());
                }
            return true;
        }

        protected void onPostExecute(Boolean feed) {
            asyncResponse.onResponse(feed);
        }
    }


    public void checkCreds(){
        new NNTPConnect().execute();
    }

    class NNTPConnect extends AsyncTask<String, Boolean, Boolean> {

        protected Boolean doInBackground(String... urls) {
            NNTPClient client = new NNTPClient();


            try{
                client.setSocketFactory(new MySSLSocketFactory());
                client.connect("news.ceng.metu.edu.tr",563);
                if(!client.authenticate(username, password)) {
                    Log.d("test","false");
                    return false;

                }
            }catch (Exception e){
                Log.d("nntp", e.getMessage());
            }
            return true;
        }

        protected void onPostExecute(Boolean feed) {
            asyncResponse.onResponse(feed);
        }
    }

    public class MySSLSocketFactory extends SSLSocketFactory {

        private final SSLContext sslContext = SSLContext.getInstance("TLS");

        public MySSLSocketFactory() throws Exception {

            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
            sslContext.init(null, trustAllCerts, null);
        }

        public SSLContext getSllContext() {
            return sslContext;
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port,
                                   boolean autoClose) throws IOException, UnknownHostException {
            Socket result = sslContext.getSocketFactory().createSocket(socket,
                    host, port, autoClose);
            //log.debug("Configuring SSLSocket for SSLv3 protocol only");
            ((SSLSocket) result).setEnabledProtocols(new String[] { "SSLv3" });
            ((SSLSocket) result).setUseClientMode(true);
            return result;
        }

        @Override
        public Socket createSocket() throws IOException {
            Socket result = sslContext.getSocketFactory().createSocket();
            //log.debug("Configuring SSLSocket for SSLv3 protocol only");
            ((SSLSocket) result).setEnabledProtocols(new String[] { "SSLv3" });
            ((SSLSocket) result).setUseClientMode(true);
            return result;
        }

        @Override
        public String[] getDefaultCipherSuites() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String[] getSupportedCipherSuites() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Socket createSocket(String host, int port)
                throws IOException, UnknownHostException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Socket createSocket(InetAddress host, int port)
                throws IOException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Socket createSocket(String host, int port,
                                   InetAddress localHost, int localPort) throws IOException,
                UnknownHostException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Socket createSocket(InetAddress address, int port,
                                   InetAddress localAddress, int localPort) throws IOException {
            // TODO Auto-generated method stub
            return null;
        }
    }


}
