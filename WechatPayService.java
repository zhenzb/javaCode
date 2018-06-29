
package action.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import common.PropertiesConf;
import utils.HttpUtils;
import utils.JsonUtils;
import utils.MD5Util;
import utils.StringUtil;
import utils.WechatRequest;

public class WechatPayService{



	/** 交易等待 **/
	public static String WECHAT_TRADE_WAIT = "WECHAT_TRADE_WAIT";
	/** 交易成功 **/
	public static String WECHAT_TRADE_SUCCESS = "TRADE_SUCCESS";
	/** 交易失败 **/
	public static String WECHAT_TRADE_FAIL = "TRADE_FAIL";
 	
	
	/** 统一下单URL **/
	public static String UNIFIEDORDER_REQUST_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";

	/** jscode2session **/
	public static String JSCODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";		
		
		

	/**
	 * 2018-3-8 libai小程序微信支付创建预订单
	 * 
	 * @Title: miniAppUnifiedOrderCreate
	 * @param app_id 
	 * @param app_key  
	 * @param mch_id
	 * @param notify_url
	 * @param spbill_create_ip
	 * @param out_trade_no
	 * @param amount
	 * @param body
	 * @return
	 * @throws Exception
	 * @author tiny
	 * @param openid 
	 */
	public static HashMap<String, Object> miniWetCatOrderCreate(String jsCode,String transactionBody,String transactionNo,String totalPrice)
					throws Exception {
		HashMap<String, Object> wechatPayInfoMap = new HashMap<String, Object>();

		wechatPayInfoMap.put("appid", PropertiesConf.APP_ID.trim());
		wechatPayInfoMap.put("mch_id", PropertiesConf.MINI_MCH_ID.trim());
		wechatPayInfoMap.put("nonce_str", StringUtil.getRandomStringByLength(32));
		//商品描述
		wechatPayInfoMap.put("body", transactionBody);
		//本系统内商户订单号
		wechatPayInfoMap.put("out_trade_no", transactionNo);
		//交易金额默认为人民币交易，接口中参数支付金额单位为【分】，参数值不能带小数。对账单中的交易金额单位为【元】
		wechatPayInfoMap.put("total_fee", totalPrice);
		//wechatPayInfoMap.put("total_fee", MoneyUtil.moneyMulOfNotPoint(totalPrice, String.valueOf(100)));
		//支付回调地址	
		//wechatPayInfoMap.put("notify_url", "http://uranus.sweetguo.top:8080/uranus/wechatNotityAction");
		wechatPayInfoMap.put("notify_url", PropertiesConf.MINI_NOTIFY_URL.trim());
		//微信小程序 选择类型
		wechatPayInfoMap.put("trade_type", "JSAPI");
		
		//openid是固定的但是js_code是动态的 ：测试情况下先写死 
		String openid = "";
		Map<String, Object> findOpenid = findOpenid(jsCode);
		if(findOpenid != null ){
			openid = findOpenid.get("openid").toString();
		}
		wechatPayInfoMap.put("openid", openid);
		//wechatPayInfoMap.put("openid", "oA9D20EZogOYpVNKYEjfAVUWfGaQ");
		
		wechatPayInfoMap.put("sign",MD5Util.sign(StringUtil.createLinkString(wechatPayInfoMap), "&key=" + PropertiesConf.MINI_APP_KEY.trim(), "UTF-8").toUpperCase());
		String xml = StringUtil.mapToXML(wechatPayInfoMap);
		WechatRequest wchatHttpsRequest = new WechatRequest();
		String responseString = wchatHttpsRequest.httpRequest(WechatRequest.UNIFIEDORDER_REQUST_URL,"POST", xml);
		Map<String, Object> resultMap = StringUtil.xml2Map(responseString);
		if (!"SUCCESS".equals(resultMap.get("return_code"))) {
			return	null;
		}
		// 组装返回参数
		wechatPayInfoMap.clear();
		// 小程序ID 需要小程序appId生成数据签名，所以需要放入
		wechatPayInfoMap.put("appId", resultMap.get("appid"));
		// 随机字符串
		wechatPayInfoMap.put("nonceStr", resultMap.get("nonce_str"));
		//预支付交易会话标识
		wechatPayInfoMap.put("package", "prepay_id=" + resultMap.get("prepay_id"));
		// 时间戳
		wechatPayInfoMap.put("timeStamp", System.currentTimeMillis() / 1000 + "");
		//签名方式，固定值MD5
		wechatPayInfoMap.put("signType", "MD5");
		// 将数据签名
		wechatPayInfoMap.put("paySign",
				MD5Util.sign(StringUtil.createLinkString(wechatPayInfoMap), "&key=" +  PropertiesConf.MINI_APP_KEY.trim(), "UTF-8").toLowerCase());
		// 小程序ID绝密文件 不能回传网路传输可能引起安全问题,回传需要隐藏
		wechatPayInfoMap.remove("appId");
		return wechatPayInfoMap;
	}
	
	
	
