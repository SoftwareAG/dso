/*
 * All content copyright (c) 2003-2006 Terracotta, Inc., except as may otherwise be noted in a separate copyright notice.  All rights reserved.
 */
package com.tctest.performance.sampledata;

public final class OrganicObjectGraphNode_6 extends OrganicObjectGraph {

  private int size = 44;
  private int[] types = new int[] { 2, 0, 2, 0, 0, 3, 1, 3, 2, 2, 1, 2, 3, 0, 3, 0, 1, 2, 1, 0, 0, 0, 2, 2, 1, 2, 0, 1, 0, 2, 1, 3, 2, 2, 1, 1, 1, 2, 0, 0, 3, 2, 3, 0 };

  private short f0;
  private int f1;
  private short f2;
  private int f3;
  private int f4;
  private double f5;
  private String f6;
  private double f7;
  private short f8;
  private short f9;
  private String f10;
  private short f11;
  private double f12;
  private int f13;
  private double f14;
  private int f15;
  private String f16;
  private short f17;
  private String f18;
  private int f19;
  private int f20;
  private int f21;
  private short f22;
  private short f23;
  private String f24;
  private short f25;
  private int f26;
  private String f27;
  private int f28;
  private short f29;
  private String f30;
  private double f31;
  private short f32;
  private short f33;
  private String f34;
  private String f35;
  private String f36;
  private short f37;
  private int f38;
  private int f39;
  private double f40;
  private short f41;
  private double f42;
  private int f43;

  public OrganicObjectGraphNode_6(int sequenceNumber, String envKey) {
    super(sequenceNumber, envKey);
  }

  public OrganicObjectGraphNode_6() {
    super();
  }

  protected int getSize() {
    return size;
  }

  protected int getType(int index) {
    return types[index];
  }

  protected void setValue(int index, short value) {
    switch (index) {
      case 0:
        f0 = value;
      case 2:
        f2 = value;
      case 8:
        f8 = value;
      case 9:
        f9 = value;
      case 11:
        f11 = value;
      case 17:
        f17 = value;
      case 22:
        f22 = value;
      case 23:
        f23 = value;
      case 25:
        f25 = value;
      case 29:
        f29 = value;
      case 32:
        f32 = value;
      case 33:
        f33 = value;
      case 37:
        f37 = value;
      case 41:
        f41 = value;
      default:
        break;
    }
  }

  protected void setValue(int index, int value) {
    switch (index) {
      case 1:
        f1 = value;
      case 3:
        f3 = value;
      case 4:
        f4 = value;
      case 13:
        f13 = value;
      case 15:
        f15 = value;
      case 19:
        f19 = value;
      case 20:
        f20 = value;
      case 21:
        f21 = value;
      case 26:
        f26 = value;
      case 28:
        f28 = value;
      case 38:
        f38 = value;
      case 39:
        f39 = value;
      case 43:
        f43 = value;
      default:
        break;
    }
  }

  protected void setValue(int index, double value) {
    switch (index) {
      case 5:
        f5 = value;
      case 7:
        f7 = value;
      case 12:
        f12 = value;
      case 14:
        f14 = value;
      case 31:
        f31 = value;
      case 40:
        f40 = value;
      case 42:
        f42 = value;
      default:
        break;
    }
  }

  protected void setValue(int index, String value) {
    switch (index) {
      case 6:
        f6 = value;
      case 10:
        f10 = value;
      case 16:
        f16 = value;
      case 18:
        f18 = value;
      case 24:
        f24 = value;
      case 27:
        f27 = value;
      case 30:
        f30 = value;
      case 34:
        f34 = value;
      case 35:
        f35 = value;
      case 36:
        f36 = value;
      default:
        break;
    }
  }

  public boolean equals(Object rawObj) {
    if (!(rawObj instanceof OrganicObjectGraphNode_6)) { System.out.println("not instanceof"); System.out.println(rawObj.getClass().getName() + "=OrganicObjectGraphNode_6"); return false; }
    OrganicObjectGraphNode_6 obj = (OrganicObjectGraphNode_6) rawObj;
    if (f0 != obj.f0) return false;
    if (f1 != obj.f1) return false;
    if (f2 != obj.f2) return false;
    if (f3 != obj.f3) return false;
    if (f4 != obj.f4) return false;
    if (f5 != obj.f5) return false;
    if (!("" + f6).equals("" + obj.f6)) return false;
    if (f7 != obj.f7) return false;
    if (f8 != obj.f8) return false;
    if (f9 != obj.f9) return false;
    if (!("" + f10).equals("" + obj.f10)) return false;
    if (f11 != obj.f11) return false;
    if (f12 != obj.f12) return false;
    if (f13 != obj.f13) return false;
    if (f14 != obj.f14) return false;
    if (f15 != obj.f15) return false;
    if (!("" + f16).equals("" + obj.f16)) return false;
    if (f17 != obj.f17) return false;
    if (!("" + f18).equals("" + obj.f18)) return false;
    if (f19 != obj.f19) return false;
    if (f20 != obj.f20) return false;
    if (f21 != obj.f21) return false;
    if (f22 != obj.f22) return false;
    if (f23 != obj.f23) return false;
    if (!("" + f24).equals("" + obj.f24)) return false;
    if (f25 != obj.f25) return false;
    if (f26 != obj.f26) return false;
    if (!("" + f27).equals("" + obj.f27)) return false;
    if (f28 != obj.f28) return false;
    if (f29 != obj.f29) return false;
    if (!("" + f30).equals("" + obj.f30)) return false;
    if (f31 != obj.f31) return false;
    if (f32 != obj.f32) return false;
    if (f33 != obj.f33) return false;
    if (!("" + f34).equals("" + obj.f34)) return false;
    if (!("" + f35).equals("" + obj.f35)) return false;
    if (!("" + f36).equals("" + obj.f36)) return false;
    if (f37 != obj.f37) return false;
    if (f38 != obj.f38) return false;
    if (f39 != obj.f39) return false;
    if (f40 != obj.f40) return false;
    if (f41 != obj.f41) return false;
    if (f42 != obj.f42) return false;
    if (f43 != obj.f43) return false;
    return super.equals(obj);
  }
}
