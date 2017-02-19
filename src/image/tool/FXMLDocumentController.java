/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package image.tool;


import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javax.imageio.ImageIO;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;

/**
 *
 * @author user
 */
public class FXMLDocumentController implements Initializable {
    
    public static final int IMAGE_UNKNOWN = -1;
    public static final int IMAGE_JPEG = 0;
    public static final int IMAGE_PNG = 1;
    public static final int IMAGE_GIF = 2;
    
    String[] widthArray = {"640","800","1200","1280","1366"};
    String[] heightArray = {"480","720","1080"};
    ObservableList<String> widths = FXCollections.observableArrayList(widthArray);
    ObservableList<String> heights = FXCollections.observableArrayList(heightArray);
    List<File> file;
    int width,height,totalImages,count;
    BooleanProperty retainAspectRatio;
    String DestinationPath=null;
    DirectoryChooser folder;
    FileChooser File;
    Task task;
    ExtensionFilter jpg = new ExtensionFilter("JPG Files(*.jpg)","*.JPG");
    ExtensionFilter png = new ExtensionFilter("JPG Files(*.png)","*.PNG");
    
    @FXML
    private Text title;
    
    @FXML
    private Label destination;
    
    @FXML
    private Label totalFiles;
    
    @FXML
    private Label progress;
    
    @FXML
    private Button chooseImage;
    
    @FXML
    private ComboBox Width;
    
    @FXML
    private ComboBox Height ;
    
    @FXML
    private TextField cWidth;
    
    @FXML
    private TextField cHeight;
    
    @FXML
    private ProgressBar progressBar;
    
    
    @FXML
    private void handleButtonAction(ActionEvent event) {
        System.out.println("You clicked me!");

    }
    
    @FXML
    private void chooseDestinationPath(ActionEvent event) throws IOException{
        System.out.println("Choose path!");
        File file = folder.showDialog(Width.getScene().getWindow());
        
        if(file!=null){
            System.out.println(file);
            DestinationPath = file.getCanonicalPath()+"/";
            destination.setText(DestinationPath);
        }
        
    }
    
    @FXML
    private void chooseImage(ActionEvent event){
        System.out.println("Choose image!");
        file = File.showOpenMultipleDialog(Width.getScene().getWindow());
        if(file!=null){
            totalImages = file.size();
            for(File f:file){
                System.out.println("Files: "+f);             
            }
            totalFiles.setText("Images selected: "+file.size());
        }
    }
    
    @FXML
    private void convertImage(ActionEvent event) throws IOException{
   
        if(!cWidth.getText().equals("") && !cHeight.getText().equals("")){
          
                width = Integer.parseInt(cWidth.getText().toString());
                height = Integer.parseInt(cHeight.getText().toString());
                System.out.println("Got custom dimension");
   
        }else if(Width.getValue()!=null && Height.getValue()!=null){
            width = Integer.parseInt(Width.getValue().toString());
            height = Integer.parseInt(Height.getValue().toString());
            System.out.println("Got predefined dimension");
        }
        
        if(DestinationPath != null){
            if(file!=null){
                if(width!=0 && height!=0){
                    //Resize image logic here...
                    count=0;
                    
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
                }else{
                    System.out.println("Select width and height");
                }
                
            }else{
                totalFiles.setText("Select atleast one image");
            }
        }else{
            destination.setText("Select destination path!");
        }
    }
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        File = new FileChooser();
        File.setTitle("Select images to resize");
        File.getExtensionFilters().addAll(jpg,png);
        
        folder = new DirectoryChooser();
        folder.setTitle("Select destination folder");
        Width.setItems(widths);
        Height.setItems(heights);

        task = new Task<Void>(){

                        @Override
                        protected java.lang.Void call() throws Exception {
                            for(File f:file){
                                String filename = f.getName();
                                String type = filename.substring(filename.lastIndexOf("."),filename.length());
                                int t = type==".jpg"?IMAGE_JPEG : IMAGE_PNG;

                                BufferedImage image = ImageIO.read(f);
                                
                                //BufferedImage resizedImage = resizeImageWithHint(image,t);
                                BufferedImage resizedImage = Scalr.resize(image,Method.QUALITY, width, height);
                                ImageIO.write(resizedImage, //Image to write
                                        type==".jpg"?"jpg":"png", //get type of image
                                        new File(DestinationPath+filename)); //save to destination path
                                count++;
                                updateProgress(count,totalImages);
                                updateMessage("Done: "+count+" of "+totalImages);
                            }
                            return null;
                        }
                    
                };
                
                progressBar.progressProperty().bind(task.progressProperty());
                progress.textProperty().bind(task.messageProperty());
    }    

    private BufferedImage resizeImage(BufferedImage image, int width, int height, int type) {
        
        if (type == IMAGE_PNG ) {
            type = BufferedImage.TYPE_INT_ARGB;
          }else {
            type = BufferedImage.TYPE_INT_RGB;
          }
        BufferedImage resized = new BufferedImage(width,height,type);
              
        int destWidth = image.getWidth() / width;
        int destHeight = image.getHeight() / height;
        
        if(retainAspectRatio.getValue()==true){
            int aspectRatio = image.getWidth() / image.getHeight();
             width = height * image.getWidth() / image.getHeight();
             height =width * image.getHeight() / image.getWidth();
            System.out.println("Preserving aspect ratio"+width+"X"+height);
        }
        
        
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
              resized.setRGB(x, y, image.getRGB(x * destWidth, y * destHeight));
            }
    }
        return resized;
    }
    
    private BufferedImage resizeImageWithHint(BufferedImage originalImage, int type){

        if (type == IMAGE_PNG ) {
            type = BufferedImage.TYPE_INT_ARGB;
          }else {
            type = BufferedImage.TYPE_INT_RGB;
          }
        
	BufferedImage resizedImage = new BufferedImage(width, height, type);
	Graphics2D g = resizedImage.createGraphics();
	g.drawImage(originalImage, 0, 0, width, width, null);
		
	g.setComposite(AlphaComposite.Src);

	g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
	RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	g.setRenderingHint(RenderingHints.KEY_RENDERING,
	RenderingHints.VALUE_RENDER_QUALITY);
	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	RenderingHints.VALUE_ANTIALIAS_ON);
        
	g.dispose();
	return resizedImage;
    }	
    
}
