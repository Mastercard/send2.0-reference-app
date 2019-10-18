package mastercard.api.send.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import mastercard.api.send.model.PaymentRequestWrapper;
import org.json.JSONObject;
import org.openapitools.client.ApiException;
import org.openapitools.client.api.PaymentsApi;
import org.openapitools.client.api.TransferEligibilityApi;
import org.openapitools.client.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

/**
 * Class that handles the service side of the application, i.e. making calls to APIs
 */
@Service
public class SendService {
    // Partner ID which is pulled from application.properties
    @Value("${partnerId}")
    private String partnerId;

    // Used to interact with the payments portion of the Send API
    @Autowired
    private PaymentsApi paymentsApi;

    // Used to interact with the transfer eligibility portion of the Send API
    @Autowired
    private TransferEligibilityApi transferEligibilityApi;

    // Most recent payment request made
    private PaymentRequest paymentRequest;

    // Most recent error response
    private String error;

    // Message to accompany most recent error response
    private String errorMessage = "";

    @Autowired
    public SendService() { }

    /**
     * Takes a PaymentRequestWrapper, and uses it to make an API call which will create a payment.
     * Uses unwrapRequest to get the data in the form needed.
     * Checks transfer eligibility of the parties before making the call.
     * @param paymentRequestWrapper PaymentRequestWrapper instance, that contains all data needed to construct request
     * @return Instance of PaymentResponse if the call was made successfully, or null otherwise
     */
    public PaymentResponse makeCall(PaymentRequestWrapper paymentRequestWrapper) {
        paymentRequest = RequestBuilder.createPaymentRequest(paymentRequestWrapper);

        if (isEligible(paymentRequest)) {
            try {
                return paymentsApi.createPaymentUsingPOST(partnerId, paymentRequest, false, false);
            } catch (ApiException e) {
                JSONObject json = new JSONObject(e.getResponseBody()).getJSONObject("Errors").getJSONArray("Error").getJSONObject(0);
                errorMessage = "Error creating payment";
                error = json.toString(4);
            }
        }
        return null;
    }

    /**
     * Using data from an instance of PaymentRequest, a request for the Transfer Eligibility portion of the Send API
     * is created, and a call is made to check if the accounts are eligible for transfer or not.
     * @param paymentRequest PaymentRequest instance, which needs to be checked for transfer eligibility
     * @return True if both parties are eligible for transfer, false in all other cases
     */
    private boolean isEligible(PaymentRequest paymentRequest) {
        TransferEligibilityRequest transferEligibilityRequest = RequestBuilder.createTransferEligibilityRequest(paymentRequest);
        TransferEligibilityResponse transferEligibilityResponse;
        try {
            transferEligibilityResponse = transferEligibilityApi.checkTransferEligibilityUsingPOST(partnerId, transferEligibilityRequest, false);
            if (transferEligibilityResponse.getTransferEligibility().getEligible()) {
                return true;
            } else {
                if (!transferEligibilityResponse.getSendingEligibility().getEligible() &&
                        !transferEligibilityResponse.getReceivingEligibility().getEligible()) {
                    errorMessage = "Both the sender and recipient are not eligible for transfers. ";
                }
                else if (!transferEligibilityResponse.getReceivingEligibility().getEligible()) {
                    errorMessage = "The recipient is not eligible for transfers. ";
                }
                else if (!transferEligibilityResponse.getSendingEligibility().getEligible()) {
                    errorMessage = "The sender is not eligible for transfers. ";
                }
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    error = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(transferEligibilityResponse);
                } catch (Exception e) {
                    System.out.println("This should never happen");
                }
                return false;
            }
        } catch (ApiException e) {
            JSONObject json = new JSONObject(e.getResponseBody()).getJSONObject("Errors").getJSONArray("Error").getJSONObject(0);
            error = json.toString(4);
            errorMessage = "Error checking transfer eligibility";
            return false;
        }
    }

    /**
     * Returns most recent PaymentRequest
     * @return paymentRequest
     */
    public PaymentRequest getPaymentRequest() {
        return paymentRequest;
    }

    /**
     * Returns most recent error response body
     * @return error response
     */
    public String getError() {
        return error;
    }

    /**
     * Returns most recent error message
     * @return error message
     */
    public String getErrorMessage(){
        return errorMessage;
    }

    /**
     * Sets the errorMessage. Mostly used to reset the error message after it is used in the controller
     * @param newErrorMessage string containing new error message
     */
    public void setErrorMessage(String newErrorMessage) {
        errorMessage = newErrorMessage;
    }
}
