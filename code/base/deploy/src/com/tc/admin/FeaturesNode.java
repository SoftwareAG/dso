/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package com.tc.admin;

import org.apache.commons.lang.StringUtils;

import com.tc.admin.common.BrowserLauncher;
import com.tc.admin.common.ComponentNode;
import com.tc.admin.common.XScrollPane;
import com.tc.admin.common.XTextPane;
import com.tc.admin.dso.ClientsNode;
import com.tc.admin.model.IClusterModel;
import com.tc.admin.model.IServer;
import com.tc.management.beans.TIMByteProviderMBean;

import java.awt.Component;
import java.awt.Cursor;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;

public class FeaturesNode extends ComponentNode implements NotificationListener, HyperlinkListener {
  protected IAdminClientContext               adminClientContext;
  protected IClusterModel                     clusterModel;
  protected ClusterNode                       clusterNode;
  protected ClusterListener                   clusterListener;
  protected XScrollPane                       myApplicationPanel;
  protected ClientsNode                       clientsNode;
  protected Map<String, Feature>              activeFeatureMap;
  protected Map<Feature, FeatureNode>         nodeMap;

  protected static final Map<String, Feature> allFeaturesMap = new LinkedHashMap<String, Feature>();

  public FeaturesNode(ClusterNode clusterNode, IAdminClientContext adminClientContext, IClusterModel clusterModel) {
    super(adminClientContext.getString("cluster.features"));
    this.adminClientContext = adminClientContext;
    this.clusterModel = clusterModel;
    this.clusterNode = clusterNode;
    this.clusterListener = new ClusterListener(clusterModel);
    activeFeatureMap = new LinkedHashMap<String, Feature>();
    nodeMap = new LinkedHashMap<Feature, FeatureNode>();

    clusterModel.addPropertyChangeListener(clusterListener);
    if (clusterModel.getActiveCoordinator() != null) {
      init();
    }
  }

  protected synchronized IClusterModel getClusterModel() {
    return clusterModel;
  }

  private class ClusterListener extends AbstractClusterListener {
    private ClusterListener(IClusterModel clusterModel) {
      super(clusterModel);
    }

    @Override
    protected void handleActiveCoordinator(IServer oldActive, IServer newActive) {
      IClusterModel theClusterModel = getClusterModel();
      if (theClusterModel == null) { return; }

      if (newActive != null) {
        init();
      }
    }
  }

  private void addMBeanServerDelegateListener() {
    IServer activeCoord = clusterModel.getActiveCoordinator();
    try {
      ObjectName on = new ObjectName("JMImplementation:type=MBeanServerDelegate");
      activeCoord.addNotificationListener(on, this);
    } catch (Exception e) {
      /**/
    }
  }

  private void removeMBeanServerDelegateListener() {
    IServer activeCoord = clusterModel.getActiveCoordinator();
    if (activeCoord != null) {
      try {
        ObjectName on = new ObjectName("JMImplementation:type=MBeanServerDelegate");
        activeCoord.addNotificationListener(on, this);
      } catch (Exception e) {
        /**/
      }
    }
  }

  public void init() {
    addMBeanServerDelegateListener();

    Set<ObjectName> s;
    try {
      s = clusterModel.getActiveCoordinator().queryNames(null, null);
    } catch (Exception e) {
      s = Collections.emptySet();
    }

    Iterator<ObjectName> iter = s.iterator();
    while (iter.hasNext()) {
      testRegisterFeature(iter.next());
    }
    ensureFeatureNodes();
  }

  private void ensureFeatureNodes() {
    Iterator<Map.Entry<String, Feature>> featureIter = activeFeatureMap.entrySet().iterator();
    while (featureIter.hasNext()) {
      Map.Entry<String, Feature> entry = featureIter.next();
      Feature feature = entry.getValue();
      if (!nodeMap.containsKey(feature)) {
        FeatureNode featureNode = newFeatureNode(feature);
        if (getParent() == null) {
          clusterNode.insertChild(this, 0);
        }
        add(featureNode);
        nodeStructureChanged();
        adminClientContext.getAdminClientController().expand(featureNode);
      }
    }
  }

  private FeatureNode newFeatureNode(Feature feature) {
    FeatureNode node = new FeatureNode(feature, adminClientContext, clusterModel);
    nodeMap.put(feature, node);
    return node;
  }

