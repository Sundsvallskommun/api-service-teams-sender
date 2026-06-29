package se.sundsvall.teamssender.apptest;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.http.client.HttpRedirects;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.teamssender.Application;

import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FOUND;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@WireMockAppTestSuite(files = "classpath:/AuthIT/", classes = Application.class)
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class AuthIT extends AbstractAppTest {

	private static final String RESPONSE_FILE = "response.json";

	@BeforeEach
	void disableRedirectFollowing() {
		// The login endpoint replies with a 302 to the Azure login URL; the autowired TestRestTemplate would otherwise
		// follow the redirect into WireMock (which has no stub there) and the Location header would be lost.
		restTemplate = restTemplate.withRedirects(HttpRedirects.DONT_FOLLOW);
	}

	@Test
	void test01_loginRedirect() {
		setupCall()
			.withServicePath("/api/teamssender/2281/login")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(FOUND)
			.withExpectedResponseHeader(LOCATION, List.of("^http://localhost:\\d+/azure/login$"))
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_callback() {
		setupCall()
			.withServicePath("/api/teamssender/callback?code=auth-code-123&state=2281")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("mock-token-success")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_loginWithInvalidMunicipalityId() {
		setupCall()
			.withServicePath("/api/teamssender/not-a-valid-id/login")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(BAD_REQUEST)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_loginWithUnknownMunicipality() {
		setupCall()
			.withServicePath("/api/teamssender/1230/login")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
}
