/*
 * All content copyright (c) 2003-2008 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package com.tc.cluster;

import java.util.Set;

public interface DsoClusterTopology {
  public Set<DsoNode> getNodes();
}