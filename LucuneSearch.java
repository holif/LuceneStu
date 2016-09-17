package xyz.baal.lucene;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
/**
 * lucene简单搜索流程
 * @author 
 * @version lucene 5.5.2
 */
public class LucuneSearch {

	private Directory directory = null;//索引存放目录

	public LucuneSearch() {
		try {
			directory = FSDirectory.open(Paths.get("D:/lucene/"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 搜索方法
	 * @param path	所要搜索的目录
	 * @param key	搜索的关键字
	 */
	public void index_search(String path, String key) {
		//创建索引
		IndexWriter indxw = null;
		//SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");//设置日期格式
		try {
			IndexWriterConfig indxwc = new IndexWriterConfig(new StandardAnalyzer());
			indxw = new IndexWriter(directory, indxwc);
			File fs = new File(path);
			
			//System.out.println(df.format(new Date()));
			//递归索引目录下的所有文档
			FileList(fs,indxw);
			//System.out.println(df.format(new Date()));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				indxw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		//创建搜索
		try {
			Query query = new QueryParser("content", new StandardAnalyzer()).parse(key);

			IndexReader indexReader = DirectoryReader.open(directory);
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			TopScoreDocCollector top = TopScoreDocCollector.create(10);
			indexSearcher.search(query, top);
			ScoreDoc[] hits = top.topDocs().scoreDocs;

			for (int i = 0; i < hits.length; i++) {
				int docid = hits[i].doc;
				Document rd = indexSearcher.doc(docid);
				System.out.println(i + 1 + "." + rd.get("name") + "\t" + rd.get("path"));
			}
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
	}

	private  void FileList(File file,IndexWriter w){
		if(file.isDirectory()){
			for(File f : file.listFiles()){
				FileList(f, w);
			}
		} else {
			addDoc(w, file.getName(), file, file.getPath());
		}
	}
	
	private  void addDoc(IndexWriter w, String name, File file, String path) {
		try {
			Document doc = new Document();
			doc.add(new TextField("name", name, Store.YES));
			doc.add(new TextField("content", new FileReader(file)));
			doc.add(new StringField("path", path, Store.YES));
			w.addDocument(doc);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
