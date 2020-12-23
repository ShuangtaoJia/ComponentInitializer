package com.billy.android.register1

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

/**
 *
 * @author billy.qi
 * @since 17/3/21 11:48
 */
class RegisterTransform extends Transform {

    Project project
    AutoRegisterConfig config;

    RegisterTransform(Project project) {
        this.project = project
    }


    @Override
    String getName() {
        return "auto-register"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    /**
     * 是否支持增量编译
     * @return
     */
    @Override
    boolean isIncremental() {
        return true
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs
                   , Collection<TransformInput> referencedInputs
                   , TransformOutputProvider outputProvider
                   , boolean isIncremental) throws IOException, TransformException, InterruptedException {
        project.logger.warn("start auto-register transform...")
        config.reset()
        project.logger.warn(config.toString())
        def clearCache = !isIncremental
        // clean build cache
        if (clearCache) {
            outputProvider.deleteAll()
        }

        long time = System.currentTimeMillis()
        boolean leftSlash = File.separator == '/'


        def cacheEnabled = config.cacheEnabled
        println("auto-register-----------isIncremental:${isIncremental}--------config.cacheEnabled:${cacheEnabled}--------------------\n")

        File jarManagerfile = null
        Map<String, ScanJarHarvest> cacheMap = null
        File cacheFile = null
        Gson gson = null

        if (cacheEnabled) { //开启了缓存
            gson = new Gson()
            cacheFile = AutoRegisterHelper.getRegisterCacheFile(project)
            if (clearCache && cacheFile.exists())
                cacheFile.delete()
            cacheMap = AutoRegisterHelper.readToMap(cacheFile, new TypeToken<HashMap<String, ScanJarHarvest>>() {
            }.getType())
        }

        CodeScanProcessor scanProcessor = new CodeScanProcessor(config.list, cacheMap)

        // 遍历输入文件
        inputs.each { TransformInput input ->
            // 遍历jar
            input.jarInputs.each { JarInput jarInput ->
                if (jarInput.status != Status.NOTCHANGED && cacheMap) {
                    cacheMap.remove(jarInput.file.absolutePath)
                }
                scanJar(jarInput, outputProvider, scanProcessor)
            }
            // 遍历目录
            input.directoryInputs.each { DirectoryInput directoryInput ->
                long dirTime = System.currentTimeMillis();
                // 获得产物的目录
                File dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                String root = directoryInput.file.absolutePath
                if (!root.endsWith(File.separator))
                    root += File.separator
                //遍历目录下的每个文件
                directoryInput.file.eachFileRecurse { File file ->
                    def path = file.absolutePath.replace(root, '')
                    if (file.isFile()) {
                        def entryName = path
                        if (!leftSlash) {
                            entryName = entryName.replaceAll("\\\\", "/")
                        }
                        scanProcessor.checkInitClass(entryName, new File(dest.absolutePath + File.separator + path))
                        if (scanProcessor.shouldProcessClass(entryName)) {
                            scanProcessor.scanClass(file)
                        }
                    }
                }
                long scanTime = System.currentTimeMillis();
                // 处理完后拷到目标文件
                FileUtils.copyDirectory(directoryInput.file, dest)
                println "auto-register cost time: ${System.currentTimeMillis() - dirTime}, scan time: ${scanTime - dirTime}. path=${root}"
            }
        }

        if (cacheMap != null && cacheFile && gson) {
            def json = gson.toJson(cacheMap)
            AutoRegisterHelper.cacheRegisterHarvest(cacheFile, json)
        }

        def scanFinishTime = System.currentTimeMillis()
        project.logger.error("register scan all class cost time: " + (scanFinishTime - time) + " ms")

        config.list.each { ext ->
            if (ext.fileContainsInitClass) {
                println('')
                println("insert register code to file:" + ext.fileContainsInitClass.absolutePath)
                if (ext.classList.isEmpty()) {
                    project.logger.error("No class implements found for interface:" + ext.interfaceName)
                } else {
                    ext.classList.each {
                        println(it)
                    }
                    CodeInsertProcessor.insertInitCodeTo(ext)
                }
            } else {
                project.logger.error("The specified register class not found:" + ext.registerClassName)
            }
        }
        def finishTime = System.currentTimeMillis()
        project.logger.error("register insert code cost time: " + (finishTime - scanFinishTime) + " ms")
        project.logger.error("register cost time: " + (finishTime - time) + " ms")
    }

    void scanJar(JarInput jarInput, TransformOutputProvider outputProvider, CodeScanProcessor scanProcessor) {

        // 获得输入文件
        File src = jarInput.file
        //遍历jar的字节码类文件，找到需要自动注册的类
        File dest = getDestFile(jarInput, outputProvider)
        long time = System.currentTimeMillis();
        if (!scanProcessor.scanJar(src, dest) //直接读取了缓存，没有执行实际的扫描
                //此jar文件中不需要被注入代码
                //为了避免增量编译时代码注入重复，被注入代码的jar包每次都重新复制
                && !scanProcessor.isCachedJarContainsInitClass(src.absolutePath)) {
            //不需要执行文件复制，直接返回
            return
        }
        println "auto-register cost time: " + (System.currentTimeMillis() - time) + " ms to scan jar file:" + dest.absolutePath
        //复制jar文件到transform目录：build/transforms/auto-register/
        FileUtils.copyFile(src, dest)
    }

    static File getDestFile(JarInput jarInput, TransformOutputProvider outputProvider) {
        def destName = jarInput.name
        // 重名名输出文件,因为可能同名,会覆盖
        def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath)
        if (destName.endsWith(".jar")) {
            destName = destName.substring(0, destName.length() - 4)
        }
        // 获得输出文件
        File dest = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
        return dest
    }

}