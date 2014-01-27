/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package main.java;
 
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
 
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
 
@WebServlet(name = "uploads",urlPatterns = {"/uploads/*"})
public class Uploads extends HttpServlet {
 
 
  private static final long serialVersionUID = 2857847752169838915L;
  int BUFFER_LENGTH = 4096;
 
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
    PrintWriter out = response.getWriter();
    FileOutputStream os;
    BufferedReader b = new BufferedReader(request.getReader());  
    String workString;
    StringBuilder workBuffer = new StringBuilder();              
    while((workString = b.readLine()) != null) {  
           workBuffer.append(workString);  
    }  
    workString = workBuffer.toString();  
    InputStream is = new ByteArrayInputStream(workString.getBytes("UTF-8"));
    os = new FileOutputStream(System.getenv("OPENSHIFT_DATA_DIR") + "trainingfile.arff", true);
    byte[] bytes = new byte[BUFFER_LENGTH];
    int read = 0;
    while ((read = is.read(bytes, 0, BUFFER_LENGTH)) != -1) {
        os.write(bytes, 0, read);
    }   
    os.flush();
    os.close();
    out.println("Was succesfully uploaded to " + System.getenv("OPENSHIFT_DATA_DIR")+ "trainingfile.arff");
  }
  
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
    String filePath = request.getRequestURI();
 
    File file = new File(System.getenv("OPENSHIFT_DATA_DIR") + filePath.replace("/uploads/",""));
    InputStream input = new FileInputStream(file);
 
    response.setContentLength((int) file.length());
    response.setContentType(new MimetypesFileTypeMap().getContentType(file));
 
    OutputStream output = response.getOutputStream();
    byte[] bytes = new byte[BUFFER_LENGTH];
    int read = 0;
    while ((read = input.read(bytes, 0, BUFFER_LENGTH)) != -1) {
        output.write(bytes, 0, read);
        output.flush();
    }
 
    input.close();
    output.close();
  }
}
