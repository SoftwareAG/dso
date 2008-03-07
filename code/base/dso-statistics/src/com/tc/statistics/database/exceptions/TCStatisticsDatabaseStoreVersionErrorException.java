/*
 * All content copyright (c) 2003-2008 Terracotta, Inc., except as may otherwise be noted in a separate copyright notice.  All rights reserved.
 */
package com.tc.statistics.database.exceptions;

public class TCStatisticsDatabaseStoreVersionErrorException extends TCStatisticsDatabaseException {
  private final int version;
  
  public TCStatisticsDatabaseStoreVersionErrorException(final int version, final Throwable cause) {
    super("Unexpected error while storing the database structure version as "+version+".", cause);
    this.version = version;
  }

  public int getVersion() {
    return version;
  }
}