package com.lucence;


import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;

/**
 * @author akun
 * @version v1.0
 * @date 2019/1/12 9:33
 * @description TODO
 **/
public class IndexManager {

    //1.创建索引
    @Test
    public void createIndex() throws Exception {
        //指定索引库存放的路径
        //E:\08-Lucene\index
        Directory directory = FSDirectory.open(new File("E:\\08-Lucene\\index"));
        //索引库还可以存放到内存中
        //Directory directory = new RAMDirectory();
        //创建一个标准分析器
        Analyzer analyzer = new IKAnalyzer();
        //创建indexwriterCofig对象
        //第一个参数： Lucene的版本信息，可以选择对应的lucene版本也可以使用LATEST
        //第二根参数：分析器对象
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST,analyzer);
        //创建indexwriter对象
        IndexWriter indexWriter = new IndexWriter(directory,config);
        indexWriter.deleteAll();//删除所有索引
        //原始文档的路径D:\传智播客\01.课程\04.lucene\01.参考资料\searchsource
        File dir = new File("E:\\08-Lucene\\资料\\上课用的查询资料searchsource");
        for (File f : dir.listFiles()) {
            //文件名
            String fileName = f.getName();
            //文件内容
            String fileContent = FileUtils.readFileToString(f, "utf-8");
            //文件路径
            String filePath = f.getPath();
            //文件大小
            long fileSize = FileUtils.sizeOf(f);
            //创建文件名域
            //第一个参数：域的名称 相当于表中的列  相当于类中的属性
            //第二个参数：域的内容
            //第三个参数：是否存储
            Field filenameField = new TextField("filename", fileName, Field.Store.YES);
            //文件内容域
            Field fileContentField = new TextField("content", fileContent, Field.Store.YES);
            //文件路径域（不分析、不索引、只存储）
            Field filePathField = new TextField("path", filePath , Field.Store.YES);
            //文件大小域
            Field fileSizeField = new LongField("size", fileSize , Field.Store.YES);
            //创建document对象
            Document document = new Document();
            document.add(filenameField);
            document.add(fileContentField);
            document.add(filePathField);
            document.add(fileSizeField);
            //创建索引,并写入索引库
            indexWriter.addDocument(document);
        }
        //关闭indexwriter
        indexWriter.close();
    }


    //2.查询索引
    @Test
    public void searchIndex() throws Exception{
        //指定索引库存放的路径
        //E:\08-Lucene\index
        Directory directory = FSDirectory.open(new File("E:\\08-Lucene\\index"));
        //创建indexReader对象
        IndexReader indexReader = DirectoryReader.open(directory);
        //创建indexsearcher对象
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        //创建查询
        Query query = new TermQuery(new Term("filename","全文"));
        System.out.println(query);
        //执行查询
        //第一个参数是查询对象，第二个参数是查询结果返回的最大值
        TopDocs topDocs = indexSearcher.search(query, 10);
        //查询结果的总条数
        System.out.println("查询结果的总条数:"+topDocs.totalHits);
        //遍历查询结果
        //topDocs.scoreDocs存储了document对象的id
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            //scoreDoc.doc属性就是document对象的id
            //根据document的id找到document对象
            Document document = indexSearcher.doc(scoreDoc.doc);
            System.out.println(document.get("filename"));
            //System.out.println(document.get("content"));
            System.out.println(document.get("path"));
            System.out.println(document.get("size"));
            System.out.println("-------------------");
        }
        //关闭indexreader对象
        indexReader.close();
    }


    //查看IKAnalyzer分析器的分词效果
    @Test
    public void testTokenStream() throws Exception{
        //创建一个标准分析器对象
        Analyzer analyzer = new IKAnalyzer();
        //获得tokenStream对象
        //第一个参数：域名，可以随便给一个
        //第二个参数：要分析的文本内容
        TokenStream tokenStream = analyzer.tokenStream("test","什么是全文检索 java.txt");
        //添加一个引用，可以获得每个关键词
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        //将指针调整到列表的头部
        tokenStream.reset();
        //遍历关键词列表，通过incrementToken方法判断列表是否结束
        while (tokenStream.incrementToken()){
            //取关键词
            System.out.println(charTermAttribute);
        }
        tokenStream.close();

    }






}
