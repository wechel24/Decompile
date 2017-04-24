package com.duan.analysis;

import com.duan.common.ComPrint;
import com.duan.table_manager.SamplesManager;

import java.io.*;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by DuanJiaNing on 2017/4/24.
 * *
 */
public abstract class Analysis {

    private SamplesManager mSamplesManager;

    public Analysis() {
        this.mSamplesManager = new SamplesManager();
    }

    /**
     * 获得要解析的smail文件的根目录
     *
     * @param sample 当前解析的样本
     * @return 子类返回待解析文件
     */
    abstract File getFile(SamplesManager.Samples sample);

    /**
     * 获得当前解析使用的正则表达式
     *
     * @param sample 当前解析的样本
     * @return 子类返回正则表达式
     */
    abstract String getRegex(SamplesManager.Samples sample);

    /**
     * 有符合的匹配被找到时调用此方法
     *
     * @param m      匹配者
     * @param sample 当前解析的样本
     */
    abstract void find(Matcher m, SamplesManager.Samples sample);

    public void analysis(String whichType) {
        SamplesManager.Samples samples[] = mSamplesManager.getSampleInfo(whichType);
        Arrays.stream(samples).forEach(bean -> ergodicFile(getFile(bean), bean));
    }

    public void analysis(int... ids) {
//        SamplesManager.Samples samples[] = mSamplesManager.getSampleInfo(ids);
        SamplesManager.Samples samples[] = new SamplesManager.Samples[ids.length];
        int i = 0;
        for (int id : ids)
            samples[i++] = mSamplesManager.getSampleInfo(id);

        Arrays.stream(samples).forEach(bean -> ergodicFile(getFile(bean), bean));
    }

    // 递归遍历 ..\smail\com\ 目录或直接匹配（初始传入为文件时AndroidManifest.xml）
    private void ergodicFile(File tempF, SamplesManager.Samples bean) {
        if (tempF.exists()) {
            if (tempF.isFile()) {
                matcher(tempF, bean);
            } else if (tempF.isDirectory()) {
                File[] files = tempF.listFiles();
                for (int i = 0; i < files.length; i++) {
                    this.ergodicFile(files[i], bean);
                }
                tempF.delete();
            }
        } else {
            ComPrint.error("文件不存在");
        }
    }

    //匹配查找
    private void matcher(File file, SamplesManager.Samples bean) {
        BufferedReader reader = null;
        try {

            FileInputStream is = new FileInputStream(file);
            InputStreamReader ir = new InputStreamReader(is);
            reader = new BufferedReader(ir);

            String line = null;
            Pattern p = Pattern.compile(getRegex(bean));
            while ((line = reader.readLine()) != null) {
                Matcher m = p.matcher(line);
                while (m.find()) {
                    find(m, bean);
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
}