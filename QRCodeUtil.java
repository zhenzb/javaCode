package utils;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


public class QRCodeUtil {
     //小程序码接口地址
     static String wxacodeUrl = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=";
     //小程序二维码接口地址
     static String wxacodeUr_2 = "https://api.weixin.qq.com/cgi-bin/wxaapp/createwxaqrcode?access_token=";
    /**
     * 获取商品的小程序码
     */
    public static void getQRCode(String access_token, String stationCode, String goodsPath, int imgWidth, HttpServletResponse response) {

        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            Map<String, Object> params = new HashMap<>();
            HttpPost httpPost = null;
            if(stationCode == null){
                //组装参数
                params.put("width", imgWidth);
                params.put("path", goodsPath);
                httpPost = new HttpPost(wxacodeUr_2 + access_token);
            }else {
                //组装参数
                params.put("scene", stationCode);
                params.put("page", goodsPath);
                params.put("width", imgWidth);
                httpPost = new HttpPost(wxacodeUrl + access_token);
            }
            //格式化参数为json字符串
            String body = JSON.toJSONString(params);
            System.out.println("请求参数："+body);
            StringEntity entity = new StringEntity(body);
            entity.setContentType("image/jpg");
            httpPost.addHeader(HTTP.CONTENT_TYPE, "application/json");
            httpPost.setEntity(entity);
            //发送请求
            HttpResponse resoult = httpClient.execute(httpPost);
            InputStream inputStream = resoult.getEntity().getContent();
            /*String result = new BufferedReader(new InputStreamReader(inputStream))
                    .lines().collect(Collectors.joining(System.lineSeparator()));
            System.out.println("++++++++++++++++++++++++++++++"+result);*/
            byte data[] = readInputStream(inputStream);
            response.setHeader("Content-Type","image/jped");
            response.setContentType("image/jpg"); //设置返回的文件类型
            OutputStream os = response.getOutputStream();
            os.write(data);
            os.flush();
            os.close();
            //todo
            //保存到服务器
            /*String fileName1 = "station_wxacode_" + stationCode + ".jpg";
            File file = new File("D:\\huidian\\" + fileName1);
            if (!file.exists()) {
                    file.createNewFile();
                    OutputStream outputStream = new FileOutputStream(file);
                    int len = 0;
                    byte[] buf = new byte[1024];
                    while ((len = inputStream.read(buf, 0, 1024)) != -1) {
                        outputStream.write(buf, 0, len);
                    }
                    outputStream.flush();
                    outputStream.close();
            }*/
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static byte[] readInputStream(InputStream inStream) throws Exception{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while( (len=inStream.read(buffer)) != -1 ){
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        outStream.close();
        return outStream.toByteArray();
    }
}
