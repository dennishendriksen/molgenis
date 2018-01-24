package org.molgenis.web.exception;

import org.molgenis.data.UnknownDataException;
import org.molgenis.data.validation.BatchValidationException;
import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.web.ErrorMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.molgenis.web.exception.ExceptionHandlerUtils.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalControllerExceptionHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

	@Value("${environment:production}")
	private String environment;

	@ExceptionHandler(UnknownDataException.class)
	public Object handleException(UnknownDataException e, HandlerMethod handlerMethod)
	{
		LOG.info("", e);
		return ExceptionHandlerUtils.handleException(e, handlerMethod, NOT_FOUND, e.getErrorCode(), environment);
	}

	// TODO remove temporary exception handler meant for testing
	@ExceptionHandler({ BatchValidationException.class })
	public Object handleException(BatchValidationException e, HandlerMethod handlerMethod)
	{
		LOG.info("", e);

		Locale locale = LocaleContextHolder.getLocale();
		MessageSource messageSource = MessageSourceHolder.getMessageSource();

		List<ErrorMessageResponse.ErrorMessage> errorMessages = newArrayList();
		e.getErrorsList().forEach(errors -> errors.getAllErrors().forEach(error ->
		{
			String message = messageSource.getMessage(error.getCode(), error.getArguments(), locale);
			errorMessages.add(new ErrorMessageResponse.ErrorMessage(message, error.getCode()));
		}));

		HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
		ErrorMessageResponse errorMessageResponse = new ErrorMessageResponse(errorMessages);
		return handle(e, handlerMethod, httpStatus, errorMessageResponse);
	}

	// TODO remove temporary exception handler meant for testing
	private Object handle(Exception e, HandlerMethod handlerMethod, HttpStatus httpStatus,
			ErrorMessageResponse errorMessageResponse)
	{
		if (ExceptionHandlerUtils.isHtmlRequest(handlerMethod))
		{
			Map<String, Object> model = new HashMap<>();
			model.put(KEY_ERROR_MESSAGE_RESPONSE, errorMessageResponse);
			model.put(HTTP_STATUS_CODE, httpStatus.value());
			if (environment.equals(DEVELOPMENT))
			{
				model.put(STACK_TRACE, e.getStackTrace());
			}
			return new ModelAndView(VIEW_EXCEPTION, model, httpStatus);
		}
		else
		{
			return new ResponseEntity<>(errorMessageResponse, httpStatus);
		}
	}
}
