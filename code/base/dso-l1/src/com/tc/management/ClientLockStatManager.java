/*
 * All content copyright (c) 2003-2007 Terracotta, Inc., except as may otherwise be noted in a separate copyright notice.  All rights reserved.
 */
package com.tc.management;

import com.tc.async.api.Sink;
import com.tc.object.lockmanager.api.LockID;
import com.tc.object.net.DSOClientMessageChannel;

public interface ClientLockStatManager {
  public final static ClientLockStatManager NULL_CLIENT_LOCK_STAT_MANAGER = new ClientLockStatManager() {

    public void recordStackTrace(LockID lockID, Throwable t) {
      // do nothing
    }

    public void enableStat(LockID lockID) {
      // do nothing
    }

    public boolean isStatEnabled(LockID lockID) {
      return false;
    }

    public void disableStat(LockID lockID) {
      // do nothing
    }

    public void start(DSOClientMessageChannel channel, Sink sink) {
      // do nothing
    }

    public int getBatchSize() {
      return 0;
    }
  };
  
  public void start(DSOClientMessageChannel channel, Sink sink);
  
  public void recordStackTrace(LockID lockID, Throwable t);
  
  public void enableStat(LockID lockID);
  
  public void disableStat(LockID lockID);
  
  public boolean isStatEnabled(LockID lockID);
  
  public int getBatchSize();
}
