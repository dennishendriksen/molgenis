package org.molgenis.web.bootstrap;

public class ConfigurationRequest
{
	private String hostname;

	private int port;

	private String database;

	private String username;

	private String password;

	private String adminPassword;

	private String adminEmail;

	private String clusterName;

	private String transportAddresses;

	public String getHostname()
	{
		return hostname;
	}

	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}

	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public String getDatabase()
	{
		return database;
	}

	public void setDatabase(String database)
	{
		this.database = database;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getAdminPassword()
	{
		return adminPassword;
	}

	public void setAdminPassword(String adminPassword)
	{
		this.adminPassword = adminPassword;
	}

	public String getAdminEmail()
	{
		return adminEmail;
	}

	public void setAdminEmail(String adminEmail)
	{
		this.adminEmail = adminEmail;
	}

	public String getClusterName()
	{
		return clusterName;
	}

	public void setClusterName(String clusterName)
	{
		this.clusterName = clusterName;
	}

	public String getTransportAddresses()
	{
		return transportAddresses;
	}

	public void setTransportAddresses(String transportAddresses)
	{
		this.transportAddresses = transportAddresses;
	}
}
