package com.example.imagetotext.core;


import java.io.Serializable;

public class TextOCR implements Serializable {
    public String line, groupId="";
    public int topMin, topMax, align = MyConstants.AlignLeft, left, right;
    public int lastTopDifference = 0, joinyCount = 0, firstJoinyLeft = 0, firstJoinyRight = 0;
    public boolean isSecondaryContent = false;
    public boolean isBold = false;
    public boolean isUnderline = false;
    public int spanCount = 0;

    public TextOCR(String ln) {
        this.line = ln;
    }
    public void joinGroup(String grId) {
        this.isSecondaryContent = true;
        this.groupId = grId;
    }
    public void incrementJoinyCount(int fJL, int fJR) {
        this.firstJoinyLeft = fJL;
        this.firstJoinyRight = fJR;
        this.joinyCount++;
    }
    public void incrementJoinyCount() {
        this.joinyCount++;
    }
    public void setParams(int tpMin, int tpMax, int lft, int rght) {
        this.topMin = tpMin;
        this.topMax = tpMax;
        this.left = lft;
        this.right = rght;
    }
    public void setGroupId(String gid) {
        this.groupId = gid;
    }
    public void setAlign(int aln) {
        this.align = aln;
    }
    public void setLastTopDifference(int lTD) {
       this.lastTopDifference = lTD;
    }
}
