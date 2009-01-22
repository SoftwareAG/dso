/*
 * All content copyright (c) 2003-2008 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package com.tc.object.msg;

import com.tc.bytes.TCByteBuffer;
import com.tc.io.TCByteBufferOutputStream;
import com.tc.io.TCSerializable;
import com.tc.net.protocol.tcm.MessageChannel;
import com.tc.net.protocol.tcm.MessageMonitor;
import com.tc.net.protocol.tcm.TCMessageHeader;
import com.tc.net.protocol.tcm.TCMessageType;
import com.tc.object.ObjectID;
import com.tc.object.lockmanager.api.LockContext;
import com.tc.object.lockmanager.api.TryLockContext;
import com.tc.object.lockmanager.api.WaitContext;
import com.tc.object.session.SessionID;
import com.tc.object.tx.TransactionID;
import com.tc.util.SequenceID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ClientHandshakeMessageImpl extends DSOMessageBase implements ClientHandshakeMessage {

  private static final byte MANAGED_OBJECT_ID        = 1;
  private static final byte WAIT_CONTEXT             = 2;
  private static final byte LOCK_CONTEXT             = 3;
  private static final byte TRANSACTION_SEQUENCE_IDS = 4;
  private static final byte PENDING_LOCK_CONTEXT     = 5;
  private static final byte RESENT_TRANSACTION_IDS   = 6;
  private static final byte REQUEST_OBJECT_IDS       = 7;
  private static final byte PENDING_TRY_LOCK_CONTEXT = 8;
  private static final byte CLIENT_VERSION           = 9;

  private final Set         objectIDs                = new HashSet();
  private final Set         lockContexts             = new HashSet();
  private final Set         waitContexts             = new HashSet();
  private final Set         pendingLockContexts      = new HashSet();
  private final Set         pendingTryLockContexts   = new HashSet();
  private final List        sequenceIDs              = new ArrayList();
  private final List        txnIDs                   = new ArrayList();
  private boolean           requestObjectIDs;
  private String            clientVersion            = "UNKNOW";

  public ClientHandshakeMessageImpl(SessionID sessionID, MessageMonitor monitor, TCByteBufferOutputStream out,
                                    MessageChannel channel, TCMessageType messageType) {
    super(sessionID, monitor, out, channel, messageType);
  }

  public ClientHandshakeMessageImpl(SessionID sessionID, MessageMonitor monitor, MessageChannel channel,
                                    TCMessageHeader header, TCByteBuffer[] data) {
    super(sessionID, monitor, channel, header, data);
  }

  public Collection getLockContexts() {
    return lockContexts;
  }

  public Collection getWaitContexts() {
    return waitContexts;
  }

  public Set getObjectIDs() {
    return objectIDs;
  }

  public Collection getPendingLockContexts() {
    return pendingLockContexts;
  }

  public Collection getPendingTryLockContexts() {
    return pendingTryLockContexts;
  }

  public List getTransactionSequenceIDs() {
    return sequenceIDs;
  }

  public List getResentTransactionIDs() {
    return txnIDs;
  }

  public boolean isObjectIDsRequested() {
    return this.requestObjectIDs;
  }

  public String getClientVersion() {
    return clientVersion;
  }

  public void addTransactionSequenceIDs(List seqIDs) {
    this.sequenceIDs.addAll(seqIDs);
  }

  public void addResentTransactionIDs(List resentTransactionIDs) {
    this.txnIDs.addAll(resentTransactionIDs);
  }

  public void setIsObjectIDsRequested(boolean request) {
    this.requestObjectIDs = request;
  }

  public void setClientVersion(String version) {
    clientVersion = version;
  }

  public void addLockContext(LockContext ctxt) {
    lockContexts.add(ctxt);
  }

  public void addPendingLockContext(LockContext ctxt) {
    pendingLockContexts.add(ctxt);
  }

  public void addPendingTryLockContext(TryLockContext ctxt) {
    pendingTryLockContexts.add(ctxt);
  }

  public void addWaitContext(WaitContext ctxt) {
    waitContexts.add(ctxt);
  }

  protected void dehydrateValues() {
    for (Iterator i = objectIDs.iterator(); i.hasNext();) {
      putNVPair(MANAGED_OBJECT_ID, ((ObjectID) i.next()).toLong());
    }
    for (Iterator i = lockContexts.iterator(); i.hasNext();) {
      putNVPair(LOCK_CONTEXT, (TCSerializable) i.next());
    }
    for (Iterator i = waitContexts.iterator(); i.hasNext();) {
      putNVPair(WAIT_CONTEXT, (TCSerializable) i.next());
    }
    for (Iterator i = pendingLockContexts.iterator(); i.hasNext();) {
      putNVPair(PENDING_LOCK_CONTEXT, (TCSerializable) i.next());
    }
    for (Iterator i = pendingTryLockContexts.iterator(); i.hasNext();) {
      putNVPair(PENDING_TRY_LOCK_CONTEXT, (TCSerializable) i.next());
    }
    for (Iterator i = sequenceIDs.iterator(); i.hasNext();) {
      putNVPair(TRANSACTION_SEQUENCE_IDS, ((SequenceID) i.next()).toLong());
    }
    for (Iterator i = txnIDs.iterator(); i.hasNext();) {
      putNVPair(RESENT_TRANSACTION_IDS, ((TransactionID) i.next()).toLong());
    }
    putNVPair(REQUEST_OBJECT_IDS, this.requestObjectIDs);
    putNVPair(CLIENT_VERSION, this.clientVersion);
  }

  protected boolean hydrateValue(byte name) throws IOException {
    switch (name) {
      case MANAGED_OBJECT_ID:
        objectIDs.add(new ObjectID(getLongValue()));
        return true;
      case LOCK_CONTEXT:
        lockContexts.add(getObject(new LockContext()));
        return true;
      case WAIT_CONTEXT:
        waitContexts.add(getObject(new WaitContext()));
        return true;
      case PENDING_LOCK_CONTEXT:
        pendingLockContexts.add(getObject(new LockContext()));
        return true;
      case PENDING_TRY_LOCK_CONTEXT:
        pendingTryLockContexts.add(getObject(new TryLockContext()));
        return true;
      case TRANSACTION_SEQUENCE_IDS:
        sequenceIDs.add(new SequenceID(getLongValue()));
        return true;
      case RESENT_TRANSACTION_IDS:
        txnIDs.add(new TransactionID(getLongValue()));
        return true;
      case REQUEST_OBJECT_IDS:
        this.requestObjectIDs = getBooleanValue();
        return true;
      case CLIENT_VERSION:
        this.clientVersion = getStringValue();
        return true;
      default:
        return false;
    }
  }

}
