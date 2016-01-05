package com.eventshop.eventshoplinux.akka;

import com.eventshop.eventshoplinux.model.Emage;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * Created by abhisekmohanty on 13/7/15.
 */
public class CommonUtil {
    public Mat getArray(Emage emage) {

        double[] image = emage.getImage();
        int row = emage.getRow();
        int col = emage.getCol();
        Mat array = Mat.zeros(row, col, CvType.CV_64F);
        if(image.length > 0)
        {
            for(int i = 0; i < row; ++i)
            {
                for(int j = 0; j < col; ++j)
                {
                    array.put(i,j,image[i*col+j]);
                }
            }
        }
        return array;
    }
}
