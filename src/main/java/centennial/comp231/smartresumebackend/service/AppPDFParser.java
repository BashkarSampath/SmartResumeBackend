package centennial.comp231.smartresumebackend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;

@Service
public class AppPDFParser {
	public String readPDF(MultipartFile pdf) throws Exception {
		try {
			PdfReader pdfReader = new PdfReader(pdf.getInputStream());
			StringBuilder text = new StringBuilder();
	        SimpleTextExtractionStrategy strategy = new SimpleTextExtractionStrategy();
			for (int page = 1; page <= pdfReader.getNumberOfPages(); page++) {
		        text.append(PdfTextExtractor.getTextFromPage(pdfReader, page, strategy));  
		    }
		    pdfReader.close();
		    return  text.toString();
	    }catch(Exception ex) {
	    	ex.printStackTrace();
	    	throw ex;
	    }   
	}
}