  protected boolean testRegisterFeature(ObjectName on) {
    if (StringUtils.equals("org.terracotta", on.getDomain()) && StringUtils.equals("Loader", on.getKeyProperty("type"))) {
      String symbolicName = on.getKeyProperty("feature");
      String name = on.getKeyProperty("name");
      Feature feature;
      synchronized (allFeaturesMap) {
        feature = allFeaturesMap.get(symbolicName);
        if (feature == null) {
          feature = new Feature(symbolicName, name);
          allFeaturesMap.put(symbolicName, feature);
        }
      }
      IServer activeCoord = clusterModel.getActiveCoordinator();
      TIMByteProviderMBean byteProvider = activeCoord.getMBeanProxy(on, TIMByteProviderMBean.class);
      feature.getFeatureClassLoader().addTIMByteProvider(on, byteProvider);
      if (!activeFeatureMap.containsKey(symbolicName)) {
        activeFeatureMap.put(symbolicName, feature);
        return true;
      }
    }
    return false;
  }

  protected boolean testUnregisterFeature(ObjectName on) {
    if (StringUtils.equals("org.terracotta", on.getDomain()) && StringUtils.equals("Loader", on.getKeyProperty("type"))) {
      String symbolicName = on.getKeyProperty("feature");
      Feature feature = activeFeatureMap.get(symbolicName);
      if (feature != null) {
        feature.getFeatureClassLoader().removeTIMByteProvider(on);
        if (feature.getFeatureClassLoader().getTIMByteProviderCount() == 0) {
          tearDownFeature(feature);
          activeFeatureMap.remove(feature.getSymbolicName());
        }
        return true;
      }
    }
    return false;
  }

  private void tearDownFeature(Feature feature) {
    FeatureNode node = nodeMap.remove(feature);
    if (node != null) {
      removeChild(node);
      node.tearDown();
    }
    if (getParent() != null && getChildCount() == 0) {
      noTearDown = true;
      clusterNode.removeChild(this);
      noTearDown = false;
    }
  }

  public void handleNotification(Notification notification, Object handback) {
    IClusterModel theClusterModel = getClusterModel();
    if (theClusterModel == null) { return; }

    String type = notification.getType();
    if (notification instanceof MBeanServerNotification) {
      final MBeanServerNotification mbsn = (MBeanServerNotification) notification;
      if (type.equals(MBeanServerNotification.REGISTRATION_NOTIFICATION)) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            if (testRegisterFeature(mbsn.getMBeanName())) {
              ensureFeatureNodes();
            }
          }
        });
      } else if (type.equals(MBeanServerNotification.UNREGISTRATION_NOTIFICATION)) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            testUnregisterFeature(mbsn.getMBeanName());
          }
        });
      }
    }
  }

  @Override
  public Component getComponent() {
    if (myApplicationPanel == null) {
      XTextPane textPane = new XTextPane();
      myApplicationPanel = new XScrollPane(textPane);
      try {
        textPane.setPage(getClass().getResource("MyApplication.html"));
      } catch (Exception e) {
        e.printStackTrace();
      }
      textPane.setEditable(false);
      textPane.addHyperlinkListener(this);
    }
    return myApplicationPanel;
  }

  public void hyperlinkUpdate(HyperlinkEvent e) {
    XTextPane textPane = (XTextPane) e.getSource();
    HyperlinkEvent.EventType type = e.getEventType();
    Element elem = e.getSourceElement();

    if (elem == null || type == HyperlinkEvent.EventType.ENTERED || type == HyperlinkEvent.EventType.EXITED) { return; }

    if (textPane.getCursor().getType() != Cursor.WAIT_CURSOR) {
      AttributeSet anchor = (AttributeSet) elem.getAttributes().getAttribute(HTML.Tag.A);
      String url = (String) anchor.getAttribute(HTML.Attribute.HREF);
      BrowserLauncher.openURL(url);
    }
  }

  /**
   * Generally a node being removed is to be torn down but this node needs to come and go dependent on whether or not it
   * has any children.
   */
  private boolean noTearDown = false;

  @Override
  public void tearDown() {
    if (noTearDown) { return; }

    removeMBeanServerDelegateListener();

    clusterModel.removePropertyChangeListener(clusterListener);
    clusterListener.tearDown();

    synchronized (activeFeatureMap) {
      for (Feature feature : activeFeatureMap.values()) {
        tearDownFeature(feature);
      }
      activeFeatureMap.clear();
    }

    synchronized (this) {
      adminClientContext = null;
      clusterModel = null;
      clusterListener = null;
      activeFeatureMap = null;
      nodeMap.clear();
      nodeMap = null;
      myApplicationPanel = null;
    }

    super.tearDown();
  }
}
