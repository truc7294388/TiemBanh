package com.bakeryshop.util;

import com.bakeryshop.config.VNPayConfig;
import com.bakeryshop.entity.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class VNPayUtils {
    private final VNPayConfig vnPayConfig;

    public VNPayUtils(VNPayConfig vnPayConfig) {
        this.vnPayConfig = vnPayConfig;
    }

    public String createPaymentUrl(String orderId, long amount) throws Exception {
        String vnp_HashSecret = vnPayConfig.getVnpHashSecret();
        String vnp_TmnCode = vnPayConfig.getVnpTmnCode();
        String vnp_ReturnUrl = vnPayConfig.getVnpReturnUrl();
        String vnp_PayUrl = vnPayConfig.getVnpPayUrl();

        String vnp_Version = vnPayConfig.getVnpVersion();
        String vnp_Command = vnPayConfig.getVnpCommand();
        String orderType = "other";
        String vnp_IpAddr = "127.0.0.1";

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnp_Version);
        vnpParams.put("vnp_Command", vnp_Command);
        vnpParams.put("vnp_TmnCode", vnp_TmnCode);
        if (amount <= 0) throw new IllegalArgumentException("Amount must be > 0");
        vnpParams.put("vnp_Amount", String.valueOf(amount * 100));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", orderId.replaceAll("[^A-Za-z0-9]", ""));
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang: " + orderId);
        vnpParams.put("vnp_OrderType", orderType);
        vnpParams.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnpParams.put("vnp_IpAddr", vnp_IpAddr);
        vnpParams.put("vnp_CreateDate", vnp_CreateDate);
        vnpParams.put("vnp_ExpireDate", vnp_ExpireDate);
        vnpParams.put("vnp_Locale", "vn");

        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (int i = 0; i < fieldNames.size(); i++) {
            String key = fieldNames.get(i);
            String value = vnpParams.get(key);
            if (value != null && !value.isEmpty()) {
                hashData.append(key).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(key, StandardCharsets.US_ASCII))
                        .append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
                if (i < fieldNames.size() - 1) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        String secureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);

        return vnp_PayUrl + "?" + query.toString();
    }



//    public String createPaymentUrl(String orderId, long amount) {
//        try {
//            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
//            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
//            String vnp_CreateDate = formatter.format(cld.getTime());
//
//
//            cld.add(Calendar.MINUTE, 15);
//            String vnp_ExpireDate = formatter.format(cld.getTime());
//
//            Map<String, String> vnpParams = new HashMap<>();
//            vnpParams.put("vnp_Version", "2.0.0");
//            vnpParams.put("vnp_Command", "pay");
//            vnpParams.put("vnp_TmnCode", vnPayConfig.getVnpTmnCode());
//            vnpParams.put("vnp_Locale", "vn");
//            vnpParams.put("vnp_CurrCode", "VND");
//            vnpParams.put("vnp_TxnRef", orderId);
//            vnpParams.put("vnp_OrderInfo", "Thanh toan don hang: " + orderId);
//            vnpParams.put("vnp_OrderType", "other");
//            vnpParams.put("vnp_Amount", String.valueOf(amount * 100));
//            vnpParams.put("vnp_ReturnUrl", vnPayConfig.getVnpReturnUrl());
//            vnpParams.put("vnp_IpAddr", "127.0.0.1");
//            vnpParams.put("vnp_CreateDate", vnp_CreateDate);
//            vnpParams.put("vnp_ExpireDate", vnp_ExpireDate);
//            vnpParams.put("vnp_BankCode", "");
//
//            // Sort parameters
//            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
//            Collections.sort(fieldNames);
//
//            // Build hash data and query
//            StringBuilder hashData = new StringBuilder();
//            StringBuilder query = new StringBuilder();
//            Iterator itr = fieldNames.iterator();
//            while (itr.hasNext()) {
//                String fieldName = (String) itr.next();
//                String fieldValue = (String) vnpParams.get(fieldName);
//                if ((fieldValue != null) && (fieldValue.length() > 0)) {
//                    //Build hash data
//                    hashData.append(fieldName);
//                    hashData.append('=');
//                    try {
//                        hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
//                        //Build query
//                        query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
//                        query.append('=');
//                        query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                    }
//                    if (itr.hasNext()) {
//                        query.append('&');
//                        hashData.append('&');
//                    }
//                }
//            }
//
//            // Create secure hash
//            String queryUrl = query.toString();
//            String vnp_SecureHash = VNPayConfig.hmacSHA512(vnPayConfig.getVnpHashSecret(), hashData.toString());
//            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
//            String paymentUrl = vnPayConfig.getVnpPayUrl() + "?" + queryUrl;
//            return paymentUrl;
//        } catch (Exception e) {
//            throw new RuntimeException("Error creating payment URL", e);
//        }
//    }

    public boolean validatePaymentResponse(Map<String, String> response) {
        if (response == null || response.isEmpty()) {
            return false;
        }

        String vnp_SecureHash = response.get("vnp_SecureHash");
        String vnp_ResponseCode = response.get("vnp_ResponseCode");
        String vnp_TransactionStatus = response.get("vnp_TransactionStatus");

        if (vnp_SecureHash == null || vnp_SecureHash.isEmpty()) {
            return false;
        }

        // Remove hash params
        Map<String, String> validationParams = new HashMap<>(response);
        validationParams.remove("vnp_SecureHash");
        validationParams.remove("vnp_SecureHashType");

        // Sort fields
        List<String> fieldNames = new ArrayList<>(validationParams.keySet());
        Collections.sort(fieldNames);

        // Create validation hash
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = validationParams.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        String calculatedHash = hmacSHA512(vnPayConfig.getVnpHashSecret(), hashData.toString());
        boolean validSignature = calculatedHash.equals(vnp_SecureHash);
        boolean validResponse = "00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus);

        return validSignature && validResponse;
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKeySpec);
            byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Convert byte array to hex string (exactly like VNPay expects)
            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hash.append('0'); // ensure leading zero
                hash.append(hex);
            }
            return hash.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC-SHA512", e);
        }
    }


    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
                    .replace("+", "%20")
                    .replace("*", "%2A")
                    .replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error encoding URL", e);
        }
    }

    private String getClientIp() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                    .getRequest();
            
            String ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("HTTP_CLIENT_IP");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
            }
            
            if (ipAddress != null && ipAddress.contains(",")) {
                ipAddress = ipAddress.split(",")[0].trim();
            }
            
            return ipAddress != null ? ipAddress : "127.0.0.1";
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
} 