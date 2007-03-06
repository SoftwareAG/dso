/*
 * All content copyright (c) 2003-2007 Terracotta, Inc., except as may otherwise be noted in a separate copyright notice.  All rights reserved.
 */
package com.tc.l2.context;

import com.tc.async.api.EventContext;
import com.tc.object.msg.CommitTransactionMessage;

import java.util.List;
import java.util.Set;

public class InComingTransactionContext implements EventContext {

  private final CommitTransactionMessage ctm;
  private final List txns;
  private final Set serverTxnIDs;

  public InComingTransactionContext(CommitTransactionMessage ctm, List txns, Set serverTxnIDs) {
    this.ctm = ctm;
    this.txns = txns;
    this.serverTxnIDs = serverTxnIDs;
  }

  public CommitTransactionMessage getCommitTransactionMessage() {
    return ctm;
  }

  public Set getServerTxnIDs() {
    return serverTxnIDs;
  }

  public List getTxns() {
    return txns;
  }

}
