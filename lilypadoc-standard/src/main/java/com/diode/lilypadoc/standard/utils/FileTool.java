package com.diode.lilypadoc.standard.utils;

import com.diode.lilypadoc.standard.common.ErrorCode;
import com.diode.lilypadoc.standard.common.Result;
import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.domain.MPath;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@UtilityClass
public class FileTool {
    /**
     * 优先寻找父类加载器
     */
    public Result<String> readResource(ClassLoader classLoader, String name){
        URL resource = classLoader.getResource(name);
        return readResourceContent(resource, name);
    }

    /**
     * 不寻找父类加载器
     * @param classLoader
     * @param name
     * @return
     */
    public Result<String> readResourceFlat(URLClassLoader classLoader, String name){
        URL resource = classLoader.findResource(name);
        return readResourceContent(resource, name);
    }

    public Result<String> readResourceContent(URL url, String name) {
        if(Objects.isNull(url)){
            return Result.fail(StandardErrorCodes.IO_ERROR.of("资源文件不存在"));
        }
        try (InputStream is = url.openStream()) {
            byte[] bytes = new byte[1024];
            int len;
            String content;
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                while ((len = is.read(bytes)) != -1) {
                    bos.write(bytes, 0, len);
                }
                content = bos.toString(StandardCharsets.UTF_8.name());
            }
            return Result.ok(content);
        } catch (IOException e) {
            log.error("获取文件{}出现异常", name, e);
            return Result.fail(StandardErrorCodes.IO_ERROR.of("获取文件" + name + "时出现异常"));
        }
    }

    public ErrorCode writeExistResource(ClassLoader classLoader, String name, String content) {
        URL resource = classLoader.getResource(name);
        if (Objects.isNull(resource)) {
            return StandardErrorCodes.OK;
        }
        try {
            FileUtils.writeStringToFile(new File(resource.getPath()), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("写入文件{}出现异常", name, e);
            return StandardErrorCodes.IO_ERROR.of("写入文件" + name + "出现异常");
        }
        return StandardErrorCodes.OK;
    }

    public ErrorCode writeStringToFile(String path, String content) {
        try {
            FileUtils.writeStringToFile(new File(path), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("写入文件{}出现异常", path, e);
            return StandardErrorCodes.IO_ERROR.of("写入文件" + path + "出现异常");
        }
        return StandardErrorCodes.OK;
    }

    public Result<File> getCategoryDir(File docFile, MPath rootDir, Integer categoryDepth) {
        File root = new File(rootDir.toString());
        //1.获取文件到root的层级
        int gap = 0;
        File parent = docFile;
        while (!parent.equals(root)) {
            gap += 1;
            parent = parent.getParentFile();
        }
        int step = gap - categoryDepth;
        if(step <= 0){
            return Result.fail(StandardErrorCodes.BIZ_ERROR.of("指定层级的文件夹不存在"));
        }
        //2.向上到指定层级
        File lastCategory = docFile;
        for (int i = 0; i < step; i++) {
            lastCategory = lastCategory.getParentFile();
        }
        return Result.ok(lastCategory);
    }

    public Result<URL[]> listAllFilesByExtension(String root, String extension) {
        File parentDir = new File(root);
        if (!parentDir.exists()) {
            return Result.fail(StandardErrorCodes.VALIDATE_ERROR.of("root文件夹不存在"));
        }
        File[] files = parentDir.listFiles();
        if (null == files) {
            return Result.ok(new URL[0]);
        }

        // 从目录下筛选出所有jar文件
        List<File> jarFiles = Arrays.stream(files)
                .filter(file -> file.getName().endsWith(extension))
                .collect(Collectors.toList());
        URL[] urls = new URL[jarFiles.size()];
        for (int i = 0; i < jarFiles.size(); i++) {
            String url = "";
            try {
                url = "file:" + jarFiles.get(i).getAbsolutePath();
                // 加上 "file:" 前缀表示本地文件
                urls[i] = new URL(url);
            } catch (IOException e) {
                log.error("获取文件异常，路径:{}", url, e);
                return Result.fail(StandardErrorCodes.IO_ERROR.of("获取文件发生异常"));
            }
        }
        return Result.ok(urls);
    }

    public String removeExtension(String fileName) {
        return FilenameUtils.removeExtension(fileName);
    }

    public ErrorCode copyFile(File source, File target, boolean deleteIfFail) {
        try {
            if(source.isFile()){
                FileUtils.copyFile(source, target);
            }else {
                FileUtils.copyDirectory(source, target);
            }
            return StandardErrorCodes.OK;
        } catch (IOException e1) {
            if (deleteIfFail) {
                try {
                    log.error("复制文件失败，开始尝试删除目标文件{}", target, e1);
                    if(source.isFile()){
                        Files.deleteIfExists(target.toPath());
                    }else {
                        FileUtils.deleteDirectory(target);
                    }
                } catch (IOException e2) {
                    log.error("复制文件失败，删除文件失败，目标文件:{}", target, e2);
                    return StandardErrorCodes.IO_ERROR.of("文件" + target.getName() + "复制失败，删除失败");
                }
            }
            return StandardErrorCodes.IO_ERROR.of("文件" + target.getName() + "复制失败");
        }
    }
    
    public ErrorCode delete(File file){
        if(file.isFile()){
            return deleteIfExist(file);
        }else {
            return deleteDir(file);
        }
    }

    public ErrorCode deleteIfExist(File file){
        try {
            Files.deleteIfExists(file.toPath());
        }catch (IOException e){
            log.error("文件{}删除失败", file.getName());
            return StandardErrorCodes.IO_ERROR.of("文件"+file.getName()+"删除失败");
        }
        return StandardErrorCodes.OK;
    }

    public ErrorCode deleteDir(File file){
        try {
            FileUtils.deleteDirectory(file);
        }catch (IOException e){
            log.error("文件夹{}删除失败", file.getName());
            return StandardErrorCodes.IO_ERROR.of("文件夹"+file.getName()+"删除失败");
        }
        return StandardErrorCodes.OK;
    }

    public Result<String> readFileToString(File file){
        try {
            return Result.ok(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
        }catch (IOException e){
            log.error("文件{}读取失败", file.getName());
            return Result.fail(StandardErrorCodes.IO_ERROR.of("文件"+file.getName()+"读取失败"));
        }
    }

    public File findSameDoc(File file, MPath target, boolean strict) {
        if (strict && file.isFile() && target.in(file.getPath())) {
            return file;
        }
        if (!strict && file.isFile() && file.getName().equals(target.getName())) {
            return file;
        }
        File[] files = file.listFiles();
        if (Objects.isNull(files)) {
            return null;
        }
        for (File child : files) {
            File sameDoc = findSameDoc(child, target, true);
            if (Objects.nonNull(sameDoc)) {
                return sameDoc;
            }
            sameDoc = findSameDoc(child, MPath.of(target.getName()), false);
            if (Objects.nonNull(sameDoc)) {
                return sameDoc;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        Result<File> firstFile = findFirstFile(new File("D:\\Projects\\code\\java\\lilypadoc\\.docs\\夏\\框架\\thrift"), ".md");
        System.out.println(firstFile.get().getName());
    }

    public Result<File> findFirstFile(File file, String suffix) {
        try(Stream<Path> walk = Files.walk(file.toPath())) {
            Optional<Path> optionalPath = walk.filter(path -> path.toString().endsWith(suffix))
                    .findFirst();
            if(optionalPath.isPresent()){
                Path path = optionalPath.get();
                return Result.ok(path.toFile());
            }
        } catch (Exception e) {
            log.error("findFirstFile error.", e);
        }
        return Result.fail(StandardErrorCodes.BIZ_ERROR.of("未找到相应文件"));
    }

    public static Result<List<File>> copyJarFile(JarFile jarFile, String targetDir, String destinationPath, boolean deleteIfFail) {
        List<File> syncFileList = new ArrayList<>();
        try {
            // 创建目标路径
            Path destinationDir = Paths.get(destinationPath);
            Files.createDirectories(destinationDir);
            // 获取 JAR 文件中的所有条目
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if(!entry.getName().startsWith(targetDir + "/")){
                    continue;
                }
                // 构建目标路径
                Path destinationFilePath = destinationDir.resolve(entry.getName());

                // 如果是目录，创建对应的目录
                if (entry.isDirectory()) {
                    Files.createDirectories(destinationFilePath);
                } else {
                    // 如果是文件，拷贝文件
                    try (InputStream entryInputStream = jarFile.getInputStream(entry)) {
                        Files.copy(entryInputStream, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
                        syncFileList.add(destinationFilePath.toFile());
                    } catch (IOException e1) {
                        if (deleteIfFail) {
                            try {
                                log.error("复制jar文件失败，目标文件:{} 开始尝试删除根文件夹:{}", entry.getName(),
                                        destinationDir, e1);
                                File target = new File(destinationDir.toUri());
                                if (target.isFile()) {
                                    Files.deleteIfExists(destinationDir);
                                } else {
                                    FileUtils.deleteDirectory(target);
                                }
                            } catch (IOException e2) {
                                log.error("复制jar文件失败，删除jar文件失败，目标文件:{} 根文件夹:{}", entry.getName(),
                                        destinationDir, e2);
                                return Result.fail(
                                        StandardErrorCodes.IO_ERROR.of("文件" + entry.getName() + "复制失败，删除失败"));
                            }
                        }
                        return Result.fail(StandardErrorCodes.IO_ERROR.of("文件" + entry.getName() + "复制失败"));
                    }
                }
            }
        }catch (IOException e){
            log.error("复制jar文件失败", e);
            return Result.fail(StandardErrorCodes.IO_ERROR.of("复制jar文件失败"));
        }
        return Result.ok(syncFileList);
    }

    public static Result<JarFile> getJarFileFromUrl(URL jarFileUrl){
        try {
            JarURLConnection jarURLConnection = (JarURLConnection) jarFileUrl.openConnection();
            return Result.ok(jarURLConnection.getJarFile());
        }catch (IOException e){
            log.error("获取jarFile发生IO异常, jarFile:{}", jarFileUrl);
            return Result.fail(StandardErrorCodes.IO_ERROR.of("获取jarFile发生IO异常"));
        }
    }

    public static Result<List<MPath>> findTargetDir(MPath rootPath, String target, Integer deep){
        List<MPath> pathList = new ArrayList<>();
        Path startDir = Paths.get(rootPath.toString());
        try(Stream<Path> walk = Files.walk(startDir)) {
            walk.filter(Files::isDirectory)
                    .filter(path -> path.getFileName().toString().equals(target))
                    .filter(path -> {
                        if(Objects.isNull(deep)){
                            return true;
                        }
                        return deep == getDeep(rootPath, MPath.of(path.toString()));
                    }).forEach(path -> pathList.add(MPath.of(path.toString()).remove(rootPath)));
        }catch (IOException e){
            log.error("获取指定目录路径时发生IO异常, rootPath:{}, target:{}", rootPath, target, e);
            return Result.fail(StandardErrorCodes.IO_ERROR.of("获取指定目录路径时发生IO异常"));
        }
        return Result.ok(pathList);
    }

    public static Result<List<MPath>> findTargetFile(MPath rootPath, String target){
        List<MPath> pathList = new ArrayList<>();
        Path startDir = Paths.get(rootPath.toString());
        try(Stream<Path> walk = Files.walk(startDir)) {
            walk.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(target))
                    .forEach(path -> pathList.add(MPath.of(path.toString()).remove(rootPath)));
        }catch (IOException e){
            log.error("获取指定文件路径时发生IO异常, rootPath:{}, target:{}", rootPath, target, e);
            return Result.fail(StandardErrorCodes.IO_ERROR.of("获取指定文件路径时发生IO异常"));
        }
        return Result.ok(pathList);
    }

    private static int getDeep(MPath rootPath, MPath target){
        if(Objects.isNull(rootPath) || Objects.isNull(target)){
            return -1;
        }
        MPath parent = target;
        int deep =0;
        while(!parent.toString().equals(rootPath.toString())){
            MPath parentParent = parent.getParent();
            if(parentParent.toString().equals(parent.toString())){
                deep = -1;
                break;
            }
            parent = parentParent;
            deep++;
        }
        return deep;
    }
}
