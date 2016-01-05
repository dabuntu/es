package com.eventshop.eventshoplinux.akka.query;

import akka.camel.javaapi.UntypedConsumerActor;
import com.eventshop.eventshoplinux.model.Emage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang.ArrayUtils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.opencv.core.Core.kmeans;
import static org.opencv.core.Core.minMaxLoc;

/**
 * Created by abhisekmohanty on 25/6/15.
 */
public class GroupingQueryActor extends UntypedConsumerActor {

    private final static Logger LOGGER = LoggerFactory.getLogger(GroupingQueryActor.class);

    @Override
    public String getEndpointUri() {
        return "direct:groupingQueryActor";
    }

//    public enum GroupingColors {
//        red (0xff0000),
//        green (0x00c000),
//        yellow (0xffff00),
//        blue (0x0000ff),
//        orange (0xff9933),
//        purple (0xcc0099),
//        brown (0x996600),
//        white (0xffffff),
//        black (0x000000),
//        grey (0x999999);
//
//        private int value;
//        private GroupingColors(int value){
//            this.value=value;
//        }
//        }

//    public Vector<GroupingColors> color_codes;

//    private ActorRef masterQueryActor;
//
//
//
//    GroupingQueryActor(ActorRef masterQueryActor) {
//        this.masterQueryActor = masterQueryActor;
//    }
//
//    public static Props props(final ActorRef masterQueryActor) {
//        return Props.create(new Creator<GroupingQueryActor>() {
//            @Override
//            public GroupingQueryActor create() throws Exception {
//                return new GroupingQueryActor(masterQueryActor);
//            }
//        });
//    }

