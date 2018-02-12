package org.molgenis.security;

import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.molgenis.web.bootstrap.InitialConfigurationController.URI;

public class InitialConfigurationFilter extends GenericFilterBean
{
	public static String SERVLET_CONTEXT_ATTRIBUTE_CONFIGURATION_COMPLETED = "configurationCompleted";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException
	{
		HttpServletRequest httpServletRequest = asHttpServletRequest(request);
		HttpServletResponse httpServletResponse = asHttpServletResponse(response);
		if (new UrlPathHelper().getPathWithinApplication(httpServletRequest).equals(URI))
		{
			if (isConfigurationCompleted(httpServletRequest))
			{
				httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
			else
			{
				chain.doFilter(request, response);
			}
		}
		else
		{
			if (isConfigurationCompleted(httpServletRequest))
			{
				chain.doFilter(request, response);
			}
			else
			{
				httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + URI);
			}
		}
	}

	private HttpServletRequest asHttpServletRequest(ServletRequest request)
	{
		if (request instanceof HttpServletRequest)
		{
			return (HttpServletRequest) request;
		}
		else
		{
			throw new IllegalArgumentException("'request' is not a HttpServletRequest");
		}
	}

	private HttpServletResponse asHttpServletResponse(ServletResponse response)
	{
		if (response instanceof HttpServletResponse)
		{
			return (HttpServletResponse) response;
		}
		else
		{
			throw new IllegalArgumentException("'response' is not a HttpServletResponse");
		}
	}

	private boolean isConfigurationCompleted(ServletRequest request)
	{
		Object completed = request.getServletContext().getAttribute(SERVLET_CONTEXT_ATTRIBUTE_CONFIGURATION_COMPLETED);
		return completed != null && completed instanceof Boolean && (Boolean) completed;
	}
}
