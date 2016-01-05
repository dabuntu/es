package com.eventshop.eventshoplinux.webcrawl;

import java.util.*;
import java.util.function.ToDoubleFunction;

/**
 * Created by aravindh on 7/1/15.
 */
public class LambdaTest {

    public static void main(String args[]){
        sum();
    }
    private static void sum() {
        Integer a[] = {2, 6, 1, 4};
        Integer b[] = {2, 1, 4, 4};
        Integer c[] = {1, 4, 7 , 10};

        List<Integer> alist = Arrays.asList(a);
        List<Integer> blist = Arrays.asList(b);
        List<Integer> clist = Arrays.asList(c);


        List<List<Integer>> list = new ArrayList<List<Integer>>();
        list.add(alist);
        list.add(blist);
        list.add(clist);

        ArrayList<ArrayList<Integer>> allvalues = new ArrayList<ArrayList<Integer>>(a.length);
        ArrayList<Integer> result = new ArrayList<Integer>(allvalues.size());

        for(int i=0;i<4;i++){
            allvalues.add(new ArrayList<Integer>());
        }

        for( int i=0;i< 3;i++){
            for(int j=0;j<list.get(i).size();j++){
                allvalues.get(j).add(list.get(i).get(j));
            }
        }

        for(int i = 0;i<4;i++){
            System.out.println(Collections.max(allvalues.get(i)));
        }

//        Arrays.setAll(result, k -> Collections.max(allvalues.get(k)));
//        System.out.println(result.length);

//        allvalues.forEach((eachValue) -> result.add(Collections.max(eachValue)));
//
//        result.forEach((eachV)-> System.out.println(eachV));
        //players.forEach((player) -> System.out.print(player + "; "));


//        int result[] = new int[a.length];
//        Arrays.setAll(result, i -> Math.max(a[i], b[i]));
      //  System.out.println(Arrays.toString(result));


//        DoubleSummaryStatistics stats = listOfImages.get(0)
//                .stream()
//                .mapToDouble(new ToDoubleFunction<Double>() {
//                    @Override
//                    public double applyAsDouble(Double x) {
//                        return x;
//                    }
//                })
//                .summaryStatistics();
//
//        System.out.println("Highest number in List : " + stats.getMax());
//        System.out.println("Lowest number in List : " + stats.getMin());
//        final double arrayMin = stats.getMin();
//        final double arrayMax = stats.getMax();
    }
}
