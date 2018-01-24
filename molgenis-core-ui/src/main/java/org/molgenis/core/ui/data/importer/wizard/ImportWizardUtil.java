package org.molgenis.core.ui.data.importer.wizard;

import org.molgenis.core.ui.wizard.Wizard;
import org.molgenis.data.DatabaseAction;
import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.util.Error;
import org.molgenis.util.ErrorException;
import org.slf4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.io.File;
import java.util.Locale;
import java.util.Optional;

import static java.lang.String.format;

public class ImportWizardUtil
{
	private ImportWizardUtil()
	{

	}

	public static DatabaseAction toDatabaseAction(String actionStr)
	{
		// convert input to database action
		DatabaseAction dbAction;

		switch (actionStr)
		{
			case "add":
				dbAction = DatabaseAction.ADD;
				break;
			case "add_update":
				dbAction = DatabaseAction.ADD_UPDATE_EXISTING;
				break;
			case "update":
				dbAction = DatabaseAction.UPDATE;
				break;
			default:
				dbAction = null;
				break;
		}

		return dbAction;
	}

	public static void handleException(Exception e, ImportWizard importWizard, BindingResult result, Logger logger,
			String entityImportOption)
	{
		File file = importWizard.getFile();

		if (logger.isWarnEnabled())
		{
			logger.warn(format("Import of file [%s] failed for action [%s]",
					Optional.ofNullable(file).map(File::getName).orElse("UNKNOWN"), entityImportOption), e);
		}

		String errorMessage;
		if (e instanceof ErrorException)
		{
			ErrorException errorException = (ErrorException) e;

			Locale locale = LocaleContextHolder.getLocale();
			MessageSource messageSource = MessageSourceHolder.getMessageSource();

			StringBuilder messageBuilder = new StringBuilder();
			errorException.getError().getChildren().map(Error::getMessageSourceResolvable).forEach(error ->
			{
				String message = messageSource.getMessage(error, locale);
				messageBuilder.append("<br />").append(message);
			});
			errorMessage = messageBuilder.toString();
		}
		else
		{
			errorMessage = e.getMessage();
		}
		result.addError(new ObjectError("wizard", "<b>Your import failed:</b>" + errorMessage));
	}

	public static void validateImportWizard(Wizard wizard)
	{
		if (!(wizard instanceof ImportWizard))
		{
			throw new RuntimeException(
					"Wizard must be of type '" + ImportWizard.class.getSimpleName() + "' instead of '"
							+ wizard.getClass().getSimpleName() + "'");
		}
	}
}