    @Override
    public void onReceive(Object message) throws Exception {
        long startTime = System.currentTimeMillis();

        if (message instanceof QueryActorMessage) {
            int max = 0;
            int min = 0;
            QueryActorMessage queryActorMessage = (QueryActorMessage) message;
            Emage emage = queryActorMessage.getEmageList().get(0);
            Emage resultEmage = emage;
            double[] imageArray = emage.getImage();
            Double[] doubleArray = ArrayUtils.toObject(imageArray);
            List<Double> image = Arrays.asList(doubleArray);
            final List<Double> thresholds = new ArrayList<Double>();
            List<String> colorCodes = new ArrayList<String>();

            JsonObject query = queryActorMessage.getQuery();
            String patternType = query.get("patternType").getAsString();
            if (patternType.equalsIgnoreCase("grouping")) {
                String method = query.get("method").getAsString();
                if (method.equalsIgnoreCase("threshold")){
                    JsonArray colors = query.get("colorCodes").getAsJsonArray();
                    for (int col=0; col<colors.size(); col++) {
                        colorCodes.add(colors.get(col).getAsString());
                    }
                    JsonArray thresholdsJson = query.get("thresholds").getAsJsonArray();
                    for (int j = 0; j < thresholdsJson.size(); j++) {
                        LOGGER.debug("Thresholds are "+thresholdsJson.get(j).getAsDouble());
                        thresholds.add(thresholdsJson.get(j).getAsDouble());
                    }
                    List<Object> output = image.stream()
                            .map(new Function<Double, Object>() {
                                @Override
                                public Object apply(Double aDouble) {
                                    int i=0;
                                    for (double group : thresholds) {
                                        if(aDouble > group){
                                            i++;
                                        }else{
                                            break;
                                        }
                                    }
                                    return i;

                                }
                            })
                            .collect(Collectors.toList());


//            List<Double> myDouble = output.stream().map(x -> (Double) x).collect(Collectors.toList());
//            DoubleSummaryStatistics stats = myDouble
//                    .stream()
//                    .mapToDouble(new ToDoubleFunction<Double>() {
//                        @Override
//                        public double applyAsDouble(Double x) {
//                            return x;
//                        }
//                    })
//                    .summaryStatistics();



                    double[] target = new double[output.size()];
                    for (int i = 0; i < target.length; i++) {
                        target[i] = Double.parseDouble(output.get(i).toString());
                        if ((int)target[i]>max){
                            max = (int)target[i];
                        }
                    }


                    resultEmage.setStartTime(startTime);
                    resultEmage.setEndTime(System.currentTimeMillis());
                    resultEmage.setColors(colorCodes);
                    resultEmage.setImage(target);
                    resultEmage.setMax(max);
                    resultEmage.setMin(min);
                    getSender().tell(resultEmage, getSelf());


                } else if (method.equalsIgnoreCase("K-Means")){
//                    System.setProperty("java.library.path", "/opt/OpenCV/opencv-2.4.9/build/lib/");
//                    System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
//                    Hello hello = new Hello();
//                    hello.main();
                    int num_groups = query.get("numGroup").getAsInt();
                    int row = emage.getRow();
                    int col = emage.getCol();
                    double[] target = new double[row*col];
                    Mat array = Mat.zeros(row, col, CvType.CV_64F);


                    if(emage.getImage().length > 0)
                    {
                        for(int i = 0; i < row; ++i)
                        {
                            for(int j = 0; j < col; ++j)
                            {
                                array.put(i,j,emage.getImage()[i*col+j]);
                            }
                        }
                    }

                    Mat input = new Mat(row*col, 3, CvType.CV_32F);

                    // Initialize the sample matrix
                    for(int i = 0; i < row; ++i)
                    {
                        for(int j = 0; j < col; ++j)
                        {
                            input.put(i*col + j, 0, array.get(i,j));
                            input.put(i*col + j, 1, 0.2*i);
                            input.put(i*col + j, 2, 0.2*j);
                        }
                    }

                    Mat output = new Mat(row*col, 1, CvType.makeType(CvType.CV_32S, 1));
                    Mat centers = Mat.zeros(num_groups, 3, CvType.CV_32F);
                    Mat cluster_index = new Mat(row, col, CvType.makeType(CvType.CV_32S, 1));

                    // Do the clustering
                    TermCriteria termCriteria = new TermCriteria(1|2, 20, 0.3);
                    double val = kmeans(input, num_groups, output, termCriteria, 3, 0, centers);


                    for(int i = 0; i < row; ++i)
                    {
                        for(int j = 0; j < col; ++j)
                        {
                            cluster_index.put(i,j,output.get(i*col+j, 0));
                        }
                    }

                    Mat result = new Mat();
                    int next_index = 0;

                    boolean do_Splitting = false;
                    boolean do_Coloring = false;
                    if (query.get("split").getAsString().equalsIgnoreCase("True")) {
                        do_Splitting = true;
                    }
                    if (query.get("doColoring").getAsString().equalsIgnoreCase("True")){
                        do_Coloring = true;
                    }

                    if (!do_Coloring && do_Splitting){

                        LOGGER.debug("!do_Coloring && do_Splitting not defined");

                    } else if (!do_Coloring && !do_Splitting) {

                        LOGGER.debug("!do_Coloring && !do_Splitting not defined");

                    } else if (do_Coloring && do_Splitting){

                        LOGGER.debug("do_Coloring && do_Splitting not defined");

                    } else if (do_Coloring && !do_Splitting){

                        result = Mat.zeros(row, col, CvType.makeType(CvType.CV_32S, 1));
                        cluster_index.clone().convertTo(result, CvType.CV_64F, 1);
                        int size = 0;
                        for(int i = 0; i < row; ++i)
                            for(int j = 0; j < col; ++j)
                            {
                                target[size]=result.get(i,j)[0];
                                size++;
                            }

                    }

                    Core.MinMaxLocResult minMaxresult = minMaxLoc(result);
                    max = (int)minMaxresult.maxVal;

                    resultEmage.setStartTime(startTime);
                    resultEmage.setEndTime(System.currentTimeMillis());
                    resultEmage.setColors(colorCodes);
                    resultEmage.setImage(target);
                    resultEmage.setMax(max);
                    resultEmage.setMin(min);
                    getSender().tell(resultEmage, getSelf());

                }
            }

        }
    }


    public static final class GroupingParams {

        private String masterQueryID;
        private JsonObject query;
        private List<Emage> emageList = new ArrayList<Emage>();

        public GroupingParams(String masterQueryID, JsonObject query, List<Emage> emageList) {
            this.masterQueryID = masterQueryID;
            this.query = query;
            this.emageList = emageList;
        }

        public String getMasterQueryID() {
            return masterQueryID;
        }

        public void setMasterQueryID(String masterQueryID) {
            this.masterQueryID = masterQueryID;
        }

        public JsonObject getQuery() {
            return query;
        }

        public void setQuery(JsonObject query) {
            this.query = query;
        }

        public List<Emage> getEmageList() {
            return emageList;
        }

        public void setEmageList(List<Emage> emageList) {
            this.emageList = emageList;
        }

    }
}
