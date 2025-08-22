package cloudgene.mapred.server.controller;

//import java.util.List;

//import cloudgene.mapred.core.Group;
//import cloudgene.mapred.core.User;
import cloudgene.mapred.server.services.CountryService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.annotation.Secured;
import jakarta.inject.Inject;

@Controller("/api/v2/users/countries")
public class GroupController {
	@Inject
	protected CountryService countryService;

	@Get("/")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	//@Secured(User.ROLE_ADMIN)
	public String getAllowedCountries() {
		return countryService.getAllowedCountries();
	}

}
