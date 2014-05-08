/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package com.tc.operatorevent;

import java.io.Serializable;
import java.util.Date;

public interface TerracottaOperatorEvent extends Serializable, Cloneable {

  public static enum EventLevel {
    INFO, WARN, DEBUG, ERROR, CRITICAL
  }

  public static enum EventSubsystem {
    MEMORY_MANAGER, DGC, CLUSTER_TOPOLOGY, LOCK_MANAGER, DCV2, APPLICATION, SYSTEM_SETUP, RESOURCE
  }

  public static enum EventType {
    MEMORY_LONGGC, DGC_PERIODIC_STARTED, DGC_PERIODIC_FINISHED, DGC_PERIODIC_CANCELED, DGC_INLINE_CLEANUP_STARTED, DGC_INLINE_CLEANUP_FINISHED, DGC_INLINE_CLEANUP_CANCELED, TOPOLOGY_NODE_JOINED, TOPOLOGY_NODE_LEFT, TOPOLOGY_NODE_STATE, TOPOLOGY_HANDSHAKE_REJECT, TOPOLOGY_ACTIVE_LEFT, TOPOLOGY_MIRROR_LEFT, TOPOLOGY_ZAP_RECEIVED, TOPOLOGY_ZAP_ACCEPTED, TOPOLOGY_DB_DIRTY, DCV2_SERVERMAP_EVICTION, SYSTEM_TIME_DIFFERENT, TOPOLOGY_CONFIG_RELOADED, RESOURCE_CAPACITY_NEAR, RESOURCE_CAPACITY_FULL, RESOURCE_CAPACITY_RESTORED, APPLICATION_USER_DEFINED,
  };

  void addNodeName(String nodeId);

  EventLevel getEventLevel();

  String getNodeName();

  Date getEventTime();

  EventSubsystem getEventSubsystem();

  String getEventMessage();

  EventType getEventType();

  /**
   * These methods are there because devconsole does not take enum as the return type while updating the panel Should be
   * dealt with in future
   */
  String getEventLevelAsString();

  String getEventSubsystemAsString();

  String getEventTypeAsString();

  /**
   * These methods are to decide whether one particular event can be collapsed into an existing row in dev console.
   */
  String getCollapseString();

  /**
   * These methods are to determine whether the event has been read before or not in the dev-console
   */
  void markRead();

  void markUnread();

  boolean isRead();

  /**
   * This method is used to get the event in String format.
   */
  String extractAsText();

  TerracottaOperatorEvent cloneEvent();
}
