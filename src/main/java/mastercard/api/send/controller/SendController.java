package mastercard.api.send.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import mastercard.api.send.model.PaymentRequestWrapper;
import mastercard.api.send.service.RequestBuilder;
import mastercard.api.send.service.SendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.openapitools.client.model.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.LinkedList;
import java.util.List;

/**
 * Controller for application
 */
@Controller
public class SendController {
    /**
     * Instance of SendService that we autowire for convenience
     */
    @Autowired
    private SendService service;

    /**
     * Index page that displays a form to create payments
     * It is prepopulated with a valid request
     * Visit localhost:8080 to view
     * @param model Spring model for adding attributes
     * @return Index page
     */
    @GetMapping("/")
    public String index(Model model) {
        PaymentRequestWrapper paymentRequestWrapper = RequestBuilder.createPrefilledWrapper();
        List<String> fundingSources = new LinkedList<>();
        List<String> recipientUriSchemes = new LinkedList<>();
        List<String> senderUriSchemes = new LinkedList<>();
        fundingSources.add("CREDIT");
        fundingSources.add("DEBIT");
        fundingSources.add("PREPAID");
        recipientUriSchemes.add("PAN"); // For this reference application, we'll only be working with PAN.
        senderUriSchemes.add("PAN"); 
        model.addAttribute("fundingSources", fundingSources);
        model.addAttribute("recipientUriSchemes", recipientUriSchemes);
        model.addAttribute("senderUriSchemes", senderUriSchemes); 
        model.addAttribute("paymentRequestWrapper", paymentRequestWrapper);
        return "index";
    }

    /**
     * Handles form submission. Calls makeCall using the PaymentRequestWrapper, which initiates the chain of calls needed
     * to make the call. Will cause an error/success message to be displayed, along with the accompanying request + response.
     * @param paymentRequestWrapper Instance of PaymentRequestWrapper that is bound to the form being submitted
     * @param redirectAttributes For holding onto information
     * @return
     * @throws Exception
     */
    @PostMapping("/submitForm")
    public String submitForm(@ModelAttribute("paymentRequestWrapper") PaymentRequestWrapper paymentRequestWrapper, RedirectAttributes redirectAttributes) throws Exception{
        PaymentResponse paymentResponse = service.makeCall(paymentRequestWrapper);

        ObjectMapper mapper = new ObjectMapper();
        redirectAttributes.addFlashAttribute("request", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(service.getPaymentRequest()));

        if (paymentResponse != null) {
            redirectAttributes.addFlashAttribute("success", "Success!");
            redirectAttributes.addFlashAttribute("response", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(paymentResponse));
        }
        else {
            redirectAttributes.addFlashAttribute("error", service.getErrorMessage());
            redirectAttributes.addFlashAttribute("response", service.getError());
            service.setErrorMessage("");
        }
        return "redirect:/";
    }
}

