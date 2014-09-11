package cloudgene.mapred.resources.admin;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.database.TemplateDao;
import cloudgene.mapred.util.Template;
import cloudgene.mapred.util.Settings;

public class EnterMaintenance extends ServerResource {

	@Get
	public Representation get() {

		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());
		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}

		if (!user.isAdmin()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires administration rights.");
		}

		Settings.getInstance().setMaintenance(true);

		TemplateDao dao = new TemplateDao();
		dao.update(new Template(
				Template.MAINTENANCE_MESSAGE,
				"Sorry, our service is currently under maintenance. Imputation Server is expected to be down until <b>Tuesday 08:00 AM EDT</b>."));

		Settings.getInstance().reloadTemplates();

		return new StringRepresentation("Enter Maintenance mode.");

	}

}
