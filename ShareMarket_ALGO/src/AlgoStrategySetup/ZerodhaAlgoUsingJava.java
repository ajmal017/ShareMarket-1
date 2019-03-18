package AlgoStrategySetup;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ProtocolException;
import java.net.URL;

public class ZerodhaAlgoUsingJava {

    private static HttpURLConnection con;

    public static void main(String[] args) throws MalformedURLException,
            ProtocolException, IOException {

        String url = "https://kite.zerodha.com/api/portfolio/holdings";

        try {

            URL myurl = new URL(url);
            con = (HttpURLConnection) myurl.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("x-csrftoken", "v2yhcMoShm4xneXcVnTihihbIs2MPBxW");
            con.setRequestProperty("accept", "application/json, text/plain, */*");
            con.setRequestProperty("accept-encoding", "gzip, deflate, br");
            con.setRequestProperty("cookie", "__cfduid=dc790e43db5ad19b2e50358abac9e127c1543312659; _ga=GA1.2.80447213.1543312709; kfsession=ISrOlIS9sDG8tvGlwFtHocCYZhydMIrr; public_token=v2yhcMoShm4xneXcVnTihihbIs2MPBxW; user_id=DP3137; bchart-DP3137-GET=https://zerodha-kite-blobs.s3.amazonaws.com/chart/chartiq/DP3137?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAJCNVGANOSDRWICGA%2F20190209%2Fap-south-1%2Fs3%2Faws4_request&X-Amz-Date=20190209T134144Z&X-Amz-Expires=43200&X-Amz-SignedHeaders=host&X-Amz-Signature=67c2df7b4ccbba378984307567e6772c098e1d6f7996e7d353559742f0d3f00a; bchart-DP3137-POST=https://zerodha-kite-blobs.s3.amazonaws.com/chart/chartiq/DP3137?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAJCNVGANOSDRWICGA%2F20190209%2Fap-south-1%2Fs3%2Faws4_request&X-Amz-Date=20190209T134254Z&X-Amz-Expires=43200&X-Amz-SignedHeaders=host&X-Amz-Signature=2508f5b1dc2fab0f4a943ef04a84394a3a2b4e1523eef3061f354138d05d3381");
            con.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
            con.setRequestProperty("x-if-none-match", "4q4XmdWSt8sdDYnB");
            con.setRequestProperty("accept-language", "en-GB,en;q=0.9,en-US;q=0.8,kn;q=0.7");
            

            StringBuilder content;

            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {

                String line;
                content = new StringBuilder();

                while ((line = in.readLine()) != null) {
                    /*content.append(line);
                    content.append(System.lineSeparator());*/
                	System.out.println(line);
                }
            }

            System.out.println(content.toString());

        } finally {
            
            con.disconnect();
        }
    }
}