	/**
	 * 2018-05-29 libai小程序H5微信支付创建预订单
	 */
	public static HashMap<String, Object> winFruitWechatH5Pay(String transactionBody,String transactionNo,String totalPrice,String spbillCreateIp,String sceneInfo,String deviceInfo)
			throws  Exception {
		
		HashMap<String, Object> wechatH5PayInfoMap = new HashMap<String, Object>();
			//微信分配的公众账号ID（企业号corpid即为此appId）
			wechatH5PayInfoMap.put("appid", PropertiesConf.APP_ID.trim());
			//商户号	mch_id
			wechatH5PayInfoMap.put("mch_id", PropertiesConf.MINI_MCH_ID.trim());
			//设备号	device_info
			wechatH5PayInfoMap.put("device_info", deviceInfo);
			//随机字符串	nonce_str
			wechatH5PayInfoMap.put("nonce_str", StringUtil.getRandomStringByLength(32));
			//商品描述	body
			wechatH5PayInfoMap.put("body", transactionBody);
			//商户订单号	out_trade_no
			wechatH5PayInfoMap.put("out_trade_no", transactionNo);
			//总金额	total_fee	是	Int	888
			wechatH5PayInfoMap.put("total_fee", totalPrice);
			//终端IP	spbill_create_ip
			wechatH5PayInfoMap.put("spbill_create_ip", spbillCreateIp);
			//通知地址	notify_url
			wechatH5PayInfoMap.put("notify_url", PropertiesConf.MINI_NOTIFY_URL.trim());
			//交易类型	trade_type	MWEB
			wechatH5PayInfoMap.put("trade_type", "MWEB");
			//场景信息	scene_info
			wechatH5PayInfoMap.put("scene_info", sceneInfo);      
			//签名	sign		
			wechatH5PayInfoMap.put("sign",MD5Util.sign(StringUtil.createLinkString(wechatH5PayInfoMap), "&key=" + PropertiesConf.MINI_APP_KEY.trim(), "UTF-8").toUpperCase());
						
			String xml = StringUtil.mapToXML(wechatH5PayInfoMap);
			WechatRequest wchatHttpsRequest = new WechatRequest();
			String responseString = wchatHttpsRequest.httpRequest(WechatRequest.WetCatH5_REQUST_URL,"POST", xml);
			Map<String, Object> resultMap = StringUtil.xml2Map(responseString);
        System.out.println(resultMap.toString());
			if (!"SUCCESS".equals(resultMap.get("return_code"))) {
				return	null;
			}
			// 组装返回参数
			wechatH5PayInfoMap.clear();	
			wechatH5PayInfoMap.put("tradeType", resultMap.get("trade_type"));
			wechatH5PayInfoMap.put("prepayId", resultMap.get("prepay_id"));
			//wechatH5PayInfoMap.put("mwebUrl", "mweb_url");
            wechatH5PayInfoMap.put("mwebUrl", resultMap.get("mweb_url"));
			return wechatH5PayInfoMap;
	}
	
	

	 
	 /**
	  * 微信小程序支付 获取openid
	  * @param code
	  * @return
	  */
     public static Map<String, Object> findOpenid(String wxCode) {						
		String app_id = PropertiesConf.APP_ID.trim();	
		String app_key = PropertiesConf.APP_SECRET.trim();	
		String url = PropertiesConf.WETCAT_URL.trim();
		String param = "appid="+app_id+"&secret="+app_key+"&js_code="+ wxCode + "&grant_type=authorization_code";
		String result = HttpUtils.sendGet(url,param);	
		Map<String, Object> resultMap = JsonUtils.jsonFormatMap(result);
		if (resultMap.get("errcode")!=null) {
			System.out.println(resultMap.get("errcode"));
			return null;
		}	
		return resultMap;
	}	
     
     
    /**
     * 微信支付 request请求方法
     * @param requestUrl
     * @param requestMethod
     * @param outputStr
     * @return
     */
     public static String httpRequest(String requestUrl,String requestMethod,String outputStr){     
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
     
     
     
 	/**
 	 * 检验API返回的数据里面的签名是否合法，避免数据在传输的过程中被第三方篡改
 	 * 
 	 * @param responseString
 	 *            API返回的XML数据字符串
 	 * @return API签名是否合法
 	 * @throws ParserConfigurationException
 	 * @throws IOException
 	 * @throws SAXException
 	 */
 	public static boolean checkIsSignValidFromResponseString(Map<String, Object> map, String app_key)
 			throws ParserConfigurationException, IOException, SAXException {

 		String signFromAPIResponse = map.get("sign").toString();
 		if (signFromAPIResponse == "" || signFromAPIResponse == null) {
 			//logger.error("API返回的数据签名数据不存在，有可能被第三方篡改!!!");
 			return false;
 		}
 		// 清掉返回数据对象里面的Sign数据（不能把这个数据也加进去进行签名），然后用签名算法进行签名
 		map.remove("sign");
 		// 将API返回的数据根据用签名算法进行计算新的签名，用来跟API返回的签名进行比较
 		String signForAPIResponse = getSign(map, app_key);
 		if (!signForAPIResponse.equals(signFromAPIResponse)) {
 			// 签名验不过，表示这个API返回的数据有可能已经被篡改了
 			//logger.info("服务器回包里面的签名是:" + signFromAPIResponse);
 			//logger.info("据根据用签名算法进行计算新的签名是:" + signForAPIResponse);
 			//logger.error("API返回的数据签名验证不通过，有可能被第三方篡改!!!");
 			return false;
 		}
 		return true;
 	}

 	/**
 	 * 数据根据用签名算法进行计算新的签名
 	 * 
 	 * @param responseString
 	 *            API返回的数据
 	 * @return 新的签名
 	 * @throws ParserConfigurationException
 	 * @throws IOException
 	 * @throws SAXException
 	 */
 	public static String getSign(Map<String, Object> map, String app_key) {
 		ArrayList<String> list = new ArrayList<String>();
 		for (Map.Entry<String, Object> entry : map.entrySet()) {
 			if (entry.getValue() != "") {
 				list.add(entry.getKey() + "=" + entry.getValue() + "&");
 			}
 		}
 		int size = list.size();
 		String[] arrayToSort = list.toArray(new String[size]);
 		Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < size; i++) {
 			sb.append(arrayToSort[i]);
 		}
 		String result = sb.toString();
 		result += "key=" + app_key;
 		result = MD5Util.MD5Encode(result).toUpperCase();
 		return result;
 	}
}