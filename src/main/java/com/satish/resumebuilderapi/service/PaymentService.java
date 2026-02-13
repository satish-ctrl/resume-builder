package com.satish.resumebuilderapi.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.satish.resumebuilderapi.document.Payment;
import com.satish.resumebuilderapi.document.User;
import com.satish.resumebuilderapi.dto.AuthResponse;
import com.satish.resumebuilderapi.repository.PaymentRepository;
import com.satish.resumebuilderapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.satish.resumebuilderapi.util.AppConstants.PREMIUM;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AuthService authService;
    private final UserRepository userRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public Payment createOrder(Object principal, String planType) throws RazorpayException {
        AuthResponse authResponse = authService.getProfile(principal);


//        step 1: initialize the razorpay client
        RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

//        step 2: prepare the json object to pass the razorpay
        int amount = 99900; //amount in paise
        String currency = "INR";
        String  receipt = PREMIUM +"_"+ UUID.randomUUID().toString().substring(0,8);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount);
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", receipt);

//        step 3: call the razorpay api to create order
        Order razorpayOrder = razorpayClient.orders.create(orderRequest);

//        step 4: save the order details into database
        Payment newPayment = Payment.builder()
                .userId(authResponse.getId())
                .razorpayOrderId(razorpayOrder.get("id"))
                .amount(amount)
                .planType(planType)
                .currency(currency)
                .status("created")
                .receipt(receipt)
                .build();

//        step 5: return the result
        return paymentRepository.save(newPayment);
    }

    public boolean verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) throws RazorpayException {
       try{
           JSONObject attributes = new JSONObject();
           attributes.put("razorpay_order_id", razorpayOrderId);
           attributes.put("razorpay_payment_id", razorpayPaymentId);
           attributes.put("razorpay_signature_id", razorpaySignature);

           boolean isValidSignature = Utils.verifyPaymentSignature(attributes, razorpayKeySecret);

           if (isValidSignature){

//            update the payment status

               Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                       .orElseThrow(() -> new RuntimeException("Payment not found"));
               payment.setRazorpayPaymentId(razorpayPaymentId);
               payment.setRazorpaySignature(razorpaySignature);
               payment.setStatus("paid");
               paymentRepository.save(payment);

//            upgrade the user subscription

               upgradeUserSubscription(payment.getUserId(), payment.getPlanType());
               return true;
           }
           return false;

       } catch (Exception e) {
           log.error("Error verifying the payment: ", e);
           return false;
       }
    }

    private void upgradeUserSubscription(String userId, String planType) {
       User existingUser = userRepository.findById(userId)
                        .orElseThrow(()-> new UsernameNotFoundException("User not found"));

       existingUser.setSubscriptionPlan(planType);
       userRepository.save(existingUser);
        log.info("User {} upgrade to {} plan", userId, planType);
    }

    public List<Payment> getUserPayments(Object principal) {
//        step 1: get the current profile
        AuthResponse authResponse = authService.getProfile(principal);

//        step 2:call the repo finder method
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(authResponse.getId());
    }

    public Payment getUserPaymentDetails(String orderId) {

//        step 1: call the repo finder method
        return paymentRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(()-> new RuntimeException("Payment not found"));
    }
}
