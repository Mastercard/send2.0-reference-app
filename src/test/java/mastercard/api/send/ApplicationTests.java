package mastercard.api.send;

import mastercard.api.send.model.PaymentRequestWrapper;
import mastercard.api.send.service.RequestBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@PropertySource("classpath:application.properties")
public class ApplicationTests {
	@Autowired
	private MockMvc mvc;

	@Test
	public void testCreatePaymentSuccess() throws Exception {
		PaymentRequestWrapper paymentRequestWrapper = RequestBuilder.createPrefilledWrapper();
		mvc.perform(post("/submitForm").flashAttr("paymentRequestWrapper", paymentRequestWrapper)
		.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(MockMvcResultMatchers.flash().attribute("success", "Success!"))
				.andExpect(redirectedUrl("/"));

	}
	@Test
	public void testCreatePaymentFailure() throws Exception {
		PaymentRequestWrapper paymentRequestWrapper = RequestBuilder.createPrefilledWrapper();
		paymentRequestWrapper.setMerchantCategoryCode("3333"); // some invalid value
		mvc.perform(post("/submitForm").flashAttr("paymentRequestWrapper", paymentRequestWrapper)
		.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(MockMvcResultMatchers.flash().attribute("error", "Error creating payment"))
				.andExpect(redirectedUrl("/"));

	}

	@Test
	public void testSenderEligibilityFailure() throws Exception {
		PaymentRequestWrapper paymentRequestWrapper = RequestBuilder.createPrefilledWrapper();
		paymentRequestWrapper.setSenderUriIdentifier("1111222233334444"); // Some invalid value
		mvc.perform(post("/submitForm").flashAttr("paymentRequestWrapper", paymentRequestWrapper)
		.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(MockMvcResultMatchers.flash().attribute("error", "The sender is not eligible for transfers. "))
				.andExpect(redirectedUrl("/"));

	}

	@Test
	public void testRecipientEligibilityFailure() throws Exception {
		PaymentRequestWrapper paymentRequestWrapper = RequestBuilder.createPrefilledWrapper();
		paymentRequestWrapper.setRecipientUriIdentifier("1111222233334444"); // Some invalid value
		mvc.perform(post("/submitForm").flashAttr("paymentRequestWrapper", paymentRequestWrapper)
		.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(MockMvcResultMatchers.flash().attribute("error", "The recipient is not eligible for transfers. "))
				.andExpect(redirectedUrl("/"));

	}
}
