package mastercard.api.send.service;

import mastercard.api.send.model.PaymentRequestWrapper;
import org.openapitools.client.model.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

/**
 * Class in charge of building requests
 */
public class RequestBuilder {
    /**
     * Takes a PaymentRequestWrapper, and unpacks it into the appropriate models that our
     * API client instances want
     * @param paymentRequestWrapper PaymentRequestWrapper instance, that contains all data needed to construct request
     * @return Instance of PaymentRequest that is ready to be used with an instance of PaymentsAPI to make API calls
     */
    static PaymentRequest createPaymentRequest(PaymentRequestWrapper paymentRequestWrapper) {

        PaymentRequest paymentRequest = new PaymentRequest();

        AcquiringCredentials acquiringCredentials = new AcquiringCredentials();
        DualMessage dualMessage = new DualMessage();
        dualMessage.setAcquiringBin(paymentRequestWrapper.getAcquiringBIN());

        SingleMessage singleMessage = new SingleMessage();
        singleMessage.setProcessorId(paymentRequestWrapper.getAcquiringProcessorId());
        singleMessage.setAcquiringIdentificationCode(paymentRequestWrapper.getAcquiringIdentificationCode());

        acquiringCredentials.setAcquiringIca(paymentRequestWrapper.getAcquiringICA());
        acquiringCredentials.setAcquiringCountry(paymentRequestWrapper.getAcquiringCountry());
        acquiringCredentials.setDualMessage(dualMessage);
        acquiringCredentials.setSingleMessage(singleMessage);


        TransferAcceptor transferAcceptor = new TransferAcceptor();
        Address address1 = new Address();
        address1.setStreet(paymentRequestWrapper.getTransferAcceptorStreet());
        address1.setCity(paymentRequestWrapper.getTransferAcceptorCity());
        address1.setCountry(paymentRequestWrapper.getTransferAcceptorCountry());
        address1.setState(paymentRequestWrapper.getTransferAcceptorCountrySubdivision());
        address1.setPostalCode(paymentRequestWrapper.getTransferAcceptorPostalCode());

        transferAcceptor.setId(paymentRequestWrapper.getTransferAcceptorId());
        transferAcceptor.setName("transferacceptr");
        transferAcceptor.setAddress(address1);
        transferAcceptor.setTerminalId(paymentRequestWrapper.getTransferAcceptorTerminalId());


        Sender sender = new Sender();
        Address address2 = new Address();
        address2.setStreet(paymentRequestWrapper.getSenderStreet());
        address2.setCity(paymentRequestWrapper.getSenderCity());
        address2.setCountry(paymentRequestWrapper.getSenderCountry());
        address2.setState(paymentRequestWrapper.getSenderCountrySubdivision());
        address2.setPostalCode(paymentRequestWrapper.getSenderPostalCode());

        sender.setName(paymentRequestWrapper.getSenderLastName() + " " + paymentRequestWrapper.getSenderFirstName() + " ");

        sender.setAddress(address2);
        sender.setAccountUri("pan:" + paymentRequestWrapper.getSenderUriIdentifier() + ";exp=" + paymentRequestWrapper.getSenderUriExpYear()
                + "-" + paymentRequestWrapper.getSenderUriExpMonth() + ";cvc=" + paymentRequestWrapper.getSenderUriCvc());


        Recipient recipient = new Recipient();

        recipient.setName(paymentRequestWrapper.getRecipientLastName() + " " + paymentRequestWrapper.getRecipientFirstName() + " ");
        recipient.setAccountUri("pan:" + paymentRequestWrapper.getRecipientUriIdentifier() + ";exp=" + paymentRequestWrapper.getRecipientUriExpYear()
                + "-" + paymentRequestWrapper.getRecipientUriExpMonth() + ";cvc=" + paymentRequestWrapper.getRecipientUriCvc());//        recipient.setGovernmentIds(governmentIds2);


        AdditionalProgramData additionalProgramData = new AdditionalProgramData();
        CrossNetwork crossNetwork = new CrossNetwork();
        additionalProgramData.setCrossNetwork(crossNetwork);

        // Generating random payment reference number
        Random random = new Random();
        long n = (long) (100000000000000L + random.nextFloat() * 900000000000000L);
        String paymentReference = n + "";   //Quickly coerce our long into a string

        // Grabbing the current time in ISO 8601 format
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        String currentMoment = df.format(new Date());

        paymentRequest.setPaymentType(paymentRequestWrapper.getPaymentType());
        paymentRequest.setMerchantCategoryCode(paymentRequestWrapper.getMerchantCategoryCode());
        paymentRequest.setAcquiringCredentials(acquiringCredentials);
        paymentRequest.setTransferAcceptor(transferAcceptor);
        paymentRequest.setTransactionLocalDateTime(currentMoment);
        paymentRequest.setAmount(paymentRequestWrapper.getAmount());
        paymentRequest.setCurrency(paymentRequestWrapper.getCurrency());
        paymentRequest.setFundingSource(paymentRequestWrapper.getFundingSource());
        paymentRequest.setSender(sender);
        paymentRequest.setRecipient(recipient);
        paymentRequest.setAdditionalProgramData(additionalProgramData);
        paymentRequest.setPaymentReference(paymentReference);
        paymentRequest.setDeviceType("WEB");
        paymentRequest.setParticipationId("1234567890");
        paymentRequest.setTransactionPurpose("00");
        paymentRequest.setAdditionalMessage("adding message for this payment");
        paymentRequest.setUniqueTransactionReference("1234567890");
        return paymentRequest;
    }

