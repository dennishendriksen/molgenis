package org.molgenis.data.index;


public interface MolgenisIndexUtil
{
	public static final String DEFAULT_INDEX_NAME = "molgenis";

	/**
	 * Delete index
	 * 
	 * @param index
	 */
	public void deleteIndex(String index);

	/**
	 * IndexExists
	 * 
	 * @param index
	 * @return boolean
	 */
	public boolean indexExists(String index);

	/**
	 * Wait until elastic is ready
	 */
	public void waitForYellowStatus();

	/**
	 * Refresh index
	 */
	public void refreshIndex(String index);
}
