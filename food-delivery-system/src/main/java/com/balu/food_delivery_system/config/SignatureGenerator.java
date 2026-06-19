//package com.balu.food_delivery_system.config;
//
//import javax.crypto.Mac;
//import javax.crypto.spec.SecretKeySpec;
//import java.security.NoSuchAlgorithmException;
//
//public class SignatureGenerator {
//    public static void main(String[] args) throws Exception {
//
//        String razorpayOrderId = "order_T3MchTCfypb5G6";
//        String razorpayPaymentId = "pay_TestPayment001";
//        String keySecret = System.getenv("RAZORPAY_KEY_SECRET");
//
//        String payload = razorpayOrderId + "|" + razorpayPaymentId;
//
//        Mac mac = Mac.getInstance("HmacSHA256");
//        mac.init(new SecretKeySpec(keySecret.getBytes(), "HmacSHA256"));
//        byte[] hash = mac.doFinal(payload.getBytes());
//
//        StringBuilder sb = new StringBuilder();
//        for (byte b : hash) {
//            sb.append(String.format("%02x", b));
//        }
//        System.out.println("paymentId: " + razorpayPaymentId);
//        System.out.println("signature: " + sb.toString());
//    }
//}
