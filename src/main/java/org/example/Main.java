package org.example;

import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    //private static final String file_path="C:\\Users\\91751\\Documents\\dipole.rtf";
    private static final String file_path="./sampleFile.pdf";
    private static final String video_path="./file.avi";
    static int frame_height=480;
    static int frame_width=640;

    public static void main(String[] args) throws IOException {
        //loading opencv lib
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        //create video file
        //convertFiletoVideo();

        //create pdf file
        convertVideoToFile();
    }

    private static void convertFiletoVideo(){
        try{
            byte[] fileByteArray=getFileBytes();
            createFrames(fileByteArray);
        }catch (Exception exe){
            throw new RuntimeException("Something went wrong");
        }
    }

    private static byte[] getFileBytes(){
        //laoding file
        File myfile=new File(file_path);

        //loading user file into byte array
        byte[] byteBuffer=new byte[1024];

        try(FileInputStream fis=new FileInputStream(myfile);ByteArrayOutputStream bos=new ByteArrayOutputStream()) {
            int bytesRead=0;
            while((bytesRead=fis.read(byteBuffer))!=-1){
                bos.write(byteBuffer,0,bytesRead);
            }
            return bos.toByteArray();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createFrames(byte[] fileBytes){


        int bytesPerFrame=frame_height*frame_width*3;
        int bytesInFile=fileBytes.length;

        try{
            //creating single frame with 3 channels
            Mat frame=new Mat(frame_height,frame_width, CvType.CV_8UC1);

            //init video writer
            VideoWriter videoWriter=new VideoWriter(video_path,VideoWriter.fourcc('Y', '8', ' ', ' '),30,new Size(frame_width,frame_height),false);

            if(!videoWriter.isOpened()){
                throw new RuntimeException("Video writer is not opened");
            }

            for(int i=0;i<bytesInFile;i+=bytesPerFrame){
                byte[] frameData=new byte[bytesPerFrame];
                Arrays.fill(frameData, (byte) 0);

                int bytesToFill=Math.min(bytesPerFrame,bytesInFile-i);

                System.arraycopy(fileBytes,i,frameData,0,bytesToFill);
                frame.put(0,0,frameData);
                videoWriter.write(frame);
            }
            videoWriter.release();
        }
        catch (Exception exe){
            System.out.println(exe);
            throw exe;
        }

    }

    private static void convertVideoToFile() throws IOException {
        // get byte from video
        byte[] fileByteArray=createBytes();

        //write byte to file
        //byte[] arr={109, 107, 108, 73, 71, 72, 96, 94, 95, 93, 91, 92, 102, 100, 101, 16, 14};

        writeFileBytes(fileByteArray);
    }

    private static byte[] createBytes() throws IOException {


        Mat frame =new Mat(frame_height,frame_width, CvType.CV_8UC1);
        try(ByteArrayOutputStream bos=new ByteArrayOutputStream()){
            VideoCapture videoCapture=new VideoCapture();
            videoCapture.open(video_path);

            if(!videoCapture.isOpened())
                throw new RuntimeException("Video capture not open");

            int frame_count=(int)videoCapture.get(Videoio.CAP_PROP_FRAME_COUNT);
            int video_height=(int)videoCapture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
            int video_width=(int)videoCapture.get(Videoio.CAP_PROP_FRAME_WIDTH);
            int frame_pos=(int)videoCapture.get(Videoio.CAP_PROP_POS_FRAMES);
            byte[] fileArray=new byte[frame_count*video_height*video_width*3];
            //Arrays.fill(fileArray,(byte)0);
            int currentFrame=0;
            int bytesFilled=0;
            ArrayList<Byte> byteList = new ArrayList<>();
            while(videoCapture.read(frame)){

                // Access raw pixel data from the frame
                byte[] frameData = new byte[(int) (frame.total() * frame.channels())];
                frame.get(0, 0, frameData);
                for(int i=0;i<frameData.length;i+=3) {
                    if (frameData[i] != 0) {
                        byteList.add(frameData[i]);
                    } else {
                        break; // Stop at the first zero byte as this indicates padding start
                    }
                }
                // Extract and collect data
                //int length=getvalidLength(frameData);
//                int length=17;
//                System.arraycopy(frameData,0,fileArray,bytesFilled,length);
            }
            videoCapture.release();
            byte[] recoveredBytes = new byte[byteList.size()];
            for (int i = 0; i < byteList.size(); i++) {
                recoveredBytes[i] = byteList.get(i);
            }
            return recoveredBytes;
        }
        catch (Exception exe){
            throw exe;
        }
    }

    private static void writeFileBytes(byte[] fileByteArray) throws IOException{
        String file_path="./decoded.pdf";
        File pdf_file=new File(file_path);

        try(FileOutputStream fos=new FileOutputStream(file_path)){
            fos.write(fileByteArray);
        }
        catch (Exception exe){
            throw exe;
        }
    }

    private static int getvalidLength(byte[] byteArray){
        int i=byteArray.length-1;
        while(i>0 && byteArray[i]==0){
            i--;
        }
        return i+1;
    }
}