    /**
     * Creates a TransferEligibilityRequest using a PaymentRequest object
     * @param paymentRequest PaymentRequest instance
     * @return Instance of TransferEligibilityRequest
     */
    static TransferEligibilityRequest createTransferEligibilityRequest(PaymentRequest paymentRequest) {
        TransferEligibilityRequest transferEligibilityRequest = new TransferEligibilityRequest();
        transferEligibilityRequest.setRecipientAccountUri(paymentRequest.getRecipient().getAccountUri());
        transferEligibilityRequest.setSenderAccountUri(paymentRequest.getSender().getAccountUri());
        transferEligibilityRequest.setAcquirerCountry(paymentRequest.getAcquiringCredentials().getAcquiringCountry());
        transferEligibilityRequest.setTransferAcceptorCountry(paymentRequest.getTransferAcceptor().getAddress().getCountry());
        transferEligibilityRequest.setAdditionalProgramData(paymentRequest.getAdditionalProgramData());
        transferEligibilityRequest.setPaymentType(paymentRequest.getPaymentType());
        transferEligibilityRequest.setCurrency(paymentRequest.getCurrency());
        transferEligibilityRequest.setAmount(paymentRequest.getAmount());
        transferEligibilityRequest.setCrossBorderEligible("D");
        return transferEligibilityRequest;
    }

    /**
     * Creates an instance of PaymentRequestWrapper with pre-populated fields to
     * allow for fast form submissions.
     * @return Instance of PaymentRequestWrapper class
     */
    public static PaymentRequestWrapper createPrefilledWrapper() {
        PaymentRequestWrapper paymentRequestWrapper = new PaymentRequestWrapper();
        paymentRequestWrapper.setSenderFirstName("John");
        paymentRequestWrapper.setSenderLastName("Wrangler");
        paymentRequestWrapper.setSenderStreet("114 Wacker Ave");
        paymentRequestWrapper.setSenderCity("Chicago");
        paymentRequestWrapper.setSenderPostalCode("22245");
        paymentRequestWrapper.setSenderCountrySubdivision("IL");
        paymentRequestWrapper.setSenderCountry("USA");
        paymentRequestWrapper.setSenderUriScheme("PAN");
        paymentRequestWrapper.setSenderUriIdentifier("5432123456789012");
        paymentRequestWrapper.setSenderUriExpYear("2050");
        paymentRequestWrapper.setSenderUriExpMonth("02");
        paymentRequestWrapper.setSenderUriCvc("123");
        paymentRequestWrapper.setRecipientStreet("2200 Mastercard Blvd");
        paymentRequestWrapper.setRecipientCity("Cape Girardeau");
        paymentRequestWrapper.setRecipientPostalCode("23232");
        paymentRequestWrapper.setRecipientCountrySubdivision("MO");
        paymentRequestWrapper.setRecipientCountry("USA");
        paymentRequestWrapper.setRecipientFirstName("Jane");
        paymentRequestWrapper.setRecipientLastName("Juniper");
        paymentRequestWrapper.setRecipientUriScheme("PAN");
        paymentRequestWrapper.setRecipientUriIdentifier("4024140000000065");
        paymentRequestWrapper.setRecipientUriExpYear("2050");
        paymentRequestWrapper.setRecipientUriExpMonth("02");
        paymentRequestWrapper.setRecipientUriCvc("123");
        paymentRequestWrapper.setRecipientNameOnAccount("Jane Juniper");
        paymentRequestWrapper.setTransferAcceptorId("456487898368");
        paymentRequestWrapper.setTransferAcceptorTerminalId("1367-hgf");
        paymentRequestWrapper.setTransferAcceptorName("transferacceptr");
        paymentRequestWrapper.setTransferAcceptorPaymentFacilitatorId("123");
        paymentRequestWrapper.setTransferAcceptorSubMerchantId("223");
        paymentRequestWrapper.setTransferAcceptorMastercardAssignedId("12A346");
        paymentRequestWrapper.setTransferAcceptorStreet("1400 Michigan Ave");
        paymentRequestWrapper.setTransferAcceptorCity("Port Richey");
        paymentRequestWrapper.setTransferAcceptorPostalCode("12345");
        paymentRequestWrapper.setTransferAcceptorCountrySubdivision("FL");
        paymentRequestWrapper.setTransferAcceptorCountry("USA");
        paymentRequestWrapper.setAcquiringICA("1234");
        paymentRequestWrapper.setAcquiringCountry("USA");
        paymentRequestWrapper.setAcquiringBIN("123456");
        paymentRequestWrapper.setAcquiringProcessorId("1234567898");
        paymentRequestWrapper.setAcquiringIdentificationCode("12346");
        paymentRequestWrapper.setFundingSource("CREDIT");
        paymentRequestWrapper.setMerchantCategoryCode("4121");
        paymentRequestWrapper.setPaymentType("P2M");
        paymentRequestWrapper.setAmount("1000");
        paymentRequestWrapper.setCurrency("USD");
        return paymentRequestWrapper;
    }
}
