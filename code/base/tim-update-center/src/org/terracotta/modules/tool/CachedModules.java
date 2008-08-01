/*
 * All content copyright (c) 2003-2008 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package org.terracotta.modules.tool;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.terracotta.modules.tool.config.TerracottaVersion;
import org.terracotta.modules.tool.util.DataLoader;

import com.google.inject.Inject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link Modules} that uses a cached XML file as its data source.
 */
class CachedModules implements Modules {

  private Map<ModuleId, Module> modules;
  private List<Module> qualifiedModules;

  private final String          tcVersion;
  private final DataLoader      dataLoader;

  public CachedModules(@TerracottaVersion String tcVersion, InputStream dataInputStream) throws JDOMException,
      IOException {
    this.tcVersion = tcVersion;
    this.dataLoader = null;
    loadData(dataInputStream);
  }

  @Inject
  public CachedModules(@TerracottaVersion String tcVersion, DataLoader dataLoader) throws JDOMException, IOException {
    this.tcVersion = tcVersion;
    this.dataLoader = dataLoader;
    loadData(new FileInputStream(this.dataLoader.getDataFile()));
  }

  private void loadData(InputStream in) throws JDOMException, IOException {
    if (modules == null) {
      SAXBuilder builder = new SAXBuilder();
      Document document = builder.build(in);
      Element root = document.getRootElement();
      this.modules = new HashMap<ModuleId, Module>();

      List<Element> children = root.getChildren();
      for (Element child : children) {
        Module module = Module.create(this, child);
        this.modules.put(module.getId(), module);
      }
    }
  }

  private boolean qualify(Module module) {
    return module.getTcVersion().equals("*") || module.getTcVersion().equals(tcVersion);
  }

  public String tcVersion() {
    return tcVersion;
  }

  public Module get(ModuleId id) {
    return this.modules.get(id);
  }

  public List<Module> list() {
    if (this.qualifiedModules != null) return this.qualifiedModules;
    
    List<Module> list = new ArrayList<Module>();
    for(Module module : this.modules.values()) {
      if (qualify(module)) list.add(module);
    }
    Collections.sort(list);
    this.qualifiedModules = list;
    return list;
  }

  public List<Module> listLatest() {
    List<Module> list = list();
    Map<String, Module> group = new HashMap<String, Module>();
    for (Module module : list) {
      Module other = group.get(module.getSymbolicName());
      if (other == null) {
        group.put(module.getSymbolicName(), module);
        continue;
      }
      if (module.isOlder(other)) continue;
      group.put(module.getSymbolicName(), module);
    }
    list = new ArrayList<Module>(group.values());
    Collections.sort(list);
    return list;
  }

  /**
   * Return a list of modules matching the groupId and artifactId.
   * 
   * @param groupId
   * @param artifactId
   */
  public List<Module> get(String groupId, String artifactId) {
    List<Module> list = new ArrayList<Module>();
    for (Module module : list()) {
      if (!module.getSymbolicName().equals(ModuleId.computeSymbolicName(groupId, artifactId))) continue;
      list.add(module);
    }
    Collections.sort(list);
    return list;
  }

  /**
   * Return a the latest module matching the groupId and artifactId.
   */
  public Module getLatest(String groupId, String artifactId) {
    List<Module> list = get(groupId, artifactId);
    Collections.reverse(list);
    return list.isEmpty() ? null : list.get(0);
  }

  public List<Module> find(String artifactId, String version, String groupId) {
    List<Module> list = new ArrayList<Module>();
    for (Module module : list()) {
      ModuleId moduleId = module.getId();
      boolean m0 = (artifactId == null) ? true : moduleId.getArtifactId().equals(artifactId);
      boolean m1 = (version == null) ? true : moduleId.getVersion().equals(version);
      boolean m2 = (groupId == null) ? true : moduleId.getGroupId().equals(groupId);
      if (!m0 || !m1 || !m2) continue;
      list.add(module);
    }
    Collections.reverse(list);
    return list;
  }

  /**
   * Get latest from a list of modules. Returns null if the modules in the list are not siblings
   */
  public Module getLatest(List<Module> list) {
    if (list.isEmpty()) return null;

    Module module = list.get(0);
    if (list.size() == 1) return module;

    List<Module> siblings = new ArrayList<Module>(list);
    for (Module sibling : siblings) {
      if (module.isSibling(sibling)) continue;
      return null;
    }

    Collections.sort(siblings);
    Collections.reverse(siblings);
    return siblings.get(0);
  }

}
