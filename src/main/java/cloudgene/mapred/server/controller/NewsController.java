package cloudgene.mapred.server.controller;

import cloudgene.mapred.server.services.NewsService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.annotation.Secured;
import jakarta.inject.Inject;

@Controller
public class NewsController {
	@Inject
	protected NewsService newsService;

	@Get("/api/v2/users/news")
	@Secured(SecurityRule.IS_ANONYMOUS)
	//@Secured(User.ROLE_ADMIN)
	public String getNews() {
		return newsService.getNews();
	}
	@Get("/api/v2/users/latestnews")
	@Secured(SecurityRule.IS_ANONYMOUS)
	//@Secured(User.ROLE_ADMIN)
	public String getLatestNews() {
		return newsService.getLatestNews();
	}

}
