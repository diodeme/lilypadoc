package com.diode.lilypadoc.standard.domain;

import lombok.Data;

import java.io.File;

@Data
public class LilypadocContext {

    /**
     * 文章的路径
     * current doc
     */
    private File doc;

    /**
     * md文件所在的仓库根路径 （绝对路径）
     * .md file repo root dir (absolute path)
     */
    private MPath docRootDir;

    /**
     * 最后一层category的路径 （绝对路径）
     * the last category dir path (absolute path)
     */
    private MPath lastCategory;

    /**
     * 文件相对仓库根路径的相对路径
     * doc's absolute path - doc root dir (relative path)
     */
    private MPath docRPath;

    //TODO catgory+group机制要在日常文档中进行实验 看下复不复杂
    /**
     * category深度
     * category depth
     */
    private Integer categoryDepth;

    //TODO 如果根目录作为一个页面，这里是不是不需要了？
    /**
     * 自定义html文件的相对路径
     * custom html file relative path
     */
    private MPath htmlDocRPath;

}
