package utils;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;



/**
 * User: Tiny Date: 2018/3/8 
 */

/**
 * 微信支付获取订单请求
 * 
 * @author libai
 *
 */
public class WechatRequest {

	/** 微信小程序统一下单URL **/
	public static String UNIFIEDORDER_REQUST_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
	
	/** 微信H5统一下单URL **/
	public static String WetCatH5_REQUST_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";

	/** jscode2session **/
	public static String JSCODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";		
	
	
	/**   
     * 微信小程序的https请求
     * @param requestUrl请求地址   
     * @param requestMethod请求方法   
     * @param outputStr参数   
     */     
    public String httpRequest(String requestUrl,String requestMethod,String outputStr){     
        // 创建SSLContext     
        StringBuffer buffer = null;     
        try{     
            URL url = new URL(requestUrl);     
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();     
            conn.setRequestMethod(requestMethod);     
            conn.setDoOutput(true);     
            conn.setDoInput(true);     
            conn.connect();     
            //往服务器端写内容     
            if(null !=outputStr){     
                OutputStream os=conn.getOutputStream();     
                os.write(outputStr.getBytes("utf-8"));     
                os.close();     
            }     
            // 读取服务器端返回的内容     
            InputStream is = conn.getInputStream();     
            InputStreamReader isr = new InputStreamReader(is, "utf-8");     
            BufferedReader br = new BufferedReader(isr);     
            buffer = new StringBuffer();     
            String line = null;     
            while ((line = br.readLine()) != null) {     
                buffer.append(line);     
            }     
        }catch(Exception e){     
            e.printStackTrace();     
        }  
        return buffer.toString();  
    }       
    
	public static String mapToXMLWeChat(Map<String, Object> params) {
		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);
		String prestr = "<xml>";
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = params.get(key).toString();
			prestr += "<" + key + "><![CDATA[" + value + "]]></" + key + ">";
		}
		return prestr + "</xml>";
	}
}
