package com.diode.lilypadoc.standard.domain;

import com.diode.lilypadoc.standard.utils.StringTool;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class MPath {
    private String val = "";

    public static MPath ofHtml(String val) {
        MPath MPath = new MPath();
        MPath.val = Objects.isNull(val) ? "" :
                FilenameUtils.removeExtension(val.replace("\\", "/")) + ".html";
        return MPath;
    }

    public static MPath ofHtml(MPath MPath) {
        MPath newMPath = new MPath();
        newMPath.val = Objects.isNull(MPath) ? "" :
                FilenameUtils.removeExtension(MPath.replace("\\", "/").val) + ".html";
        return newMPath;
    }

    public static MPath ofMd(MPath MPath) {
        MPath newMPath = new MPath();
        newMPath.val = Objects.isNull(MPath) ? "" :
                FilenameUtils.removeExtension(MPath.replace("\\", "/").val) + ".md";
        return newMPath;
    }

    public static MPath of(String val) {
        MPath MPath = new MPath();
        val = Objects.isNull(val) ? "" : val.replace("\\", "/");
        MPath.val = val;
        return MPath;
    }

    public MPath appendChild(String child) {
        return of(val + "/" + child).replace("//","/");
    }

    public MPath appendChild(MPath child) {
        return of(val + "/" + child).replace("//","/");
    }

    public MPath getTargetCate(int targetCate){
        if(targetCate == 0){
            return this;
        }
        String str = val;
        if(!val.startsWith("/")){
            str = "/"+val;
        }
        String[] split = val.split("/");
        if(split.length < targetCate+1){
            return null;
        }
        String[] res = new String[targetCate];
        for (int i = 0; i < targetCate; i++) {
            res[i] = split[i];
        }
        return MPath.of("/"+ StringUtils.join(res, "/"));
    }

    public MPath getParent() {
        if(!val.contains("/")){
            return MPath.of(val);
        }
        return MPath.of(val.substring(0, val.lastIndexOf("/")));
    }

    public String getName() {
        return val.substring(val.lastIndexOf("/") + 1);
    }

    public MPath substring(int beginIndex, int endIndex) {
        return MPath.of(val.substring(beginIndex, endIndex));
    }

    public boolean contains(MPath MPath) {
        return val.contains(MPath.val);
    }

    public boolean in(String content) {
        if (StringUtils.isBlank(content)) {
            return false;
        }
        return content.replace("\\", "/").contains(val);
    }

    public MPath substring(int beginIndex) {
        return MPath.of(val.substring(beginIndex));
    }

    public int indexOf(String str) {
        return val.indexOf(str);
    }

    public MPath replace(CharSequence target, CharSequence replacement){
        return MPath.of(val.replace(target, replacement));
    }

    public MPath remove(MPath target){
        return MPath.of(val.replace("\\", "/").replace(target.val, ""));
    }

    public MPath remove(CharSequence target){
        return MPath.of(val.replace("\\", "/").replace(target, ""));
    }

    public int getLevel(){
        return StringTool.count(val, "/");
    }

    @Override
    public String toString() {
        return val;
    }
}