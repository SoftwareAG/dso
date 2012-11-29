/*
 * All content copyright (c) 2003-2008 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package com.tc.objectserver.handler;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.mockito.ArgumentCaptor;

import com.tc.async.api.EventContext;
import com.tc.async.api.Sink;
import com.tc.async.api.Stage;
import com.tc.net.ClientID;
import com.tc.object.dmi.DmiDescriptor;
import com.tc.object.dna.api.MetaDataReader;
import com.tc.object.locks.LockID;
import com.tc.object.locks.Notify;
import com.tc.object.locks.NotifyImpl;
import com.tc.object.locks.StringLockID;
import com.tc.object.locks.ThreadID;
import com.tc.object.tx.TransactionID;
import com.tc.object.tx.TxnBatchID;
import com.tc.object.tx.TxnType;
import com.tc.objectserver.context.ApplyTransactionContext;
import com.tc.objectserver.context.BroadcastChangeContext;
import com.tc.objectserver.core.api.ServerConfigurationContext;
import com.tc.objectserver.core.impl.TestServerConfigurationContext;
import com.tc.objectserver.gtx.ServerGlobalTransactionManager;
import com.tc.objectserver.impl.ObjectInstanceMonitorImpl;
import com.tc.objectserver.locks.LockManager;
import com.tc.objectserver.locks.NotifiedWaiters;
import com.tc.objectserver.locks.ServerLock;
import com.tc.objectserver.api.Transaction;
import com.tc.objectserver.api.TransactionListener;
import com.tc.objectserver.api.TransactionProvider;
import com.tc.objectserver.tx.ServerTransaction;
import com.tc.objectserver.tx.ServerTransactionImpl;
import com.tc.objectserver.tx.ServerTransactionManager;
import com.tc.objectserver.tx.TransactionalObjectManager;
import com.tc.util.SequenceID;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ApplyTransactionChangeHandlerTest extends TestCase {

  private ApplyTransactionChangeHandler  handler;
  private LockManager                    lockManager;
  private Sink                           broadcastSink;
  private ArgumentCaptor<NotifiedWaiters> notifiedWaitersArgumentCaptor;

  @Override
  public void setUp() throws Exception {
    this.lockManager = mock(LockManager.class);
    this.notifiedWaitersArgumentCaptor = ArgumentCaptor.forClass(NotifiedWaiters.class);
    TransactionProvider persistenceTransactionProvider = mock(TransactionProvider.class);
    Transaction persistenceTransaction = new Transaction() {
        LinkedList<TransactionListener> listeners = new LinkedList<TransactionListener>();
          @Override
          public void commit() {
              for ( TransactionListener l : listeners ) {
                  l.committed(this);
              }
          }

          @Override
          public void abort() {
              for ( TransactionListener l : listeners ) {
                  l.committed(this);
              }
          }

          @Override
          public void addTransactionListener(TransactionListener l) {
             listeners.add(l);
          }
        
    };
    when(persistenceTransactionProvider.newTransaction()).thenReturn(persistenceTransaction);

    this.handler = new ApplyTransactionChangeHandler(new ObjectInstanceMonitorImpl(), mock(ServerGlobalTransactionManager.class),
        persistenceTransactionProvider);

    this.broadcastSink = mock(Sink.class);
    Stage broadcastStage = mock(Stage.class);
    Stage recycle = mock(Stage.class);
    when(broadcastStage.getSink()).thenReturn(broadcastSink);
    when(recycle.getSink()).thenReturn(mock(Sink.class));
    TestServerConfigurationContext context = new TestServerConfigurationContext();
    context.transactionManager = mock(ServerTransactionManager.class);
    context.txnObjectManager = mock(TransactionalObjectManager.class);
    context.addStage(ServerConfigurationContext.BROADCAST_CHANGES_STAGE, broadcastStage);
    context.addStage(ServerConfigurationContext.COMMIT_CHANGES_STAGE, mock(Stage.class));
    context.addStage(ServerConfigurationContext.SERVER_MAP_CAPACITY_EVICTION_STAGE, mock(Stage.class));
    context.addStage(ServerConfigurationContext.APPLY_CHANGES_STAGE, recycle);
    context.lockManager = this.lockManager;

    this.handler.initializeContext(context);
  }

  public void testLockManagerNotifyOnNoApply() throws Exception {
    ServerTransaction tx = createServerTransaction();
    this.handler.handleEvent(new ApplyTransactionContext(tx));
    verifyNotifies(tx);
  }

  public void testLockManagerNotifyOnApply() throws Exception {
    ServerTransaction tx = createServerTransaction();
    this.handler.handleEvent(new ApplyTransactionContext(tx, Collections.emptyMap()));
    this.handler.handleEvent(new ApplyTransactionContext(null));
    verifyNotifies(tx);
  }

  private void verifyNotifies(ServerTransaction tx) {
    verify(lockManager, times(tx.getNotifies()
        .size())).notify(any(LockID.class), any(ClientID.class), any(ThreadID.class), any(ServerLock.NotifyAction.class), any(NotifiedWaiters.class));
    verify(broadcastSink, atLeastOnce()).add(any(EventContext.class));
    for (Notify notify : (Collection<Notify>) tx.getNotifies()) {
      verify(lockManager).notify(eq(notify.getLockID()), eq((ClientID)tx.getSourceID()), eq(notify.getThreadID()),
          eq(notify.getIsAll() ? ServerLock.NotifyAction.ALL : ServerLock.NotifyAction.ONE),
          notifiedWaitersArgumentCaptor.capture());
    }

    verify(broadcastSink).add(argThat(new BroadcastNotifiedWaiterMatcher(notifiedWaitersArgumentCaptor.getValue())));
  }

  private class BroadcastNotifiedWaiterMatcher extends BaseMatcher<EventContext> {
    private final NotifiedWaiters notifiedWaiters;

    private BroadcastNotifiedWaiterMatcher(final NotifiedWaiters notifiedWaiters) {
      this.notifiedWaiters = notifiedWaiters;
    }

    @Override
    public boolean matches(final Object o) {
      if (o instanceof BroadcastChangeContext) {
        if (notifiedWaiters == null) {
          return ((BroadcastChangeContext)o).getNewlyPendingWaiters() == null;
        } else {
          return notifiedWaiters.equals(((BroadcastChangeContext)o).getNewlyPendingWaiters());
        }
      } else {
        return false;
      }
    }

    @Override
    public void describeTo(final Description description) {

    }
  }

  private ServerTransaction createServerTransaction() {
    final ClientID cid = new ClientID(1);
    LockID[] lockIDs = new LockID[] { new StringLockID("1") };

    List<Notify> notifies = new LinkedList<Notify>();
    for (int i = 0; i < 10; i++) {
      notifies.add(new NotifyImpl(new StringLockID("" + i), new ThreadID(i), i % 2 == 0));
    }

    return new ServerTransactionImpl(new TxnBatchID(1), new TransactionID(1), new SequenceID(1),
        lockIDs, cid, Collections.emptyList(), null,
        Collections.emptyMap(), TxnType.NORMAL, notifies, DmiDescriptor.EMPTY_ARRAY,
        new MetaDataReader[0], 1, new long[0]);
  }
}
